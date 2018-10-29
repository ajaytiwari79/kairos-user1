package com.kairos.service.flexible_time;
/*
 *Created By Pavan on 20/10/18
 *
 */

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.activity.flexible_time.FlexibleTimeSettingsDTO;
import com.kairos.persistence.model.flexible_time.FlexibleTimeSettings;
import com.kairos.persistence.repository.flexible_time.FlexibleTimeSettingsRepository;
import com.kairos.service.MongoBaseService;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class FlexibleTimeSettingsService extends MongoBaseService {


    @Inject
    private FlexibleTimeSettingsRepository flexibleTimeSettingsRepository;
    @Inject
    private ExceptionService exceptionService;

    public FlexibleTimeSettingsDTO saveFlexibleTimeSettings(Long countryId,FlexibleTimeSettingsDTO flexibleTimeSettingsDTO){
        FlexibleTimeSettings flexibleTimeSettings=flexibleTimeSettingsRepository.getFlexibleTimeSettingsByIdAndDeletedFalse(flexibleTimeSettingsDTO.getId());
//        if(flexibleTimeSettings==null){
//            exceptionService.dataNotFoundException("message.dataNotFound","Flexi Time Settings",flexibleTimeSettingsDTO.getId());
//        }
        flexibleTimeSettings=new FlexibleTimeSettings(flexibleTimeSettingsDTO.getId(),flexibleTimeSettingsDTO.getFlexibleTimeForCheckIn(),flexibleTimeSettingsDTO.getFlexibleTimeForCheckOut(),flexibleTimeSettingsDTO.getTimeLimit());
        flexibleTimeSettings.setCountryId(countryId);
        save(flexibleTimeSettings);
        flexibleTimeSettingsDTO.setId(flexibleTimeSettings.getId());
        return flexibleTimeSettingsDTO;
    }

    public FlexibleTimeSettingsDTO getFlexibleTimeSettings(Long countryId){
        return flexibleTimeSettingsRepository.getFlexibleTimeSettingsByCountryIdAndDeletedFalse(countryId);
    }
}
