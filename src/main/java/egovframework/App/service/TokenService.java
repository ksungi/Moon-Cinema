package egovframework.App.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import egovframework.App.model.UserEntity;

@Service
public class TokenService {
	
	private final dbMapper dbMapper;
	
	public TokenService(dbMapper dbMapper) {
		this.dbMapper = dbMapper;
	}

	//리프레시 토큰 저장
	public void saveRefreshToken(int UserID, String TokenValue, 
					LocalDateTime expirationDateTime) throws Exception {
		dbMapper.saveRefreshToken(UserID, TokenValue, expirationDateTime);
	}

	//리프레시 토큰 제거
	public void deleteRefreshToken(String Email) throws Exception {
		UserEntity userEntity =  dbMapper.selectUserByEmail(Email);
		int UserID = userEntity.getUserID();
		dbMapper.deleteRefreshToken(UserID);
	}

	//리프레시 토큰 가져오기
	public String getRefreshToken(String Email) throws Exception {
		UserEntity userEntity =  dbMapper.selectUserByEmail(Email);
		int UserID = userEntity.getUserID();
		
		String refreshToken = dbMapper.getRefreshToken(UserID);
		return refreshToken;
	}
}
