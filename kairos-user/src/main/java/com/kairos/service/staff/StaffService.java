package com.kairos.service.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.client.TaskServiceRestClient;
import com.kairos.config.env.EnvConfig;
import com.kairos.constants.AppConstants;
import com.kairos.custom_exception.DataNotFoundByIdException;
import com.kairos.custom_exception.DataNotMatchedException;
import com.kairos.custom_exception.DuplicateDataException;
import com.kairos.custom_exception.FlsCredentialException;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.UnitManagerDTO;
import com.kairos.persistence.model.organization.enums.OrganizationLevel;
import com.kairos.persistence.model.user.access_permission.AccessGroup;
import com.kairos.persistence.model.user.access_permission.AccessPage;
import com.kairos.persistence.model.user.auth.User;
import com.kairos.persistence.model.user.client.Client;
import com.kairos.persistence.model.user.client.ContactAddress;
import com.kairos.persistence.model.user.client.ContactDetail;
import com.kairos.persistence.model.user.country.EngineerType;
import com.kairos.persistence.model.user.expertise.Expertise;
import com.kairos.persistence.model.user.language.Language;
import com.kairos.persistence.model.user.region.ZipCode;
import com.kairos.persistence.model.user.skill.Skill;
import com.kairos.persistence.model.user.staff.*;
import com.kairos.persistence.repository.organization.OrganizationGraphRepository;
import com.kairos.persistence.repository.user.access_permission.AccessGroupRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.client.ClientGraphRepository;
import com.kairos.persistence.repository.user.country.CountryGraphRepository;
import com.kairos.persistence.repository.user.country.EngineerTypeGraphRepository;
import com.kairos.persistence.repository.user.expertise.ExpertiseGraphRepository;
import com.kairos.persistence.repository.user.language.LanguageGraphRepository;
import com.kairos.persistence.repository.user.region.ZipCodeGraphRepository;
import com.kairos.persistence.repository.user.staff.*;
import com.kairos.response.dto.web.ClientStaffInfoDTO;
import com.kairos.response.dto.web.PasswordUpdateDTO;
import com.kairos.response.dto.web.StaffAssignedTasksWrapper;
import com.kairos.response.dto.web.StaffTaskDTO;
import com.kairos.service.UserBaseService;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.access_permisson.AccessPageService;
import com.kairos.service.fls_visitour.schedule.Scheduler;
import com.kairos.service.integration.IntegrationService;
import com.kairos.service.mail.MailService;
import com.kairos.service.organization.OrganizationService;
import com.kairos.service.organization.TeamService;
import com.kairos.service.skill.SkillService;
import com.kairos.util.DateConverter;
import com.kairos.util.FileUtil;
import com.kairos.util.userContext.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.constants.AppConstants.*;
import static com.kairos.util.FileUtil.createDirectory;

/**
 * Created by prabjot on 24/10/16.
 */
@Transactional
@Service
public class StaffService extends UserBaseService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private UserGraphRepository userGraphRepository;
    @Inject
    private ExpertiseGraphRepository expertiseGraphRepository;
    @Inject
    private AccessGroupRepository accessGroupRepository;
    @Inject
    private LanguageGraphRepository languageGraphRepository;
    @Inject
    private CountryGraphRepository countryGraphRepository;
    @Inject
    private OrganizationGraphRepository organizationGraphRepository;
    @Inject
    private EmploymentGraphRepository employmentGraphRepository;
    @Inject
    private UnitEmploymentGraphRepository unitEmploymentGraphRepository;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private Scheduler scheduler;
    @Inject
    private ZipCodeGraphRepository zipCodeGraphRepository;
    @Inject
    private EngineerTypeGraphRepository engineerTypeGraphRepository;
    @Inject
    private TeamService teamService;
    @Inject
    private PartialLeaveGraphRepository partialLeaveGraphRepository;
    @Inject
    IntegrationService integrationService;
    @Inject
    private MailService mailService;
    @Inject
    private EmploymentService employmentService;
    @Inject
    private AccessPageService accessPageService;
    @Inject
    private SkillService skillService;
    @Inject
    private StaffAddressService staffAddressService;
    @Inject
    private AccessGroupService accessGroupService;

    @Autowired
    UnitEmpAccessGraphRepository unitEmpAccessGraphRepository;
    @Autowired
    ClientGraphRepository clientGraphRepository;
    @Autowired
    TaskServiceRestClient taskServiceRestClient;
    @Inject
    private OrganizationService organizationService;

    public String uploadPhoto(Long staffId, MultipartFile multipartFile) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return null;
        }
        createDirectory(IMAGES_PATH);
        String fileName = new Date().getTime() + multipartFile.getOriginalFilename();
        final String path = IMAGES_PATH + File.separator + fileName;
        FileUtil.writeFile(path, multipartFile);
        staff.setProfilePic(fileName);
        save(staff);
        return envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath() + fileName;

    }

    public boolean removePhoto(Long staffId) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return false;
        }
        staff.setProfilePic(null);
        save(staff);
        return true;
    }


    public boolean updatePassword(long staffId, PasswordUpdateDTO passwordUpdateDTO) {

        User user = userGraphRepository.getUserByStaffId(staffId);
        if (!Optional.ofNullable(user).isPresent()) {
            logger.error("User not found belongs to this staff id " + staffId);
            throw new DataNotFoundByIdException("User not found belongs to this staff id " + staffId);
        }
        CharSequence oldPassword = CharBuffer.wrap(passwordUpdateDTO.getOldPassword());
        if (new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
            CharSequence newPassword = CharBuffer.wrap(passwordUpdateDTO.getNewPassword());
            user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
            save(user);
        } else {
            logger.error("Password not matched ");
            throw new DataNotMatchedException("Password not matched");
        }
        return true;

    }

    public StaffPersonalDetail savePersonalDetail(long staffId, StaffPersonalDetail staffPersonalDetail, long unitId) throws ParseException {
        Staff objectToUpdate = staffGraphRepository.findOne(staffId);

        if (objectToUpdate == null) {
            throw new InternalError("Staff can't null");
        }
        Language language = languageGraphRepository.findOne(staffPersonalDetail.getLanguageId());
        Expertise expertise = expertiseGraphRepository.findOne(staffPersonalDetail.getExpertiseId());
        Expertise oldExpertise = objectToUpdate.getExpertise();
        objectToUpdate.setLanguage(language);
        objectToUpdate.setExpertise(expertise);
        objectToUpdate.setFirstName(staffPersonalDetail.getFirstName());
        objectToUpdate.setLastName(staffPersonalDetail.getLastName());
        objectToUpdate.setFamilyName(staffPersonalDetail.getFamilyName());
        objectToUpdate.setActive(staffPersonalDetail.getActive());
        objectToUpdate.setCprNumber(staffPersonalDetail.getCprNumber());
        objectToUpdate.setSpeedPercent(staffPersonalDetail.getSpeedPercent());
        objectToUpdate.setWorkPercent(staffPersonalDetail.getWorkPercent());
        objectToUpdate.setOvertime(staffPersonalDetail.getOvertime());
        objectToUpdate.setCostDay(staffPersonalDetail.getCostDay());
        objectToUpdate.setCostCall(staffPersonalDetail.getCostCall());
        objectToUpdate.setCostKm(staffPersonalDetail.getCostKm());
        objectToUpdate.setCostHour(staffPersonalDetail.getCostHour());
        objectToUpdate.setCostHourOvertime(staffPersonalDetail.getCostHourOvertime());
        objectToUpdate.setCapacity(staffPersonalDetail.getCapacity());

        int count = staffGraphRepository.checkIfStaffIsTaskGiver(staffId, unitId);
        if (count != 0 && objectToUpdate.getVisitourId() != 0) {
            updateStaffPersonalInfoInFLS(objectToUpdate, unitId); // Update info to FLS
        }
        if (!staffPersonalDetail.getActive()) {
            objectToUpdate.setInactiveFrom(DateConverter.parseDate(staffPersonalDetail.getInactiveFrom()).getTime());
        }
        objectToUpdate.setSignature(staffPersonalDetail.getSignature());
        objectToUpdate.setContactDetail(staffPersonalDetail.getContactDetail());
        save(objectToUpdate);
        if (oldExpertise != null) {
            staffGraphRepository.removeSkillsByExpertise(objectToUpdate.getId(), oldExpertise.getId());
        }
        staffGraphRepository.updateSkillsByExpertise(objectToUpdate.getId(), expertise.getId(), new Date().getTime(), new Date().getTime(), Skill.SkillLevel.ADVANCE);

        return staffPersonalDetail;
    }

    public Map<String, Object> getPersonalInfo(long staffId, long unitId, String type) {

        Staff staff = null;

        if (TEAM.equalsIgnoreCase(type)) {
            staff = staffGraphRepository.getTeamStaff(unitId, staffId);
        } else if (ORGANIZATION.equalsIgnoreCase(type)) {
            staff = staffGraphRepository.getStaffByUnitId(unitId, staffId);
        }

        if (staff == null) {
            throw new DataNotFoundByIdException("Staff not found with provided Staff ID: " + staffId + " and " + type + " ID: " + unitId);
        }


        Map<String, Object> personalInfo = new HashMap<>(2);
        Long countryId = countryGraphRepository.getCountryIdByUnitId(unitId);
        List<Expertise> expertise;
        List<Language> languages;
        List<EngineerType> engineerTypes;
        if (countryId != null) {
            expertise = expertiseGraphRepository.getAllExpertiseByCountry(countryId);
            engineerTypes = engineerTypeGraphRepository.findEngineerTypeByCountry(countryId);
            languages = languageGraphRepository.getLanguageByCountryId(countryId);
        } else {
            expertise = Collections.emptyList();
            languages = Collections.emptyList();
            engineerTypes = Collections.emptyList();
        }
        personalInfo.put("employmentInfo", employmentService.retrieveEmploymentDetails(staff));
        personalInfo.put("personalInfo", retrievePersonalInfo(staff));
        personalInfo.put("expertise", expertise);
        personalInfo.put("languages", languages);
        personalInfo.put("engineerTypes", engineerTypes);
        return personalInfo;

    }


    public Map<String, Object> retrievePersonalInfo(Staff staff) {
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", staff.getFirstName());
        map.put("lastName", staff.getLastName());
        map.put("profilePic", envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath() + staff.getProfilePic());
        map.put("familyName", staff.getFamilyName());
        map.put("active", staff.isActive());
        map.put("signature", staff.getSignature());
        map.put("inactiveFrom", DateConverter.getDate(staff.getInactiveFrom()));
        map.put("expertiseId", staffGraphRepository.getExpertiseId(staff.getId()));
        map.put("languageId", staffGraphRepository.getLanguageId(staff.getId()));
        map.put("contactDetail", staffGraphRepository.getContactDetail(staff.getId()));
        map.put("cprNumber", staff.getCprNumber());

        // Visitour Speed Profile
        map.put("speedPercent", staff.getSpeedPercent());
        map.put("workPercent", staff.getWorkPercent());
        map.put("overtime", staff.getOvertime());
        map.put("costDay", staff.getCostDay());
        map.put("costCall", staff.getCostCall());
        map.put("costKm", staff.getCostKm());
        map.put("costHour", staff.getCostHour());
        map.put("costHourOvertime", staff.getCostHourOvertime());
        map.put("capacity", staff.getCapacity());
        return map;
    }

    public Map<String, Object> saveNotes(long staffId, String generalNote, String requestFromPerson) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff != null) {
            logger.info("General note: " + generalNote + "\nPerson: " + requestFromPerson);
            staff.saveNotes(generalNote, requestFromPerson);
            save(staff);
            return staff.retrieveNotes();
        }
        return null;
    }

    public Map<String, Object> getNotes(long staffId) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return null;
        }
        return staff.retrieveNotes();
    }


    public Map<String, Object> getStaff(String type, long id) {

        List<Map<String, Object>> staff = null;
        Long countryId = null;
        List<AccessGroup> roles = null;
        List<EngineerType> engineerTypes = null;
        if (ORGANIZATION.equalsIgnoreCase(type)) {
            staff = getStaffWithBasicInfo(id);
            roles = accessGroupService.getAccessGroups(id);
            countryId = countryGraphRepository.getCountryIdByUnitId(id);
            engineerTypes = engineerTypeGraphRepository.findEngineerTypeByCountry(countryId);
        } else if (GROUP.equalsIgnoreCase(type)) {
            staff = staffGraphRepository.getStaffByGroupId(id, envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath());
            Organization organization = organizationGraphRepository.getOrganizationByGroupId(id).getOrganization();
            countryId = countryGraphRepository.getCountryIdByUnitId(organization.getId());
            roles = accessGroupService.getAccessGroups(organization.getId());
        } else if (TEAM.equalsIgnoreCase(type)) {
            staff = staffGraphRepository.getStaffByTeamId(id, envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath());
            Organization organization = organizationGraphRepository.getOrganizationByTeamId(id);
            roles = accessGroupService.getAccessGroups(organization.getId());
            countryId = countryGraphRepository.getCountryIdByUnitId(organization.getId());
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map<String, Object> map : staff) {
            response.add((Map<String, Object>) map.get("data"));
        }
        Map<String, Object> map = new HashMap();
        map.put("staffList", response);
        map.put("engineerTypes", engineerTypes);
        map.put("engineerList", engineerTypeGraphRepository.findEngineerTypeByCountry(countryId));
        map.put("roles", roles);
        return map;
    }

    public List<Map<String, Object>> getStaffWithBasicInfo(long unitId) {
        Organization unit = organizationGraphRepository.findOne(unitId, 0);
        if (unit == null) {
            throw new InternalError("Unit can not be null");
        }
        Organization parent = null;
        if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }

        if (parent == null) {
            return staffGraphRepository.getStaffWithBasicInfo(unit.getId(), unit.getId(), envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath());
        } else {
            return staffGraphRepository.getStaffInfoForFilters(parent.getId(), unit.getId(), envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath());
        }
    }

    public List<StaffAdditionalInfoQueryResult> getStaffWithAdditionalInfo(long unitId) {

        Organization unit = organizationGraphRepository.findOne(unitId, 0);
        if (unit == null) {
            throw new InternalError("Unit can not be null");
        }

        return staffGraphRepository.getStaffAndCitizenDetailsOfUnit(unitId);
        //TODO unnecessary queries should be removed
        /*Organization parent = null;
        if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }

        if (parent == null) {
            return staffGraphRepository.getStaffWithAdditionalInfo(unit.getId(), unit.getId());
        } else {
            return staffGraphRepository.getStaffWithAdditionalInfo(parent.getId(), unit.getId());
        }*/
    }

    public Staff assignExpertiseToStaff(long staffId, long expertiseId) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return null;
        }
        Expertise expertise = expertiseGraphRepository.findOne(expertiseId);
        if (expertise != null) {
            staff.setExpertise(expertise);
            staffGraphRepository.save(staff);
        }
        return staff;
    }

    public Map<String, Object> getExpertiseOfStaff(long countryId, long staffId) {
        Staff staff = staffGraphRepository.findOne(staffId);
        if (staff == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("allExpertise", expertiseGraphRepository.getAllExpertiseByCountry(countryId));
        map.put("myExpertise", staff.getExpertise().getId());
        return map;
    }

    public List<Staff> getPlannerInOrganization(Long organizationId) {
        return staffGraphRepository.findAllPlanners(organizationId);

    }

    public List<Staff> getManagersInOrganization(Long organizationId) {
        return staffGraphRepository.findAllManager(organizationId);

    }

    public List<Staff> getVisitatorsInOrganization(Long organizationId) {
        return staffGraphRepository.findAllVisitator(organizationId);

    }

    public List<Staff> getTeamLeadersInOrganization(Long organizationId) {
        return staffGraphRepository.findAllTeamLeader(organizationId);

    }

    public List<Staff> batchAddStaffToDatabase(long unitId, MultipartFile multipartFile, Long accessGroupId) {

        AccessGroup accessGroup = accessGroupRepository.findOne(accessGroupId);
        if (!Optional.ofNullable(accessGroup).isPresent()) {
            logger.error("Access group not found");
            throw new InternalError("Access group not found " + accessGroupId);
        }
        ;
        List<Staff> staffList = new ArrayList<>();
        Organization unit = organizationGraphRepository.findOne(unitId);
        if (unit == null) {
            logger.info("Organization is null");
            return null;
        }
        Organization parent = null;
        if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }
        try (InputStream stream = multipartFile.getInputStream()) {
            //Get the workbook instance for XLS file
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            //Get first sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(3);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new InternalError("Sheet has no more rows,we are expecting sheet at 2 position");
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (String.valueOf(row.getCell(0)) == null || String.valueOf(row.getCell(0)).isEmpty()) {
                    break;
                }
                if (row.getCell(0) == null) {
                    logger.info("No more rows");
                    if (staffList.size() != 0) {
                        break;
                    }
                }
                // Skip headers
                if (row.getRowNum() == 0) {
                    continue;
                }
                Cell cell = row.getCell(8);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                if ("14".equals(cell.getStringCellValue())) {

                    cell = row.getCell(2);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    Long externalId = (StringUtils.isBlank(cell.getStringCellValue())) ? 0 : Long.parseLong(cell.getStringCellValue());
                    StaffQueryResult staffQueryResult = (Optional.ofNullable(parent).isPresent()) ? staffGraphRepository.getStaffByExternalIdInOrganization(parent.getId(), externalId)
                            : staffGraphRepository.getStaffByExternalIdInOrganization(unitId, externalId);
                    Staff staff;
                    if (Optional.ofNullable(staffQueryResult).isPresent()) {
                        staff = staffQueryResult.getStaff();
                    } else {
                        logger.info("Creating new staff with kmd external id " + externalId + " in unit " + unit.getId());
                        staff = new Staff();
                    }
                    boolean isEmploymentExist = (staff.getId()) != null;
                    staff.setExternalId(externalId);
                    if (row.getCell(17) != null) {
                        staff.setBadgeNumber(row.getCell(17).toString());
                    }
                    staff.setFirstName(row.getCell(20).toString());
                    staff.setLastName(row.getCell(21).toString());
                    staff.setFamilyName(row.getCell(21).toString());
                    staff.setUserName(row.getCell(19).toString());
                    ContactAddress contactAddress = extractContactAddressFromRow(row);
                    if (!Optional.ofNullable(contactAddress).isPresent()) {
                        contactAddress = staffAddressService.getStaffContactAddressByOrganizationAddress(unit);
                    }
                    ContactDetail contactDetail = extractContactDetailFromRow(row);
                    if (Optional.ofNullable(staffQueryResult).isPresent()) {

                        if (Optional.ofNullable(contactDetail).isPresent()) {
                            contactDetail.setId(staffQueryResult.getContactDetailId());
                        }

                        if (Optional.ofNullable(contactAddress).isPresent()) {
                            contactAddress.setId(staffQueryResult.getContactAddressId());
                        }
                    }
                    staff.setContactDetail(contactDetail);
                    staff.setContactAddress(contactAddress);
                    cell = row.getCell(2);
                    if (Optional.ofNullable(cell).isPresent()) {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        User user = userGraphRepository.findByTimeCareExternalId(cell.getStringCellValue());
                        if (!Optional.ofNullable(user).isPresent()) {
                            user = new User();
                            user.setFirstName(row.getCell(20).toString());
                            user.setLastName(row.getCell(21).toString());
                            user.setTimeCareExternalId(cell.getStringCellValue());
                            if (Optional.ofNullable(contactDetail).isPresent() && Optional.ofNullable(contactDetail.getPrivateEmail()).isPresent()) {
                                user.setUserName(contactDetail.getPrivateEmail().toLowerCase());
                                user.setEmail(contactDetail.getPrivateEmail().toLowerCase());
                            }
                            String defaultPassword = user.getFirstName().trim() + "@kairos";
                            user.setPassword(new BCryptPasswordEncoder().encode(defaultPassword));
                        }
                        staff.setUser(user);
                    }
                    staffGraphRepository.save(staff);
                    staffList.add(staff);
                    if (!staffGraphRepository.staffAlreadyInUnit(externalId, unit.getId())) {
                        createEmployment(parent, unit, staff, accessGroupId, isEmploymentExist);
                    }
                }
            }
            return staffList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    private ContactAddress extractContactAddressFromRow(Row row) {

        Cell cell = row.getCell(24);
        if (Optional.ofNullable(cell).isPresent()) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            ZipCode zipCode = zipCodeGraphRepository.findByZipCode((StringUtils.isBlank(cell.getStringCellValue())) ? 0 : Integer.parseInt(cell.getStringCellValue()));
            if (zipCode != null) {
                ContactAddress contactAddress = new ContactAddress();
                contactAddress.setZipCode(zipCode);
                String address = row.getCell(23).getStringCellValue();
                String arr[] = address.split(",");
                String houseNumber = "";
                String fullStreetName = "";
                if (arr.length != 0) {
                    String street = arr[0];
                    String newArray[] = street.split(" ");
                    houseNumber = newArray[newArray.length - 1];
                    for (int i = 0; i < newArray.length - 1; i++) {
                        if (i == 0) {
                            fullStreetName = fullStreetName + newArray[i];
                        } else {
                            fullStreetName = fullStreetName + " " + newArray[i];
                        }
                    }
                    contactAddress.setHouseNumber(houseNumber);
                    contactAddress.setStreet1(fullStreetName);
                    contactAddress.setCity(row.getCell(25).toString());
                }
                return contactAddress;
            }
        }
        return null;
    }

    private ContactDetail extractContactDetailFromRow(Row row) {
        Cell cell = row.getCell(26);
        ContactDetail contactDetail = null;
        if (cell != null) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            String telephoneNumber = cell.getStringCellValue();
            if (!StringUtils.isBlank(telephoneNumber)) {
                contactDetail = new ContactDetail();
                contactDetail.setPrivatePhone(telephoneNumber.trim());
            }
        }
        cell = row.getCell(27);
        if (cell != null) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            String cellPhoneNumber = cell.getStringCellValue();
            if (!StringUtils.isBlank(cellPhoneNumber)) {
                if (!Optional.ofNullable(contactDetail).isPresent()) {
                    contactDetail = new ContactDetail();
                }
                contactDetail.setMobilePhone(cellPhoneNumber.trim());
            }
        }
        cell = row.getCell(28);
        if (cell != null) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            String email = cell.getStringCellValue();
            if (!StringUtils.isBlank(email)) {
                if (!Optional.ofNullable(contactDetail).isPresent()) {
                    contactDetail = new ContactDetail();
                }
                contactDetail.setPrivateEmail(email.toLowerCase());
            }
        }
        return contactDetail;
    }

    public Staff createStaff(Staff staff) {

        if (checkStaffEmailConstraint(staff)) {

            logger.info("Creating Staff.......... " + staff.getFirstName() + " " + staff.getLastName());
            logger.info("Creating User for Staff");
            User user = new User();
            user.setEmail(staff.getEmail());
            staff.setUser(userGraphRepository.save(user));
            save(staff);
            return staff;
        }
        logger.info("Not Creating Staff.......... " + staff.getFirstName() + " " + staff.getLastName());
        return null;
    }

    private boolean checkStaffEmailConstraint(Staff staff) {
        logger.info("Checking Email constraint");
        if (staff.getEmail() != null && userGraphRepository.findByEmail(staff.getEmail()) != null) {

            logger.info("Email matched !");
            return false;
        }
        return true;
    }

    public Map<String, Object> deleteNote(long staffId) {
        Staff currentStaff = staffGraphRepository.findOne(staffId);
        currentStaff.saveNotes("", "");
        staffGraphRepository.save(currentStaff);
        return currentStaff.retrieveNotes();

    }

    public List<Staff> getAllStaff() {
        return staffGraphRepository.findAll();
    }

    public Staff getByExternalId(Long externalId) {
        return staffGraphRepository.findByExternalId(externalId);
    }

    public boolean deleteStaffById(Long staffId, Long employmentId) {
        staffGraphRepository.deleteStaffEmployment(staffId, employmentId);
        staffGraphRepository.deleteStaffById(staffId);
        return staffGraphRepository.findOne(staffId) == null;

    }


    public User createCountryAdmin(User admin) {
        User user = userGraphRepository.findByEmail(admin.getEmail());
        if (user != null) {
            return null;
        }
        admin.setPassword(new BCryptPasswordEncoder().encode(admin.getPassword()));
        userGraphRepository.save(admin);
        Staff adminAsStaff = new Staff();
        adminAsStaff.setGeneralNote("Will manage the platform");
        adminAsStaff.setUser(admin);
        adminAsStaff.setFirstName(admin.getFirstName());
        adminAsStaff.setLastName(admin.getLastName());
        adminAsStaff.setActive(true);
        adminAsStaff.setEmail(admin.getEmail());
        adminAsStaff.setUserName(admin.getEmail());
        staffGraphRepository.save(adminAsStaff);

        List<Organization> organizations = organizationGraphRepository.findByOrganizationLevel(OrganizationLevel.COUNTRY);
        Organization organization = null;
        if (!organizations.isEmpty()) {
            organization = organizations.get(0);
        }
        if (organization != null) {
            Employment employment = new Employment("working as country admin", adminAsStaff);
            organization.getEmployments().add(employment);
            organizationGraphRepository.save(organization);

            AccessGroup accessGroup = accessGroupRepository.findAccessGroupByName(organization.getId(), AppConstants.COUNTRY_ADMIN);
            UnitEmployment unitEmployment = new UnitEmployment();
            unitEmployment.setOrganization(organization);
            AccessPermission accessPermission = new AccessPermission(accessGroup);
            UnitEmpAccessRelationship unitEmpAccessRelationship = new UnitEmpAccessRelationship(unitEmployment, accessPermission);
            unitEmpAccessRelationship.setEnabled(true);
            unitEmpAccessGraphRepository.save(unitEmpAccessRelationship);
            accessPageService.setPagePermissionToAdmin(accessPermission);
            employment.getUnitEmployments().add(unitEmployment);
            organization.getEmployments().add(employment);
            organizationGraphRepository.save(organization);
        } else {
            return null;
        }
        return admin;
    }


    public Staff createStaffFromPlanningWorkflow(StaffDTO data, long unitId) {
        if (data == null) {
            return null;
        }
        Staff staff = new Staff();
        staff.setFirstName(data.getFirstName());
        staff.setLastName(data.getLastName());
        staff.setCprNumber(String.valueOf(data.getCprNumber()));
        staff.setFamilyName(data.getFamilyName());
        staff.setEmployedSince(data.getEmployedSince().getTime());
        staff.setActive(data.getActive());
        staff = createStaff(staff);
        if (staff != null) {
            if (data.getTeamId() != null) {
                //TODO hardcoded unit id to removes
                boolean result = teamService.addStaffInTeam(staff.getId(), data.getTeamId(), false, unitId);
                logger.info("Assigning team to staff: " + result);
            }
            if (data.getSkills() != null) {
                List<Map<String, Object>> result = skillService.assignSkillToStaff(staff.getId(), data.getSkills(), false, unitId);
                logger.info("Assigned Number of Skills to staff: " + result.size());
            }
            taskServiceRestClient.updateTaskForStaff(staff.getId(), data.getAnonymousStaffId());
            return staff;
        }
        return null;
    }


    public Map<String, String> createStaffSchedule(long organizationId, Long unitId) throws ParseException {

        Map<String, String> workScheduleStatus = new HashMap<>();
        Map<String, String> flsCredentials = integrationService.getFLS_Credentials(unitId);
        List<Map<String, Object>> fieldStaffs = staffGraphRepository.getFieldStaff(organizationId, unitId);
        logger.debug("field staff found is" + fieldStaffs);
        Map<String, Object> staffData;

        for (Map fieldStaff : fieldStaffs) {
            staffData = (Map<String, Object>) fieldStaff.get("data");
            Map<String, Object> workScheduleMetaData = new HashMap<>();
            workScheduleMetaData.put("fmvtid", staffData.get("fmVTID"));
            workScheduleMetaData.put("fmextID", staffData.get("fmVTID"));
            workScheduleMetaData.put("type", -1); // Zero : Engineer is available for scheduling
            workScheduleMetaData.put("info", "Create Workschedule from 0500 to 1700");
            workScheduleMetaData.put("startLocation", -1);
            workScheduleMetaData.put("endLocation", -1);
            Map<String, Object> dateTimeInfo = new HashMap<>();
            dateTimeInfo.put("startDate", DateConverter.convertToDate("16/03/2017")); //Assigning Available starting from tomorrow
            dateTimeInfo.put("endDate", DateConverter.convertToDate("01/10/2019")); //till day after tomorrow
            int flsResponse = scheduler.createEngineerWorkSchedule(workScheduleMetaData, dateTimeInfo, flsCredentials);
            logger.info("Fls response after syncing work schedule:: " + flsResponse);

        }
        workScheduleStatus.put("message", "success");
        return workScheduleStatus;
    }


    public Staff createStaffFromWeb(Long unitId, StaffCreationPOJOData payload) {

        Organization unit = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(unit).isPresent()) {
            throw new DataNotFoundByIdException("Incorrect id of an organization  " + unitId);
        }
        User user = Optional.ofNullable(userGraphRepository.findByEmail(payload.getPrivateEmail().trim())).orElse(new User());
        if (staffGraphRepository.staffAlreadyInUnit(payload.getExternalId(), unit.getId())) {
            throw new DuplicateDataException("Staff already exist in organization");
        }
        setBasicDetailsOfUser(user, payload);
        Organization parent = null;
        if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }

        Staff staff = createStaffObject(parent, unit, payload);
        Client client = createClientObject(parent, unit, payload);
        boolean isEmploymentExist = (staff.getId()) != null;
        staff.setUser(user);
        staff.setClient(client);
        staffGraphRepository.save(staff);
        createEmployment(parent, unit, staff, payload.getAccessGroupId(), isEmploymentExist);
        return staff;
    }

    private void setBasicDetailsOfUser(User user, StaffCreationPOJOData staffCreationDTO) {
        user.setEmail(staffCreationDTO.getPrivateEmail());
        user.setUserName(staffCreationDTO.getPrivateEmail());
        user.setFirstName(staffCreationDTO.getFirstName());
        user.setGender(staffCreationDTO.getGender());
        user.setLastName(staffCreationDTO.getLastName());
        String defaultPassword = user.getFirstName().trim() + "@kairos";
        user.setPassword(new BCryptPasswordEncoder().encode(defaultPassword));
        if (!StringUtils.isBlank(staffCreationDTO.getCprNumber())) {
            user.setAge(Integer.valueOf(staffCreationDTO.getCprNumber().substring(staffCreationDTO.getCprNumber().length() - 1)));
        }
    }

    private Staff createStaffObject(Organization parent, Organization unit, StaffCreationPOJOData payload) {

        StaffQueryResult staffQueryResult;
        if (Optional.ofNullable(parent).isPresent()) {
            staffQueryResult = staffGraphRepository.getStaffByExternalIdInOrganization(parent.getId(), payload.getExternalId());
        } else {
            staffQueryResult = staffGraphRepository.getStaffByExternalIdInOrganization(unit.getId(), payload.getExternalId());
        }
        Staff staff;
        if (Optional.ofNullable(staffQueryResult).isPresent()) {
            staff = staffQueryResult.getStaff();
        } else {
            logger.info("Creating new staff with kmd external id " + payload.getExternalId() + " in unit " + unit.getId());
            staff = new Staff();
        }
        staff.setEmail(payload.getPrivateEmail());
        staff.setInactiveFrom(payload.getInactiveFrom());
        staff.setEmployedSince(payload.getEmployedSince());
        staff.setExternalId(payload.getExternalId());
        staff.setFirstName(payload.getFirstName());
        staff.setLastName(payload.getLastName());
        staff.setFamilyName(payload.getFamilyName());
        staff.setCprNumber(payload.getCprNumber());
        ContactAddress contactAddress = staffAddressService.getStaffContactAddressByOrganizationAddress(unit);
        staff.setContactAddress(contactAddress);
        ObjectMapper objectMapper = new ObjectMapper();
        ContactDetail contactDetail = objectMapper.convertValue(payload, ContactDetail.class);
        staff.setContactDetail(contactDetail);
        if (Optional.ofNullable(staffQueryResult).isPresent()) {
            contactAddress.setId(staffQueryResult.getContactAddressId());
            contactDetail.setId(staffQueryResult.getContactDetailId());
        }
        if (Optional.ofNullable(payload.getEngineerTypeId()).isPresent()) {
            EngineerType engineerType = engineerTypeGraphRepository.findOne(payload.getExternalId());
            staff.setEngineerType(engineerType);
        }
        return staff;
    }

    private Client createClientObject(Organization parent, Organization unit, StaffCreationPOJOData payload) {
        Client client = clientGraphRepository.findByCprNumber(payload.getCprNumber());

        if (!Optional.ofNullable(client).isPresent()) {
            client = new Client();
            client.setFirstName(payload.getFirstName());
            client.setLastName(payload.getLastName());
            client.setEmail(payload.getPrivateEmail());
            client.setCprNumber(payload.getCprNumber());
            client.setKmdNexusExternalId(payload.getExternalId().toString());
        }
        return client;
    }

    private void createEmployment(Organization organization,
                                  Organization unit, Staff staff, Long accessGroupId, boolean employmentAlreadyExist) {
        AccessGroup accessGroup = accessGroupRepository.findOne(accessGroupId);
        if (!Optional.ofNullable(accessGroup).isPresent()) {
            throw new DataNotFoundByIdException("Access group not found " + accessGroupId);
        }
        Employment employment;
        if (employmentAlreadyExist) {
            employment = (Optional.ofNullable(organization).isPresent()) ?
                    employmentGraphRepository.findEmployment(organization.getId(), staff.getId()) :
                    employmentGraphRepository.findEmployment(unit.getId(), staff.getId());
        } else {
            employment = new Employment();
        }
        employment.setName("Working as staff");
        employment.setStaff(staff);
        UnitEmployment unitEmployment = new UnitEmployment();
        unitEmployment.setOrganization(unit);
        //set permission in unit employment
        AccessPermission accessPermission = new AccessPermission(accessGroup);
        UnitEmpAccessRelationship unitEmpAccessRelationship = new UnitEmpAccessRelationship(unitEmployment, accessPermission);
        unitEmpAccessGraphRepository.save(unitEmpAccessRelationship);
        accessPageService.setPagePermissionToStaff(accessPermission, accessGroup.getId());
        employment.getUnitEmployments().add(unitEmployment);
        if (Optional.ofNullable(organization).isPresent()) {
            organization.getEmployments().add(employment);
            organizationGraphRepository.save(organization);
        } else {
            unit.getEmployments().add(employment);
            organizationGraphRepository.save(unit);
        }
        if (accessGroup.isTypeOfTaskGiver()) {
            Map<String, String> flsCredentials = integrationService.getFLS_Credentials(unit.getId());
            if (StringUtils.isBlank(flsCredentials.get("flsDefaultUrl"))) {
                throw new FlsCredentialException("Fls credentials not found");
            }
            employmentService.syncStaffInVisitour(staff, unit.getId(), flsCredentials);
        }
    }


    public Staff createStaffObject(User user, Staff staff, Long engineerTypeId, Organization unit) {
        ContactAddress contactAddress = staffAddressService.getStaffContactAddressByOrganizationAddress(unit);
        if (contactAddress != null)
            staff.setContactAddress(contactAddress);
        if (engineerTypeId != null)
            staff.setEngineerType(engineerTypeGraphRepository.findOne(engineerTypeId));
        staff.setUser(user);
        staff.setOrganizationId(unit.getId());
        staff = staffGraphRepository.save(staff);

        if (unit == null) {
            throw new InternalError("unit can't be null");
        }

        Organization parent = null;
        if (!unit.isParentOrganization() && OrganizationLevel.CITY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else if (!unit.isParentOrganization() && OrganizationLevel.COUNTRY.equals(unit.getOrganizationLevel())) {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }
        if (parent == null) {
            if (employmentGraphRepository.findEmployment(unit.getId(), staff.getId()) == null) {
                employmentGraphRepository.createEmployments(unit.getId(), Arrays.asList(staff.getId()), unit.getId());
            }
        } else {
            if (employmentGraphRepository.findEmployment(parent.getId(), staff.getId()) == null) {
                employmentGraphRepository.createEmployments(parent.getId(), Arrays.asList(staff.getId()), unit.getId());
            }
        }
        return staff;
    }

    private void updateStaffPersonalInfoInFLS(Staff staff, long unitId) {
        logger.info(":::::::::::::: Start updating personal info to FLS :::::::::::::");
        Map<String, String> flsCredentials = integrationService.getFLS_Credentials(unitId);
        Map<String, Object> engineerMetaData = new HashMap<>();
        engineerMetaData.put("fmvtid", staff.getVisitourId());
        engineerMetaData.put("fmextID", staff.getVisitourId());
        engineerMetaData.put("speedPercent", staff.getSpeedPercent());
        engineerMetaData.put("workPercent", staff.getWorkPercent());
        engineerMetaData.put("overtime", staff.getOvertime());
        engineerMetaData.put("costDay", staff.getCostDay());
        engineerMetaData.put("costCall", staff.getCostCall());
        engineerMetaData.put("costKm", staff.getCostKm());
        engineerMetaData.put("costHour", staff.getCostHour());
        engineerMetaData.put("costHourOvertime", staff.getCostHourOvertime());
        engineerMetaData.put("capacity", staff.getCapacity());
        int code = scheduler.createEngineer(engineerMetaData, flsCredentials);
        logger.info(" Status code :: " + code);
    }


    public void updateStaffFromExcel(MultipartFile multipartFile) {

        int staffUpdated = 0;

        List<Staff> staffList = new ArrayList<>();

        try (InputStream stream = multipartFile.getInputStream()) {
            //Get the workbook instance for XLS file
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            //Get first sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                throw new InternalError("Sheet has no more rows,we are expecting sheet at 2 position");
            }

            Staff staff;
            Cell cell;
            Row row;
            long staffId;
            String firstName;
            String lastName;
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                if (row.getRowNum() > 0) {
                    cell = row.getCell(0);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    staffId = Long.valueOf(cell.getStringCellValue());

                    staff = staffGraphRepository.findOne(staffId);
                    if (staff != null) {
                        cell = row.getCell(1);
                        firstName = cell.getStringCellValue();
                        cell = row.getCell(2);
                        lastName = cell.getStringCellValue();

                        staff.setFirstName(firstName);
                        staff.setLastName(lastName);
                        staffList.add(staff);
                        staffUpdated++;
                    }
                }
            }
            staffGraphRepository.save(staffList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("total staff updated  " + staffUpdated);
    }


    public Map createUnitManager(long unitId, UnitManagerDTO unitManagerDTO) {

        User user = userGraphRepository.findByEmail(unitManagerDTO.getEmail());
        Organization unit = organizationGraphRepository.findOne(unitId);
        Organization parent;
        if (unit.getOrganizationLevel().equals(OrganizationLevel.CITY)) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());
        } else {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }
        final String password = unitManagerDTO.getFirstName().trim().toLowerCase() + "@kairos";
        ObjectMapper mapper = new ObjectMapper();
        Map unitManagerDTOMap = mapper.convertValue(unitManagerDTO, Map.class);
        if (user == null) {
            logger.info("Unit manager is null..creating new user first");
            user = new User();
            user.setUserName(unitManagerDTO.getEmail());
            user.setEmail(unitManagerDTO.getEmail());
            user.setFirstName(unitManagerDTO.getFirstName().trim());
            user.setLastName(unitManagerDTO.getLastName().trim());
            user.setContactDetail(unitManagerDTO.getContactDetail());
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            userGraphRepository.save(user);
            Staff staff = createStaff(user);
            unitManagerDTOMap.put("id", staff.getId());
            employmentService.createEmploymentForUnitManager(staff, parent, unit, unitManagerDTO.getAccessGroupId());
            sendEmailToUnitManager(unitManagerDTO, password);
            return unitManagerDTOMap;
        } else {
            long organizationId = (parent == null) ? unitId : parent.getId();
            if (staffGraphRepository.countOfUnitEmployment(organizationId, unitId, user.getEmail()) == 0) {
                Staff staff = createStaff(user);
                unitManagerDTOMap.put("id", staff.getId());
                employmentService.createEmploymentForUnitManager(staff, parent, unit, unitManagerDTO.getAccessGroupId());
                userGraphRepository.save(user);
                sendEmailToUnitManager(unitManagerDTO, password);
                return unitManagerDTOMap;
            } else {
                return null;
            }
        }
    }

    private Staff createStaff(User user) {
        Staff staff = new Staff();
        staff.setEmail(user.getEmail());
        staff.setFirstName(user.getFirstName());
        staff.setLastName(user.getLastName());
        staff.setUser(user);
        staff.setContactDetail(user.getContactDetail());
        staffGraphRepository.save(staff);
        return staff;
    }

    public Map<String, Object> getUnitManager(long unitId) {
        Organization unit = organizationGraphRepository.findOne(unitId);

        Organization parent;
        if (unit.getOrganizationLevel().equals(OrganizationLevel.CITY)) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }


        List<Map<String, Object>> unitManagers;
        if (parent == null)
            unitManagers = staffGraphRepository.getUnitManagers(unitId, unitId);
        else
            unitManagers = staffGraphRepository.getUnitManagers(parent.getId(), unitId);

        List<Map<String, Object>> unitManagerList = new ArrayList<>();
        for (Map<String, Object> unitManager : unitManagers) {
            unitManagerList.add((Map<String, Object>) unitManager.get("data"));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("unitManager", unitManagerList);
        map.put("accessGroups", accessGroupRepository.getAccessGroups(unitId));
        return map;
    }


    private void sendEmailToUnitManager(UnitManagerDTO unitManagerDTO, String password) {

        String body = "Hi,\n\n" + "You are assigned as an unit manager and to get access in KairosPlanning.\n" + "Your username " + unitManagerDTO.getEmail() + " and password is " + password + "\n\n Thanks";
        String subject = "You are a unit manager at KairosPlanning";
        mailService.sendPlainMail(unitManagerDTO.getEmail(), body, subject);
    }

    public List<Staff> getUploadedStaffByOrganizationId(Long organizationId) {
        return staffGraphRepository.getUploadedStaffByOrganizationId(organizationId);
    }


    public UnitManagerDTO updateUnitManager(Long staffId, UnitManagerDTO unitManagerDTO) {

        Staff staff = staffGraphRepository.findOne(staffId);
        User user = userGraphRepository.findByEmail(unitManagerDTO.getEmail());
        staff.setFirstName(unitManagerDTO.getFirstName());
        staff.setLastName(unitManagerDTO.getLastName());
        staff.setContactDetail(unitManagerDTO.getContactDetail());
        user.setFirstName(unitManagerDTO.getFirstName().trim());
        user.setLastName(unitManagerDTO.getLastName().trim());
        user.setContactDetail(unitManagerDTO.getContactDetail());
        userGraphRepository.save(user);
        staffGraphRepository.save(staff);
        unitManagerDTO.setStaffId(staffId);
        return unitManagerDTO;

    }

    /**
     * @param unitId
     * @param staffId
     * @param date
     * @return
     * @auther anil maurya
     */
    public List<StaffTaskDTO> getAssignedTasksOfStaff(long unitId, long staffId, String date) {

        Staff staff = staffGraphRepository.getStaffByUnitId(unitId, staffId);
        if (staff == null) {
            throw new InternalError("Staff not found");
        }
        List<StaffAssignedTasksWrapper> tasks = taskServiceRestClient.getAssignedTasksOfStaff(staffId, date);
        List<Long> citizenIds = tasks.stream().map(task -> task.getId()).collect(Collectors.toList());
        List<Client> clients = clientGraphRepository.findByIdIn(citizenIds);
        ObjectMapper objectMapper = new ObjectMapper();
        StaffTaskDTO staffTaskDTO;
        List<StaffTaskDTO> staffTaskDTOS = new ArrayList<>(clients.size());
        int taskIndex = 0;
        for (Client client : clients) {
            staffTaskDTO = objectMapper.convertValue(client, StaffTaskDTO.class);
            staffTaskDTO.setTasks(tasks.get(taskIndex).getTasks());
            staffTaskDTOS.add(staffTaskDTO);
            taskIndex++;
        }
        return staffTaskDTOS;
    }


    public Map<String, Object> getTeamStaffAndStaffSkill(Long organizationId, List<Long> staffIds) {
        Map<String, Object> responseMap = new HashMap();
        List<Object> teamStaffList = new ArrayList<>();
        List<Object> staffList = new ArrayList<>();
        List<Map<String, Object>> teamStaffs = staffGraphRepository.getTeamStaffList(organizationId, staffIds);
        List<Map<String, Object>> staffs = staffGraphRepository.getSkillsOfStaffs(staffIds);
        for (Map<String, Object> map : teamStaffs) {
            Object o = map.get("data");
            teamStaffList.add(o);
        }
        for (Map<String, Object> map : staffs) {
            Object o = map.get("data");
            staffList.add(o);
        }

        responseMap.put("teamStaffList", teamStaffList);
        responseMap.put("staffs", staffList);
        return responseMap;
    }


    /**
     * @return
     * @auther anil maurya
     * this method is called from task micro service
     */
    public ClientStaffInfoDTO getStaffInfo(String loggedInUserName) {
        Staff staff = staffGraphRepository.getByUser(userGraphRepository.findByUserNameIgnoreCase(loggedInUserName).getId());
        if (staff == null) {
            throw new DataNotFoundByIdException("Staff Id is invalid");
        }
        return new ClientStaffInfoDTO(staff.getId());
    }

    public Staff getStaffById(long staffId) {
        Staff staff = staffGraphRepository.findOne(staffId, 0);
        if (staff == null) {
            logger.debug("Searching staff by id " + staffId);
            throw new DataNotFoundByIdException("Incorrect id of staff " + staffId);
        }
        return staff;
    }

    public List<Long> getCountryAdminIds(long organizationId) {
        return staffGraphRepository.getCountryAdminIds(organizationId);

    }

    public List<Long> getUnitManagerIds(long unitId) {
        Organization unit = organizationGraphRepository.findOne(unitId);

        Organization parent;
        if (unit.getOrganizationLevel().equals(OrganizationLevel.CITY)) {
            parent = organizationGraphRepository.getParentOrganizationOfCityLevel(unit.getId());

        } else {
            parent = organizationGraphRepository.getParentOfOrganization(unit.getId());
        }


        List<Long> unitManagers;
        if (parent == null)
            unitManagers = staffGraphRepository.getUnitManagersIds(unitId, unitId);
        else
            unitManagers = staffGraphRepository.getUnitManagersIds(parent.getId(), unitId);


        return unitManagers;
    }

    public List<StaffPersonalDetailDTO> getAllStaffByUnitId(long unitId) {
        Organization unit = organizationGraphRepository.findOne(unitId);
        if (!Optional.ofNullable(unit).isPresent()) {
            throw new DataNotFoundByIdException("unit  not found  Unit ID: " + unitId);
        }
        List<StaffPersonalDetailDTO> staffPersonalDetailDTOS = staffGraphRepository.getAllStaffByUnitId(unitId);
        return staffPersonalDetailDTOS;
    }

    public List<StaffPersonalDetailDTO> getStaffInfoById(long staffId, long unitId) {
        List<StaffPersonalDetailDTO> staffPersonalDetailList = staffGraphRepository.getStaffInfoById(unitId, staffId);
        if (!Optional.ofNullable(staffPersonalDetailList).isPresent()) {
            throw new DataNotFoundByIdException("Staff not found with provided Staff ID: " + staffId + " and Unit ID: " + unitId);
        }
        return staffPersonalDetailList;

    }

    public Long verifyStaffBelongsToUnit(long staffId, long id, String type) {
        Long unitId = -1L;
        unitId = organizationService.getOrganization(id, type);
        Staff staff = staffGraphRepository.getStaffByUnitId(unitId, staffId);
        if (!Optional.ofNullable(staff).isPresent()) {
            unitId = -1L;
        }
        return unitId;
    }

    public StaffFilterDTO addStaffFavouriteFilters(StaffFilterDTO staffFilterDTO) {
        /*StaffFavouriteFilters alreadyExistFilter = staffGraphRepository.getStaffFavouriteFiltersByStaffAndView(staffId, staffFilterDTO.getModuleId());
        if(Optional.ofNullable(alreadyExistFilter).isPresent()){
            throw new DuplicateDataException("StaffFavouriteFilters already exist !");
        }*/


        StaffFavouriteFilters staffFavouriteFilters = new StaffFavouriteFilters();
        Long userId = UserContext.getUserDetails().getId();
        Staff staff = staffGraphRepository.getStaffByUserId(userId);
        AccessPage accessPage = accessPageService.findByModuleId(staffFilterDTO.getModuleId());
        staffFavouriteFilters.setAccessPage(accessPage);
        staffFavouriteFilters.setFilterJson(staffFilterDTO.getFilterJson());
        staffFavouriteFilters.setName(staffFilterDTO.getName());
        save(staffFavouriteFilters);
        staff.addFavouriteFilters(staffFavouriteFilters);
        save(staff);
        staffFilterDTO.setFilterJson(staffFavouriteFilters.getFilterJson());
        staffFilterDTO.setModuleId(accessPage.getModuleId());
        staffFilterDTO.setName(staffFavouriteFilters.getName());
        staffFilterDTO.setId(staffFavouriteFilters.getId());
        return staffFilterDTO;


    }

    public StaffFilterDTO updateStaffFavouriteFilters(StaffFilterDTO staffFilterDTO) {
        Long userId = UserContext.getUserDetails().getId();
        Staff staff = staffGraphRepository.getStaffByUserId(userId);
        StaffFavouriteFilters staffFavouriteFilters = staffGraphRepository.getStaffFavouriteFiltersById(staff.getId(), staffFilterDTO.getId());
        if (!Optional.ofNullable(staffFavouriteFilters).isPresent()) {
            throw new DataNotFoundByIdException("StaffFavouriteFilters  not found  with ID: " + staffFilterDTO.getId());
        }
        /*AccessPage accessPage = accessPageService.findByModuleId(staffFilterDTO.getModuleId());
        staffFavouriteFilters.setAccessPage(accessPage);*/
        staffFavouriteFilters.setFilterJson(staffFilterDTO.getFilterJson());
        staffFavouriteFilters.setName(staffFilterDTO.getName());
        staffFavouriteFilters.setEnabled(true);
        save(staffFavouriteFilters);
        staffFilterDTO.setFilterJson(staffFavouriteFilters.getFilterJson());
        // staffFilterDTO.setModuleId(accessPage.getModuleId());
        return staffFilterDTO;

    }

    public boolean removeStaffFavouriteFilters(Long staffFavouriteFilterId) {
        Long userId = UserContext.getUserDetails().getId();
        Staff staff = staffGraphRepository.getStaffByUserId(userId);
        StaffFavouriteFilters staffFavouriteFilters = staffGraphRepository.getStaffFavouriteFiltersById(staff.getId(), staffFavouriteFilterId);
        if (!Optional.ofNullable(staffFavouriteFilters).isPresent()) {
            throw new DataNotFoundByIdException("StaffFavouriteFilters  not found  with ID: " + staffFavouriteFilterId);
        }

        staffFavouriteFilters.setEnabled(false);
        save(staffFavouriteFilters);
        return true;

    }

    public List<StaffFavouriteFilters> getStaffFavouriteFilters(String moduleId) {
        Long userId = UserContext.getUserDetails().getId();
        Staff staff = staffGraphRepository.getStaffByUserId(userId);
        return staffGraphRepository.getStaffFavouriteFiltersByStaffAndView(staff.getId(), moduleId);
    }


    /**
     * This method return Staff from given user id
     *
     * @param userId
     * @return
     */
    public Staff getStaffByUserId(Long userId) {
        return staffGraphRepository.getByUser(userId);
    }

}
