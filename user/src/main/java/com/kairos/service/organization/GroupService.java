package com.kairos.service.organization;

import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.organization.group.Group;
import com.kairos.persistence.model.organization.group.GroupDTO;
import com.kairos.persistence.repository.organization.GroupGraphRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static com.kairos.commons.utils.ObjectUtils.isNull;
import static com.kairos.constants.UserMessagesConstants.MESSAGE_GROUP_ALREADY_EXISTS_IN_UNIT;
import static com.kairos.constants.UserMessagesConstants.MESSAGE_GROUP_NOT_FOUND;

/**
 * Created By G.P.Ranjan on 19/11/19
 **/
public class GroupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TeamService.class);
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private GroupGraphRepository groupGraphRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;

    public GroupDTO createGroup(Long unitId, GroupDTO groupDTO) {
        Unit unit = unitGraphRepository.getUnitWithGroupsByUnitId(unitId);
        if (groupGraphRepository.existsByName(unitId,-1L, groupDTO.getName())){
            exceptionService.duplicateDataException(MESSAGE_GROUP_ALREADY_EXISTS_IN_UNIT, groupDTO.getName(), unitId);
        }
        Group group = new Group(groupDTO.getName(),groupDTO.getDescription());
        groupGraphRepository.save(group);
        unit.getGroups().add(group);
        unitGraphRepository.save(unit);
        groupDTO.setId(group.getId());
        return groupDTO;
    }

    public GroupDTO updateGroup(Long unitId, Long groupId, GroupDTO groupDTO) {
        if (groupGraphRepository.existsByName(unitId,groupId, groupDTO.getName())){
            exceptionService.duplicateDataException(MESSAGE_GROUP_ALREADY_EXISTS_IN_UNIT, groupDTO.getName(), unitId);
        }
        Group group = groupGraphRepository.findGroupByIdAndDeletedFalse(groupId);
        if(isNull(group)){
            exceptionService.dataNotFoundByIdException(MESSAGE_GROUP_NOT_FOUND,groupDTO.getName());
        }
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        group.setExcludeStaffs(groupDTO.getExcludeStaffs());
        groupGraphRepository.save(group);
        groupDTO.setId(group.getId());
        return groupDTO;
    }

    public GroupDTO getGroupDetails(Long groupId) {
        return ObjectMapperUtils.copyPropertiesByMapper(groupGraphRepository.findGroupByIdAndDeletedFalse(groupId), GroupDTO.class);
    }

    public List<GroupDTO> getAllGroupsOfUnit(Long unitId) {
        Unit unit = unitGraphRepository.getUnitWithGroupsByUnitId(unitId);
        return ObjectMapperUtils.copyPropertiesOfListByMapper(unit.getGroups(), GroupDTO.class);
    }

    public Boolean deleteGroup(Long groupId) {
        Group group = groupGraphRepository.findGroupByIdAndDeletedFalse(groupId);
        if(isNull(group)){
            exceptionService.dataNotFoundByIdException(MESSAGE_GROUP_NOT_FOUND,groupId);
        }
        group.setDeleted(false);
        groupGraphRepository.save(group);
        return true;
    }
}
