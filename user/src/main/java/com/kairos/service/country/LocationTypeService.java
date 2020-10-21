package com.kairos.service.country;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.TranslationUtil;
import com.kairos.dto.TranslationInfo;
import com.kairos.persistence.model.country.Country;
import com.kairos.persistence.model.country.default_data.LocationType;
import com.kairos.persistence.model.country.default_data.LocationTypeDTO;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.LocationTypeGraphRepository;
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
public class LocationTypeService {

    @Inject
    private LocationTypeGraphRepository locationTypeGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private ExceptionService exceptionService;

    public LocationTypeDTO createLocationType(long countryId, LocationTypeDTO locationTypeDTO) {
        Country country = countryGraphRepository.findOne(countryId);
        LocationType locationType = null;
        if (country == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_COUNTRY_ID_NOTFOUND, countryId);
        } else {
            Boolean locationTypeExistInCountryByName = locationTypeGraphRepository.locationTypeExistInCountryByName(countryId, "(?i)" + locationTypeDTO.getName(), -1L);
            if (locationTypeExistInCountryByName) {
                exceptionService.duplicateDataException("error.LocationType.name.exist");
            }
            locationType = new LocationType(locationTypeDTO.getName(), locationTypeDTO.getDescription());
            locationType.setCountry(country);
            locationTypeGraphRepository.save(locationType);
        }
        locationTypeDTO.setId(locationType.getId());
        return locationTypeDTO;
    }

    public List<LocationTypeDTO> getLocationTypeByCountryId(long countryId) {
        List<LocationType> locationTypes = locationTypeGraphRepository.findLocationTypeByCountry(countryId);
        List<LocationTypeDTO> locationTypeDTOS = ObjectMapperUtils.copyCollectionPropertiesByMapper(locationTypes,LocationTypeDTO.class);
        locationTypeDTOS.forEach(locationTypeDTO -> {
            locationTypeDTO.setCountryId(countryId);
            locationTypeDTO.setTranslations(TranslationUtil.getTranslatedData(locationTypeDTO.getTranslatedNames(),locationTypeDTO.getTranslatedDescriptions()));
        });
        return locationTypeDTOS;
    }

    public LocationTypeDTO updateLocationType(long countryId, LocationTypeDTO locationTypeDTO) {
        Boolean locationTypeExistInCountryByName = locationTypeGraphRepository.locationTypeExistInCountryByName(countryId, "(?i)" + locationTypeDTO.getName(), locationTypeDTO.getId());
        if (locationTypeExistInCountryByName) {
            exceptionService.duplicateDataException("error.LocationType.name.exist");
        }
        LocationType currentLocationType = locationTypeGraphRepository.findOne(locationTypeDTO.getId());
        if (currentLocationType != null) {
            currentLocationType.setName(locationTypeDTO.getName());
            currentLocationType.setDescription(locationTypeDTO.getDescription());
            locationTypeGraphRepository.save(currentLocationType);
        }
        return locationTypeDTO;
    }

    public boolean deleteLocationType(long locationTypeId) {
        LocationType locationType = locationTypeGraphRepository.findOne(locationTypeId);
        if (locationType != null) {
            locationType.setEnabled(false);
            locationTypeGraphRepository.save(locationType);
            return true;
        } else {
            exceptionService.dataNotFoundByIdException("error.LocationType.notfound");
        }
        return false;
    }

    public Map<String, TranslationInfo> updateTranslation(Long locationTypeId, Map<String,TranslationInfo> translations) {
        Map<String,String> translatedNames = new HashMap<>();
        Map<String,String> translatedDescriptions = new HashMap<>();
        TranslationUtil.updateTranslationData(translations,translatedNames,translatedDescriptions);
        LocationType locationType =locationTypeGraphRepository.findOne(locationTypeId);
        locationType.setTranslatedNames(translatedNames);
        locationType.setTranslatedDescriptions(translatedDescriptions);
        locationTypeGraphRepository.save(locationType);
        return locationType.getTranslatedData();
    }
}
