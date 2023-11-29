package egovframework.App.persistence;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import egovframework.App.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>{
	@Query("SELECT u FROM UserEntity u WHERE u.Email = :email")
	UserEntity findUserByEmail(@Param("email") String email);
}