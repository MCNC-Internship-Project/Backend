package mcnc.survwey.domain.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcnc.survwey.domain.user.dto.AuthDTO;
import mcnc.survwey.domain.user.dto.ChangePasswordDTO;
import mcnc.survwey.domain.user.dto.ProfileDTO;
import mcnc.survwey.domain.user.User;
import mcnc.survwey.domain.user.dto.ProfileModifyDTO;
import mcnc.survwey.domain.user.repository.UserRepository;
import mcnc.survwey.global.exception.custom.CustomException;
import mcnc.survwey.global.exception.custom.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    /**
     * 회원가입 메소드
     * @param authDTO
     */
    public void registerUser(AuthDTO authDTO) {
        if (userRepository.existsById(authDTO.getUserId())) {//해당 아이디 이미 존재
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.USER_ID_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(authDTO.getEmail())) {//해당 이메일 존재
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        //ID, EMAIL 중복이 없을 경우 저장
        userRepository.save(User.builder()
                .userId(authDTO.getUserId())
                .email(authDTO.getEmail())
                .password(passwordEncoder.encode(authDTO.getPassword()))
                .name(authDTO.getName())
                .registerDate(LocalDateTime.now())
                .birth(authDTO.getBirth())
                .gender(authDTO.getGender())
                .build()
        );
    }

    /**
     * id, email 중복 검증
     *
     * @param userId
     * @param email
     * @return
     */
    public Map<String, Boolean> duplicatedUserNameAndEmail(String userId, String email) {
        Map<String, Boolean> map = new HashMap<>();

        map.put("id", userRepository.existsById(userId));
        map.put("email", userRepository.existsByEmail(email));

        return map;
    }


    /**
     * 프로필 수정
     * @param profileModifyDTO
     * @param userId
     */
    public ProfileModifyDTO modifyUserProfile(ProfileModifyDTO profileModifyDTO, String userId) {
        User user = userService.findByUserId(userId);

        if (profileModifyDTO.getName() == null || profileModifyDTO.getName().isEmpty() || profileModifyDTO.getName().isBlank()) {
            profileModifyDTO.setName(user.getName());
        }
        if (profileModifyDTO.getEmail() == null || profileModifyDTO.getEmail().isEmpty() || profileModifyDTO.getEmail().isBlank()) {
            profileModifyDTO.setEmail(user.getEmail());
        }
        //사용자가 특정 항목을 수정하지 않을 시 원래 user 정보를 가져옴
        //아이디, 성별, 생일은 변경하지 않음

        user.setEmail(profileModifyDTO.getEmail());
        user.setName(profileModifyDTO.getName());

        userRepository.save(user);

        return ProfileModifyDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * 사용자 프로필 정보 조회
     * @param userId
     * @return
     */
    public ProfileDTO getProfile(String userId){
        User user = userService.findByUserId(userId);
        return ProfileDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .birth(user.getBirth())
                .gender(user.getGender())
                .build();
    }

    /**
     * 비밀번호 변경
     *
     * @param changePasswordDTO
     */
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        User user = userService.findByUserId(changePasswordDTO.getUserId());
        //사용자 Id 찾은 후
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
        //사용자 password 재설정
        userRepository.save(user);
    }

}
