package egovframework.App.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
	private String token;
	
	private int userId;
	private String email;
	private String password;
	private String username;
	
	private int age;
	private int gender;
	private String actors;
	private String genre1;
	private String genre2;
	
	private int EmailVerified;
	private String accessToken;
	private String refreshToken;
	
}