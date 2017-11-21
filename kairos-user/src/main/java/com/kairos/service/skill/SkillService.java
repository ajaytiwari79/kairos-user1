package com.kairos.service.skill;

import com.kairos.client.SkillServiceTemplateClient;
import com.kairos.client.TaskDemandRestClient;
import com.kairos.config.env.EnvConfig;
import com.kairos.custom_exception.DuplicateDataException;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.enums.OrganizationLevel;
import com.kairos.persistence.model.user.country.Country;
import com.kairos.persistence.model.user.skill.Skill;
import com.kairos.persistence.model.user.skill.SkillCategory;
import com.kairos.persistence.model.user.staff.Staff;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.organization.OrganizationMetadataRepository;
import com.kairos.persistence.repository.organization.OrganizationServiceRepository;
import com.kairos.persistence.repository.organization.TeamGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.skill.SkillCategoryGraphRepository;
import com.kairos.persistence.repository.user.skill.SkillGraphRepository;
import com.kairos.persistence.repository.user.skill.UserSkillLevelRelationshipGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.response.dto.web.organization.OrganizationSkillDTO;
import com.kairos.service.UserBaseService;
import com.kairos.service.country.CitizenStatusService;
import com.kairos.service.fls_visitour.schedule.Scheduler;
import com.kairos.service.integration.IntegrationService;
import com.kairos.service.mail.MailService;
import com.kairos.service.organization.TeamService;
import com.kairos.service.organization.TimeSlotService;
import com.kairos.service.staff.StaffService;
import com.kairos.util.DateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

import static com.kairos.constants.AppConstants.*;

/**
 * Created by oodles on 15/9/16.
 */
@Service
@Transactional
public class SkillService extends UserBaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private SkillGraphRepository skillGraphRepository;
    @Inject
    private SkillCategoryGraphRepository skillCategoryGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private TeamService teamService;
    @Inject
    private TeamGraphRepository teamGraphRepository;
    @Inject
    private OrganizationServiceRepository organizationServiceRepository;
    //anilm2 will remove this repo call this method from rest template
  /*  @Inject
    private TaskTypeMongoRepository taskTypeMongoRepository;*/
    @Inject
    private MailService mailService;
    @Inject
    private StaffService staffService;
    @Inject
    private UserSkillLevelRelationshipGraphRepository userSkillLevelRelationshipGraphRepository;
    @Inject
    private IntegrationService integrationService;
    @Inject
    private Scheduler scheduler;
    @Inject
    private OrganizationMetadataRepository organizationMetadataRepository;
    @Inject
    private CitizenStatusService citizenStatusService;
    @Inject
    private EnvConfig envConfig;
    @Autowired
    SkillServiceTemplateClient skillServiceTemplateClient;
    @Inject
    private TimeSlotService timeSlotService;
    @Inject
    private TaskDemandRestClient taskDemandRestClient;


    public Map<String, Object> createSkill(Skill skill, long skillCategoryId) {
        SkillCategory skillCategory = skillCategoryGraphRepository.findOne(skillCategoryId);
        if (skillCategory == null) {
            return null;
        }
        String name = "(?i)" + skill.getName();
        logger.info("Added regex to Name: " + name);
        if (skillGraphRepository.checkDuplicateSkill(skillCategoryId, name).isEmpty()) {
            logger.info("Creating unique skill");
            skill.setSkillCategory(skillCategory);
            skillGraphRepository.save(skill);
            Map<String, Object> response = skill.retrieveDetails();
            return response;
        }
        throw new DuplicateDataException("Can't create duplicate skill in same category");

    }

    public List<Map<String, Object>> getAllSkills(long countryId) {
        Country country = countryGraphRepository.findOne(countryId);
        if (country == null) {
            return null;
        }
        List<Map<String, Object>> response = new ArrayList<>();
        for (Map<String, Object> result : skillGraphRepository.getSkillsByCountryId(countryId)) {
            response.add((Map<String, Object>) result.get("result"));
        }
        return response;
    }

    public Skill getSkillById(Long id, int depth) {
        return skillGraphRepository.findOne(id, depth);
    }


    public Map<String, Object> updateSkill(long countryId, Skill data) {
        if (data != null) {
            Skill skill = skillGraphRepository.findOne(data.getId());

            if (skill != null) {
                skill.setName(data.getName());
                skill.setDescription(data.getDescription());
                skill.setShortName(data.getShortName());
                return skillGraphRepository.save(skill).retrieveDetails();
            }

            return null;
        }
        return null;
    }

    public void deleteSkill(Long id) {
        super.delete(id);
    }

    public SkillCategory safeDeleteSkill(Long categoryId, Long skillId) {
        return skillGraphRepository.safeDelete(categoryId, skillId);
    }

    public List<Skill> getSkillsByCategoryId(Long id) {
        return skillGraphRepository.skillsByCategoryId(id);
    }



    /**
     * @param id   {id of team or organization based on type}
     * @param type type could be oranization or team
     * @return
     * @author prabjot
     * this method returns all skills based on type of node{all skills of organization or team it depends on type parameter} and relationship of staff and skills
     */
    public HashMap<String, Object> getAllAvailableSkills(long id, String type) {

        HashMap<String, Object> response = new HashMap<>();


        if (ORGANIZATION.equalsIgnoreCase(type)) {

            Organization unit = organizationGraphRepository.findOne(id, 0);
            Organization parent = null;

            if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
                parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

            } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
                parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
            }

            List<Map<String, Object>> organizationSkills;
            if (parent == null) {
                organizationSkills = organizationGraphRepository.getSkillsOfParentOrganization(id);
            } else {
                organizationSkills = organizationGraphRepository.getSkillsOfChildOrganization(parent.getId(), id);
            }

            List<Map<String, Object>> orgSkillRel = new ArrayList<>(organizationSkills.size());
            for (Map<String, Object> map : organizationSkills) {
                orgSkillRel.add((Map<String, Object>) map.get("data"));
            }

            response.put("orgData", orgSkillRel);
        } else if (TEAM.equalsIgnoreCase(type)) {

            List<Map<String, Object>> teamSkills = teamGraphRepository.getSkillsOfTeam(id);

            List<Map<String, Object>> teamSkillRel = new ArrayList<>(teamSkills.size());
            for (Map<String, Object> map : teamSkills) {
                teamSkillRel.add((Map<String, Object>) map.get("data"));
            }
            response.put("orgData", teamSkillRel);
        }
        List<Long> serviceIds = organizationServiceRepository.getServiceIdsByOrgId(id);
        Map<String, Object> taskTypeList=skillServiceTemplateClient.getTaskTypeList(serviceIds,id);
        response.putAll(taskTypeList);
        response.put("skillLevels", Skill.SkillLevel.values());
        response.put("teamList", teamService.getAllTeamsInOrganization(id));

        return response;

    }


    /**
     * * this method returns all skills and staff of unit/organization
     *
     * @param unitId {id of unit/organization}
     * @return
     */
    public HashMap<String, Object> getUnitData(long unitId) {

        HashMap<String, Object> response = new HashMap<>();

        List<Map<String, Object>> skills = organizationGraphRepository.getSkillsOfOrganization(unitId);

        List<Map<String, Object>> filterSkillData = new ArrayList<>();
        for (Map<String, Object> map : skills) {
            filterSkillData.add((Map<String, Object>) map.get("data"));
        }

        response.put("skillList", filterSkillData);
        List<Long> serviceIds = organizationServiceRepository.getServiceIdsByOrgId(unitId);
        Map<String, Object> taskTypeList=skillServiceTemplateClient.getTaskTypeList(serviceIds,unitId);
        response.putAll(taskTypeList);

        response.put("teamList", teamService.getAllTeamsInOrganization(unitId));
        response.put("civilianStatus", citizenStatusService.getCitizenStatusByCountryIdAnotherFormat(countryGraphRepository.getCountryIdByUnitId(unitId)));
        Map<String, Object> timeSlotData = timeSlotService.getTimeSlots(unitId);

        if (timeSlotData != null) {
            response.put("timeSlotList", timeSlotData);
        }

        List<Map<String, Object>> staff = staffGraphRepository.getStaffWithBasicInfo(unitId, unitId,envConfig.getServerHost() + FORWARD_SLASH);

        List<Map<String, Object>> staffList = new ArrayList<>();
        for (Map<String, Object> map : staff) {
            staffList.add((Map<String, Object>) map.get("data"));
        }
        List<Object> localAreaTagsList = new ArrayList<>();
        List<Map<String, Object>> tagList = organizationMetadataRepository.findAllByIsDeletedAndUnitId(unitId);
        for (Map<String, Object> map : tagList) {
            localAreaTagsList.add(map.get("tags"));
        }
        response.put("staffList", staffList);
        response.put("localAreaTags", localAreaTagsList);
        response.put("serviceTypes", organizationServiceRepository.getOrganizationServiceByOrgId(unitId));
        response.put("exceptionTypes", taskDemandRestClient.getCitizensExceptionTypes(unitId));

        return response;

    }


    /**
     * @param id         {id of team or organization based on type}
     * @param skillId
     * @param isSelected {true or false if true skill will be added if not exist otherwise updated, if false skill will be removed}
     * @param type       {organization,team}
     * @return updated skills irrespective of team or organization
     * @author prabjot
     * to add new skill based onn type of node {organization,team}
     * if type is an organozation then skill will be added to an organization otherwise it will added to team
     */
    public HashMap<String, Object> addNewSkill(long id, long skillId, boolean isSelected, String type,String visitourId) {


        if (ORGANIZATION.equalsIgnoreCase(type)) {

            if (isSelected) {
                if (organizationGraphRepository.isSkillAlreadyExist(id, skillId) == 0) {
                    organizationGraphRepository.addSkillInOrganization(id, Arrays.asList(skillId), new Date().getTime(), new Date().getTime());
                } else {
                    organizationGraphRepository.updateSkillInOrganization(id, Arrays.asList(skillId), new Date().getTime(), new Date().getTime());
                }
            } else {
                organizationGraphRepository.removeSkillFromOrganization(id, skillId, new Date().getTime());
            }
            return getAllAvailableSkills(id, type);

        } else if (TEAM.equalsIgnoreCase(type)) {
            long createdDate = new Date().getTime();
            if (isSelected) {
                teamGraphRepository.addSkillInTeam(id,skillId, visitourId,createdDate, createdDate,true);
            } else {
                teamGraphRepository.addSkillInTeam(id,skillId, visitourId,createdDate, createdDate,false);
            }
            return getAllAvailableSkills(id, type);
        } else {
            throw new InternalError("Type not correct");
        }
    }


    public boolean deleteSkill(long skillId) {
        Skill skill = skillGraphRepository.findOne(skillId);
        if (skill == null) {
            return false;
        }
        skill.setEnabled(false);
        save(skill);
        return true;
    }
    /**
     * to update visitour id of skill for particular organization
     *
     * @param unitId
     * @param skillId
     * @return
     */
   /* public boolean updateVisitourIdOfSkill(long unitId, long skillId, String visitourId,String type) {

        if(ORGANIZATION.equalsIgnoreCase(type)){
            return skillGraphRepository.updateVisitourIdOfSkillInOrganization(unitId, skillId, visitourId);
        } else if(TEAM.equalsIgnoreCase(type)) {
            return skillGraphRepository.updateVisitourIdOfSkillInTeam(unitId,skillId,visitourId);
        } else {
            throw new InternalError("Type incorrect");
        }
    }*/

    public boolean updateSkillOfOrganization(long unitId, long skillId, String type, OrganizationSkillDTO organizationSkillDTO) {
        
        if(ORGANIZATION.equalsIgnoreCase(type)){
            if(organizationSkillDTO.getCustomName() == null || organizationSkillDTO.getCustomName() == ""){
                return skillGraphRepository.updateSkillOfOrganization(unitId, skillId, organizationSkillDTO.getVisitourId());
            } else {
                return skillGraphRepository.updateSkillOfOrganizationWithCustomName(unitId, skillId, organizationSkillDTO.getVisitourId(), organizationSkillDTO.getCustomName());
            }
         } else if(TEAM.equalsIgnoreCase(type)) {
            return skillGraphRepository.updateVisitourIdOfSkillInTeam(unitId,skillId,organizationSkillDTO.getVisitourId());
        } else {
            throw new InternalError("Type incorrect");
        }
    }

    public boolean requestForCreateNewSkill(long unitId, Skill skill) {
        Organization organization = organizationGraphRepository.findOne(unitId);

        if (organization == null) {
            return false;
        }
        skill.setEnabled(false);
        skill.setSkillStatus(Skill.SkillStatus.PENDING);
        save(skill);
        mailService.sendPlainMail(ADMIN_EMAIL, "Request for create new skill", "Skill creation request");
        return true;
    }
    public Map<String, Object> getSkills(long staffId,long id,String type) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return null;
        }

        long unitId;
        if(ORGANIZATION.equalsIgnoreCase(type)){
            unitId = id;
        } else if(TEAM.equalsIgnoreCase(type)) {
            Organization unit = organizationGraphRepository.getOrganizationByTeamId(id);
            unitId = unit.getId();
        } else {
            throw new InternalError("Type incorrect");
        }

        List<Long> selectedSkillId = new ArrayList<>();
        List<Map<String, Object>> treeData = new ArrayList<>();
        for (Map<String, Object> data : staffGraphRepository.getSkills(staffId,unitId)) {
            Map<String, Object> map = (Map<String, Object>) data.get("data");
            for (Map<String, Object> skill : (List<Map<String, Object>>) map.get("children")) {
                if (skill.get("isSelected") != null && (boolean) skill.get("isSelected")) {
                    selectedSkillId.add((long) skill.get("id"));
                }
            }
            treeData.add(map);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> map : userSkillLevelRelationshipGraphRepository.getStaffSkillRelationship(staffId, selectedSkillId, unitId)) {
            Map<String, Object> data = (Map<String, Object>) map.get("data");
            list.add(data);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("tableData", list);
        map.put("treeData", treeData);
        map.put("skillLevels", Arrays.asList(Skill.SkillLevel.ADVANCE, Skill.SkillLevel.BASIC, Skill.SkillLevel.EXPERT));
        return map;
    }

    public List assignSkillToStaff(long staffId, List<Long> removedSkillIds, boolean isSelected,long unitId) {

        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return null;
        }
        List<Map<String,Object>> response;
        if (isSelected) {
            staffGraphRepository.addSkillInStaff(staffId, removedSkillIds,new Date().getTime(),new Date().getTime(), Skill.SkillLevel.ADVANCE,true);
            response = prepareSelectedSkillResponse(staffId,removedSkillIds, unitId);
        } else {
            staffGraphRepository.deleteSkillFromStaff(staffId, removedSkillIds,new Date().getTime());
            response = Collections.emptyList();
        }
        if (staffGraphRepository.checkIfStaffIsTaskGiver(staffId,unitId)!=0){
            logger.info("Staff  is TaskGiver: Now Syncing Skills in Visitour");
            updateSkillsOfStaffInVisitour(staff,unitId);
        }
        return response;

    }

    private List<Map<String, Object>> prepareSelectedSkillResponse(long staffId,List<Long> skillId, long unitId) {

        List<Map<String,Object>> staffSkillInfo = staffGraphRepository.getStaffSkillInfo(staffId,skillId,unitId);

        List<Map<String, Object>> list = new ArrayList<>();

        Map<String,Object> copyMap;
        for(Map<String,Object> staffSkillRel : staffSkillInfo){
            Map<String,Object> staffSkillRelInfo = (Map<String,Object>) staffSkillRel.get("data");
            copyMap = new HashMap<>();
            copyMap.putAll(staffSkillRelInfo);
            copyMap.put("startDate", DateConverter.getDate((long)staffSkillRelInfo.get("startDate")));
            copyMap.put("endDate",DateConverter.getDate((long)staffSkillRelInfo.get("endDate")));
            copyMap.put("lastSyncInVisitour",DateConverter.getDate((long)staffSkillRelInfo.get("lastSyncInVisitour")));
            list.add(copyMap);
        }
        return list;

    }



    public void updateStaffSkillLevel(long staffId, long skillId, Skill.SkillLevel skillLevel, long startDate, long endDate, boolean status, long unitId) {
        Staff staff = staffGraphRepository.findOne(staffId);
        userSkillLevelRelationshipGraphRepository.updateStaffSkill(staffId, skillId, skillLevel, startDate, endDate,status);
        updateSkillsOfStaffInVisitour(staff,unitId);
    }


    public boolean assignSkillToStaff(long id, long staffId, long skillId, boolean isSelected,String type) {

        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            throw new InternalError("staff can not be null");
        }

        long lastModificationDate = new Date().getTime();
        if (isSelected) {
            staffGraphRepository.addSkillInStaff(staffId, Arrays.asList(skillId), lastModificationDate, lastModificationDate, Skill.SkillLevel.ADVANCE,true);
        } else {
            staffGraphRepository.addSkillInStaff(staffId, Arrays.asList(skillId), lastModificationDate, lastModificationDate, Skill.SkillLevel.ADVANCE,false);
        }
        int count = staffGraphRepository.checkIfStaffIsTaskGiver(staffId, id);
        if (count != 0) {
            logger.info("Staff  is TaskGiver: Now Syncing Skills in Visitour");
            updateSkillsOfStaffInVisitour(staff, id);
        }
        return true;

    }

    public Map<String, Object> getStaffSkills(long id,String type) {


        List<Map<String, Object>> skills;
        List<Map<String, Object>> response = new ArrayList<>();
        if (ORGANIZATION.equalsIgnoreCase(type)) {
            List<Map<String, Object>> staffList = staffService.getStaffWithBasicInfo(id);
            List<Long> staffIds = new ArrayList<>(staffList.size());
            for (Map<String, Object> map : staffList) {
                response.add((Map<String, Object>) map.get("data"));
                staffIds.add((long) ((Map<String, Object>) map.get("data")).get("id"));
            }
            skills = organizationGraphRepository.getAssignedSkillsOfStaffByOrganization(id, staffIds);

        } else if (TEAM.equalsIgnoreCase(type)) {
            List<Map<String, Object>> staffList = staffGraphRepository.getStaffByTeamId(id,envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath());
            List<Long> staffIds = new ArrayList<>(staffList.size());
            for (Map<String, Object> map : staffList) {
                response.add((Map<String, Object>) map.get("data"));
                staffIds.add((long) ((Map<String, Object>) map.get("data")).get("id"));
            }
            skills = teamGraphRepository.getAssignedSkillsOfStaffByTeam(id, staffIds);
        } else {
            throw new InternalError("Type is not valid");
        }
        List<Map<String, Object>> skillsResponse = new ArrayList<>();
        for (Map<String, Object> map : skills) {
            skillsResponse.add((Map<String, Object>) map.get("data"));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("skills", skillsResponse);
        map.put("staffList", response);
        return map;
    }




    public boolean updateSkillsOfStaffInVisitour(Staff staff, long unitId){

        Map<String, String> flsCredentials = integrationService.getFLS_Credentials(unitId);

        List<String> skillsToUpdate = staffGraphRepository.getStaffVisitourIdWithLevel(unitId,staff.getId());

        String visitourSkillRequestData = "";
        for(String skill:skillsToUpdate){
            visitourSkillRequestData = skill + "," + visitourSkillRequestData;
        }

        int code = -1 ;
        if(staff.getVisitourId() > 0) {
            Map<String, Object> engineerMetaData = new HashMap<>();
            engineerMetaData.put("fmvtid", staff.getVisitourId());
            engineerMetaData.put("fmextID", staff.getVisitourId());
            engineerMetaData.put("lskills", visitourSkillRequestData);
            code = scheduler.createEngineer(engineerMetaData, flsCredentials);
            logger.info("fls data to sync" + engineerMetaData);
            logger.info("FLS staff sync status-->" + code);
        }
        if(code == 0){
            return true;
        }
        return false;

    }

    public Map<String, Object> getSkills(Long organizationId,Long subServiceId){
        Map<String, Object> response = new HashMap<>();
        List<Map<String,Object>> skills;
        if(organizationId == 0){
            Country country = countryGraphRepository.getCountryByOrganizationService(subServiceId);
            skills = (country == null)?skillGraphRepository.getSkillsForTaskType():skillGraphRepository.getSkillsByCountryForTaskType(country.getId());
        } else {
            skills = organizationGraphRepository.getSkillsOfOrganization(organizationId);
        }

        List<Map<String, Object>> filterSkillData = new ArrayList<>();
        for (Map<String, Object> map : skills) {
            filterSkillData.add((Map<String, Object>) map.get("data"));
        }
        response.put("treeData", filterSkillData);
        response.put("skillLevel", Skill.SkillLevel.values());
        return response;

    }

    public List<Map<String,Object>> getSkillsOfOrganization(long organizationId ){;
        return organizationGraphRepository.getSkillsOfOrganization(organizationId);
    }

    public List<Map<String,Object>> getSkillsForTaskType(@PathVariable long countryId){
        return skillGraphRepository.getSkillsByCountryForTaskType(countryId);
    }
}
