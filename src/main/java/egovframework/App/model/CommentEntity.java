package egovframework.App.model;

import java.util.Date;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommentEntity {
	@Id
	private Long id;
	
    @JsonProperty("CommentID")
    private int CommentID;
    
    @JsonProperty("TimeStamp")
    private Date TimeStamp;
    
    @JsonProperty("Content")
    private String Content;
    
    @JsonProperty("Rating")
    private float Rating;
    
    @JsonProperty("MovieID")
    private int MovieID;
    
    @JsonProperty("UserID")
    private int UserID;
    
    //외부에서 가져온 것
    @JsonProperty("Email")
    private String Email;
    
    @JsonProperty("UserName")
    private String UserName;
    
    @JsonProperty("Title")
    private String Title;

}
