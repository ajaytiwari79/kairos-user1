package com.kairos.service.organization;

import com.kairos.commons.custom_exception.DataNotFoundByIdException;
import com.kairos.commons.utils.CommonsExceptionUtil;
import com.kairos.commons.utils.DateUtils;
import com.kairos.config.env.EnvConfig;
import com.kairos.dto.activity.activity.ActivityCategoryListDTO;
import com.kairos.dto.activity.activity.ActivityDTO;
import com.kairos.dto.user.country.agreement.cta.cta_response.ActivityCategoryDTO;
import com.kairos.enums.team.LeaderType;
import com.kairos.persistence.model.client.ContactAddress;
import com.kairos.persistence.model.organization.OrganizationContactAddress;
import com.kairos.persistence.model.organization.StaffTeamRelationShipQueryResult;
import com.kairos.persistence.model.organization.StaffTeamRelationship;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.organization.team.Team;
import com.kairos.persistence.model.organization.team.TeamDTO;
import com.kairos.persistence.model.staff.StaffTeamDTO;
import com.kairos.persistence.model.staff.personal_details.Staff;
import com.kairos.persistence.model.staff.personal_details.StaffPersonalDetailQueryResult;
import com.kairos.persistence.model.user.region.Municipality;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.repository.custom_repository.CommonRepository;
import com.kairos.persistence.repository.organization.TeamGraphRepository;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import com.kairos.persistence.repository.user.client.ContactAddressGraphRepository;
import com.kairos.persistence.repository.user.region.RegionGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffTeamRelationshipGraphRepository;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.skill.SkillService;
import com.kairos.service.staff.StaffTeamRankingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ArrayUtil.getUnionOfList;
import static com.kairos.commons.utils.ObjectMapperUtils.copyCollectionPropertiesByMapper;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.AppConstants.FORWARD_SLASH;
import static com.kairos.constants.AppConstants.MAIN_TEAM_RANKING;
import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.enums.team.TeamType.MAIN;

/**
 * Created by oodles on 7/10/16.
 */
@Transactional
@Service
public class TeamService {
    @Inject
    private TeamGraphRepository teamGraphRepository;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private RegionGraphRepository regionGraphRepository;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private ContactAddressGraphRepository contactAddressGraphRepository;
    @Inject
    private OrganizationService organizationService;
    @Inject
    private ActivityIntegrationService activityIntegrationService;
    @Inject
    private SkillService skillService;
    @Inject
    private StaffTeamRelationshipGraphRepository staffTeamRelationshipGraphRepository;
    @Inject
    private AccessGroupService accessGroupService;
    @Inject private CommonRepository commonRepository;
    @Inject private StaffTeamRankingService staffTeamRankingService;

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public TeamDTO createTeam(Long unitId, TeamDTO teamDTO) {
        OrganizationContactAddress organizationContactAddress = unitGraphRepository.getOrganizationByOrganizationId(unitId);
        validateDetails(unitId, teamDTO, organizationContactAddress);
        Unit unit = organizationContactAddress.getUnit();
        ZipCode zipCode = organizationContactAddress.getZipCode();
        if (zipCode == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ZIPCODE_NOTFOUND);
        }
        Municipality municipality = organizationContactAddress.getMunicipality();
        if (municipality == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_MUNICIPALITY_NOTFOUND);

        }
        ContactAddress contactAddress = new ContactAddress(municipality, organizationContactAddress.getContactAddress().getLongitude(), organizationContactAddress.getContactAddress().getLatitude(),
                organizationContactAddress.getContactAddress().getProvince(), organizationContactAddress.getContactAddress().getRegionName(), organizationContactAddress.getContactAddress().getCity(),
                organizationContactAddress.getContactAddress().getCountry(), zipCode, organizationContactAddress.getContactAddress().getHouseNumber(),
                organizationContactAddress.getContactAddress().getStreet(), organizationContactAddress.getContactAddress().getStreetUrl(), organizationContactAddress.getContactAddress().getFloorNumber()
        );
        contactAddressGraphRepository.save(contactAddress, 2);
        Team team = new Team(teamDTO.getName(), teamDTO.getDescription(), contactAddress);
        teamGraphRepository.save(team);
        teamDTO.setId(team.getId());
        unit.getTeams().add(team);
        unitGraphRepository.save(unit, 2);
        teamDTO.setId(team.getId());
        assignTeamLeadersToTeam(teamDTO, team);
        return teamDTO;
    }

    private void validateDetails(Long unitId, TeamDTO teamDTO, OrganizationContactAddress organizationContactAddress) {
        if (organizationContactAddress.getUnit() == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TEAMSERVICE_UNIT_ID_NOTFOUND_BY_GROUP);
        }
        boolean teamExistInOrganizationByName = teamGraphRepository.teamExistInOrganizationByName(unitId, -1L, "(?i)" + teamDTO.getName());
        if (teamExistInOrganizationByName) {
            exceptionService.duplicateDataException(MESSAGE_TEAMSERVICE_TEAM_ALREADYEXISTS_IN_UNIT, teamDTO.getName());
        }
    }

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public TeamDTO updateTeam(Long unitId, Long teamId, TeamDTO teamDTO) {
        boolean teamExistInOrganizationAndGroupByName = teamGraphRepository.teamExistInOrganizationByName(unitId, teamId, "(?i)" + teamDTO.getName());
        if (teamExistInOrganizationAndGroupByName) {
            exceptionService.duplicateDataException(MESSAGE_TEAMSERVICE_TEAM_ALREADYEXISTS_IN_UNIT, teamDTO.getName());
        }
        Team team = teamGraphRepository.findOne(teamId, 0);
        if (team != null) {
            team.setName(teamDTO.getName());
            team.setDescription(teamDTO.getDescription());
            teamGraphRepository.save(team);
        } else {
            exceptionService.dataNotFoundByIdException(MESSAGE_TEAMSERVICE_TEAM_NOTFOUND);
        }
        assignTeamLeadersToTeam(teamDTO, team);
        return teamDTO;
    }

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public boolean updateActivitiesOfTeam(Long unitId , Long teamId, BigInteger activityId) {
        Team team = teamGraphRepository.findOne(teamId);
        team.setActivityId(activityId);
        teamGraphRepository.save(team);
        staffTeamRankingService.updateActivityIdInTeamRanking(teamId, activityId);
        return true;
    }

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public List<StaffTeamDTO> updateStaffsInTeam(Long unitId, Long teamId, List<StaffTeamDTO> staffTeamDTOs) {
        List<StaffPersonalDetailQueryResult> staffSkills = staffGraphRepository.getSkillIdsByStaffIds(staffTeamDTOs.stream().map(staffTeamDTO -> staffTeamDTO.getStaffId()).collect(Collectors.toList()));
        Map<Long,Set<Long>> staffSkillMap = staffSkills.stream().collect(Collectors.toMap(k -> k.getId(),v->v.getSkillIds()));
        for (StaffTeamDTO staffTeamDTO : staffTeamDTOs) {
            if (MAIN.equals(staffTeamDTO.getTeamType()) && staffTeamRelationshipGraphRepository.anyMainTeamExists(staffTeamDTO.getStaffId(), teamId)) {
                exceptionService.actionNotPermittedException("staff.main_team.exists");
            }
            if (staffTeamDTO.getLeaderType() != null && !accessGroupService.findStaffAccessRole(unitId, staffTeamDTO.getStaffId()).isStaff()) {
                exceptionService.actionNotPermittedException(STAFF_CAN_NOT_BE_TEAM_LEADER);
            }
            Team team = teamGraphRepository.findOne(teamId);
            Staff staff = staffGraphRepository.findByStaffId(staffTeamDTO.getStaffId());
            if(!(isCollectionEmpty(team.getSkillList()) && isNull(staffSkillMap.get(staff.getId())))){
                  if(!team.getSkillList().stream().anyMatch(skill->staffSkillMap.getOrDefault(staff.getId(),new HashSet<>()).contains(skill.getId()))){
                exceptionService.actionNotPermittedException(STAFF_SKILL_DOES_NOT_MATCHED);
                  }
            }
            StaffTeamRelationShipQueryResult staffTeamRelationShipQueryResult = staffTeamRelationshipGraphRepository.findByStaffIdAndTeamId(staffTeamDTO.getStaffId(), teamId);
            StaffTeamRelationship staffTeamRelationship;
            if(isNull(staffTeamRelationShipQueryResult)){
                staffTeamRelationship = new StaffTeamRelationship(null, team, staff, staffTeamDTO.getLeaderType(), staffTeamDTO.getTeamType());
            } else {
                staffTeamRelationship = new StaffTeamRelationship(staffTeamRelationShipQueryResult.getId(), team, staff, staffTeamRelationShipQueryResult.getLeaderType(), staffTeamDTO.getTeamType());
            }
            staffTeamRelationship.setStartDate(staffTeamDTO.getStartDate());
            staffTeamRelationship.setEndDate(staffTeamDTO.getEndDate());
            if(MAIN.equals(staffTeamRelationship.getTeamType())){
                staffTeamRelationship.setSequence(MAIN_TEAM_RANKING);
            }else {
                if (!isSequenceExistOrNot(staffTeamDTO.getStaffId(),staffTeamDTO.getSequence(),teamId)) {
                    staffTeamRelationship.setSequence(staffTeamDTO.getSequence());
                }
            }
            staffTeamRelationshipGraphRepository.save(staffTeamRelationship);
            staffTeamRankingService.addOrUpdateStaffTeamRanking(unitId, staffTeamDTO.getStaffId(), team, staffTeamRelationship, staffTeamRelationShipQueryResult);
        }
        return staffTeamDTOs;
    }

    public boolean isSequenceExistOrNot(Long staffId,int sequence,Long teamId){
        return staffTeamRelationshipGraphRepository.sequenceExists(staffId,sequence,teamId);
    }


    public TeamDTO getTeamDetails(Long teamId) {
        return teamGraphRepository.getTeamDetailsById(teamId);
    }

    public Map<String, Object> getTeamsAndPrerequisite(long unitId) {
        List<TeamDTO> teams = teamGraphRepository.getTeams(unitId);
        Map<String, Object> map = new HashMap<>();
        map.put("teams", (isCollectionNotEmpty(teams)) ? teams : Collections.emptyList());
        List<StaffPersonalDetailQueryResult> staffPersonalDetailQueryResults = staffGraphRepository.getAllStaffPersonalDetailsByUnit(unitId, envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath());
        map.put("staffList", staffPersonalDetailQueryResults);
        map.put("skillList", skillService.getSkillsOfOrganization(unitId));

        List<ActivityDTO> activityDTOList = activityIntegrationService.getActivitiesWithCategories(unitId);
        List<ActivityCategoryListDTO> activityCategoryListDTOS=new ArrayList<>();
        if(isCollectionNotEmpty(activityDTOList)) {
            Map<ActivityCategoryDTO, List<ActivityDTO>> activityTypeCategoryListMap = activityDTOList.stream().collect(
                    Collectors.groupingBy(activityType -> new ActivityCategoryDTO(activityType.getCategoryId(), activityType.getCategoryName()), Collectors.toList()));
            activityCategoryListDTOS = activityTypeCategoryListMap.entrySet().stream().map(activity -> new ActivityCategoryListDTO(activity.getKey(),
                    activity.getValue())).collect(Collectors.toList());
        }
        map.put("activityList", activityCategoryListDTOS);
        return map;
    }

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public boolean deleteTeamByTeamId(long teamId) {
        Team team = teamGraphRepository.findOne(teamId);
        if (team != null) {
            team.setEnabled(false);
            teamGraphRepository.save(team);
        } else {
            exceptionService.dataNotFoundByIdException(MESSAGE_TEAMSERVICE_TEAM_NOTFOUND);
        }
        return true;
    }


    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public boolean addStaffInTeam(long teamId, long staffId, boolean isAssigned) {

        Staff staff = staffGraphRepository.findById(staffId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(ERROR_TEAMSERVICE_STAFFORTEAM_NOTEMPTY)));
        int countOfRel = teamGraphRepository.countRelBetweenStaffAndTeam(teamId, staffId);
        if (countOfRel == 0) {

            int countOfRelCreated = teamGraphRepository.linkOfTeamAndStaff(teamId, staffId, DateUtils.getDate().getTime(), DateUtils.getDate().getTime());
            if (countOfRelCreated > 0) {
                return true;
            } else {
                exceptionService.dataNotFoundByIdException(MESSAGE_TEAMSERVICE_SOMETHINGWRONG);
            }
        } else {
            int countOfRelCreated = teamGraphRepository.updateStaffTeamRelationship(teamId, staffId, DateUtils.getDate().getTime(), isAssigned);
            if (countOfRelCreated > 0) {
                if (!isAssigned) {
                    staffGraphRepository.save(staff);
                }
                return true;
            } else {
                exceptionService.dataNotFoundByIdException(MESSAGE_TEAMSERVICE_SOMETHINGWRONG);

            }
        }
        return false;
    }

    /**
     * this method will return all staff in unit
     * and parameter {isSelected} in query response has value true or false
     * if true then staff is already in team otherwise it can be imported in team
     *
     * @return
     */
    public List<Map<String, Object>> getStaffForImportInTeam(long teamId) {
        Team team = teamGraphRepository.findOne(teamId, 0);
        if (team == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_TEAMSERVICE_TEAM_NOTFOUND, teamId);
        }
        List<Map<String, Object>> queryResult = teamGraphRepository.getAllStaffByOrganization(teamId, envConfig.getServerHost() + FORWARD_SLASH);
        List<Map<String, Object>> staff = new ArrayList<>();
        for (Map<String, Object> staffInfo : queryResult) {
            staff.add((Map<String, Object>) staffInfo.get("data"));
        }
        return staff;
    }


    public List<Map<String, Object>> getTeamSelectedServices(Long teamId) {
        return unitGraphRepository.getTeamAllSelectedSubServices(teamId);
    }

    public List<com.kairos.persistence.model.organization.services.OrganizationService> addTeamSelectedServices(Long teamId, Long[] organizationService) {
        return teamGraphRepository.addSelectedSevices(teamId, organizationService);

    }

    public boolean addTeamSelectedSkills(Long teamId, Set<Long> skillIds) {
        teamGraphRepository.removeAllSkillsFromTeam(teamId,skillIds);
        if (isCollectionNotEmpty(skillIds)) {
            teamGraphRepository.saveSkill(teamId, skillIds);
        }
        return true;
    }

    public List<Map<String, Object>> getTeamSelectedSkills(Long teamId) {
        return teamGraphRepository.getSelectedSkills(teamId);

    }

    public List<Map<String, Object>> getAllTeamsInOrganization(Long unitId) {

        List<Map<String, Object>> queryResult = teamGraphRepository.getAllTeamsInOrganization(unitId);

        List<Map<String, Object>> teams = new ArrayList<>();
        for (Map<String, Object> staffInfo : queryResult) {
            teams.add((Map<String, Object>) staffInfo.get("data"));
        }
        return teams;

    }

    public List<TeamDTO> getAllTeamsOfOrganization(Long unitId) {
        return teamGraphRepository.findAllTeamsInOrganization(unitId);
    }

    public Long getOrganizationIdByTeamId(Long teamId) {
        Unit unit = unitGraphRepository.getOrganizationByTeamId(teamId);
        return unit.getId();
    }

    public Unit getOrganizationByTeamId(Long teamId) {
        return unitGraphRepository.getOrganizationByTeamId(teamId);
    }

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public TeamDTO updateTeamGeneralDetails(long teamId, TeamDTO teamDTO) {
        Team team = teamGraphRepository.findById(teamId).orElseThrow(()->new DataNotFoundByIdException(CommonsExceptionUtil.convertMessage(MESSAGE_TEAMSERVICE_TEAM_NOTFOUND, teamId)));
        team.setName(teamDTO.getName());
        teamGraphRepository.save(team);
        return teamDTO;
    }

    public List<Object> getTeamsInUnit(Long unitId) {
        List<Map<String, Object>> data = unitGraphRepository.getUnitTeams(unitId);
        List<Object> response = new ArrayList<>();
        for (Map<String, Object> map :
                data) {
            Object o = map.get("result");
            response.add(o);
        }
        return response;
    }

    public List<BigInteger> getTeamActivitiesOfStaff(Long staffId) {
        return teamGraphRepository.getTeamActivitiesOfStaff(staffId);
    }

    public boolean isActivityAssignedToTeam(BigInteger activityId) {
        return teamGraphRepository.activityExistInTeamByActivityId(activityId);

    }


    private void assignTeamLeadersToTeam(TeamDTO teamDTO, Team team) {
        Set<Long> staffIds = getUnionOfList(new ArrayList<>(teamDTO.getMainTeamLeaderIds()), new ArrayList<>(teamDTO.getActingTeamLeaderIds()));
        List<Staff> staffList = staffGraphRepository.findAllById(new ArrayList<>(staffIds));
        teamGraphRepository.removeAllStaffsFromTeam(teamDTO.getId(), staffIds);
        teamGraphRepository.removeLeaderTypeFromTeam(teamDTO.getId());
        List<StaffTeamRelationship> staffTeamRelationships = new ArrayList<>();
        List<StaffTeamRelationship>  staffTeamRelationShipQueryResults = staffTeamRelationshipGraphRepository.findByStaffIdsAndTeamId(staffIds,teamDTO.getId());
        Map<Long,StaffTeamRelationship> staffTeamRelationShipQueryResultMap = staffTeamRelationShipQueryResults.stream().collect(Collectors.toMap(k->k.getStaff().getId(),v->v));
        staffList.forEach(staff -> {
            if(staffTeamRelationShipQueryResultMap.containsKey(staff.getId())){
                StaffTeamRelationship staffTeamRelationship = staffTeamRelationShipQueryResultMap.get(staff.getId());
                staffTeamRelationship.setLeaderType(teamDTO.getMainTeamLeaderIds().contains(staff.getId()) ? LeaderType.MAIN_LEAD : LeaderType.ACTING_LEAD);
                staffTeamRelationships.add(staffTeamRelationship);
            }else {
                staffTeamRelationships.add(new StaffTeamRelationship(team, staff, teamDTO.getMainTeamLeaderIds().contains(staff.getId()) ? LeaderType.MAIN_LEAD : LeaderType.ACTING_LEAD));
            }
        });
        if (isCollectionNotEmpty(staffTeamRelationships)) {
            staffTeamRelationshipGraphRepository.saveAll(staffTeamRelationships);
        }
    }

    public void assignStaffInTeams(Staff staff, List<com.kairos.dto.user.team.TeamDTO> staffTeamDetails, Long unitId) {
        Set<Long> teamIds = staffTeamDetails.stream().map(k -> k.getId()).collect(Collectors.toSet());
        if(teamIds.size()<staffTeamDetails.size()){
            exceptionService.actionNotPermittedException(TEAM_SHOULD_BE_UNIQUE);
        }
        long mainTeamCount = staffTeamDetails.stream().filter(staffTeamDetail->MAIN.equals(staffTeamDetail.getTeamType())).count();
        if(mainTeamCount > 1){
            exceptionService.actionNotPermittedException(MAIN_TEAM_SHOULD_BE_UNIQUE);
        }
        if (staffTeamDetails.stream().anyMatch(k -> k.getLeaderType() != null) && !accessGroupService.findStaffAccessRole(unitId, staff.getId()).isManagement()) {
            exceptionService.actionNotPermittedException(STAFF_CAN_NOT_BE_TEAM_LEADER);
        }
        List<com.kairos.dto.user.team.TeamDTO> oldStaffTeams = copyCollectionPropertiesByMapper(teamGraphRepository.getTeamDetailsOfStaff(staff.getId(), unitId), com.kairos.dto.user.team.TeamDTO.class);
        teamGraphRepository.removeStaffFromAllTeams(staff.getId());
        List<Team> teams = teamGraphRepository.findAllById(new ArrayList<>(teamIds));
        Map<Long, Team> teamMap = teams.stream().collect(Collectors.toMap(k -> k.getId(), Function.identity()));
        List<StaffTeamRelationship> staffTeamRelationshipList = staffTeamDetails.stream().map(staffTeamDetail -> new StaffTeamRelationship(null, teamMap.get(staffTeamDetail.getId()), staff, staffTeamDetail.getLeaderType(), staffTeamDetail.getTeamType(),isNotNull(staffTeamDetail.getStartDate())?staffTeamDetail.getStartDate():DateUtils.getCurrentLocalDate(),staffTeamDetail.getEndDate(),staffTeamDetail.isTeamMembership())).collect(Collectors.toList());
        if (isCollectionNotEmpty(staffTeamRelationshipList)) {
            staffTeamRelationshipGraphRepository.saveAll(staffTeamRelationshipList);
        }
        if(isCollectionNotEmpty(staffTeamRelationshipList) || isCollectionNotEmpty(oldStaffTeams)) {
            staffTeamRankingService.updateStaffTeamRankingFromPersonalInfo(unitId, staff.getId(), teams, staffTeamRelationshipList, oldStaffTeams);
        }
    }

    @CacheEvict(value="getStaffSpecificActivitySettings", allEntries = true)
    public boolean removeStaffsFromTeam(Long unitId, Long teamId, List<Long> staffIds) {
        List<Long> validStaffIds = new ArrayList<>();
        List<Long> onlyTeamLeader = new ArrayList<>();
        List<Map<String,Object>> staffLeaderTypes = teamGraphRepository.getStaffLeaderTypeMap(teamId);
        Map<Long,Boolean> staffIdAndLeaderTypeMap = new HashMap<>();
        staffLeaderTypes.forEach(staffLeaderType->{
            Map<String,Object> data = (Map<String,Object>) staffLeaderType.get(DATA);
            staffIdAndLeaderTypeMap.put(Long.valueOf(data.get(STAFF_ID).toString()), Boolean.valueOf(data.get(LEADER_TYPE).toString()));
        });
        for (Long staffId : staffIds) {
            if(staffIdAndLeaderTypeMap.containsKey(staffId) && staffIdAndLeaderTypeMap.get(staffId)){
                onlyTeamLeader.add(staffId);
            } else {
                validStaffIds.add(staffId);
            }
            staffTeamRankingService.removeStaffTeamInfo(unitId, staffId, teamId);
        }
        if(isCollectionNotEmpty(onlyTeamLeader)) {
            teamGraphRepository.assignStaffAsTeamLeaderOnly(onlyTeamLeader, teamId);
        }
        if(isCollectionNotEmpty(validStaffIds)) {
            teamGraphRepository.removeStaffsFromTeam(validStaffIds, teamId);
        }
        return true;
    }

    public List<Long> getAllStaffToAssignActivitiesByTeam(Long unitId, Collection<BigInteger> activityIds){
        return teamGraphRepository.getAllStaffToAssignActivitiesByTeam(unitId, activityIds);
    }
    public List<BigInteger> getTeamActivityIdsByUnit(Long unitId) {
        return teamGraphRepository.getTeamActivityIdsByUnit(unitId);
    }
}
