package egovframework.App.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import egovframework.App.dto.ResponseDTO;
import egovframework.App.model.MovieEntity;
import egovframework.App.service.dbService;
import egovframework.App.util.CacheUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/recommend")
public class RecommendController {

	private dbService dbService = null;
	private IndexSearcher searcher;

	// 생성자 주입
	@Autowired
	public RecommendController(dbService dbService) {
		this.dbService = dbService;
	}
	
	public RecommendController(IndexReader reader) {
		BM25Similarity bm25Similarity = new BM25Similarity(1.2f, 0.75f); // 여기에서 가중치를 설정
	    searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(bm25Similarity);
	}

	static class Movie { // 
		String title;
		String synopsis;

		public Movie(String title, String synopsis) {
			this.title = title;
			this.synopsis = synopsis;
		}
	}

	static class MovieIndexer {
		private IndexWriter writer;

		public MovieIndexer(Directory indexDir) throws IOException {
			writer = new IndexWriter(indexDir, new IndexWriterConfig(new StandardAnalyzer()));
		}

		public void close() throws IOException {
			writer.close();
		}

		public void indexMovie(Movie movie) throws IOException {
			Document doc = new Document();
			doc.add(new TextField("title", movie.title, Field.Store.YES));
			doc.add(new TextField("overview", movie.synopsis, Field.Store.YES));
			writer.addDocument(doc);
		}
	}

	@GetMapping("/{title}")
	public ResponseEntity<?> recommendMovie(@PathVariable("title") String title,
									HttpServletRequest request) throws Exception {
		log.info("recommendMovie about "+ title + "...");
		List<MovieEntity> searchData = dbService.selectMovie(title);
		List<MovieEntity> data = dbService.selectAllMovie();
		List<MovieEntity> responseList = new ArrayList<>();

		List<String> responseData = recommendMain(searchData, data);
		
	    for (int i = 1; i < 11; i++) {
	        String movieTitle = responseData.get(i);

	        // 각 영화 제목에 대한 MovieEntity를 DB에서 가져옴
	        List<MovieEntity> movieEntity = dbService.selectMovie(movieTitle);
	        
	        responseList.add(movieEntity.get(0));
	    }

		// 응답을 위한 UserDTO 생성
		ResponseDTO<MovieEntity> responseUserDTO = ResponseDTO.<MovieEntity>builder().data(responseList).build();

		// 요청 헤더를 HttpHeaders 객체로 변환
		HttpHeaders requestHeaders = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			requestHeaders.set(headerName, request.getHeader(headerName));
		}

		// 캐싱 적용하여 응답 생성
		return CacheUtil.getResponseEntityWithCaching(responseUserDTO, requestHeaders, title);
	}

	public List<String> recommend(String overview, String selfTitle) throws Exception {
	    QueryParser parser = new QueryParser("overview", new StandardAnalyzer());
	    Query query = parser.parse(QueryParser.escape(overview));
	    log.debug("Query: " + query.toString()); // 쿼리 로깅

	    TopDocs docs = searcher.search(query, Integer.MAX_VALUE);

	    // ScoreDoc 배열을 유사도 내림차순으로 정렬
	    ScoreDoc[] sortedDocs = Arrays.copyOf(docs.scoreDocs, docs.scoreDocs.length);
	    Arrays.sort(sortedDocs, Comparator.comparingDouble(doc -> -doc.score));

	    List<String> results = new ArrayList<>();
	    for (ScoreDoc scoreDoc : sortedDocs) {
	        String foundTitle = searcher.doc(scoreDoc.doc).get("title");
	        if (!foundTitle.equalsIgnoreCase(selfTitle)) { // 본래의 영화 제목과 동일한 제목의 영화를 제외
	            results.add(foundTitle);
	        }
	    }
	    log.debug("Results: " + results.toString()); // 결과 로깅
	    return results;
	}



	public static List<String> recommendMain(List<MovieEntity> searchData, List<MovieEntity> data) throws IOException {
	    ByteBuffersDirectory indexDir = new ByteBuffersDirectory();
	    MovieIndexer indexer = new MovieIndexer(indexDir);

	    // 중복된 영화를 필터링하기 위한 Set
	    Set<String> movieTitles = new HashSet<>();

	    for (MovieEntity movieEntity : data) {
	        // 중복된 영화 제목이 이미 추가되었는지 확인
	        if (!movieTitles.contains(movieEntity.getTitle())) {
	            indexer.indexMovie(new Movie(movieEntity.getTitle(), movieEntity.getSynopsis()));
	            movieTitles.add(movieEntity.getTitle());
	        }
	    }
	    indexer.close();

	    IndexReader reader = DirectoryReader.open(indexDir);
	    RecommendController controller = new RecommendController(reader);

	    try {
	        // 자신의 영화를 제외하고 추천 결과를 반환
	        String selfTitle = searchData.get(0).getTitle();
	        List<String> recommendations = controller.recommend(searchData.get(0).getSynopsis(), selfTitle);
	        recommendations.remove(selfTitle); // 자신의 영화 제거
	        return recommendations;
	    } catch (Exception e) {
	        e.printStackTrace();
	        List<String> err = new ArrayList<>();
	        err.add("error");
	        return err;
	    }
	}
}
