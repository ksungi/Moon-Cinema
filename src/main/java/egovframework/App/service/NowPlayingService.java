package egovframework.App.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.App.dao.MovieEntityDao;
import egovframework.App.model.MovieEntity;
import egovframework.App.model.NowMovieEntity;
import egovframework.App.util.MyBatisUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NowPlayingService {
	private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3MDk4ZGI5MmE2ZWIyZGExYWE3ZWZjZjc4NzAyNzhmYSIsInN1YiI6IjY1M2EwYmNjMjgxMWExMDEyYzk5MDY4MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.TSP9aBsO-a006_nbIqKc_v5FWKh54lPOcf_0U7lxWwE";

	public static void NowPlaying() {
		deleteDB(); // deleteDB
		resetAutoIncrement(); // resetAutoIncrement
		List<NowMovieEntity> dataList = new ArrayList<>();
		SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
		MovieEntityDao movieEntityDao = new MovieEntityDao(sqlSessionFactory);

		for (int i = 0; i < 5; i++) {
			String URL = "https://api.themoviedb.org/3/movie/now_playing?language=ko-KR&page=" + (i + 1) + "&region=KR";
			log.info(URL);
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).header("accept", "application/json")
					.header("Authorization", "Bearer " + API_KEY).method("GET", HttpRequest.BodyPublishers.noBody())
					.build();

			try {
				HttpResponse<String> response = HttpClient.newHttpClient().send(request,
						HttpResponse.BodyHandlers.ofString());
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> jsonMap = objectMapper.readValue(response.body(),
						new TypeReference<Map<String, Object>>() {
						});
				List<Map<String, Object>> results = (List<Map<String, Object>>) jsonMap.get("results");
				for (Map<String, Object> result : results) {
					NowMovieEntity movieEntity = movieInfo(String.valueOf(result.get("id")));
					dataList.add(movieEntity);
				}
				movieOtherInfo(dataList);
				Scraping(dataList);

				List<MovieEntity> movieEntities = new ArrayList<>();
				for (int j = 0; j < dataList.size(); j++) {
					NowMovieEntity nowMovie = dataList.get(j);
					if (movieEntityDao.selectMovie(nowMovie.getTitle()).isEmpty()) {
						MovieEntity movieEntity = new MovieEntity();
						movieEntity.setTitle(nowMovie.getTitle());
						movieEntity.setSynopsis(nowMovie.getSynopsis());
						movieEntity.setDuration(nowMovie.getDuration());
						movieEntity.setRating(nowMovie.getRating());
						movieEntity.setImgURL(nowMovie.getImgURL());
						movieEntity.setGenre1(nowMovie.getGenre1());
						movieEntity.setGenre2(nowMovie.getGenre2());
						movieEntity.setGenre3(nowMovie.getGenre3());
						movieEntity.setReleaseDate(nowMovie.getReleaseDate(j));
						movieEntity.setDirector(nowMovie.getDirector());
						movieEntity.setActors(nowMovie.getActors());
						movieEntity.setAgeGrade(nowMovie.getAgeGrade());
						movieEntities.add(movieEntity);
					} else {
						MovieEntity movieEntity = movieEntityDao.selectMovie(nowMovie.getTitle()).get(0);
						movieEntity.setRating(nowMovie.getRating());
						movieEntity.setImgURL(nowMovie.getImgURL());
						movieEntity.setReleaseDate(nowMovie.getReleaseDate(j));
						movieEntity.setDirector(nowMovie.getDirector());
						movieEntity.setActors(nowMovie.getActors());
						movieEntity.setAgeGrade(nowMovie.getAgeGrade());
						movieEntityDao.updateMovie(movieEntity);
					}
				}
				saveDB(movieEntities);

				// dataList 초기화
				dataList.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void movieOtherInfo(List<NowMovieEntity> dataList) {
		Iterator<NowMovieEntity> iterator = dataList.iterator();

		while (iterator.hasNext()) {
			NowMovieEntity movie = iterator.next();

			String apiKey = "f5eef3421c602c6cb7ea224104795888";
			String movieName = movie.getTitle().replace(" ", ""); // 띄어쓰기를 없애고 검색하고자 하는 영화 이름 설정

			try {
				HttpClient client = HttpClient.newHttpClient();

				String url = "https://kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieList.json?key=" + apiKey
						+ "&movieNm=" + movieName;

				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() == 200) {
					String responseBody = response.body();

					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, Object> jsonMap = objectMapper.readValue(responseBody,
							new TypeReference<Map<String, Object>>() {
							});

					// jsonMap을 사용하여 필요한 정보를 추출 후 처리
					// 예를 들어, "movieListResult" 키 아래에 검색 결과가 있을 것입니다.
					Map<String, Object> movieListResult = (Map<String, Object>) jsonMap.get("movieListResult");
					List<Map<String, Object>> movieList = (List<Map<String, Object>>) movieListResult.get("movieList");
					
					if (movieList.size() == 0) {
						log.info("Title Search Failed Movie: " + movie.getTitle());
						//검색 실패한 영화제목은 삭제. 영화 제목 검색 시 없는 경우는 국내에 미개봉일 경우가 대다수
						iterator.remove();
					} else {
						Map<String, Object> movieinfo = movieList.get(0);
						String movieCode = (String) movieinfo.get("movieCd");

						String url2 = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?key="
								+ apiKey + "&movieCd=" + movieCode;
						HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create(url2)).GET().build();
						HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

						if (response2.statusCode() == 200) {
							String responseBody2 = response2.body();

							ObjectMapper objectMapper2 = new ObjectMapper();
							Map<String, Object> jsonMap2 = objectMapper2.readValue(responseBody2,
									new TypeReference<Map<String, Object>>() {
									});

							Map<String, Object> movieInfoResult = (Map<String, Object>) jsonMap2.get("movieInfoResult");
							Map<String, Object> movieInfo = (Map<String, Object>) movieInfoResult.get("movieInfo");
							List<Map<String, Object>> directors = (List<Map<String, Object>>) movieInfo.get("directors"); // 감독
							if (directors != null && !directors.isEmpty()) {
								movie.setDirector((String) directors.get(0).get("peopleNm"));
							} else {
								movie.setDirector("Unknown Director"); // 감독이 표기되어 있지 않은 영화가 있다
							}
							List<Map<String, Object>> actors = (List<Map<String, Object>>) movieInfo.get("actors"); // 배우
							if (actors != null && !actors.isEmpty()) {
								movie.setActors((String) actors.get(0).get("peopleNm"));
							} else {
								movie.setActors("Unknown Actor"); // 배우 정보가 없는 경우 대체값을 설정
							}
							List<Map<String, Object>> audits = (List<Map<String, Object>>) movieInfo.get("audits"); // 시청등급
							if (audits != null && !audits.isEmpty()
									&& ((Map<String, Object>) audits.get(0)).get("watchGradeNm") != "") {
								String watchGradeNm = (String) ((Map<String, Object>) audits.get(0))
										.get("watchGradeNm");
								if (watchGradeNm.equals("18세 미만인 자는 관람할 수 없는 등급") || watchGradeNm.equals("연소자관람불가"))
									watchGradeNm = "청소년관람불가";
								else if (watchGradeNm.equals("고등학생이상관람가") || watchGradeNm.equals("15세관람가")
										|| watchGradeNm.equals("15세 미만인 자는 관람할 수 없는 등급 "))
									watchGradeNm = "15세이상관람가";
								else if (watchGradeNm.equals("중학생이상관람가") || watchGradeNm.equals("12세관람가")
										|| watchGradeNm.equals("국민학생관람불가")
										|| watchGradeNm.equals("12세 미만인 자는 관람할 수 없는 등급"))
									watchGradeNm = "12세이상관람가";
								else if (watchGradeNm.equals("모든 관람객이 관람할 수 있는 등급") || watchGradeNm.equals("연소자관람가"))
									watchGradeNm = "전체관람가";
								movie.setAgeGrade(watchGradeNm);
							} else {
								movie.setAgeGrade("Unknown Audits"); // 배우 정보가 없는 경우 대체값을 설정
							}
						}
					}
				} else {
					log.warn("API 요청 실패: " + response.statusCode());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/* 영화제목, 줄거리, 런타임, 포스터url, 영화장르, 개봉일, 평점 추가함수 */
	private static NowMovieEntity movieInfo(String movieCode) {
		String url = "https://api.themoviedb.org/3/movie/" + movieCode + "?language=ko-KR";
		NowMovieEntity movieEntity = new NowMovieEntity();

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "application/json")
				.header("Authorization", "Bearer " + API_KEY).method("GET", HttpRequest.BodyPublishers.noBody()).build();

		try {
			HttpResponse<String> response = HttpClient.newHttpClient().send(request,
					HttpResponse.BodyHandlers.ofString());

			// JSON 데이터를 Map으로 파싱
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> jsonMap = objectMapper.readValue(response.body(),
					new TypeReference<Map<String, Object>>() {
					});

			movieEntity.setTitle((String) jsonMap.get("title")); // 제목
			movieEntity.setSynopsis((String) jsonMap.get("overview")); // 시놉시스
			movieEntity.setDuration((int) jsonMap.get("runtime")); // 런타임
			movieEntity.setRating((double) jsonMap.get("vote_average")); // 평점
			if (jsonMap.get("poster_path") != null && !jsonMap.isEmpty()) {
				movieEntity.setImgURL((String) jsonMap.get("poster_path")); // 포스터 url
			} else {
				movieEntity.setImgURL("Unknown ImgURL");
			}

			// https://image.tmdb.org/t/p/<이미지 크기>/<이미지 파일명>

			List<Map<String, Object>> genres = (List<Map<String, Object>>) jsonMap.get("genres"); // 영화 장르
			if (genres != null && !genres.isEmpty()) {
				for (int i = 0; i < genres.size(); i++) {
					String genreName = (String) genres.get(i).get("name");
					if (i == 0) {
						movieEntity.setGenre1(genreName);
					} else if (i == 1) {
						movieEntity.setGenre2(genreName);
					} else if (i == 2) {
						movieEntity.setGenre3(genreName);
					}
				}
			} else {
				movieEntity.setGenre1("Unknown Genre");
				movieEntity.setGenre2("Unknown Genre");
				movieEntity.setGenre3("Unknown Genre");
			}

			String dateString = (String) jsonMap.get("release_date"); // 개봉일
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date releaseDate = null;
			try {
				releaseDate = sdf.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// Date 객체에서 연, 월, 일을 추출하여 LocalDate로 변환
			if (releaseDate != null) {
				LocalDate localReleaseDate = releaseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				// 이제 localReleaseDate를 MovieEntity에 저장
				movieEntity.setReleaseDate(localReleaseDate);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return movieEntity;
	}

	private static void Scraping(List<NowMovieEntity> dataList) throws IOException {
		// Jsoup : 각종 사이트(HTML)에서 데이터를 취합할 수 있는 Library
		Document doc = Jsoup.connect("http://www.cgv.co.kr/movies/?lt=1&ft=0").get();

		Elements titles = doc.select("div.box-contents strong.title");
		Elements percents = doc.select("div.box-contents div.score strong.percent span");

		for (int i = 0; i < dataList.size(); i++) {
			String movieTitle = normalizeString(dataList.get(i).getTitle());

			// dataList의 title과 크롤링한 title을 비교
			for (int j = 0; j < titles.size(); j++) {
				Element titleElement = titles.get(j);
				String crawlingTitle = normalizeString(titleElement.text());

				if (movieTitle.equals(crawlingTitle)) {
					// 크롤링한 percent 값을 가져와서 % 기호를 제거하고 float로 변환
					Element percentElement = percents.get(j);
					String percentText = percentElement.text().replace("%", "");
					float percentValue = Float.parseFloat(percentText);
					dataList.get(i).setReservationRate(percentValue);
					break; // 일치하는 경우, 나머지 title에 대한 비교는 불필요
				}
			}
		}

		// dataList을 데이터베이스에 삽입
		SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
		MovieEntityDao movieEntityDao = new MovieEntityDao(sqlSessionFactory);
		movieEntityDao.insertNowMovieEntities(dataList);
	}
	
	private static String normalizeString(String input) {
	    // 공백과 특수문자를 제거하여 반환
	    return input.replaceAll("[\\s\\p{Punct}]", "");
	}

	private static void saveDB(List<MovieEntity> dataList) {
		SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
		MovieEntityDao movieEntityDao = new MovieEntityDao(sqlSessionFactory);
		movieEntityDao.insertMovieEntities(dataList);
	}

	private static void deleteDB() {
		SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
		MovieEntityDao movieEntityDao = new MovieEntityDao(sqlSessionFactory);
		movieEntityDao.deleteAllMovieData();
	}
	
	private static void resetAutoIncrement() {
	    SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
	    MovieEntityDao movieEntityDao = new MovieEntityDao(sqlSessionFactory);
	    movieEntityDao.resetAutoIncrement();
	}


	public static void main(String[] args) {
		NowPlaying();
	}
}
