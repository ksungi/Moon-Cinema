package egovframework.App.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WatchHistoryEntity {
	
    @JsonProperty("WatchHistoryID")
    private int WatchHistoryID;
    
    @JsonProperty("WatchedDate")
    private Date WatchedDate;
    
    @JsonProperty("UserID")
    private int UserID;
    
    @JsonProperty("MovieID")
    private int MovieID;
    
    
    //외부에서 가져 온 것
    
    @JsonProperty("UserName")
    private String UserName;
    
    @JsonProperty("Title")
    private String Title;
    
    @JsonProperty("ImgURL")
    private String ImgURL;
    
    
    //임시로 사용하는 것
    @JsonProperty("exists")
    private int exists;
    
	@JsonProperty("Email")
	private String Email;
    
}
