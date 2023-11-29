package egovframework.App.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import egovframework.App.model.UserEntity;
import egovframework.App.service.TokenService;
import egovframework.App.service.dbService;
import egovframework.App.util.TokenInfo;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	@Autowired
	private LoginTokenProvider tokenProvider;
	
	@Autowired
	private dbService dbService;
	
	@Autowired
	private TokenService tokenService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try { //첫 try
			
			// 리퀘스트에서 토큰 가져오기
			String token = parseBearerToken(request);
			log.debug("Filter is running...");

			// 토큰 검사
			if (token != null && !token.equalsIgnoreCase("null")) {  //If - start
				authenticateUser(request, token);
				
				try { //2차 try
				    tokenProvider.validateAndGetUserId(token);
				} catch (ExpiredJwtException e){  //2차의 1st catch
					log.debug("Access 토큰 갱신 시작...");
				    try { //3차 try
				        // 액세스 토큰에서 사용자 ID 추출
				        String Email = e.getClaims().getSubject();

				        // 사용자 ID를 기반으로 서버에서 리프레시 토큰을 가져옴
				        String refreshToken = tokenService.getRefreshToken(Email);

				        // 리프레시 토큰이 유효한지 검사하고, 유효하다면 새로운 액세스 토큰을 발급
				        if (refreshToken != null && tokenProvider.validateAndGetUserId(refreshToken) != null ) {
				            
				        	// 사용자 정보를 데이터베이스에서 가져옴
				            UserEntity userEntity = dbService.selectUserByEmail(Email);
				            
				        	// 새로운 액세스 토큰 생성
				            TokenInfo newTokenInfo = tokenProvider.create(userEntity, TokenInfo.REFRESH);
				            String newAccessToken = newTokenInfo.getToken();

				            // 새로운 액세스 토큰을 응답 헤더에 추가
				            response.addHeader("Authorization", "Bearer " + newAccessToken);
				            
				            // 새 접근 토큰으로 인증을 진행 
				            authenticateUser(request, newAccessToken);
				        }
				    } catch (Exception ex) {    //3차 try의 2nd catch
				        log.error("Access 토큰 갱신 실패");
				    }
				} catch (Exception e) {  //2차의 2nd catch
					log.error("유효하지 않은 토큰입니다. \nError: ", e);
				}

			} //If - end
		////END Of 첫 Try 		
		} catch (Exception e) {
			log.error("토큰이 존재하지 않거나 손상되었습니다. \nError: ", e);
		}
		filterChain.doFilter(request, response);
	}
	
	
    private void authenticateUser(HttpServletRequest request, String token) {
    	// userId 가져오기. 위조된 경우 예외처리
    	String userId = tokenProvider.validateAndGetUserId(token);
        log.debug("Authenticated user ID : " + userId);
        
        // 인증 완료; SecurityContextHolder에 등록해야 인증된 사용자라고 생각한다.
        AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, AuthorityUtils.NO_AUTHORITIES);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    
	private String parseBearerToken(HttpServletRequest request){
		// Http 리퀘스트의 헤더를 파싱해 Bearer 토큰을 리턴한다.
		String bearerToken = request.getHeader("Authorization");
		if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}