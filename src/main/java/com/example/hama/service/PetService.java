package com.example.hama.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.hama.model.Pet;
import com.example.hama.repository.PetRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PetService {
   private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }
    
 // 특정 사용자에 따른 반려동물 목록 조회
    public List<Pet> getPetsByUserId(String userId) {
        return petRepository.findByUserUserId(userId);
    }

    public Pet addPet(Pet pet) {
        return petRepository.save(pet);
    }


    
   public Pet getPetById(Long petId) {
       return petRepository.findById(petId).orElse(null);
   }

    
   // 펫 정보 수정
   public Pet updatePet(Pet pet) {
       if (pet.getPetId() == null || !petRepository.existsById(pet.getPetId())) {
           throw new IllegalArgumentException("존재하지 않는 펫입니다.");
       }

       // 영속성 컨텍스트에서 이미 관리되는 객체가 되도록 할 수 있습니다.
       Pet existingPet = petRepository.findById(pet.getPetId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펫입니다."));

       // 기존의 값을 업데이트
       existingPet.setPetName(pet.getPetName());
       existingPet.setPetAge(pet.getPetAge());
       existingPet.setPetBreed(pet.getPetBreed());
       existingPet.setPetBirthdate(pet.getPetBirthdate());
       if (pet.getPhoto() != null) {
           existingPet.setPhoto(pet.getPhoto());
       }

       // 업데이트된 객체를 저장 (영속성 컨텍스트에 반영)
       return petRepository.save(existingPet); // 기존 객체를 수정하여 저장
   }


    // 펫 삭제
    public boolean deletePetById(Long petId) {
        if (petRepository.existsById(petId)) {
            petRepository.deleteById(petId);
            return true;
        }
        return false;
    }

   public List<Pet> getAllPets() {
      // TODO Auto-generated method stub
      return petRepository.findAll();
   }
   
   
   
   
   }

   

