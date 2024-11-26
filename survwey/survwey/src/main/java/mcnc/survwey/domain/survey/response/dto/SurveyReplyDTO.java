package mcnc.survwey.domain.survey.response.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import mcnc.survwey.domain.respond.dto.ResponseDTO;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyReplyDTO {
    @NotNull(message = "설문 아이디는 필수입니다.")
    private Long surveyId;
    private List<ResponseDTO> responseList;
}

