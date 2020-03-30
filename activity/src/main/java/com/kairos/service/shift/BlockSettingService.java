package com.kairos.service.shift;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.dto.activity.shift.BlockSettingDTO;
import com.kairos.persistence.model.shift.BlockSetting;
import com.kairos.persistence.repository.shift.BlockSettingMongoRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.activity.ActivityService;
import com.kairos.service.exception.ExceptionService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.asDate;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.ActivityMessagesConstants.ERROR_BLOCK_SETTING_NOT_FOUND;


/**
 * Created By G.P.Ranjan on 3/12/19
 **/
@Service
public class BlockSettingService {
    @Inject
    private BlockSettingMongoRepository blockSettingMongoRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private ActivityService activityService;
    @Inject
    private ExceptionService exceptionService;

    public BlockSettingDTO saveBlockSetting(Long unitId, BlockSettingDTO blockSettingDTO) {
        Map<Long, Set<BigInteger>> blockDetails = blockSettingDTO.getBlockDetails();
        if(isMapEmpty(blockDetails) || blockDetails.size() == 1 && blockDetails.containsKey(null)){
            Set<BigInteger> activitySet;
            if(isMapNotEmpty(blockDetails)){
                activitySet = blockDetails.get(null);
            }else{
                activitySet = activityService.getAbsenceActivityIds(unitId, asDate(blockSettingDTO.getDate()));
            }
            Set<Long> staffIds = userIntegrationService.getStaffByUnitId(unitId).stream().map(staffDTO -> staffDTO.getId()).collect(Collectors.toSet());
            blockDetails = new HashMap<>();
            for (Long staffId : staffIds) {
                blockDetails.put(staffId,activitySet);
            }
        }
        BlockSetting blockSetting = blockSettingMongoRepository.findBlockSettingByUnitIdAndDate(unitId, blockSettingDTO.getDate());
        if(isNull(blockSetting)){
            blockSetting = new BlockSetting(unitId, blockSettingDTO.getDate(), blockDetails);
        }else{
            blockSetting.setBlockDetails(blockDetails);
        }
        blockSettingMongoRepository.save(blockSetting);
        blockSettingDTO.setId(blockSetting.getId());
        blockSettingDTO.setBlockDetails(blockDetails);
        return blockSettingDTO;
    }

    public List<BlockSettingDTO> getBlockSettings(Long unitId, LocalDate startDate, LocalDate endDate) {
        return ObjectMapperUtils.copyPropertiesOrCloneCollectionByMapper(blockSettingMongoRepository.findAllBlockSettingByUnitIdAndDateRange(unitId, startDate, endDate), BlockSettingDTO.class);
    }

    public BlockSettingDTO getBlockSetting(Long unitId, LocalDate date) {
        BlockSetting blockSetting = blockSettingMongoRepository.findBlockSettingByUnitIdAndDate(unitId, date);
        return isNotNull(blockSetting) ? ObjectMapperUtils.copyPropertiesOrCloneByMapper(blockSetting, BlockSettingDTO.class) : null;
    }

    public boolean deleteBlockSetting(Long unitId, LocalDate date) {
        BlockSetting blockSetting = blockSettingMongoRepository.findBlockSettingByUnitIdAndDate(unitId, date);
        if(isNull(blockSetting)){
            exceptionService.dataNotFoundException(ERROR_BLOCK_SETTING_NOT_FOUND);
        }
        blockSetting.setDeleted(true);
        blockSettingMongoRepository.save(blockSetting);
        return true;
    }
}
