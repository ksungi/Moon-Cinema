package egovframework.App.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import egovframework.App.model.CommentEntity;
import egovframework.App.model.MovieEntity;
import egovframework.App.model.NowMovieEntity;
import egovframework.App.model.UserEntity;
import egovframework.App.model.WatchHistoryEntity;
import egovframework.App.model.WishListEntity;

@Service
public class dbService {

	private final dbMapper dbMapper;

	// 생성자 주입
	public dbService(dbMapper dbMapper) {
		this.dbMapper = dbMapper;
	}

	// Now_Movie_SELECT
	public List<NowMovieEntity> selectMovieData(int movieID) throws Exception {
		List<NowMovieEntity> printData = dbMapper.selectMovieData(movieID);
		return printData;
	}
	
	// Now_Movie_SELECT_BY_TITLE
	public List<NowMovieEntity> selectMovieData(String title) throws Exception {
		List<NowMovieEntity> printData = dbMapper.selectMovieDataByTitle(title);
		return printData;
	}
	
	// Now_Movie_SELECT_(Title -> ID)
		//Delete전용 수정 금지
	public NowMovieEntity selectMovieID(String temp) throws Exception {
		NowMovieEntity printData = dbMapper.selectMovieIDByTitle(temp);
		return printData;
	}
	
	// Now_Movie_SELECT_ALL
	public List<NowMovieEntity> selectAllMovieData() throws Exception {
		List<NowMovieEntity> printData = dbMapper.selectAllMovieData();
		return printData;
	}

	// Now_Movie_INSERT
	public Object insertNowMovie(NowMovieEntity params) throws Exception {
		int result = dbMapper.insertNowMovie(params);
		return result;
	}

	// Now_Movie_DELETE
	public Object deleteMovieData(int movieID) throws Exception {
		int result = dbMapper.deleteMovieData(movieID);
		return result;
	}

	// Now_Movie_UPDATE
	public Object updateMovieData(NowMovieEntity params) throws Exception {
		int result = dbMapper.updateMovieData(params);
		return result;
	}
	
	// Now_Movie_SELECT_DESC_Rating
	public List<MovieEntity> selectNowMovieDESCRating() throws Exception {
		List<MovieEntity> printData = dbMapper.selectNowMovieDESCRating();
		return printData;
	}

	// Now_Movie_SELECT_DESC_ReservationRate
	public List<MovieEntity> selectNowMovieDESCReserveRate(int page, int pageSize) throws Exception {
		int offset = (page - 1) * pageSize;
		return dbMapper.selectNowMovieDESCReserveRate(offset, pageSize);
	}
	
	////////////
	////////////
	////////////

	// Movie_SELECT_By_title
	public List<MovieEntity> selectMovie(String title) throws Exception {
		List<MovieEntity> printData = dbMapper.selectMovie(title);
		return printData;
	}
	
	public List<MovieEntity> selectMovieByTitle(String title) throws Exception {
		List<MovieEntity> printData = dbMapper.selectMovieByTitle(title);
		return printData;
	}
	
	public List<MovieEntity> selectMovieTitle(String title) throws Exception {
		List<MovieEntity> printData = dbMapper.selectMovieTitle(title);
		return printData;
	}
	
	// Movie_SELECT_By_MovieID
	//Delete전용 수정 금지
	public String selectMovieByID(int movieID) throws Exception {
		String printData = dbMapper.selectMovieByID(movieID);
		return printData;
	}

	// Movie_SELECT_ALL
	public List<MovieEntity> selectAllMovie() throws Exception {
		List<MovieEntity> printData = dbMapper.selectAllMovie();
		return printData;
	}

	// Movie_INSERT
	public Object insertMovie(MovieEntity params) throws Exception {
		int result = dbMapper.insertMovie(params);
		return result;
	}

	// Movie_DELETE
	public Object deleteMovie(int movieID) throws Exception {
		int result = dbMapper.deleteMovie(movieID);
		return result;
	}

	// Movie_UPDATE
	public Object updateMovie(MovieEntity params) throws Exception {
		int result = dbMapper.updateMovie(params);
		return result;
	}
	
	// Movie_SELECT_DESC_Rating
	public List<MovieEntity> selectMovieDESCRating(int page, int pageSize) throws Exception {
	    int offset = (page - 1) * pageSize;
	    return dbMapper.selectMovieDESCRating(offset, pageSize);
	}
	
	//Movie_SELECT_By_Actor
	public List<MovieEntity> selectMovieByActor(String actors, int page, int pageSize) throws Exception {
		int offset = (page - 1) * pageSize;
		return dbMapper.selectMovieByActor(actors, offset, pageSize);
	}
	
	//Movie_SELECT_By_Genre
	public List<MovieEntity> selectMovieByGenre(String genre1, String genre2, int page, int pageSize) throws Exception {
		int offset = (page - 1) * pageSize;
		return dbMapper.selectMovieByGenre(genre1, genre2, offset, pageSize);
	}

	////////////
	////////////
	////////////

	// User_INSERT
	public UserEntity insertUserData(UserEntity params) throws Exception {
		dbMapper.insertUserData(params);
		return params;
	}

	// User_DELETE
	public Object deleteUserData(int userID) throws Exception {
		int result = dbMapper.deleteUserData(userID);
		return result;
	}

	// User_UPDATE
	public Object updateUserData(UserEntity params) throws Exception {
		int result = dbMapper.updateUserData(params);
		return result;
	}

	// User_SELECT
	public Object selectUserData(int UserID) throws Exception {
		List<UserEntity> printData = dbMapper.selectUserData(UserID);
		return printData;
	}

	// User_SELECT_ALL
	public Object selectAllUserData() throws Exception {
		List<UserEntity> printData = dbMapper.selectAllUserData();
		return printData;
	}

	// User_SELECT_BY_EMAIL with PasswordEncoder
	public UserEntity selectUserByEmail(String Email, final String password, final PasswordEncoder encoder)
			throws Exception {
		final UserEntity originalUser = dbMapper.selectUserByEmail(Email);
		if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
			return originalUser;
		}
		return null;
	}

	// User_SELECT_BY_EMAIL
	public UserEntity selectUserByEmail(String Email) throws Exception {
		UserEntity printData = dbMapper.selectUserByEmail(Email);
		return printData;
	}
	
	// Unverified_User_DELETE
	public void deleteUnverifiedUser() throws Exception {
		dbMapper.deleteUnverifiedUser();
	}

	////////////
	////////////
	////////////

	// Commnet_INSERT
	public Object insertComment(CommentEntity params) throws Exception {
		int result = dbMapper.insertComment(params);
		return result;
	}

	// Commnet_DELETE
	public Object deleteComment(int commentID) throws Exception {
		int result = dbMapper.deleteComment(commentID);
		return result;
	}

	// Commnet_UPDATE
	public Object updateComment(CommentEntity params) throws Exception {
		int result = dbMapper.updateComment(params);
		return result;
	}

	// Comment_SELECT_ALL_IN_MOVIE_INFO
	public Object selectAllMComment(int movieID) throws Exception {
		List<CommentEntity> printData = dbMapper.selectAllMComment(movieID);
		return printData;
	}

	// Comment_SELECT_ALL_IN_USER_MYROOM
	public Object selectAllUComment(int UserID) throws Exception {
		List<CommentEntity> printData = dbMapper.selectAllUComment(UserID);
		return printData;
	}

	////////////
	////////////
	////////////

	// WatchHistory_INSERT_OR_UPDATE
	public Object insertOrUpdateW_History(WatchHistoryEntity params) throws Exception {
		int result = dbMapper.insertOrUpdateW_History(params);
		return result;
	}

	// WatchHistory_DELETE
	public Object deleteW_History(int watchHistoryID) throws Exception {
		int result = dbMapper.deleteW_History(watchHistoryID);
		return result;
	}

	// WatchHistory_SELECT_All_ON_MOVIE_ID
	public Object selectAllMW_History(int movieID) throws Exception {
		List<WatchHistoryEntity> printData = dbMapper.selectAllMW_History(movieID);
		return printData;
	}

	// WatchHistory_SELECT_ALL_ON_USER_ID
	public Object selectAllUW_History(int userID) throws Exception {
		List<WatchHistoryEntity> printData = dbMapper.selectAllUW_History(userID);
		return printData;
	}

	////////////
	////////////
	////////////

	// WishList_INSERT
	public Object insertWishList(WishListEntity params) throws Exception{
		int result = dbMapper.insertWishList(params);
		return result;
	}

	// WishList_DELETE
	public Object deleteWishList(int userID, int movieID) throws Exception {
		int result = dbMapper.deleteWishList(userID, movieID);
		return result;
	}

	// WishList_SELECT_ALL_ON_USER_ID
	public Object selectAllUWishList(int userID) throws Exception {
		List<WishListEntity> printData = dbMapper.selectAllUWishList(userID);
		return printData;
	}
	
	// WishList_DELETE
	public int isWishExists(int UserID, int MovieID) throws Exception {
		int result = dbMapper.isWishExists(UserID, MovieID);
		return result;
	}

}
