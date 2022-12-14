package com.kairos.response.dto.public_legal_document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.commons.annotation.EnableStringTrimer;
import com.kairos.constants.GdprMessagesConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * Created By G.P.Ranjan on 26/6/19
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@EnableStringTrimer
public class PublicLegalDocumentDTO {

    private Long id;

    @Valid
    @NotEmpty(message = GdprMessagesConstants.MESSAGE_ENTER_VALID_DATA)
    private String name;

    private String bodyContentInHtml;

    private String publicLegalDocumentLogo;

    public PublicLegalDocumentDTO(Long id,String name,String publicLegalDocumentLogo,String bodyContentInHtml){
        this.id=id;
        this.name=name;
        this.publicLegalDocumentLogo=publicLegalDocumentLogo;
        this.bodyContentInHtml=bodyContentInHtml;
    }


    @Override
    public String toString(){
        return this.name;
    }
}
