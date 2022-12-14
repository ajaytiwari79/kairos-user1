package com.kairos.dto.gdpr.agreement_template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter @Setter @NoArgsConstructor
public class AgreementTemplateDTO {

    protected Long id;

    @NotBlank(message = "error.message.name.notNull.orEmpty")
    @Pattern(message = "error.message.name.special.character.notAllowed", regexp = "^[a-zA-Z0-9\\s]+$")
    protected String name;

    @NotBlank(message = "error.message.description.notNull.orEmpty")
    protected String description;

    @NotNull(message = "error.message.templateType.notNull")
    protected Long templateTypeId;

    protected String dataHandlerHtmlContent;

    private boolean generalAgreementTemplate;

    public AgreementTemplateDTO(Long id,String name,String description,Long templateTypeId,String dataHandlerHtmlContent){
        this.id=id;
        this.name=name;
        this.description=description;
        this.templateTypeId=templateTypeId;
        this.dataHandlerHtmlContent=dataHandlerHtmlContent;
    }

}
