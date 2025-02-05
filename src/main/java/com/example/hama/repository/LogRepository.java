package com.example.hama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.log.Log;
import com.example.hama.model.user.User;


public interface LogRepository extends JpaRepository<Log, Long> {
	List<Log> findByUser(User user);
	//일지 등록
	//save(log);
	
	//일지 검색
	//findById(id);
	
	//일지 수정
	//save(log);
	
	//일지 삭제
	//delete(id);
	
	//일지 전체 목록
	//findAll();
}
