package egovframework.App.service;

import java.time.LocalDateTime;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.App.model.CommentEntity;
import egovframework.App.model.MovieEntity;
import egovframework.App.model.NowMovieEntity;
import egovframework.App.model.UserEntity;
import egovframework.App.model.WatchHistoryEntity;
import egovframework.App.model.WishListEntity;

@Mapper
public interface dbMapper {

	// NOW_Movie용
	public List<NowMovieEntity> selectMovieData(int movieID) throws Exception;
	
	public List<NowMovieEntity> selectMovieDataByTitle(String title) throws Exception;

	public int insertNowMovie(NowMovieEntity params) throws Exception;

	public int deleteMovieData(int movieID) throws Exception;
	
	public void deleteAllMovieData() throws Exception;
	
	public int updateMovieData(NowMovieEntity params) throws Exception;

	public List<NowMovieEntity> selectAllMovieData() throws Exception;
	
		//Delete전용 수정 금지
	public NowMovieEntity selectMovieIDByTitle(String temp) throws Exception;
	
	public List<MovieEntity> selectNowMovieDESCRating() throws Exception;
	
	public List<MovieEntity> selectNowMovieDESCReserveRate(int offset, int pageSize) throws Exception;

	// Movie용
	public List<MovieEntity> selectMovie(String title) throws Exception;
	
	public List<MovieEntity> selectMovieByTitle(String title) throws Exception;
	
	public List<MovieEntity> selectMovieTitle(String title) throws Exception;

	public int insertMovie(MovieEntity params) throws Exception;

	public int deleteMovie(int movieID) throws Exception;
	
	public void deleteAllMovie() throws Exception;

	public int updateMovie(MovieEntity params) throws Exception;
	
	public List<MovieEntity> selectMovie() throws Exception;
	
	public int countMovieByTitle() throws Exception;
	
		//Delete 전용 수정 금지
	public String selectMovieByID(int movieID) throws Exception;

	public List<MovieEntity> selectAllMovie() throws Exception;
	
	public List<MovieEntity> selectMovieDESCRating(int offset, int pageSize) throws Exception;
	
	public List<MovieEntity> selectMovieByActor(String Actors, int offset, int pageSize) throws Exception;

	public List<MovieEntity> selectMovieByGenre(String Genre1, String Genre2, int offset, int pageSize) throws Exception;

	// User용
	public int insertUserData(UserEntity params) throws Exception;

	public int deleteUserData(int userID) throws Exception;

	public int updateUserData(UserEntity params) throws Exception;

	public List<UserEntity> selectUserData(int UserID) throws Exception;

	public List<UserEntity> selectAllUserData() throws Exception;

	public UserEntity selectUserByEmail(String Email) throws Exception;
	
	public void deleteUnverifiedUser()throws Exception;

	// Comment용
	public int insertComment(CommentEntity params) throws Exception;

	public int deleteComment(int commentID) throws Exception;

	public int updateComment(CommentEntity params) throws Exception;

	public List<CommentEntity> selectAllMComment(int movieID) throws Exception;

	public List<CommentEntity> selectAllUComment(int userID) throws Exception;

	// Watch_History용
	public int insertOrUpdateW_History(WatchHistoryEntity params) throws Exception;

	public int deleteW_History(int watchHistoryID) throws Exception;

	public List<WatchHistoryEntity> selectAllMW_History(int movieID) throws Exception;

	public List<WatchHistoryEntity> selectAllUW_History(int userID) throws Exception;

	// Wish_List용
	public int insertWishList(WishListEntity params) throws Exception;

	public int deleteWishList(int userID, int movieID) throws Exception;

	public List<WishListEntity> selectAllUWishList(int userID) throws Exception;
	
	public int isWishExists(int userID, int movieID) throws Exception;

	//Token 용
	public void saveRefreshToken(int UserID, String TokenValue, LocalDateTime ExpirationDateTime) throws Exception;

	public void deleteRefreshToken(int UserID) throws Exception;

	public String getRefreshToken(int userID) throws Exception;


	//DB용
	public void resetAutoIncrement() throws Exception;
}
