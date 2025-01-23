package com.example.hama.model;

import java.time.LocalDate;

import com.example.hama.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Pet {
   
   @Id
   @Column(name="pet_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petId; // 반려동물 ID (Primary Key)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // 사용자 ID (Foreign Key로 매핑)

    @Column(name = "pet_name", nullable = false, length = 100)
    private String petName; // 반려동물 이름

    @Column(name = "pet_age", nullable = false)
    private Long petAge; // 반려동물 나이

    @Column(name = "pet_breed", nullable = false, length = 100)
    private String petBreed; // 반려동물 품종

    @Column(name = "pet_birthdate", nullable = false)
    private LocalDate petBirthdate; // 반려동물 생일

    @Lob
    @Column(name = "photo", columnDefinition = "BLOB")
    private byte[] photo; // 반려동물 사진 (바이너리 데이터)
    
    @Column(name = "photo_mime_type")
    private String photoMimeType; // 이미지 타입 (예: image/jpeg, image/png)
    

}
