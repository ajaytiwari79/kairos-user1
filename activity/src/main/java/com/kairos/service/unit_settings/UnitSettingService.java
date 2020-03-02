package com.kairos.service.unit_settings;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.constants.AppConstants;
import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.dto.activity.time_type.TimeTypeDTO;
import com.kairos.dto.activity.unit_settings.*;
import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.model.unit_settings.FlexibleTimeSettings;
import com.kairos.persistence.model.unit_settings.UnitAgeSetting;
import com.kairos.persistence.model.unit_settings.UnitSetting;
import com.kairos.persistence.repository.unit_settings.UnitAgeSettingMongoRepository;
import com.kairos.persistence.repository.unit_settings.UnitSettingRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.MongoBaseService;
import com.kairos.service.activity.TimeTypeService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.phase.PhaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_UNIT_AGESETTING_NOTFOUND;
import static com.kairos.constants.ActivityMessagesConstants.MESSAGE_UNIT_SETTING_NOTFOUND;
import static com.kairos.service.shift.ShiftValidatorService.convertMessage;

@Service
@Transactional
public class UnitSettingService{

    @Inject
    private ExceptionService exceptionService;

    @Inject
    private UnitAgeSettingMongoRepository unitAgeSettingMongoRepository;
    @Inject
    private UnitSettingRepository unitSettingRepository;
    @Inject
    private PhaseService phaseService;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private TimeTypeService timeTypeService;

    public UnitAgeSetting createDefaultNightWorkerSettings(Long unitId) {
        UnitAgeSetting unitAgeSetting = new UnitAgeSetting(AppConstants.YOUNGER_AGE, AppConstants.OLDER_AGE, unitId);
        unitAgeSettingMongoRepository.save(unitAgeSetting);
        return unitAgeSetting;
    }

    public UnitAgeSettingDTO getUnitAgeSettings(Long unitId) {
        UnitAgeSetting unitAgeSetting = unitAgeSettingMongoRepository.findByUnit(unitId);
        if (!Optional.ofNullable(unitAgeSetting).isPresent()) {
            unitAgeSetting = createDefaultNightWorkerSettings(unitId);
        }
        return ObjectMapperUtils.copyPropertiesByMapper(unitAgeSetting, UnitAgeSettingDTO.class);
    }

    public UnitAgeSettingDTO updateUnitAgeSettings(Long unitId, UnitAgeSettingDTO unitSettingsDTO) {
        UnitAgeSetting unitAgeSetting = unitAgeSettingMongoRepository.findByUnit(unitId);
        if (!Optional.ofNullable(unitAgeSetting).isPresent()) {
            exceptionService.dataNotFoundByIdException(MESSAGE_UNIT_AGESETTING_NOTFOUND, unitId);
        }
        unitAgeSetting.setYounger(unitSettingsDTO.getYounger());
        unitAgeSetting.setOlder(unitSettingsDTO.getOlder());

        unitAgeSettingMongoRepository.save(unitAgeSetting);
        return unitSettingsDTO;
    }

    public List<UnitSettingDTO> getOpenShiftPhaseSettings(Long unitId) {
        List<UnitSettingDTO> openShiftPhaseSettings = unitSettingRepository.getOpenShiftPhaseSettings(unitId);
        openShiftPhaseSettings.forEach(openSettingDTO ->{
            openSettingDTO.getOpenShiftPhaseSetting().getOpenShiftPhases().sort(Comparator.comparingInt(OpenShiftPhase::getSequence));
        });
        return openShiftPhaseSettings;
    }

    public UnitSettingDTO updateOpenShiftPhaseSettings(Long unitId, BigInteger unitSettingsId, UnitSettingDTO unitSettingsDTO) {
        UnitSetting unitSetting = unitSettingRepository.findById(unitSettingsId).orElseThrow(()->new DataNotFoundByIdException(convertMessage(MESSAGE_UNIT_SETTING_NOTFOUND, unitSettingsId)));
        unitSetting.setUnitId(unitId);
        unitSetting.setOpenShiftPhaseSetting(unitSettingsDTO.getOpenShiftPhaseSetting());
        unitSettingRepository.save(unitSetting);
        return unitSettingsDTO;
    }

    public boolean createDefaultOpenShiftPhaseSettings(Long unitId, List<Phase> phases) {
        if (!Optional.ofNullable(phases).isPresent()) {
            phases = ObjectMapperUtils.copyPropertiesOfCollectionByMapper(phaseService.getPhasesByUnit(unitId), Phase.class);
        }
        List<UnitSettingDTO> openShiftPhaseSettings = unitSettingRepository.getOpenShiftPhaseSettings(unitId);
        if (ObjectUtils.isCollectionEmpty(openShiftPhaseSettings)) {
            if (Optional.ofNullable(phases).isPresent()) {
                List<OpenShiftPhase> openShiftPhases = new ArrayList<>();
                phases.forEach(phase -> {
                    OpenShiftPhase openShiftPhase = new OpenShiftPhase(phase.getId(), phase.getName(), false,phase.getSequence());
                    openShiftPhases.add(openShiftPhase);
                });
                OpenShiftPhaseSetting openShiftPhaseSetting = new OpenShiftPhaseSetting(4, openShiftPhases);
                UnitSetting unitSetting = new UnitSetting(openShiftPhaseSetting, unitId);
                unitSettingRepository.save(unitSetting);
                return true;
            }
        }


        return false;

    }

    public FlexibleTimeSettingDTO getFlexibleTime(Long unitId) {
        UnitSettingDTO unitSettingDTO = unitSettingRepository.getFlexibleTimingByUnit(unitId);
        FlexibleTimeSettingDTO flexibleTimeSettingDTO = new FlexibleTimeSettingDTO();
        if (unitSettingDTO != null) {
            flexibleTimeSettingDTO = unitSettingDTO.getFlexibleTimeSettings();
        }
        return flexibleTimeSettingDTO;
    }

    public FlexibleTimeSettingDTO updateFlexibleTime(Long unitId, FlexibleTimeSettingDTO flexibleTimeSettingDTO) {
        UnitSetting unitSetting = unitSettingRepository.findByUnitIdAndDeletedFalse(unitId);
        if (unitSetting == null) {
            exceptionService.dataNotFoundException(MESSAGE_UNIT_SETTING_NOTFOUND);
        }
        FlexibleTimeSettings flexibleTimeSettings = ObjectMapperUtils.copyPropertiesByMapper(flexibleTimeSettingDTO, FlexibleTimeSettings.class);
        unitSetting.setUnitId(unitId);
        unitSetting.setFlexibleTimeSettings(flexibleTimeSettings);
        unitSettingRepository.save(unitSetting);
        return flexibleTimeSettingDTO;
    }

    public List<TimeTypeDTO> getAllTimeTypes(Long unitId) {
        Long countryId=userIntegrationService.getCountryId(unitId);
        return timeTypeService.getAllTimeType(null,countryId);
    }
}
