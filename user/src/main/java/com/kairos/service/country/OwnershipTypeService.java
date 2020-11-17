package com.kairos.service.country;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.TranslationUtil;
import com.kairos.dto.TranslationInfo;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.default_data.ClinicType;
import com.kairos.persistence.model.country.default_data.OwnershipType;
import com.kairos.persistence.model.country.default_data.OwnershipTypeDTO;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.OwnershipTypeGraphRepository;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kairos.constants.UserMessagesConstants.MESSAGE_COUNTRY_ID_NOTFOUND;

/**
 * Created by oodles on 9/1/17.
 */
@Service
@Transactional
public class OwnershipTypeService {

    @Inject
    private OwnershipTypeGraphRepository ownershipTypeGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ExceptionService exceptionService;

    public OwnershipTypeDTO createOwnershipType(long countryId, OwnershipTypeDTO ownershipTypeDTO){
        Country country = countryGraphRepository.findOne(countryId);
        OwnershipType ownershipType = null;
        if ( country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        } else {
            Boolean ownershipTypeExistInCountryByName = ownershipTypeGraphRepository.ownershipTypeExistInCountryByName(countryId, "(?i)" + ownershipTypeDTO.getName(), -1L);
            if (ownershipTypeExistInCountryByName) {
                exceptionService.duplicateDataException("error.OwnershipType.name.exist");
            }
            ownershipType = new OwnershipType(ownershipTypeDTO.getName(), ownershipTypeDTO.getDescription());
            ownershipType.setCountry(country);
            ownershipTypeGraphRepository.save(ownershipType);
        }
        ownershipTypeDTO.setId(ownershipType.getId());
        return ownershipTypeDTO;
    }

    public List<OwnershipTypeDTO> getOwnershipTypeByCountryId(long countryId){
        List<OwnershipType> ownershipTypes = ownershipTypeGraphRepository.findOwnershipTypeByCountry(countryId);
        List<OwnershipTypeDTO> ownershipTypeDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(ownershipTypes,OwnershipTypeDTO.class);
        for(OwnershipTypeDTO ownershipTypeDTO :ownershipTypeDTOS){
            ownershipTypeDTO.setCountryId(countryId);
            ownershipTypeDTO.setTranslations(TranslationUtil.getTranslatedData(ownershipTypeDTO.getTranslatedNames(),ownershipTypeDTO.getTranslatedDescriptions()));
        }
        return ownershipTypeDTOS;
    }

    public OwnershipTypeDTO updateOwnershipType(long countryId, OwnershipTypeDTO ownershipTypeDTO){
        Boolean ownershipTypeExistInCountryByName = ownershipTypeGraphRepository.ownershipTypeExistInCountryByName(countryId, "(?i)" + ownershipTypeDTO.getName(), ownershipTypeDTO.getId());
        if (ownershipTypeExistInCountryByName) {
            exceptionService.duplicateDataException("error.OwnershipType.name.exist");
        }
        OwnershipType currentOwnershipType = ownershipTypeGraphRepository.findOne(ownershipTypeDTO.getId());
        if (currentOwnershipType!=null){
            currentOwnershipType.setName(ownershipTypeDTO.getName());
            currentOwnershipType.setDescription(ownershipTypeDTO.getDescription());
            ownershipTypeGraphRepository.save(currentOwnershipType);
        }
        return ownershipTypeDTO;
    }

    public boolean deleteOwnershipType(long ownershipTypeId){
        OwnershipType ownershipType = ownershipTypeGraphRepository.findOne(ownershipTypeId);
        if (ownershipType!=null){
            ownershipType.setEnabled(false);
            ownershipTypeGraphRepository.save(ownershipType);
        } else {
            exceptionService.dataNotFoundByIdException("error.OwnershipType.notfound");
        }
        return true;
    }

    public Map<String, TranslationInfo> updateTranslation(Long ownershipTypeId, Map<String,TranslationInfo> translations) {
        Map<String,String> translatedNames = new HashMap<>();
        Map<String,String> translatedDescriptios = new HashMap<>();
        TranslationUtil.updateTranslationData(translations,translatedNames,translatedDescriptios);
        OwnershipType ownershipType =ownershipTypeGraphRepository.findOne(ownershipTypeId);
        ownershipType.setTranslatedNames(translatedNames);
        ownershipType.setTranslatedDescriptions(translatedDescriptios);
        ownershipTypeGraphRepository.save(ownershipType);
        return ownershipType.getTranslatedData();
    }
}
