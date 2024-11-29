package mcnc.survwey.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcnc.survwey.domain.enums.Gender;
import mcnc.survwey.domain.user.User;
import mcnc.survwey.domain.user.dto.AgeCountDTO;
import mcnc.survwey.domain.user.dto.GenderCountDTO;
import mcnc.survwey.domain.user.repository.UserRepository;
import mcnc.survwey.global.exception.custom.CustomException;
import mcnc.survwey.global.exception.custom.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.USER_NOT_FOUND_BY_ID));
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.USER_NOT_FOUND_BY_EMAIL));
    }

    /**
     * 설문 아이디를 통한 성별 분포 조회
     * - 요청 설문 아이디의 DB 조회
     * - 해당 설문에 응답한 성별 분포 수 조회 후 응답
     * @param surveyId
     * @return
     */
    public List<GenderCountDTO> getGenderCountListBySurveyId(Long surveyId) {
        List<Object[]> recordList = userRepository.findGenderCountBySurveyId(surveyId);

        //응답 Map 생성
        Map<Gender, GenderCountDTO> genderCountDTOMap = new LinkedHashMap<>();

        //Key 초기화
        genderCountDTOMap.put(Gender.M, new GenderCountDTO(Gender.M.getValue(), 0));
        genderCountDTOMap.put(Gender.F, new GenderCountDTO(Gender.F.getValue(), 0));

        //응답 Map에 값 할당
        for (Object[] record : recordList) {
            Gender gender = (Gender) record[0];
            Long count = (Long) record[1];
            genderCountDTOMap.get(gender).setCount(count);
        }
        return genderCountDTOMap.values().stream().toList();
    }

    /**
     * 설문 아이디를 통한 나이대 분포 조회
     * - 요청 설문 아이디의 DB 조회
     * - 해당 설문에 응답한 나이대 분포 수 처리 후 응답
     * @param surveyId
     * @return
     */
    public List<AgeCountDTO> getAgeGroupCountBySurveyId(Long surveyId) {
        List<LocalDate> birthList = userRepository.findBirthBySurveyId(surveyId);
        //DB 조회 결과를 나이대(10대, 20대 ...)를 key로 하는 Map 생성
        Map<Integer, Integer> ageMap = groupAgesByDecade(birthList);
        return mapAgeGroupsToDTO(ageMap);
    }

    /**
     * 나이대 분포 Map 생성
     * - DB 조회 결과를 나이대 별로 분류
     * - 10년 단위로 분류
     * - 70대 이하와 80대 이상으로 분류
     * @param birthList
     * @return
     */
    private Map<Integer, Integer> groupAgesByDecade(List<LocalDate> birthList) {
        Map<Integer, Integer> ageMap = new LinkedHashMap<>();
        for (LocalDate birthDate : birthList) {
            //10년 단위로 계산
            int decade = calculateAge(birthDate) / 10;
            if (decade < 8) {
                //70대 이하
                ageMap.put(decade, ageMap.getOrDefault(decade, 0) + 1);
            } else {
                //80대 이상
                ageMap.put(8, ageMap.getOrDefault(8, 0) + 1);
            }
        }
        return ageMap;
    }


    /**
     * 분류한 나이대 분포를 DTO 객체로 매핑
     * - 10대미만, 10대, 20대, ... , 70대, 80대 이상으로 분류
     * - 객체 생성 후 리스트로 묶어 반환
     * @param ageMap
     * @return
     */
    private List<AgeCountDTO> mapAgeGroupsToDTO(Map<Integer, Integer> ageMap) {
        List<AgeCountDTO> ageCountDTOList = new ArrayList<>();
        ageCountDTOList.add(new AgeCountDTO("10대 미만", ageMap.getOrDefault(0, 0)));
        for (int decade = 1; decade <= 7; decade++) {
            String ageGroup = decade * 10 + "대";
            ageCountDTOList.add(new AgeCountDTO(ageGroup, ageMap.getOrDefault(decade, 0)));
        }
        ageCountDTOList.add(new AgeCountDTO("80세 이상", ageMap.getOrDefault(8, 0)));
        return ageCountDTOList;
    }


    /**
     * 나이 계산 메서드
     * - DB에는 CURRENT_TIMESTAMP로 저장되어 있어, 현재 시간을 기준으로 나이 계산
     * - 생년월일이 없으면 0세로 처리
     * @param birthDate
     * @return
     */
    private int calculateAge(LocalDate birthDate) {
        LocalDate currentDate = LocalDate.now();
        if (birthDate != null) {
            return Period.between(birthDate, currentDate).getYears();
        } else {
            return 0;  // 생년월일이 없으면 0세로 처리
        }
    }
}
