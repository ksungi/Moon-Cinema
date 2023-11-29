package egovframework.App.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.App.dao.MovieEntityDao;
import egovframework.App.model.MovieEntity;
import egovframework.App.util.MyBatisUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MovieService {
	private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3MDk4ZGI5MmE2ZWIyZGExYWE3ZWZjZjc4NzAyNzhmYSIsInN1YiI6IjY1M2EwYmNjMjgxMWExMDEyYzk5MDY4MCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.TSP9aBsO-a006_nbIqKc_v5FWKh54lPOcf_0U7lxWwE";

	public static void NowPlaying(int startNum, int endNum) {
		List<MovieEntity> dataList = new ArrayList<>();

		for (int i = startNum; i < endNum; i++) {
			if (i % 30 == 0 && i !=0) {
				try {
					Thread.sleep(10000); //10초 휴식
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			String URL = "https://api.themoviedb.org/3/movie/top_rated?language=ko-KR&page=" + (i + 1) + "&region=KR";
			log.info(URL);
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).header("accept", "application/json")
					.header("Authorization",
							"Bearer " + API_KEY)
					.method("GET", HttpRequest.BodyPublishers.noBody()).build();

			try {
				HttpResponse<String> response = HttpClient.newHttpClient().send(request,
						HttpResponse.BodyHandlers.ofString());
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> jsonMap = objectMapper.readValue(response.body(),
						new TypeReference<Map<String, Object>>() {
						});
				List<Map<String, Object>> results = (List<Map<String, Object>>) jsonMap.get("results");
				for (Map<String, Object> result : results) {
					MovieEntity movieEntity = movieInfo(String.valueOf(result.get("id")));
					dataList.add(movieEntity);
				}
				movieOtherInfo(dataList);
				saveDB(dataList);

				// dataList 초기화
				dataList.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void movieOtherInfo(List<MovieEntity> dataList) {
		Iterator<MovieEntity> iterator = dataList.iterator();

		while (iterator.hasNext()) {
			MovieEntity movie = iterator.next();

			String apiKey = "f5eef3421c602c6cb7ea224104795888";
			String movieName = movie.getTitle().replace(" ", ""); // 띄어쓰기를 없애고 검색하고자 하는 영화 이름 설정
			// '%'를 기준으로 문자열을 분할
			String[] parts = movieName.split("%");

			// 앞쪽의 단어를 movieName에 저장
			movieName = parts[0];

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

						String movieCode = null;
						for (int i = 0; i < movieList.size(); i++) {
							Map<String, Object> movieinfo = movieList.get(i);
							String movieNm = ((String) movieinfo.get("movieNm")).replace(" ", "");
							if (movieNm.equals(movieName)) {
								movieCode = (String) movieinfo.get("movieCd");
								break;
							}
						}
						if (movieCode == null) {
							Map<String, Object> movieinfo = movieList.get(0);
							movieCode = (String) movieinfo.get("movieCd");
						}

						String url2 = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?key="
								+ apiKey + "&movieCd=" + movieCode;
						HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create(url2)).GET().timeout(Duration.ofSeconds(10)).build();
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
								log.debug(watchGradeNm);
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
								movie.setAgeGrade("Unknown Audits"); // 배우 정보가 없는 경우 대체 값을 설정
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
	private static MovieEntity movieInfo(String movieCode) {
		String url = "https://api.themoviedb.org/3/movie/" + movieCode + "?language=ko-KR";
		MovieEntity movieEntity = new MovieEntity();

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "application/json").header(
				"Authorization",
				"Bearer " + API_KEY)
				.method("GET", HttpRequest.BodyPublishers.noBody()).build();

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
			movieEntity.setImgURL((String) jsonMap.get("poster_path")); // 포스터 url
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

	private static void saveDB(List<MovieEntity> dataList) {
		// dataList을 데이터베이스에 삽입
		SqlSessionFactory sqlSessionFactory = MyBatisUtil.getSqlSessionFactory();
		MovieEntityDao movieEntityDao = new MovieEntityDao(sqlSessionFactory);
		for (MovieEntity movie : dataList) {
	        // 중복 여부를 확인하고 중복된 경우에는 업데이트, 아닌 경우에는 삽입
	        if (movieEntityDao.isMovieExists(movie.getTitle()) >= 1) {
	            // 이미 존재하는 영화인 경우 업데이트 수행
	            movieEntityDao.updateMovie(movie);
	        } else {
	            // 존재하지 않는 영화인 경우 삽입 수행
	            movieEntityDao.insertMovieEntities(movie);
	        }
	    }
	}
}
