package egovframework.App.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WishListEntity {

    @JsonProperty("WishID")
    private int WishID;
    
    @JsonProperty("WishDate")
    private Date WishDate;
    
    @JsonProperty("UserID")
    private int UserID;
    
    @JsonProperty("MovieID")
    private int MovieID;
    
    @JsonProperty("Email")
    private String Email;
    
    
    //외부에서 가져오는 것
    @JsonProperty("UserName")
    private String UserName;

    @JsonProperty("Title")
    private String Title;
    
    @JsonProperty("ImgURL")
    private String ImgURL;
    
}
