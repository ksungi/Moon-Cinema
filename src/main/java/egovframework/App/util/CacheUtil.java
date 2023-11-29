package egovframework.App.util;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CacheUtil {
    public static <T> ResponseEntity<T> getResponseEntityWithCaching(T data, 
    							HttpHeaders requestHeaders, Object... params) {
        // ETag 생성
        String etag = "\"" + Objects.hash(data.hashCode()) + "\"";

        // 매 새벽 2시에 수명이 끝나도록 계산하여 유효기간 설정
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextEndTime = now.withHour(2).withMinute(0).withSecond(0).withNano(0);
        
        //현재시간 기준 종료시간을 지난 시간이라면 하루 + 1day
        if(now.compareTo(nextEndTime) > 0)	nextEndTime = nextEndTime.plusDays(1);
        
        long hoursTillNextEndTime = Duration.between(now, nextEndTime).toHours();

        // HTTP 헤더 설정
        CacheControl cacheControl = CacheControl.maxAge(hoursTillNextEndTime, TimeUnit.HOURS);

        // If-None-Match 헤더 확인
        if (requestHeaders.getIfNoneMatch().contains(etag)) {
            // ETag가 일치하면 304 Not Modified(변경점 없음) 응답을 보냄
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setCacheControl(cacheControl.getHeaderValue());
            return new ResponseEntity<>(responseHeaders, HttpStatus.NOT_MODIFIED);
        } else {
            // ETag가 일치하지 않으면 데이터와 함께 응답을 보냄
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setCacheControl(cacheControl.getHeaderValue());
            responseHeaders.setETag(etag);
            return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
        }
    }
}
