package com.kairos.persistence.model.master_data.default_proc_activity_setting;

import com.kairos.enums.gdpr.SuggestedDataStatus;
import com.kairos.persistence.model.common.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ResponsibilityType extends BaseEntity {


    @NotBlank(message = "error.message.name.cannot.be.null.or.empty")
    @Pattern(message = "error.message.name.special.character.notAllowed",regexp = "^[a-zA-Z0-9\\s]+$")
    private String name;
    private Long countryId;
    private SuggestedDataStatus suggestedDataStatus;
    private LocalDate suggestedDate;
    private Long organizationId;
    public String getName() {
        return name.trim();
    }

    public ResponsibilityType(Long countryId, @NotBlank(message = "error.message.name.cannot.be.null.or.empty") @Pattern(message = "error.message.name.special.character.notAllowed", regexp = "^[a-zA-Z\\s]+$") String name) {
        this.name = name;
        this.countryId = countryId;
    }

    public ResponsibilityType(String name) {
        this.name = name;
    }

}
