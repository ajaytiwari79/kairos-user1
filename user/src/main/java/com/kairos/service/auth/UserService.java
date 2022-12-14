package com.kairos.service.auth;

import com.kairos.commons.service.mail.KMailService;
import com.kairos.commons.service.mail.SendGridMailService;
import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.config.env.EnvConfig;
import com.kairos.config.security.CustomDefaultTokenServices;
import com.kairos.constants.AppConstants;
import com.kairos.dto.kpermissions.ModelDTO;
import com.kairos.dto.user.access_group.UserAccessRoleDTO;
import com.kairos.dto.user.access_permission.AccessGroupRole;
import com.kairos.dto.user.auth.GoogleCalenderTokenDTO;
import com.kairos.dto.user.auth.UserDetailsDTO;
import com.kairos.dto.user.country.system_setting.SystemLanguageDTO;
import com.kairos.dto.user.staff.staff.UnitWiseStaffPermissionsDTO;
import com.kairos.dto.user.user.password.FirstTimePasswordUpdateDTO;
import com.kairos.dto.user.user.password.PasswordUpdateDTO;
import com.kairos.dto.user_context.UserContext;
import com.kairos.enums.OrganizationCategory;
import com.kairos.enums.user.ChatStatus;
import com.kairos.enums.user.UserType;
import com.kairos.persistence.model.access_permission.AccessGroup;
import com.kairos.persistence.model.access_permission.AccessPage;
import com.kairos.persistence.model.access_permission.AccessPageQueryResult;
import com.kairos.persistence.model.access_permission.UserPermissionQueryResult;
import com.kairos.persistence.model.auth.User;
import com.kairos.persistence.model.client.ContactDetail;
import com.kairos.persistence.model.common.UserBaseEntity;
import com.kairos.persistence.model.organization.Organization;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.model.query_wrapper.OrganizationWrapper;
import com.kairos.persistence.model.staff.personal_details.Staff;
import com.kairos.persistence.model.system_setting.SystemLanguage;
import com.kairos.persistence.repository.system_setting.SystemLanguageGraphRepository;
import com.kairos.persistence.repository.user.access_permission.AccessPageRepository;
import com.kairos.persistence.repository.user.auth.UserGraphRepository;
import com.kairos.persistence.repository.user.staff.StaffGraphRepository;
import com.kairos.persistence.repository.user.staff.UnitPermissionGraphRepository;
import com.kairos.service.SmsService;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.access_permisson.AccessPageService;
import com.kairos.service.country.CountryService;
import com.kairos.service.exception.ExceptionService;
import com.kairos.service.integration.ActivityIntegrationService;
import com.kairos.service.kpermissions.PermissionService;
import com.kairos.service.organization.OrganizationService;
import com.kairos.service.organization.UnitService;
import com.kairos.service.redis.RedisService;
import com.kairos.utils.CPRUtil;
import com.kairos.utils.OtpGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.ObjectMapperUtils.copyPropertiesByMapper;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.AppConstants.OTP_MESSAGE;
import static com.kairos.constants.CommonConstants.*;
import static com.kairos.constants.UserMessagesConstants.*;
import static com.kairos.dto.user.access_permission.AccessGroupRole.MANAGEMENT;

/**
 * Calls UserGraphRepository to perform CRUD operation on  User
 */
@Transactional
@PropertySource("classpath:email-config.properties")
@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String DESCRIPTION = "description";

    @Inject
    private UserGraphRepository userGraphRepository;
    @Inject
    private UserDetailService userDetailsService;
    @Inject
    private StaffGraphRepository staffGraphRepository;
    @Inject
    private SmsService smsService;
    @Inject
    private AccessPageRepository accessPageRepository;
    @Inject
    private AccessGroupService accessGroupService;
    @Inject
    private ExceptionService exceptionService;
    @Inject
    private SystemLanguageGraphRepository systemLanguageGraphRepository;
    @Inject
    private SendGridMailService sendGridMailService;
    @Inject
    private ForgetPasswordTokenService forgetPasswordTokenService;
    @Inject
    private EnvConfig config;
    @Inject
    private RedisService redisService;
    @Inject
    private CountryService countryService;
    private TokenExtractor tokenExtractor = new BearerTokenExtractor();
    @Inject
    private TokenStore tokenStore;
    @Inject
    private UnitPermissionGraphRepository unitPermissionGraphRepository;
    @Inject
    private OrganizationService organizationService;
    @Inject private UnitService unitService;
    @Inject private PermissionService permissionService;
    @Inject
    private  UserGraphRepository userRepository;

    @Inject
    private AccessPageService accessPageService;

    @Inject
    private KMailService kMailService;
    @Inject
    private ActivityIntegrationService activityIntegrationService;

    private CustomDefaultTokenServices customDefaultTokenServices;

    public void setCustomDefaultTokenServices(CustomDefaultTokenServices customDefaultTokenServices) {
        this.customDefaultTokenServices = customDefaultTokenServices;
    }

    /**
     * Calls UserGraphRepository,
     * creates a new user as provided in method argument
     *
     * @param user
     * @return User
     */
    public User createUser(User user) {
        user.setUserType(UserType.USER_ACCOUNT);
        return userGraphRepository.save(user);
    }


    /**
     * Calls UserGraphRepository and finds User by id given in method argument
     *
     * @param id
     * @return User
     */
    public User getUserById(Long id) {
        return userGraphRepository.findOne(id);
    }


    /**
     * Calls UserGraphRepository and delete user by id given in method argument
     *
     * @param id
     */
    public void deleteUserById(Long id) {
        userGraphRepository.deleteById(id);
    }


    /**
     * Calls UserGraphRepository , find User by id as provided in method argument
     * and return updated User
     *
     * @param user
     * @return User
     */
    public User updateUser(User user) {
        User currentUser = userGraphRepository.findOne(user.getId());
        if (currentUser != null) {
            currentUser = user;
        }
        return userGraphRepository.save(currentUser);
    }


    /**
     * Calls UserGraphRepository and return the list of all User
     *
     * @return List of User- All user from db
     */
    public List<User> getAllUsers() {
        return userGraphRepository.findAll();
    }


    /**
     * Calls UserGraphRepository and find User by name provided in method argument.
     *
     * @param name
     * @return User
     */
    public User getUserByName(String name) {
        return userGraphRepository.findByUserNameIgnoreCase(name);
    }

    public User findOne(Long id) {
        return userGraphRepository.findOne(id, 0);
    }

    /**
     * Calls UserGraphRepository and Check if User with combination of username & password exists.
     *
     * @param user
     * @return User
     */
    public Map<String, Object> authenticateUser(User user) {
        User currentUser = userDetailsService.loadUserByUserName(user.getUserName(), user.getPassword());
        if (!Optional.ofNullable(currentUser).isPresent()) {
            return null;
        }
        int otp = OtpGenerator.generateOtp();
        currentUser.setOtp(otp);
        userGraphRepository.save(currentUser);
        Map<String, Object> map = new HashMap<>();
        map.put("email", currentUser.getEmail());
        map.put("userNameUpdated",currentUser.isUserNameUpdated());
        map.put("otp", otp);
        map.put("userName", currentUser.getUserName());
        return map;
    }

    public User findByForgotPasswordToken(String token) {
        return userGraphRepository.findByForgotPasswordToken(token);
    }


    public boolean logout(boolean logoutFromAllMachine, HttpServletRequest request) {
        boolean logoutSuccessfull = false;
        Authentication authentication = tokenExtractor.extract(request);
        if (authentication != null) {
            OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication((String) authentication.getPrincipal());
            if (logoutFromAllMachine) {
                redisService.invalidateAllTokenOfUser(oAuth2Authentication.getUserAuthentication().getName());
            } else {
                redisService.removeUserTokenFromRedisByUserNameAndToken(oAuth2Authentication.getUserAuthentication().getName(),(String) authentication.getPrincipal());
            }
            tokenStore.removeAccessToken(tokenStore.getAccessToken(oAuth2Authentication));
            SecurityContextHolder.clearContext();
            updateChatStatus(ChatStatus.OFFLINE);
            logoutSuccessfull = true;
        } else {
            exceptionService.internalServerError("message.authentication.null");
        }
        return logoutSuccessfull;

    }

    public List<OrganizationWrapper> getOrganizations(long userId) {
        return userGraphRepository.getOrganizations(userId);
    }

    /**
     * @param moduleId       ,some of access page which will be treated as main module like visitator,citizen
     * @param organizationId
     * @param userId
     * @return
     * @author prabjot
     */
    public List<Map<String, Object>> getPermissionForModuleInOrganization(long moduleId, long organizationId, long userId) {
        List<Map<String, Object>> map = userGraphRepository.getPermissionForModuleInOrganization(moduleId, organizationId, userId);

        List<Map<String, Object>> response = new ArrayList<>();
        for (Map<String, Object> result : map) {
            response.add((Map<String, Object>) result.get("result"));
        }
        return response;
    }


    public boolean sendOtp(String email) {

        User user = userGraphRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        int otp = OtpGenerator.generateOtp();
        user.setOtp(otp);
        userGraphRepository.save(user);

        //send otp in sms
        String message = OTP_MESSAGE + otp;
        ContactDetail contactDetail = user.getContactDetail();
        if (contactDetail != null && (contactDetail.getMobilePhone() != null || !contactDetail.getMobilePhone().isEmpty())) {
            smsService.sendSms(user.getContactDetail().getMobilePhone(), message);
            return true;
        } else {
            exceptionService.dataNotFoundByIdException(MESSAGE_USER_MOBILENUMBER_NOTFOUND);
        }
        return false;
    }

    public Map<String, Object> verifyOtp(int otp, String email) {
        LOGGER.info("OTP::" + email);
        User currentUser = userGraphRepository.findByEmail(email);
        if (currentUser == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", currentUser.getId());
        map.put("userName", currentUser.getUserName());
        map.put("email", currentUser.getEmail());
        /*map.put(ACCESS_TOKEN, currentUser.getAccessToken());*/
        if (currentUser.getCountryList() != null) {
            map.put("countryId", currentUser.getCountryList().get(0).getId());
        }
        map.put("age", currentUser.getAge());
        map.put("name", currentUser.getFirstName());
        map.put("organizations", getOrganizations(currentUser.getId()));

        return map;
    }

    /**
     * Calls UserGraphRepository and Check if User with combination of username & password exists.
     *
     * @param user
     * @return User
     */
    public Map<String, Object> authenticateUserFromMobileApi(User user) {

        User currentUser = userDetailsService.loadUserByEmail(user.getUserName(), user.getPassword());
        if (currentUser == null) {
            return null;
        }
        Unit org = staffGraphRepository.getStaffOrganization(currentUser.getId());
        if (org == null) {
            exceptionService.dataNotFoundByIdException(MESSAGE_ORGANISATION_NOTFOUND);

        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", currentUser.getId());
        map.put("name", currentUser.getFirstName());
        map.put("appId", org.getEstimoteAppId());
        map.put("appToken", org.getEstimoteAppToken());
        map.put("organization", org.getId());

        return map;

    }

    /**
     * Calls UserGraphRepository and Check if User with combination of username & password exists.
     *
     * @param mbNumber
     * @return User
     */
    public Map<String, Object> authenticateUserFromMobileNumber(String mbNumber) {
        List<User> userList = staffGraphRepository.getStaffByMobileNumber(mbNumber);
        if (userList != null && userList.size() == 1) {
            User currentUser = userList.get(0);
            if (currentUser == null) {
                return null;
            }
            Unit org = staffGraphRepository.getStaffOrganization(currentUser.getId());
            if (org == null) {
                exceptionService.dataNotFoundByIdException(MESSAGE_ORGANISATION_NOTFOUND);

            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", currentUser.getId());
            map.put("name", currentUser.getFirstName());
            map.put("organization", org.getId());
            map.put("appId", org.getEstimoteAppId());
            map.put("appToken", org.getEstimoteAppToken());

            return map;
        }

        return null;

    }

    public boolean updatePassword(FirstTimePasswordUpdateDTO firstTimePasswordUpdateDTO) {
        User user = userGraphRepository.findByEmail("(?i)" + firstTimePasswordUpdateDTO.getEmail());
        if (user == null) {
            LOGGER.error("User not found belongs to this email {}" , firstTimePasswordUpdateDTO.getEmail());
            exceptionService.dataNotFoundByIdException(MESSAGE_USER_EMAIL_NOTFOUND, firstTimePasswordUpdateDTO.getEmail());
        }
        if(userGraphRepository.existByUserName("(?i)" + firstTimePasswordUpdateDTO.getUserName(),"(?i)" + firstTimePasswordUpdateDTO.getEmail())){
            exceptionService.actionNotPermittedException(MESSAGE_USER_USERNAME_ALREADY_USE);
        }
        CharSequence password = CharBuffer.wrap(firstTimePasswordUpdateDTO.getRepeatPassword());
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setPasswordUpdated(true);
        user.setUserName(firstTimePasswordUpdateDTO.getUserName());
        user.setUserNameUpdated(true);
        userGraphRepository.save(user);
        return true;
    }


    public Map<String, AccessPageQueryResult> prepareUnitPermissions(List<AccessPageQueryResult> accessPageQueryResults, boolean parentOrganization) {
        List<AccessPage> accessPagesDetails = accessPageRepository.findAllById(accessPageQueryResults.stream().map(accessPageQueryResult -> accessPageQueryResult.getId()).collect(Collectors.toList()));
        Map<Long,List<Long>> accessPageIdAndChildrenId = accessPagesDetails.stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getSubPages().stream().map(accessPage -> accessPage.getId()).collect(Collectors.toList())));
        Map<Long,AccessPage> accessPageIdAndAccessPageMap = accessPagesDetails.stream().collect(Collectors.toMap(k->k.getId(),v->v));
        Map<String, AccessPageQueryResult> unitPermissionMap = new HashMap<>();
        for (AccessPageQueryResult permission : accessPageQueryResults) {
            if (unitPermissionMap.containsKey(permission.getModuleId()) && parentOrganization) {
                AccessPageQueryResult existingPermission = unitPermissionMap.get(permission.getModuleId());
                existingPermission.setRead(existingPermission.isRead() || isAccessPageRead(permission, accessPageQueryResults, accessPageIdAndChildrenId));
                existingPermission.setWrite(existingPermission.isWrite() || isAccessPageWrite(permission, accessPageQueryResults, accessPageIdAndChildrenId));
                existingPermission.setActive(existingPermission.isRead() || existingPermission.isWrite());
                unitPermissionMap.put(permission.getModuleId(), existingPermission);
            } else {
                permission.setRead(isAccessPageRead(permission, accessPageQueryResults, accessPageIdAndChildrenId));
                permission.setWrite(isAccessPageWrite(permission, accessPageQueryResults, accessPageIdAndChildrenId));
                permission.setActive(permission.isRead() || permission.isWrite());
                permission.setTranslatedNames(accessPageIdAndAccessPageMap.get(permission.getId()).getTranslatedNames());
                unitPermissionMap.put(permission.getModuleId(), permission);
            }
        }
        return unitPermissionMap;
    }

    private boolean isAccessPageRead(AccessPageQueryResult currentAccessPage, List<AccessPageQueryResult> accessPages, Map<Long,List<Long>> accessPageIdAndChildrenId){
        boolean read = currentAccessPage.isRead();
        if(!read) {
            List<AccessPageQueryResult> childAccessPages = new ArrayList<>();
            if (isCollectionNotEmpty(accessPageIdAndChildrenId.get(currentAccessPage.getId()))) {
                childAccessPages = accessPages.stream().filter(accessPageQueryResult -> accessPageIdAndChildrenId.get(currentAccessPage.getId()).contains(accessPageQueryResult.getId())).collect(Collectors.toList());
            }
            for (AccessPageQueryResult childAccessPage : childAccessPages) {
                read = isAccessPageRead(childAccessPage, accessPages, accessPageIdAndChildrenId);
                if (read) {
                    break;
                }
            }
        }
        return read;
    }

    private boolean isAccessPageWrite(AccessPageQueryResult currentAccessPage, List<AccessPageQueryResult> accessPages, Map<Long,List<Long>> accessPageIdAndChildrenId){
        boolean write = currentAccessPage.isWrite();
        if(write){
            return true;
        }
        List<AccessPageQueryResult> childAccessPages = new ArrayList<>();
        if(isCollectionNotEmpty(accessPageIdAndChildrenId.get(currentAccessPage.getId()))) {
            childAccessPages = accessPages.stream().filter(accessPageQueryResult -> accessPageIdAndChildrenId.get(currentAccessPage.getId()).contains(accessPageQueryResult.getId())).collect(Collectors.toList());
        }
        for (AccessPageQueryResult childAccessPage : childAccessPages) {
            write = isAccessPageWrite(childAccessPage, accessPages, accessPageIdAndChildrenId);
            if (write) {
                break;
            }
        }
        return write;
    }

    //@Cacheable(value = "getPermission", key = "{#unitId, #userId}", cacheManager = "cacheManager")
    public UnitWiseStaffPermissionsDTO getPermission(Long unitId, Long userId) {
        UserAccessRoleDTO userAccessRoleDTO = accessGroupService.findUserAccessRole(unitId);
        UnitWiseStaffPermissionsDTO permissionData = new UnitWiseStaffPermissionsDTO();
        permissionData.setHub(accessPageRepository.isHubMember(userId));
        Set<Long> unitAccessGroupIds=new HashSet<>();
        if(UserContext.getUserDetails().isSystemAdmin()){
            List<AccessPageQueryResult> permissions = accessPageRepository.fetchHubSystemAdminPermissions();
            preparePermission(permissionData, permissions);
        }
        else if (permissionData.isHub()) {
             Organization parentHub = accessPageRepository.fetchParentHub(userId);
             unitAccessGroupIds=parentHub.getId().equals(unitId)?parentHub.getAccessGroups().stream().map(UserBaseEntity::getId).collect(Collectors.toSet()):accessGroupService.getAccessGroupIdsOfUnit(unitId);
            //List<AccessGroupQueryResult> accessGroupQueryResults = accessGroupService.getCountryAccessGroupByOrgCategory(UserContext.getUserDetails().getCountryId(), OrganizationCategory.HUB.toString());
            Set<Long> accessGroupIds = parentHub.getAccessGroups().stream().map(UserBaseEntity::getId).collect(Collectors.toSet());
            List<AccessPageQueryResult> permissions = accessPageRepository.fetchHubUserPermissions(userId, parentHub.getId(), accessGroupIds,accessGroupIds.equals(unitAccessGroupIds)?new HashSet<>():unitAccessGroupIds);
            preparePermission(permissionData, permissions);

        } else {
            loadUnitPermissions(unitId, userId, permissionData);
        }
        permissionData.setRole((userAccessRoleDTO.isManagement()) ? MANAGEMENT : AccessGroupRole.STAFF);

        permissionData.setModelPermissions(ObjectMapperUtils.copyCollectionPropertiesByMapper(permissionService.getModelPermission(new ArrayList<>(), userAccessRoleDTO.getAccessGroupIds(), UserContext.getUserDetails().isSystemAdmin(),userAccessRoleDTO.getStaffId(),unitAccessGroupIds), ModelDTO.class));
        Organization parent = organizationService.fetchParentOrganization(unitId);
        permissionData.setStaffId(staffGraphRepository.getStaffIdByUserId(userId,parent.getId()));
        updateChatStatus(ChatStatus.ONLINE);
        return permissionData;
    }

    private void preparePermission(UnitWiseStaffPermissionsDTO permissionData, List<AccessPageQueryResult> permissions) {
        Map<String, AccessPageQueryResult> permissionMap = prepareUnitPermissions(permissions,true);
        HashMap<String, Object> unitPermissionMap = new HashMap<>();
        for (AccessPageQueryResult permission : permissions) {
            unitPermissionMap.put(permission.getModuleId(), permissionMap.get(permission.getModuleId()));
        }
        permissionData.setHubPermissions(unitPermissionMap);
    }

    private void loadUnitPermissions(Long organizationId, long currentUserId, UnitWiseStaffPermissionsDTO permissionData) {
        List<UserPermissionQueryResult> unitWisePermissions;
        Long countryId = UserContext.getUserDetails().getCountryId();
        Set<BigInteger> dayTypeIds = activityIntegrationService.getApplicableDayTypes(countryId);
        boolean checkDayType = true;
        List<AccessGroup> accessGroups = accessPageRepository.fetchAccessGroupsOfStaffPermission(currentUserId);
        for (AccessGroup currentAccessGroup : accessGroups) {
            if (!currentAccessGroup.isAllowedDayTypes()) {
                checkDayType = false;
                break;
            }
        }
        if (checkDayType) {
            unitWisePermissions = accessPageRepository.fetchStaffPermissionsWithDayTypes(currentUserId, dayTypeIds.stream().map(BigInteger::toString).collect(Collectors.toSet()), organizationId);
        } else {
            unitWisePermissions = accessPageRepository.fetchStaffPermissions(currentUserId, organizationId);
        }
        HashMap<Long, Object> unitPermission = new HashMap<>();
        for (UserPermissionQueryResult userPermissionQueryResult : unitWisePermissions) {
            unitPermission.put(userPermissionQueryResult.getUnitId(),
                    prepareUnitPermissions(ObjectMapperUtils.copyCollectionPropertiesByMapper(userPermissionQueryResult.getPermission(), AccessPageQueryResult.class), userPermissionQueryResult.isParentOrganization()));
        }
        permissionData.setOrganizationPermissions(unitPermission);
    }


    public void updateLastSelectedOrganizationIdAndCountryId(Long organizationId) {
        User currentUser = userGraphRepository.findOne(UserContext.getUserDetails().getId());
        if (!organizationId.equals(currentUser.getLastSelectedOrganizationId())) {
            OrganizationCategory organizationCategory = organizationService.getOrganisationCategory(organizationId);
            currentUser.setLastSelectedOrganizationId(organizationId);
            currentUser.setLastSelectedOrganizationCategory(organizationCategory);
        }
        if(!currentUser.getUnitWiseAccessRole().containsKey(organizationId.toString())){
            Staff staff=staffGraphRepository.getStaffByUserId(currentUser.getId(),organizationService.fetchParentOrganization(organizationId).getId());
            Long staffId=staff==null?staffGraphRepository.findHubStaffIdByUserId(UserContext.getUserDetails().getId(),organizationService.fetchParentOrganization(organizationId).getId()):staff.getId();
            boolean onlyStaff=unitPermissionGraphRepository.isOnlyStaff(organizationId,staffId);
            currentUser.getUnitWiseAccessRole().put(organizationId.toString(),staff==null||!onlyStaff?MANAGEMENT.name():AccessGroupRole.STAFF.name());
        }
        if(isNull(currentUser.getCountryId())){
            Long countryId=countryService.getCountryIdByUnitId(organizationId);
            currentUser.setCountryId(countryId);
        }
        userGraphRepository.save(currentUser);
    }


    public boolean updateDateOfBirthOfUserByCPRNumber() {
        List<User> users = userGraphRepository.findAll();
        users.forEach(user -> {
            user.setDateOfBirth(Optional.ofNullable(user.getCprNumber()).isPresent() ?
                    CPRUtil.fetchDateOfBirthFromCPR(user.getCprNumber()) : null);
        });
        userGraphRepository.saveAll(users);
        return true;
    }

    public String updateSelectedLanguageOfUser(Long userLanguageId) {
        User currentUser = userGraphRepository.findOne(UserContext.getUserDetails().getId());
        userGraphRepository.updateUserSystemLanguage(currentUser.getId(),userLanguageId);
        UserContext.getUserDetails().setUserLanguage(copyPropertiesByMapper(currentUser.getUserLanguage(),SystemLanguageDTO.class));
        return getUpdatedUserToken(currentUser);
    }

    public Long getUserSelectedLanguageId(Long userId) {
        return userGraphRepository.getUserSelectedLanguageId(userId);
    }

    public boolean forgotPassword(String userEmail) {
        if (userEmail.endsWith("kairos.com") || userEmail.endsWith("kairosplanning.com")) {
            LOGGER.error("Currently email ends with kairos.com or kairosplanning.com are not valid " + userEmail);
            exceptionService.dataNotFoundByIdException(MESSAGE_USER_MAIL_INVALID, userEmail);
        }
        User currentUser = userGraphRepository.findByEmail("(?i)" + userEmail);
        if (!Optional.ofNullable(currentUser).isPresent()) {
            LOGGER.error("No User found by email " + userEmail);
            currentUser = userGraphRepository.findUserByUserName("(?i)" + userEmail);
            if (!Optional.ofNullable(currentUser).isPresent()) {
                LOGGER.error("No User found by userName " + userEmail);
                exceptionService.dataNotFoundByIdException(MESSAGE_USER_USERNAME_NOTFOUND, userEmail);
            }
        }

            String token = forgetPasswordTokenService.createForgotPasswordToken(currentUser);
            Map<String, Object> templateParam = new HashMap<>();
            templateParam.put("receiverName", EMAIL_GREETING + currentUser.getFullName());
            templateParam.put(DESCRIPTION, AppConstants.MAIL_BODY.replace("{0}", StringUtils.capitalize(currentUser.getFirstName())));
            templateParam.put("hyperLink", config.getForgotPasswordApiLink() + token);
            templateParam.put("hyperLinkName", RESET_PASSCODE);
//            sendGridMailService.sendMailWithSendGrid(DEFAULT_EMAIL_TEMPLATE, templateParam, null, AppConstants.MAIL_SUBJECT, currentUser.getEmail());
            kMailService.sendMail(null,AppConstants.MAIL_SUBJECT,templateParam.get(DESCRIPTION).toString(),templateParam,DEFAULT_EMAIL_TEMPLATE,currentUser.getEmail());
            return true;
        }


    public boolean resetPassword(String token, PasswordUpdateDTO passwordUpdateDTO) {
        if (!passwordUpdateDTO.isValid()) {
            exceptionService.actionNotPermittedException(MESSAGE_STAFF_USER_PASSCODE_NOTMATCH);
        }
        User user = findByForgotPasswordToken(token);
        if (!Optional.ofNullable(user).isPresent()) {
            LOGGER.error("No User found by token");
            exceptionService.dataNotFoundByIdException(MESSAGE_USER_TOKEN_NOTFOUND);
        }
        //We are validating password reset token for 2 hours.
        DateTimeInterval interval = new DateTimeInterval(DateUtils.asDate(user.getForgotTokenRequestTime()), DateUtils.asDate(user.getForgotTokenRequestTime().plusHours(2)));
        if (!interval.contains(DateUtils.asDate(DateUtils.getCurrentLocalDateTime()))) {
            LOGGER.error("Password reset token expired");
            exceptionService.dataNotFoundByIdException(MESSAGE_USER_TOKEN_EXPIRED);
        }
        CharSequence password = CharBuffer.wrap(passwordUpdateDTO.getConfirmPassword());
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setForgotPasswordToken(null);
        userGraphRepository.save(user);
        return true;
    }

    public boolean updateUserName(UserDetailsDTO userDetailsDTO) {
        User user = userGraphRepository.findByEmail("(?i)" + userDetailsDTO.getEmail());
        if (isNull(user)) {
            LOGGER.error("User not found belongs to this email " + userDetailsDTO.getEmail());
            exceptionService.dataNotFoundByIdException(MESSAGE_USER_EMAIL_NOTFOUND, userDetailsDTO.getEmail());
        } else {
            if (user.getUserName().equalsIgnoreCase(userDetailsDTO.getUserName())) {
                user.setUserNameUpdated(true);
                userGraphRepository.save(user);
                return true;
            }
            User userNameAlreadyExist = userGraphRepository.findUserByUserName("(?i)" + userDetailsDTO.getUserName());
            if (ObjectUtils.isNotNull(userNameAlreadyExist)) {
                LOGGER.error("This userName is already in use " + userDetailsDTO.getUserName());
                exceptionService.dataNotFoundByIdException("message.user.userName.already.use", userDetailsDTO.getUserName());
            }
            user.setUserNameUpdated(true);
            user.setUserName(userDetailsDTO.getUserName());
            userGraphRepository.save(user);
            return true;
        }
        return false;
    }

    public GoogleCalenderTokenDTO updateGoogleCalenderToken(GoogleCalenderTokenDTO googleCalenderTokenDTO) {
        User currentUser = userGraphRepository.findOne(UserContext.getUserDetails().getId());
        currentUser.setGoogleCalenderTokenId(googleCalenderTokenDTO.getGoogleCalenderTokenId());
        currentUser.setGoogleCalenderAccessToken(googleCalenderTokenDTO.getGoogleCalenderAccessToken());
        userGraphRepository.save(currentUser);
        return googleCalenderTokenDTO;
    }

    public String updateAccessRoleOfUser(Long unitId, String accessGroupRole){
        User currentUser = userGraphRepository.findOne(UserContext.getUserDetails().getId());
        currentUser.getUnitWiseAccessRole().put(String.valueOf(unitId),accessGroupRole);
        userGraphRepository.save(currentUser);
        return getUpdatedUserToken(currentUser);
    }

    public Map<String,String> getUnitWiseLastSelectedAccessRole(){
        Map<String,String> unitWiseAccessRole= userGraphRepository.findOne(UserContext.getUserDetails().getId()).getUnitWiseAccessRole();
        Map<String,String> unitWiseAccessRoleMap=new HashMap<>(unitWiseAccessRole.size());
        unitWiseAccessRole.forEach((k,v)-> unitWiseAccessRoleMap.put(k,v.toUpperCase()));
        return unitWiseAccessRoleMap;
    }

    public boolean updateChatStatus(ChatStatus chatStatus){
        User user = userGraphRepository.updateChatStatusByUserId(UserContext.getUserDetails().getId(),chatStatus);
        return true;
    }

    public User getCurrentUser(){
        User user = null;
        if(isNotNull(UserContext.getUserDetails())) {
            user = userRepository.findOne(UserContext.getUserDetails().getId());
            getUpdatedUserToken(user);
        }
        return user;
    }

    private String getUpdatedUserToken(User user) {
        user.setHubMember(accessPageService.isHubMember(user.getId()));
        user.setSystemAdmin(userGraphRepository.isSystemAdmin(user.getId()));
        SystemLanguage systemLanguage = userGraphRepository.getUserSystemLanguage(user.getId());
        if(isNull(systemLanguage)){
            systemLanguage = new SystemLanguage("English","en",true,true);
        }
        user.setUserLanguage(systemLanguage);
        return customDefaultTokenServices.updateToken(UserContext.getAuthToken(),user);
    }


}
