package com.kairos.controller.user;

import com.kairos.dto.user.auth.GoogleCalenderTokenDTO;
import com.kairos.dto.user.user.password.PasswordUpdateDTO;
import com.kairos.enums.user.ChatStatus;
import com.kairos.persistence.model.auth.User;
import com.kairos.service.access_permisson.AccessGroupService;
import com.kairos.service.auth.UserService;
import com.kairos.service.employment.EmploymentService;
import com.kairos.service.redis.RedisService;
import com.kairos.service.staff.StaffRetrievalService;
import com.kairos.service.staff.StaffService;
import com.kairos.service.staff.UserSickService;
import com.kairos.utils.response.ResponseHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.kairos.constants.ApiConstants.API_V1;
import static com.kairos.constants.ApiConstants.UNIT_URL;


/**
 * UserController
 * 1. Calls LocationService
 * 2. Call for  CRUD operation on User using UserService.
 **/

@RestController
@RequestMapping(API_V1)
@Api(value = API_V1)
public class UserController {
    @Inject
    private UserService userService;

    @Inject
    private StaffService staffService;

    @Inject
    private UserSickService userSickService;
    @Inject
    private AccessGroupService accessGroupService;
    @Inject private StaffRetrievalService staffRetrievalService;
    @Inject private EmploymentService employmentService;
    @Inject private RedisService redisService;

   /* @Inject
    TaskReportService taskReportService;*/
    /**
     * @return List of Users- All Users in db
     */
    @ApiOperation(value = "Get all Users")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    /**
     * find a user by id given in URL and return if found
     *
     * @param id
     * @return User
     */
    @ApiOperation(value = "Get User by Id")
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    User getUserById(@PathVariable Long id) {

        return userService.getUserById(id);
    }


    /**
     * Creates a New User and return it.
     *
     * @param user
     * @return User
     */
    @ApiOperation(value = "Create a New User")
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    User addUser(@Validated @RequestBody User user) {
        return userService.createUser(user);

    }


    /**
     * find a user by id given in URL
     * and updates it's properties provided in request body and return the updates User
     *
     * @param user
     * @return User
     */
    @ApiOperation(value = "Update a User by Id")
    @RequestMapping(value = "/user", method = RequestMethod.PUT)
    User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }


    /**
     * deletes a User by id given in URL
     *
     * @param id
     */
    @ApiOperation(value = "Delete User by Id")
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
    void deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @RequestMapping(value = "/user/{userId}/parent/organizations", method = RequestMethod.GET)
    @ApiOperation("get organizations of user")
    ResponseEntity<Map<String, Object>> getOrganizations(@PathVariable long userId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.getOrganizations(userId));
    }

    @RequestMapping(value = "/user/{userId}/organization/{orgId}/module/{moduleId}/access_permissions", method = RequestMethod.GET)
    @ApiOperation("get organizations of user")
    ResponseEntity<Map<String, Object>> getPermissionForModuleInOrganization(@PathVariable long userId, @PathVariable long orgId, @PathVariable long moduleId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.getPermissionForModuleInOrganization(moduleId,orgId, userId));
    }


    // Temporary to update User's date of birth by CPR Number
    @ApiOperation(value = "Update a User DOB")
    @RequestMapping(value = "/user/dob_by_cpr", method = RequestMethod.PUT)
    ResponseEntity<Map<String, Object>> updateDateOfBirthOfUserByCPRNumber() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.updateDateOfBirthOfUserByCPRNumber());
    }

    @ApiOperation(value = "Update a User default Language")
    @RequestMapping(value = "/user_language/{languageId}", method = RequestMethod.PUT)
    ResponseEntity<Map<String, Object>> updateSystemLanguageOfUser(@PathVariable long languageId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.updateSelectedLanguageOfUser(languageId));
    }

    @GetMapping(value = "/user/{userId}/staffs")
    @ApiOperation("get staff ids by userid")
    ResponseEntity<Map<String, Object>> getStaffIdsAndReasonCodeByUserId(@PathVariable long userId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffRetrievalService.getStaffDetailsByUserId(userId));
    }

    @GetMapping(value = "/user/{userId}/staff_unit_mapping")
    @ApiOperation("get staff ids and unit ids by userid")
    ResponseEntity<Map<String, Object>> getStaffIdsUnitByUserId(@PathVariable long userId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffRetrievalService.getStaffIdsUnitByUserId(userId));
    }

    @GetMapping(value = UNIT_URL+"/user/staffId")
    @ApiOperation("get staffId by userId")
    // @PreAuthorize("@customPermissionEvaluator.isAuthorized()")
    public ResponseEntity<Map<String, Object>> getStaffIdOfLoggedInUser(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffRetrievalService.getStaffIdOfLoggedInUser(unitId));
    }


    @GetMapping(value = UNIT_URL+"/staff/user/accessgroup")
    @ApiOperation("get accessgroup ids and iscountryadmin")
    public ResponseEntity<Map<String, Object>> getAccessGroupIdsOfStaffs(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffRetrievalService.getAccessGroupIdsOfStaff(unitId));
    }


    @GetMapping(value = "/user/{userId}/unit_sick_settings")
    @ApiOperation("get staff ans sick activities of a user")
    ResponseEntity<Map<String, Object>> getStaffAndUnitSickSettings(@PathVariable long userId,@RequestParam String sickSettingsRequired) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userSickService.getStaffAndUnitSickSettings(userId,sickSettingsRequired));
    }


    @GetMapping(value = UNIT_URL+"/staff/access_groups")
    @ApiOperation("get accessgroup ids and iscountryadmin")
    public ResponseEntity<Map<String, Object>> getAccessGroupIdsOfStaff(@PathVariable Long unitId) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, accessGroupService.getAccessGroupIdsByStaffIdAndUnitId(unitId));

    }

    @RequestMapping(value = UNIT_URL+"/update_password", method = RequestMethod.PUT)
    @ApiOperation("update password")
    public ResponseEntity<Map<String, Object>> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, staffService.updatePassword(passwordUpdateDTO));
    }

    @PutMapping(UNIT_URL+"/updateGoogleCalenderToken")
    @ApiOperation("update google calender token of log")
    public ResponseEntity<Map<String, Object>> updateGoogleCalenderToken(@RequestBody GoogleCalenderTokenDTO googleCalenderTokenDTO) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.updateGoogleCalenderToken(googleCalenderTokenDTO));
    }


    @ApiOperation(value = "get all staff main employments ")
    @GetMapping(value = "/staffs/main_employments")
    public ResponseEntity<Map<String, Object>> getStaffsMainEmployments()  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, employmentService.getMainEmploymentOfStaffs());
    }

    @PutMapping(UNIT_URL+"/update_access_role")
    @ApiOperation("update access_role")
    public ResponseEntity<Map<String, Object>> updateAccessRole(@PathVariable Long unitId, @RequestBody String accessGroupRole) {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.updateAccessRoleOfUser(unitId,accessGroupRole));
    }

    @GetMapping("/get_unit_wise_access_role")
    @ApiOperation("get access_role")
    public ResponseEntity<Map<String, Object>> getUnitWiseLastSelectedAccessRole() {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.getUnitWiseLastSelectedAccessRole());
    }

    @ApiOperation("Update User Chat Status ")
    @PutMapping("/user/chat_status")
    public ResponseEntity<Map<String, Object>> updateChatStatus(@RequestParam ChatStatus chatStatus)  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.updateChatStatus(chatStatus));
    }

    @ApiOperation("Get Current User")
    @GetMapping("/get_current_user")
    public ResponseEntity<Map<String, Object>> getCurrentUser()  {
        return ResponseHandler.generateResponse(HttpStatus.OK, true, userService.getCurrentUser());
    }


}
