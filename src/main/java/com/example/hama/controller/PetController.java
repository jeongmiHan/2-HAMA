package com.example.hama.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.hama.config.CustomUserDetails;
import com.example.hama.model.Pet;
import com.example.hama.model.user.User;
import com.example.hama.service.PetService;
import com.example.hama.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pets")
public class PetController {
   
   private final UserService userService;
    private final PetService petService;

    public PetController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }

    // 펫 프로필 페이지를 보여주는 GET 요청
    @GetMapping("profile")
    public String showPetProfilePage() {
        return "petProfile"; // resources/templates/petProfile.html 파일을 반환
    }

    // 펫 등록하는 POST 요청
    @PostMapping
    public ResponseEntity<Pet> addPet(
            @RequestParam("name") String name,
            @RequestParam("age") Long age,
            @RequestParam("breed") String breed,
            @RequestParam("birthdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            @RequestParam(value = "photo", required = false) MultipartFile photo) throws Exception {
       
        User currentUser = getAuthenticatedUser(); // 현재 로그인 사용자 확인
           if (currentUser == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 비로그인 상태 처리
           }

        Pet pet = new Pet();
        pet.setPetName(name);
        pet.setPetAge(age);
        pet.setPetBreed(breed);
        pet.setPetBirthdate(birthdate);
        pet.setUser(currentUser); // 사용자 설정

        // 크롭된 이미지가 제공된 경우 저장
        if (photo != null && !photo.isEmpty()) {
            System.out.println("크롭된 이미지 저장 중...");
            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get("c:/upload/", fileName);

            // 디렉토리 생성
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, photo.getBytes()); // 파일 저장

            // Pet 엔티티에 이미지 정보 저장
            pet.setPhoto(photo.getBytes()); // 바이너리 데이터 저장
            pet.setPhotoMimeType(photo.getContentType()); // MIME 타입 저장
        } else {
            System.out.println("크롭된 이미지가 제공되지 않았습니다.");
        }

        Pet savedPet = petService.addPet(pet); // DB에 저장
        return ResponseEntity.ok(savedPet);
    }

    
    @PutMapping("/{petId}")
    public ResponseEntity<Pet> updatePet(
            @PathVariable("petId") Long petId,
            @RequestParam("name") String name,
            @RequestParam("age") Long age,
            @RequestParam("breed") String breed,
            @RequestParam("birthdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthdate,
            @RequestParam(value = "photo", required = false) MultipartFile photo) throws Exception {

        // 1. 기존 펫 객체를 조회
        Pet existingPet = petService.getPetById(petId);
        if (existingPet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 2. 수정된 필드만 업데이트
        existingPet.setPetName(name);
        existingPet.setPetAge(age);
        existingPet.setPetBreed(breed);
        existingPet.setPetBirthdate(birthdate);

        // 3. 새로운 사진이 있으면 업데이트
        if (photo != null && !photo.isEmpty()) {
            existingPet.setPhoto(photo.getBytes());
            existingPet.setPhotoMimeType(photo.getContentType());
        }

        // 4. 변경된 펫 객체를 저장 (기존 객체만 업데이트)
        Pet updatedPet = petService.updatePet(existingPet);

        // 5. 업데이트된 결과 반환
        return ResponseEntity.ok(updatedPet);
    }


    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(@PathVariable("petId") Long petId) {
        boolean isDeleted = petService.deletePetById(petId); // 삭제 로직
        if (isDeleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllPets() {
        User currentUser = getAuthenticatedUser(); // 현재 로그인 사용자 가져오기
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 비로그인 상태
        }

        List<Pet> pets = petService.getPetsByUserId(currentUser.getUserId()); // 사용자 ID 기반으로 필터링
        List<Map<String, Object>> response = pets.stream().map(pet -> {
            Map<String, Object> petData = new HashMap<>();
            petData.put("petId", pet.getPetId());
            petData.put("petName", pet.getPetName());
            petData.put("petBreed", pet.getPetBreed());
            petData.put("petAge", pet.getPetAge());
            petData.put("birthdate", pet.getPetBirthdate().toString());
            petData.put("photoUrl", "/pets/" + pet.getPetId() + "/photo");
            return petData;
        }).toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{petId}/photo")
    public ResponseEntity<byte[]> getPetPhoto(@PathVariable("petId") Long petId) {
        Pet pet = petService.getPetById(petId);

        // 이미지가 없을 경우 기본 이미지 반환
        if (pet == null || pet.getPhoto() == null) {
            try (InputStream defaultImage = getClass().getResourceAsStream("/static/default-image.jpg")) {
                if (defaultImage == null) {
                    throw new IOException("기본 이미지가 존재하지 않습니다.");
                }
                byte[] defaultBytes = defaultImage.readAllBytes();
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // 기본 이미지 타입 설정
                        .body(defaultBytes);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            }
        }

        // 저장된 MIME 타입 적용 (예: image/jpeg, image/png)
        MediaType mediaType = MediaType.parseMediaType(pet.getPhotoMimeType());
        return ResponseEntity.ok()
                .contentType(mediaType) // 저장된 타입을 동적으로 적용
                .body(pet.getPhoto());
    }




    @PostMapping("/upload-cropped")
    @ResponseBody
    public ResponseEntity<?> uploadCroppedPhoto(@RequestParam("photo") MultipartFile photo) {
        try {
            // 파일 유효성 검사
            if (photo == null || photo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일이 비어 있습니다.");
            }

            // 파일 저장
            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get("c:/upload/", fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, photo.getBytes());

            // 업로드된 파일 URL 반환
            String fileUrl = "/upload/" + fileName;

            return ResponseEntity.ok(Map.of(
                "photoUrl", fileUrl // 업로드된 이미지 URL만 반환
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }



    
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return userDetails.getUser();
            } else if (principal instanceof DefaultOAuth2User oAuth2User) {
                String providerUserId = (String) oAuth2User.getAttributes().get("sub");
                return userService.findUserByProviderUserId(providerUserId);
            }
        }
        return null;
    }

    
}


