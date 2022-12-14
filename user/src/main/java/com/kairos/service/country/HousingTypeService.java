package com.kairos.service.country;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.default_data.HousingType;
import com.kairos.persistence.model.country.default_data.HousingTypeDTO;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.HousingTypeGraphRepository;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static com.kairos.constants.UserMessagesConstants.MESSAGE_COUNTRY_ID_NOTFOUND;

/**
 * Created by oodles on 9/1/17.
 */
@Service
@Transactional
public class HousingTypeService {

    @Inject
    private HousingTypeGraphRepository housingTypeGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ExceptionService exceptionService;

    public HousingTypeDTO createHousingType(long countryId, HousingTypeDTO housingTypeDTO) {
        Country country = countryGraphRepository.findOne(countryId);
        HousingType housingType = null;
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        } else {
            Boolean housingTypeExistInCountryByName = housingTypeGraphRepository.housingTypeExistInCountryByName(countryId, "(?i)" + housingTypeDTO.getName(), -1L);
            if (housingTypeExistInCountryByName) {
                exceptionService.duplicateDataException("error.HousingType.name.exist");
            }
            housingType = new HousingType(housingTypeDTO.getName(), housingTypeDTO.getDescription());
            housingType.setCountry(country);
            housingTypeGraphRepository.save(housingType);
        }
        housingTypeDTO.setId(housingType.getId());
        return housingTypeDTO;
    }

    public List<HousingTypeDTO> getHousingTypeByCountryId(long countryId) {
        List<HousingType> housingTypes = housingTypeGraphRepository.findHousingTypeByCountry(countryId);
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(housingTypes, HousingTypeDTO.class);
    }

    public HousingTypeDTO updateHousingType(long countryId, HousingTypeDTO housingTypeDTO) {
        Boolean housingTypeExistInCountryByName = housingTypeGraphRepository.housingTypeExistInCountryByName(countryId, "(?i)" + housingTypeDTO.getName(), housingTypeDTO.getId());
        if (housingTypeExistInCountryByName) {
            exceptionService.duplicateDataException("error.HousingType.name.exist");
        }
        HousingType currentHousingType = housingTypeGraphRepository.findOne(housingTypeDTO.getId());
        if (currentHousingType != null) {
            currentHousingType.setName(housingTypeDTO.getName());
            currentHousingType.setDescription(housingTypeDTO.getDescription());
            housingTypeGraphRepository.save(currentHousingType);
        }
        return housingTypeDTO;
    }

    public boolean deleteHousingType(long housingTypeId) {
        HousingType housingType = housingTypeGraphRepository.findOne(housingTypeId);
        if (housingType != null) {
            housingType.setEnabled(false);
            housingTypeGraphRepository.save(housingType);
        } else {
            exceptionService.dataNotFoundByIdException("error.HousingType.notfound");
        }
        return true;
    }
}
