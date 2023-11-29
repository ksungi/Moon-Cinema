package egovframework.App.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name ="movie_info")
public class MovieEntity {
	
	@Id
    @Column(name = "MovieID")
    @JsonProperty("MovieID")
	private int MovieID;
	
    @Column(name = "Actors")
    @JsonProperty("Actors")
    private String Actors;
    
    @Column(name = "Director")
    @JsonProperty("Director")
    private String Director;
    
    @Column(name = "Rating")
    @JsonProperty("Rating")
    private double Rating;
    
    @Column(name = "Title")
    @JsonProperty("Title")
    private String Title;
    
    @Column(name = "ImgURL")
    @JsonProperty("ImgURL")
    private String ImgURL;
    
    @Column(name = "AgeGrade")
    @JsonProperty("AgeGrade")
    private String AgeGrade;

    @Column(name = "Duration")
    @JsonProperty("Duration")
    private int Duration;
    
    @Column(name = "ReleaseDate")
    @JsonProperty("ReleaseDate")
    private LocalDate ReleaseDate;

    @Column(name = "Synopsis")
    @JsonProperty("Synopsis")
    private String Synopsis;

    @Column(name = "Genre1")
    @JsonProperty("Genre1")
    private String Genre1;
    
    @Column(name = "Genre2")
    @JsonProperty("Genre2")
    private String Genre2;
    
    @Column(name = "Genre3")
    @JsonProperty("Genre3")
    private String Genre3;
    
    public java.sql.Date getReleaseDate() {
        if (ReleaseDate != null) {
            return java.sql.Date.valueOf(ReleaseDate);
        } else {
            return null;
        }
    }
}
