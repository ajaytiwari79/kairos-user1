package com.kairos.dto.gdpr.questionnaire_template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.enums.gdpr.QuestionnaireTemplateStatus;
import com.kairos.enums.gdpr.QuestionnaireTemplateType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class QuestionnaireTemplateDTO {

    private Long id;
    @NotBlank(message = "error.message.name.notNull.orEmpty")
    @Pattern(message = "error.message.name.special.character.notAllowed", regexp = "^[a-zA-Z0-9\\s]+$")
    private String name;
    @NotBlank(message = "error.message.description.notNull.orEmpty")
    private String description;
    @NotNull(message = "error.message.template.type.notNull")
    private QuestionnaireTemplateType templateType;
    private Long assetType;
    private Long subAssetType;
    private boolean defaultAssetTemplate;
    private QuestionnaireTemplateStatus templateStatus;
    private QuestionnaireTemplateType riskAssociatedEntity;
    @Valid
    private List<QuestionnaireSectionDTO> sections=new ArrayList<>();

    public String getName() {
        return name.trim();
    }
}
