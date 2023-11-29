package egovframework.App.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.App.model.CommentEntity;
import egovframework.App.model.MovieEntity;
import egovframework.App.model.NowMovieEntity;
import egovframework.App.model.UserEntity;
import egovframework.App.model.WatchHistoryEntity;
import egovframework.App.model.WishListEntity;
import egovframework.App.service.dbService;
import egovframework.App.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class dbController {

	private dbService dbService;

	// 생성자 주입
	public dbController(dbService dbService) {
		this.dbService = dbService;
	}

	// Now_Movie_INSERT
	@RequestMapping(value = "NowMovieInsert", method = RequestMethod.POST)
	public Map<String, Object> insertMovieData(@RequestBody NowMovieEntity params) throws Exception {
		log.info("Now Movie Insert controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.insertNowMovie(params));
		return result;
	}

	// Now_Movie_DELETE
	@RequestMapping(value = "NowMovieDelete/{MovieID}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteMovieData(@PathVariable("MovieID") int MovieID) throws Exception {
		log.info("Now Movie Delete controller Selected ID: " + MovieID);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.deleteMovieData(MovieID));
		return result;
	}

	// Now_Movie_UPDATE
	@RequestMapping(value = "NowMovieUpdate", method = RequestMethod.PUT)
	public Map<String, Object> updateMovieData(@RequestBody NowMovieEntity params) throws Exception {
		log.info("Now Movie Update controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.updateMovieData(params));
		return result;
	}

	// Now_Movie_SELECT_By_MovieID
	@GetMapping(value = "NowMovieSelectID/{MovieID}")
	public ResponseEntity<Map<String, Object>> selectMovieData(@RequestHeader HttpHeaders requestHeaders,
											@PathVariable("MovieID") int movieID) throws Exception {
		log.info("Now Movie Select controller Selected ID: " + movieID);

		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectMovieData(movieID));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, movieID);
	}
	
	// Now_Movie_SELECT_Title
	@GetMapping(value = "NowMovieSelectTitle/{title}")
	public ResponseEntity<Map<String, Object>> selectMovieData(@RequestHeader HttpHeaders requestHeaders,
											@PathVariable("title") String title) throws Exception {
		log.info("Now Movie Select controller Selected: " + title);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectMovieData(title));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, title);
	}

	// Now_Movie_SELECT_ALL
	@GetMapping(value = "NowMovieSelectAll")
	public ResponseEntity<Map<String, Object>> selectAllMovieData(
										@RequestHeader HttpHeaders requestHeaders) throws Exception {
		log.info("Now Movie Select All controller");
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllMovieData());
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders);
	}
	
	// Now_Movie_SELECT_DESC_Rating
	@GetMapping(value = "NowMovieSelectDESCRating")
	public ResponseEntity<Map<String, Object>> selecNowMovieDESCRating(
										@RequestHeader HttpHeaders requestHeaders) throws Exception {
		log.info("Now Movie Select By Rating controller");
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectNowMovieDESCRating());
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders);
	}
	
	// Now_Movie_SELECT_DESC_ReservationRate
	@GetMapping(value = "NowMovieSelectDESCReserveRate")
	public ResponseEntity<Map<String, Object>> selectNowMovieDESCReserveRate(
								@RequestHeader HttpHeaders requestHeaders,
								@RequestParam(value = "page", defaultValue = "1") int page,
							    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
							    ) throws Exception {
		
		log.info("Now Movie Select By Reservation Rate controller");
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectNowMovieDESCReserveRate(page, pageSize));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders);
	}
	
	
	
	//////////////
	//////////////
	//////////////

	
	
	// Movie_INSERT
	@RequestMapping(value = "MovieInsert", method = RequestMethod.POST)
	public Map<String, Object> insertMovie(@RequestBody MovieEntity params) throws Exception {
		log.info("Movie Insert controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.insertMovie(params));
		return result;
	}

	// Movie_DELETE
	@RequestMapping(value = "MovieDelete/{MovieID}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteMovie(@PathVariable("MovieID") int MovieID) throws Exception {
		//전처리
		//현재 상영중인 영화ID -> 타이틀 추출  --Now쪽 DB에 검색-->
		String title = dbService.selectMovieByID(MovieID);
		NowMovieEntity NowMovieInfo = dbService.selectMovieID(title); //NowMovie개체
		int NowMovieID = NowMovieInfo.getMovieID(); 	// MovieID추출

		log.info("Movie Delete controller");
		log.info("Selected MovieID: " + MovieID + " || Selected NowMovieID: " + NowMovieID);
		
		Map<String, Object> result = new HashMap<>();
		result.put("deleteMovieResult", dbService.deleteMovie(MovieID));
		result.put("deleteMovieDataResult", dbService.deleteMovieData(NowMovieID));
		return result;
	}

	// Movie_UPDATE
	@RequestMapping(value = "MovieUpdate", method = RequestMethod.PUT)
	public Map<String, Object> updateMovie(@RequestBody MovieEntity params) throws Exception {
		log.info("Movie Update controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.updateMovie(params));
		return result;
	}

	// Movie_SELECT_Title
	@GetMapping(value = "MovieSelect/{title}")
	public ResponseEntity<Map<String, Object>> selectMovie(@RequestHeader HttpHeaders requestHeaders,
											@PathVariable("title") String title) throws Exception {	
		log.info("Movie Select controller Selected: " + title);
		Map<String, Object> result = new HashMap<>();
		//상영중일 경우 now_movie_info에서 조회
		if (dbService.selectMovieData(title).size() != 0) {
			result.put("result", dbService.selectMovieData(title));
			return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, title);
		}
		result.put("result", dbService.selectMovie(title));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, title);
	}
	
	// Movie_SELECT_MovieID
	@GetMapping(value = "MovieSelectByTitle/{title}")
	public ResponseEntity<Map<String, Object>> selectMovieByTitle(@RequestHeader HttpHeaders requestHeaders,
											@PathVariable("title") String title) throws Exception {
		log.info("Movie Select controller Selected: " + title);
		Map<String, Object> result = new HashMap<>();
		//상영중일 경우 now_movie_info에서 조회
		if (dbService.selectMovieData(title).size() != 0) {
			result.put("result", dbService.selectMovieData(title));
			return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, title);
		}
		result.put("result", dbService.selectMovieTitle(title));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, title);
	}
	
	// Movie_SELECT_MovieID
	@GetMapping(value = "MovieSelectByID/{MovieID}")
	public ResponseEntity<Map<String, Object>> selectMovie(@RequestHeader HttpHeaders requestHeaders,
											@PathVariable("MovieID") int MovieID) throws Exception {
		log.info("Movie Select controller Selected: " + MovieID);
		Map<String, Object> result = new HashMap<>();
		String title = dbService.selectMovieByID(MovieID);
		//상영중일 경우 now_movie_info에서 조회
		if (dbService.selectMovieData(title).size() != 0) {
			result.put("result", dbService.selectMovieData(title));
			return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, MovieID);
		}
		result.put("result", dbService.selectMovie(title));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, MovieID);
	}

	// Movie_SELECT_ALL
	@GetMapping(value = "MovieSelectAll")
	public ResponseEntity<Map<String, Object>> selectAllMovie(
											@RequestHeader HttpHeaders requestHeaders) throws Exception {
		log.info("Movie Select All controller");
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllMovie());
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders);
	}
	
	@GetMapping(value = "MovieSelectDESCRating")
	public ResponseEntity<Map<String, Object>> selecMovieDESCRating(
	    @RequestHeader HttpHeaders requestHeaders,
	    @RequestParam(value = "page", defaultValue = "1") int page,
	    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) throws Exception {
		
	    log.info("Movie Select By Rating controller");
	    Map<String, Object> result = new HashMap<>();
	    result.put("result", dbService.selectMovieDESCRating(page, pageSize));
	    return CacheUtil.getResponseEntityWithCaching(result, requestHeaders);
	}
	
	// Movie_SELECT_By_Actors
	@GetMapping(value = "MovieSelectByActor/{Actors}")
	public ResponseEntity<Map<String, Object>> selectMovieByActor(
								@RequestHeader HttpHeaders requestHeaders, @PathVariable("Actors") String Actors,
								@RequestParam(value = "page", defaultValue = "1") int page,
							    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) throws Exception {
		
		log.info("Movie Select controller Selected: " + Actors);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectMovieByActor(Actors, page, pageSize));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, Actors);
	}
	
	// Movie_SELECT_By_Genre
	@GetMapping(value = "MovieSelectByGenre/{Genre1}/{Genre2}")
	public ResponseEntity<Map<String, Object>> selectMovieByGenre(@RequestHeader HttpHeaders requestHeaders,
				@PathVariable("Genre1") String Genre1, @PathVariable("Genre2") String Genre2,
				@RequestParam(value = "page", defaultValue = "1") int page,
				@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) throws Exception {
		
		log.info("Movie Select controller Selected By Genre: " + Genre1 + ", " + Genre2);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectMovieByGenre(Genre1, Genre2, page, pageSize));
		return CacheUtil.getResponseEntityWithCaching(result, requestHeaders, Genre1, Genre2);
	}

	//////////////
	//////////////
	//////////////

	// User_INSERT
	@RequestMapping(value = "UserInsert", method = RequestMethod.POST)
	public Map<String, Object> insertUserData(@RequestBody UserEntity params) throws Exception {
		log.info("User_Data Insert controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.insertUserData(params));
		return result;
	}

	// User_DELETE
	@RequestMapping(value = "UserDelete/{UserID}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteUserData(@PathVariable("UserID") int UserID) throws Exception {
		log.info("User_Data Delete controller Selected ID: " + UserID);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.deleteUserData(UserID));
		return result;
	}

	// User_UPDATE
	@RequestMapping(value = "UserUpdate", method = RequestMethod.PUT)
	public Map<String, Object> updateUserData(@RequestBody UserEntity params) throws Exception {
		log.info("User_Data Update controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.updateUserData(params));
		return result;
	}

	// User_SELECT_By_Email
	@GetMapping(value = "UserSelect/{Email}")
	public Map<String, Object> selectUserByEmail(@PathVariable("Email") String Email) throws Exception {
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectUserByEmail(Email));
		log.debug("User Select controller Selected: " + Email);
		return result;
	}

	// User_SELECT_ALL
	@GetMapping(value = "UserSelectAll")
	public Map<String, Object> selectAllUserData() throws Exception {
		log.info("User_Data Select All controller");
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllUserData());
		return result;
	}

	//////////////
	//////////////
	//////////////

	// Comment_INSERT
	@RequestMapping(value = "CommentInsert", method = RequestMethod.POST)
	public Map<String, Object> insertCommentData(@RequestBody CommentEntity params) throws Exception {
		log.info("Comment Insert controller >" + params);
		Map<String, Object> result = new HashMap<>();
		
		List<MovieEntity> movieEntity = dbService.selectMovieTitle(params.getTitle());
		params.setMovieID(movieEntity.get(0).getMovieID());
		
		result.put("result", dbService.insertComment(params));
		return result;
	}

	// Comment_DELETE
	@RequestMapping(value = "CommentDelete/{CommentID}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteCommentData(@PathVariable("CommentID") int CommentID) throws Exception {
		log.info("Comment Delete controller Selected ID: " + CommentID);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.deleteComment(CommentID));
		return result;
	}

	// Comment_UPDATE
	@RequestMapping(value = "CommentUpdate", method = RequestMethod.PUT)
	public Map<String, Object> updateCommentData(@RequestBody CommentEntity params) throws Exception {
		log.info("Comment Update controller >" + params);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.updateComment(params));
		return result;
	}

	// Comment_SELECT_ALL_IN_MOVIE_INFO
	@GetMapping(value = "CommentSelectAllM/{title}")
	public Map<String, Object> selectAllMCommentData(@PathVariable("title") String title) throws Exception {
		log.info("Comment Select All in MovieInfo controller Selected ID: " + title);
		Map<String, Object> result = new HashMap<>();
		List<MovieEntity> MovieID = dbService.selectMovieByTitle(title);
		
		result.put("result", dbService.selectAllMComment(MovieID.get(0).getMovieID()));
		return result;
	}

	// Comment_SELECT_ALL_IN_USER_MYROOM
	@GetMapping(value = "CommentSelectAllU/{UserID}")
	public Map<String, Object> selectAllUCommentData(@PathVariable("UserID") int UserID) throws Exception {
		log.info("Comment Select All in UserInfo controller Selected ID: " + UserID);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllUComment(UserID));
		return result;
	}

	//////////////
	//////////////
	//////////////

	// WatchHistory_INSERT_OR_UPDATE
	@RequestMapping(value = "W_HistoryInsertOrUpdate", method = RequestMethod.POST)
	public Map<String, Object> insertW_History(@RequestBody WatchHistoryEntity params) throws Exception {
		log.info("Watch History Insert controller >" + params);
		//이메일로 UserID
		
		List<MovieEntity> movieEntity = dbService.selectMovieTitle(params.getTitle());
		UserEntity temp = dbService.selectUserByEmail(params.getEmail());
		params.setUserID(temp.getUserID());
		params.setMovieID(movieEntity.get(0).getMovieID());
		
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.insertOrUpdateW_History(params));
		return result;
	}

	// WatchHistory_DELETE
	@RequestMapping(value = "W_HistoryDelete/{WatchHistoryID}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteW_History(@PathVariable("WatchHistoryID") int WatchHistoryID) throws Exception {
		log.info("Comment Delete controller Selected ID: " + WatchHistoryID);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.deleteW_History(WatchHistoryID));
		return result;
	}

	// WatchHistory_SELECT_ALL_ON_MovieID
	@GetMapping(value = "W_HistorySelectAllM/{MovieID}")
	public Map<String, Object> selectAllMW_History(@PathVariable("MovieID") int MovieID) throws Exception {
		log.info("W_History Select All in MovieInfo controller Selected ID: " + MovieID);
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllMW_History(MovieID));
		return result;
	}

	// WatchHistory_SELECT_ALL_ON_UserID
	@GetMapping(value = "W_HistorySelectAllU/{Email}")
	public Map<String, Object> selectAllUW_History(@PathVariable("Email") String Email) throws Exception {
		log.info("W_History Select All in UserInfo controller Selected ID: " + Email);
		//이메일로 UserID
		UserEntity temp = dbService.selectUserByEmail(Email);
		int UserID = temp.getUserID();
				
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllUW_History(UserID));
		return result;
	}

	//////////////
	//////////////
	//////////////

	
	// WishList_INSERT
	@RequestMapping(value = "WishListInsert", method = RequestMethod.POST)
	public Map<String, Object> insertWishList(@RequestBody WishListEntity params) throws Exception {
		log.info("Wish List Insert controller >" + params);
		//이메일로 UserID
		UserEntity temp = dbService.selectUserByEmail(params.getEmail());
		List<MovieEntity> movieEntity = dbService.selectMovieTitle(params.getTitle());
		
		params.setUserID(temp.getUserID());
		params.setMovieID(movieEntity.get(0).getMovieID());
		
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.insertWishList(params));
		return result;
	}

	// WishList_DELETE
	@RequestMapping(value = "WishListDelete/{Email}/{MovieID}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteWishList(@PathVariable("Email") String Email, 
												@PathVariable("MovieID") int MovieID) throws Exception {
		log.info("Wish List Delete controller"); 
		log.info("Selected Email: " + Email + " || MovieID: " + MovieID);
		//이메일로 UserID
	  	UserEntity temp = dbService.selectUserByEmail(Email);
	  	int UserID = temp.getUserID();
		
	  	Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.deleteWishList(UserID, MovieID));
		return result;
	}

	// WishList_SELECT_ALL_ON_Email
	@GetMapping(value = "WishListSelectAllU/{Email}")
	public Map<String, Object> selectAllUWishList(@PathVariable("Email") String Email) throws Exception {
		log.info("Wish List Select All in UserInfo controller Selected ID: " + Email);
		//이메일로 UserID
		UserEntity temp = dbService.selectUserByEmail(Email);
		int UserID = temp.getUserID();
				
		Map<String, Object> result = new HashMap<>();
		result.put("result", dbService.selectAllUWishList(UserID));
		return result;
	}
	
	// WishList_SELECT_ON_Email_AND_MovieID
	@GetMapping(value = "WishListSelect/{Email}/{MovieID}")
	public boolean isWishExists(@PathVariable("Email") String Email, 
								@PathVariable("MovieID") int MovieID) throws Exception {
		log.info("Wish List Select controller"); 
		log.info("Selected Email: " + Email + " || MovieID: " + MovieID);
		String title = dbService.selectMovieByID(MovieID);
		List<MovieEntity> entity = dbService.selectMovieTitle(title);
		
	    //이메일로 UserID
	  	UserEntity temp = dbService.selectUserByEmail(Email);
	  	int UserID = temp.getUserID();
	  	boolean result = dbService.isWishExists(UserID, entity.get(0).getMovieID()) >= 1 ? true : false;
	  	
	    return result;
	}

}
