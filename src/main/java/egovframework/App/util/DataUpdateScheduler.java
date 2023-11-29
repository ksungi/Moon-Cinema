package egovframework.App.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import egovframework.App.service.MovieService;
import egovframework.App.service.NowPlayingService;
import egovframework.App.service.dbService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataUpdateScheduler {

	private final dbService dbService;
	
	@Autowired
	public DataUpdateScheduler(dbService dbService) {
	    this.dbService = dbService;
	}
	
	// 특정 시간마다 비인증 계정을 지우는 스케줄러 메소드
	@Scheduled(cron = "0 0 2-22 * * *") // 새벽 2시부터 밤 10시까지 매시간 실행
	//			       초 분  시  일 월 주  // -범위 | ,여러개 | 0/15(0에 시작해서 15마다) | * 다
	public void deleteUnverifiedUser() throws Exception {
		log.info("장기 미인증 계정 제거중...");
		dbService.deleteUnverifiedUser();
		log.info("장기 미인증 계정 제거 완료");
	}
	
	//현재 상영중인 영화 DB 업데이트//////////////////////////////////
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void updateData() {
        NowPlayingService.NowPlaying();
        log.info("상영중인 영화정보 업데이트 완료...");
    }
    
    
    //전체 영화 DB 업데이트/////////////////////////////////////////
    @Scheduled(cron = "0 30 0 ? * MON") // 월요일 자정 30분 후에 실행
    public void updateMovieDataMonday() {
        MovieService.NowPlaying(0, 98);
        log.info("영화정보 업데이트 완료...");
    }
    
    @Scheduled(cron = "0 30 0 ? * TUE") // 화요일 자정 30분 후에 실행
    public void updateMovieDataTuesday() {
        MovieService.NowPlaying(99, 197);
        log.info("영화정보 업데이트 완료...");
    }
    
    @Scheduled(cron = "0 30 0 ? * WED") // 수요일 자정 30분 후에 실행
    public void updateMovieDataWednesday() {
        MovieService.NowPlaying(198, 297);
        log.info("영화정보 업데이트 완료...");
    }
    
    @Scheduled(cron = "0 30 0 ? * THU") // 목요일 자정 30분 후에 실행
    public void updateMovieDataThursday() {
        MovieService.NowPlaying(298, 397);
        log.info("영화정보 업데이트 완료...");
    }
    
    @Scheduled(cron = "0 30 0 ? * FRI") // 금요일 자정 30분 후에 실행
    public void updateMovieDataFriday() {
        MovieService.NowPlaying(398, 450);
        log.info("영화정보 업데이트 완료...");
    }
    ////////////////////////////////////////////////////////////    
}
