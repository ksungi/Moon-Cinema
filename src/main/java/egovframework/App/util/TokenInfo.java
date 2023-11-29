package egovframework.App.util;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenInfo {
    public static final String ACCESS = "ACCESS";
    public static final String REFRESH = "REFRESH";
    
    private String token;
    private LocalDateTime expirationDateTime;
    private String type;	// [ ACCESS 토큰 | REFRESH 토큰 ]
}


