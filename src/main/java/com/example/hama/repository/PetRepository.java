package com.example.hama.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hama.model.Pet;


public interface PetRepository extends JpaRepository<Pet, Long> {
    // 사용자 ID로 반려동물 조회
    List<Pet> findByUserUserId(String userId);
    
    List<Pet> findByUser_UserId(String userId);
    
}
