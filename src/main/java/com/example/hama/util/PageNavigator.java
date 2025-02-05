package com.example.hama.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class PageNavigator {
    private int countPerPage;      // 페이지당 글 목록 수
    private int pagePerGroup;      // 그룹당 페이지 수
    private int currentPage;       // 현재 페이지
    private int totalRecordsCount; // 전체 글 수
    private int totalPageCount;    // 전체 페이지 수
    private int currentGroup;      // 현재 그룹
    private int startPageGroup;    // 현재 그룹의 첫 페이지
    private int endPageGroup;      // 현재 그룹의 마지막 페이지
    private int startRecord;       // 현재 페이지 첫 글의 위치
    private int totalPageGroup;    // 전체 그룹 수

    public PageNavigator(int countPerPage, int pagePerGroup, int currentPage, int totalRecordsCount, int totalPageCount) {
        this.countPerPage = countPerPage;
        this.pagePerGroup = pagePerGroup;
        this.totalRecordsCount = totalRecordsCount;
        this.totalPageCount = Math.max(1, totalPageCount); // 최소 1페이지 보장

        // 게시글이 없을 경우 처리
        if (totalRecordsCount == 0) {
            this.currentPage = 1;
            this.currentGroup = 0;
            this.startPageGroup = 1;
            this.endPageGroup = 1;
            this.startRecord = 0;
            this.totalPageGroup = 0;
            return;
        }

        // 현재 페이지가 1보다 작으면 1로 설정
        if (currentPage < 1) {
            currentPage = 1;
        }

        // 현재 페이지가 전체 페이지 수를 초과하면 마지막 페이지로 설정
        if (currentPage > totalPageCount) {
            currentPage = totalPageCount;
            log.info("현재 페이지: {}", currentPage);
        }

        this.currentPage = currentPage;

        // 현재 그룹 계산
        currentGroup = (currentPage - 1) / pagePerGroup;

        // 그룹의 첫 페이지와 마지막 페이지 계산
        startPageGroup = Math.max(1, currentGroup * pagePerGroup + 1);
        endPageGroup = Math.min(startPageGroup + pagePerGroup - 1, totalPageCount);

        // 현재 페이지의 첫 글 위치 계산
        startRecord = (currentPage - 1) * countPerPage;

        // 전체 그룹 수 계산
        totalPageGroup = (int) Math.ceil((double) totalPageCount / pagePerGroup);
    }
}
