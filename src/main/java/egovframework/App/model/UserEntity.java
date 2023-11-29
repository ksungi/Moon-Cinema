package egovframework.App.model;

import java.time.LocalDateTime;

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
@Table(name = "userinfo")
public class UserEntity {
    @Id
	@Column(name = "UserID")
	@JsonProperty("UserID")
	private int UserID;
		
	@Column(name = "Password")
	@JsonProperty("Password")
	private String Password;

	@Column(name = "Email")
	@JsonProperty("Email")
	private String Email;
	
	@Column(name = "UserName")
	@JsonProperty("UserName")
	private String UserName;
	
	@Column(name = "Gender")
	@JsonProperty("Gender")
	private int Gender;
	
	@Column(name = "Age")
	@JsonProperty("Age")
	private int Age;
	
	@Column(name = "Actors")
	@JsonProperty("Actors")
	private String Actors;
	
	@Column(name = "Genre1")
	@JsonProperty("Genre1")
	private String Genre1;
	
	@Column(name = "Genre2")
	@JsonProperty("Genre2")
	private String Genre2;
	
    @Column(name = "EmailVerified")
    @JsonProperty("EmailVerified")
    private int EmailVerified;
    
    @Column(name = "JoinTime")
    @JsonProperty("JoinTime")
    private LocalDateTime JoinTime;
    
    
    public boolean isEmailVerified() {
        return EmailVerified == 1;
    }

    public void setEmailVerified(boolean EmailVerified) {
        this.EmailVerified = EmailVerified ? 1 : 0;
    }
}
