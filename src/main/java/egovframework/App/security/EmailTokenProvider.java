package egovframework.App.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.App.util.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class EmailTokenProvider {
	
// yml 파일에서 값이 넘어 오지 않아 보류
//	@Value("${Security.Key}")
//	private String SECRET_KEY;

	private String SECRET_KEY = "NMA8JPctFuna59f5";
    
    //이메일 토큰 생성에 사용하는 메소드 
    public String create(String email) {
    	LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMinutes(60); //현재 설정시간: 60분 후
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .setSubject(email) // 이메일 주소
                .setIssuer("app")
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant())) // 만료 시간
                .compact();

        return token;
    }
    
    public String convertTokenInfoToJson(TokenInfo tokenInfo) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(tokenInfo);
        } catch (JsonProcessingException e) {
            log.error("토큰 정보를 JSON형태로 전환에 실패하였습니다.\nError: ", e.getMessage());
            return null;
        }
    }

    // 토큰 판별 및 이메일 반환 메소드
    public Optional<String> validateAndGetEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            // 토큰 유효기간 확인
            Date expiryDate = claims.getExpiration();
            if (expiryDate.before(new Date())) {
            	log.warn("토큰의 유효기간이 만료되었습니다.");
                return Optional.empty();  // 유효기간이 지난 경우
            }

            // 토큰 유효성 확인
            String email = claims.getSubject();
            if (email == null) {
            	log.warn("토큰에서 이메일 정보를 찾을 수 없습니다.");
                return Optional.empty();  // 토큰에 이메일 정보가 없는 경우
            }

            // 유효기간 및 유효성 확인 ->  이메일 정보 반환
            return Optional.of(email);
        } catch (JwtException e) {
            // 토큰 파싱 실패
        	log.warn("토큰 파싱에 실패하였습니다.");
            return Optional.empty();
        }
    }
}
