package egovframework.App.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import egovframework.App.model.MovieEntity;
import egovframework.App.model.NowMovieEntity;

public class MovieEntityDao {
    private SqlSessionFactory sqlSessionFactory;

    public MovieEntityDao(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void insertNowMovieEntities(List<NowMovieEntity> dataList) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 각 movieEntity를 목록에서 삽입하려면 세션을 사용합니다.
            for (NowMovieEntity movie : dataList) {
                session.insert("egovframework.App.service.dbMapper.insertNowMovie", movie);
            }
            session.commit(); // 트랜잭션 커밋
        }
    }
    
    public void insertMovieEntities(List<MovieEntity> dataList) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 각 movieEntity를 목록에서 삽입하려면 세션을 사용합니다.
            for (MovieEntity movie : dataList) {
                session.insert("egovframework.App.service.dbMapper.insertMovie", movie);
            }
            session.commit(); // 트랜잭션 커밋
        }
    }
    
    public void insertMovieEntities(MovieEntity data) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.insert("egovframework.App.service.dbMapper.insertMovie", data);
            session.commit(); // 트랜잭션 커밋
        }
    }
    
    public void deleteAllMovieData() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.delete("egovframework.App.service.dbMapper.deleteAllMovieData");
            session.commit();
        }
    }
    
	public void resetAutoIncrement() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.update("egovframework.App.service.dbMapper.resetAutoIncrement");
            session.commit();
        }
	}
    
    public int isMovieExists(String title) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne("egovframework.App.service.dbMapper.countMovieByTitle", title);
        }
    }
    
    public void updateMovie(MovieEntity movie) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            session.update("egovframework.App.service.dbMapper.updateMovie", movie);
            session.commit();
        }
    }
    
    public List<MovieEntity> selectMovie(String title) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("egovframework.App.service.dbMapper.selectMovieTitle", title);
        }
    }

}
