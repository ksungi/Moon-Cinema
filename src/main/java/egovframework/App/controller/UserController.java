package egovframework.App.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.App.dto.ResponseDTO;
import egovframework.App.dto.UserDTO;
import egovframework.App.model.UserEntity;
import egovframework.App.security.EmailTokenProvider;
import egovframework.App.security.LoginTokenProvider;
import egovframework.App.service.MailService;
import egovframework.App.service.TokenService;
import egovframework.App.service.dbService;
import egovframework.App.util.TokenInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class UserController {
	
	@Value("${URL.Front}")
	private String FrontEnd;
	@Value("${URL.Back}")
	private String BackEnd;

	private final dbService dbService;
	private final TokenService tokenService;
	private final MailService MailService;
	private final LoginTokenProvider loginTokenProvider;
	
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Autowired
	public UserController(dbService dbService, TokenService tokenService, 
							MailService MailService, LoginTokenProvider loginTokenProvider) {
	    this.dbService = dbService;
	    this.tokenService = tokenService;
	    this.MailService = MailService;
	    this.loginTokenProvider = loginTokenProvider;
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
		try {
			if(dbService.selectUserByEmail(userDTO.getEmail()) == null) {
				// UserDTO에서 필요한 정보를 추출하여 UserEntity 객체 생성
				UserEntity user = UserEntity.builder()
						.Email(userDTO.getEmail())
						.Password(passwordEncoder.encode(userDTO.getPassword()))
						.UserName(userDTO.getUsername())
						.Age(userDTO.getAge())
						.Gender(userDTO.getGender())
						.Actors(userDTO.getActors())
						.Genre1(userDTO.getGenre1())
						.Genre2(userDTO.getGenre2())
						.JoinTime(LocalDateTime.now())
						.build();
				
				// DB에 사용자 정보 입력
				dbService.insertUserData(user);
				log.debug(">>DB에 입력완료 - " + user);
                
				// 이메일 인증을 위한 토큰 생성
				EmailTokenProvider emailTokenProvider = new EmailTokenProvider();
                String token = emailTokenProvider.create(userDTO.getEmail());
                String appUrl = BackEnd;
                
                // 이메일 내용 작성
                String subject = "회원 가입 이메일 인증";
                String text = "회원 가입을 완료하려면 아래 링크를 클릭해주세요.\n"
                        + appUrl + "/auth/verify?token=" + token;

                // 이메일 전송
                log.info(">>메일이 보내질 주소: "+userDTO.getEmail());
                MailService.sendEmail(userDTO.getEmail(), subject, text);
                log.info(">>이메일이 발송 되었습니다. ");

				
				// 응답을 위한 UserDTO 생성
				UserDTO responseUserDTO = UserDTO.builder()
						.email(userDTO.getEmail())
						.build();

				return ResponseEntity.ok().body(responseUserDTO);
			} else {
				log.warn(">>중복된 이메일입니다.");
				String message = "Duplicated Email";
				ResponseDTO<String> response = ResponseDTO.<String>builder().error(message).build();
				return ResponseEntity.badRequest().body(response);
			}
		} catch (Exception e) {
			log.warn(">>회원가입 중 예외상황이 발생하였습니다.");
			ResponseDTO<String> response = ResponseDTO.<String>builder().error(e.getMessage()).build();
			return ResponseEntity.badRequest().body(response);
		}
	}

	@GetMapping("/verify")
	public ResponseEntity<?> verifyEmail(@RequestParam("token") String token, HttpServletResponse res) {
		log.debug(">>이메일 인증 시작");
		EmailTokenProvider emailTokenProvider = new EmailTokenProvider();
		Optional<String> validatedToken = emailTokenProvider.validateAndGetEmail(token);
	    try {
	    	//토큰 검증 결과 유효하다면 진입
	        if (validatedToken.isPresent()) {
	        	String email = validatedToken.get();
	            UserEntity user = dbService.selectUserByEmail(email);
	            if (user != null) {
	                user.setEmailVerified(true);
	                dbService.updateUserData(user);
	                
	                // 인증 성공 메시지 전달
	                ResponseDTO<String> response = ResponseDTO.<String>builder().data(Arrays.asList("Email verification successful")).build();

	                //인증이 끝났으니 로그인 페이지로 리다이렉트
	                	//뒤에 붙는 / 주소는 프론트 기준으로!
	                String Login = FrontEnd + "/login";
	                res.sendRedirect(Login);
	                return ResponseEntity.ok().body(response);
	            } else {
	            	log.warn(">>존재하지 않는 유저입니다.");
	            	ResponseDTO<String> response = ResponseDTO.<String>builder().error("User not found").build();
	                return ResponseEntity.badRequest().body(response);
	            }
	        } else {
	        	log.warn(">>유효하지 않은 토큰입니다.");
	        	ResponseDTO<String> response = ResponseDTO.<String>builder().error("Invalid token").build();
	            return ResponseEntity.badRequest().body(response);
	        }
	    } catch (Exception e) {
	    	log.warn(">>이메일 인증 중 예외상황이 발생하였습니다. \n Error: " + e.getMessage());
	        ResponseDTO<String> response = ResponseDTO.<String>builder().error(e.getMessage()).build();
	        return ResponseEntity.badRequest().body(response);
	    }
	}

    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) throws Exception {
        UserEntity user = dbService.selectUserByEmail(userDTO.getEmail(), userDTO.getPassword(), passwordEncoder);
        
        if (user != null) {
            if (!user.isEmailVerified()) {  // 이메일이 인증되지 않은 유저인지 확인
            	log.warn(">>이메일 인증이 필요합니다.");
                ResponseDTO<String> response = ResponseDTO.<String>builder().error("Email not verified").build();
                return ResponseEntity.badRequest().body(response);
            }
            // 이메일이 인증된 유저인 경우 로그인 처리
            
            // ACCESS 토큰 생성
            TokenInfo accessTokenInfo = loginTokenProvider.create(user, TokenInfo.ACCESS);
            //REFRESH 토큰 생성
            TokenInfo refreshTokenInfo = loginTokenProvider.create(user, TokenInfo.REFRESH);
            
            // 토큰 추출
            final String accessToken = accessTokenInfo.getToken(); 		// to. Client
            final String refreshToken = refreshTokenInfo.getToken(); 	// to. Server
            
            // Refresh 토큰 DB에 저장
            tokenService.saveRefreshToken(user.getUserID(), refreshToken, refreshTokenInfo.getExpirationDateTime());

            
            // 사용자에게 보낼 응답 생성
            final UserDTO responseUserDTO = UserDTO.builder()
                    .userId(user.getUserID())
                    .email(user.getEmail())
                    .username(user.getUserName())
                    .age(user.getAge())
                    .gender(user.getGender())
                    .actors(user.getActors())
                    .genre1(user.getGenre1())
                    .genre2(user.getGenre2())
                    .accessToken(accessToken)
                    .build();
            log.info("Logined ID: " + responseUserDTO.getEmail());
            return ResponseEntity.ok().body(responseUserDTO);
        } else {
        	log.warn(">>로그인에 실패하였습니다.");
            ResponseDTO<String> response = ResponseDTO.<String>builder().error("Login failed").build();
            return ResponseEntity.badRequest().body(response);
        }
    }

	@PutMapping("/edit")
	public ResponseEntity<?> editUserInfo(@RequestBody UserDTO userDTO) throws Exception {
		log.debug("유저 정보 업데이트 시작");
		
		int beforeUserId = dbService.selectUserByEmail(userDTO.getEmail().toString()).getUserID();
		
		UserEntity editedUserId = UserEntity.builder()
				.UserID(beforeUserId)
				.Email(userDTO.getEmail())
				.Password(passwordEncoder.encode(userDTO.getPassword()))
				.UserName(userDTO.getUsername())
				.Age(userDTO.getAge())
				.Gender(userDTO.getGender())
				.Actors(userDTO.getActors())
				.Genre1(userDTO.getGenre1())
				.Genre2(userDTO.getGenre2())
				.EmailVerified(1)
				.build();
		
		dbService.updateUserData(editedUserId);
		
		log.debug("유저 정보 업데이트 완료");
		ResponseDTO<String> response = ResponseDTO.<String>builder().data(Arrays.asList("Edit Success")).build();
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/remove")
	public ResponseEntity<?> removeUserInfo(@RequestBody UserDTO userDTO) throws Exception {
		UserEntity user = dbService.selectUserByEmail(
				userDTO.getEmail(),
				userDTO.getPassword(),
				passwordEncoder);

		dbService.deleteUserData(user.getUserID());
		
		log.debug("회원탈퇴 완료");
		ResponseDTO<String> response = ResponseDTO.<String>builder().data(Arrays.asList("Remove Success")).build();
		return ResponseEntity.ok().body(response);
	}
	
	
	@PostMapping("/forgot")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) throws Exception {
	    log.debug(">>비밀번호 찾기 시작");

	    String Email = body.get("Email");
	    UserEntity user = dbService.selectUserByEmail(Email);

	    if (user == null) {
	        log.warn(">>등록되지 않은 이메일입니다.");
	        ResponseDTO<String> response = ResponseDTO.<String>builder().error("Unregistered Email").build();
	        return ResponseEntity.badRequest().body(response);
	    }

	    // 임시 비밀번호 10자리 생성
	    String tempPassword = RandomStringUtils.randomAlphanumeric(10);
	    
	    // 사용자의 비밀번호를 임시 비밀번호로 변경
	    user.setPassword(passwordEncoder.encode(tempPassword));
	    dbService.updateUserData(user);

	    // 이메일 내용 작성
	    String subject = "비밀번호 재설정 요청";
	    String text = "임시 비밀번호는 다음과 같습니다: " + tempPassword;

	    // 이메일 전송
	    MailService.sendEmail(user.getEmail(), subject, text);
	    
	    log.info(">>이메일이 발송 되었습니다. ");
		ResponseDTO<String> response = ResponseDTO.<String>builder().data(Arrays.asList("Email Sent")).build();
	    return ResponseEntity.ok().body(response);

	}
	
	
	@PostMapping("/signout")
	public ResponseEntity<?> logout(HttpServletRequest request) throws Exception {
	    log.debug("로그아웃 시작...");
		// 헤더에서 액세스 토큰 추출
	    String header = request.getHeader("Authorization");
	    String accessToken = header.substring(7); // "Bearer " 부분 제거

	    // LoginTokenProvider를 이용한 토큰 검증
	    String Email = loginTokenProvider.validateAndGetUserId(accessToken);
	    if (Email != null) {
	        tokenService.deleteRefreshToken(Email);
	        log.info("로그아웃 되었습니다.");
			ResponseDTO<String> response = ResponseDTO.<String>builder().data(Arrays.asList("Logout Successful")).build();
		    return ResponseEntity.ok().body(response);
	    } else {
	    	log.warn("유효하지 않은 토큰입니다.");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
	    }
	}

}
