package com.kairos.service.staff;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.user.staff.staff.StaffResultDTO;
import com.kairos.persistence.model.staff.StaffInformationQueryResult;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.service.organization_meta_data.SickConfigurationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.kairos.constants.AppConstants.YES;

/**
 * CreatedBy vipulpandey on 30/8/18
 **/
@Service
@Transactional
public class UserSickService {
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private SickConfigurationService sickConfigurationService;

    public List<StaffResultDTO> getStaffAndUnitSickSettings(Long userId, String sickSettingsRequired) {
        List<StaffInformationQueryResult> staffUnitWrappers = staffGraphRepository.getAllStaffsAndUnitDetailsByUserId(userId);
        List<StaffResultDTO> staffResults = new ArrayList<>();
        if (Optional.ofNullable(staffUnitWrappers).isPresent()) {
            staffResults = ObjectMapperUtils.copyCollectionPropertiesByMapper(staffUnitWrappers, StaffResultDTO.class);
            // if the size id one then also calculating allowed time type for sick
            if (staffUnitWrappers.size() == 1 && YES.equals(sickSettingsRequired)) {
                Set<BigInteger> allowedTimeTypes = sickConfigurationService.getSickSettingsOfUnit(staffUnitWrappers.get(0).getUnitId());
                staffResults.get(0).setAllowedTimeTypesForSick(allowedTimeTypes);
            }
        }
        return staffResults;

    }

}
