package egovframework.App.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.App.model.UserEntity;
import egovframework.App.util.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class LoginTokenProvider {
	
//	yml에 기입 했으나 여기로 넘어오지 않아 보류
//	@Value("${Security.Key}")
//	private String SECRET_KEY;
	
	private String SECRET_KEY = "NMA8JPctFuna59f5";
	
	public TokenInfo create(UserEntity userEntity, String type) {
		LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDateTime  = now.plusDays(1); //현재 설정시간: 1day
        
        String token = Jwts.builder()
        					.setSubject(String.valueOf(userEntity.getEmail())) //email
        					.setIssuer("app")
        					.claim("type", type)  // 토큰 타입
        					.setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
        					.setExpiration(Date.from(
        							expirationDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        					.signWith(SignatureAlgorithm.HS512, SECRET_KEY)
        					.compact();
		log.debug("Token Create Complete...");
		return new TokenInfo(token, expirationDateTime, type);
	}
    
	public TokenInfo getTokenInfo(String token) {
	    try {
			Claims claims = Jwts.parser()
		            .setSigningKey(SECRET_KEY)
		            .parseClaimsJws(token)
		            .getBody();

		    String tokenType = (String) claims.get("type");
		    LocalDateTime expirationDateTime = LocalDateTime.ofInstant(
		    		claims.getExpiration().toInstant(), ZoneId.systemDefault());

			return new TokenInfo(token, expirationDateTime, tokenType);
	    } catch (JwtException e) {
	        log.error("토큰 정보를 가져오지 못했습니다. \nError: ", e.getMessage());
	        return null;
	    }

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

    public String validateAndGetUserId(String token) {
    	Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}