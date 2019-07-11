package com.kairos.service.public_legal_document;

import com.kairos.commons.utils.DateUtils;
import com.kairos.persistence.model.public_legal_document.PublicLegalDocument;
import com.kairos.persistence.repository.public_legal_document.PublicLegalDocumentRepository;
import com.kairos.response.dto.public_legal_document.PublicLegalDocumentDTO;
import com.kairos.service.exception.ExceptionService;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

import static com.kairos.constants.AppConstant.PUBLIC_LEGAL_Document_LOGO_PATH;

/**
 * Created By G.P.Ranjan on 26/6/19
 **/
@Service
public class PublicLegalDocumentService {
    @Inject
    private PublicLegalDocumentRepository publicLegalDocumentRepository;
    @Inject
    private ExceptionService exceptionService;

    public PublicLegalDocumentDTO createPublicLegalDocument(PublicLegalDocumentDTO publicLegalDocumentDTO) {
        PublicLegalDocument publicLegalDocument=new PublicLegalDocument(publicLegalDocumentDTO.getId(),publicLegalDocumentDTO.getName(),publicLegalDocumentDTO.getPublicLegalDocumentLogo(),publicLegalDocumentDTO.getBodyContentInHtml());
        publicLegalDocumentRepository.save(publicLegalDocument);
        publicLegalDocumentDTO.setId(publicLegalDocument.getId());
        return publicLegalDocumentDTO;
    }

    public Map<String,String> uploadPublicLegalDocumentLogo(MultipartFile file){
        File directory = new File(PUBLIC_LEGAL_Document_LOGO_PATH);
        if (!directory.exists()) {
            try {
                directory.mkdir();
            } catch (SecurityException se) {
                return null;
            }
        }
        String fileName = DateUtils.getCurrentDate().getTime() + file.getOriginalFilename();
        final String path = PUBLIC_LEGAL_Document_LOGO_PATH + File.separator + fileName;
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(new File(path))) {
            byte[] buf = new byte[1024];
            int numRead = 0;
            while ((numRead = inputStream.read(buf)) >= 0) {
                fileOutputStream.write(buf, 0, numRead);
            }
        } catch (Exception e) {
            return null;
        }
        Map<String,String> responseResult = new HashMap<>();
        responseResult.put("logoUrl",fileName);
        return responseResult;
    }

    public boolean removePublicLegalDocument(Long publicLegalDocumentId) {
        PublicLegalDocument publicLegalDocument = publicLegalDocumentRepository.findByIdAndDeletedFalse(publicLegalDocumentId);
        if (!Optional.ofNullable(publicLegalDocument).isPresent() || publicLegalDocument.isDeleted()) {
            return false;
        }
        publicLegalDocument.setDeleted(true);
        publicLegalDocumentRepository.save(publicLegalDocument);
        return true;
    }

    public PublicLegalDocumentDTO updatePublicLegalDocument(Long publicLegalDocumentId,PublicLegalDocumentDTO publicLegalDocumentDTO) {
        PublicLegalDocument publicLegalDocument = publicLegalDocumentRepository.findByIdAndDeletedFalse(publicLegalDocumentId);
        if (!Optional.ofNullable(publicLegalDocument).isPresent() || publicLegalDocument.isDeleted()) {
            exceptionService.dataNotFoundByIdException("Data Not Found", publicLegalDocumentId);
        }
        publicLegalDocumentDTO.setId(publicLegalDocumentId);
        if(publicLegalDocumentDTO.getName() != null)publicLegalDocument.setName(publicLegalDocumentDTO.getName());
        if(publicLegalDocumentDTO.getBodyContentInHtml() != null)publicLegalDocument.setBodyContentInHtml(publicLegalDocumentDTO.getBodyContentInHtml());
        publicLegalDocumentRepository.save(publicLegalDocument);
        return publicLegalDocumentDTO;
    }

    public List<PublicLegalDocumentDTO> getAllPublicLegalDocument() {
        List<PublicLegalDocument> publicLegalDocuments = publicLegalDocumentRepository.findAllAndDeletedFalse();
        List<PublicLegalDocumentDTO> publicLegalDocumentDTOS = new ArrayList<>();
        publicLegalDocuments.forEach(publicLegalDocument -> {
            publicLegalDocumentDTOS.add(new PublicLegalDocumentDTO(publicLegalDocument.getId(),publicLegalDocument.getName(),publicLegalDocument.getPublicLegalDocumentLogo(),publicLegalDocument.getBodyContentInHtml()));
        });
        return publicLegalDocumentDTOS;
    }
}