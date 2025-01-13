package com.example.hama.util;

import java.time.Duration;
import java.time.LocalDateTime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SNSTime {
    public static String getTimeAgo(LocalDateTime createdTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdTime, now);

        if (duration.toMinutes() < 5) {
            return "방금 전";
        } else if (duration.toMinutes() < 60) {
        	return duration.toMinutes() + "분 전";
        } else if (duration.toHours() < 24) {
            return duration.toHours() + "시간 전";
        } else if (duration.toDays() <= 5) {
            return duration.toDays() + "일 전"; // 1~5일 전 표시
        } else {
            // 5일 이상일 경우 날짜 표시
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return createdTime.format(formatter);
        }
    }
}

