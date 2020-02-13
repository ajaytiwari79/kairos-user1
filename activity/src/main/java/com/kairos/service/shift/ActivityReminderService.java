package com.kairos.service.shift;

import com.kairos.commons.service.mail.SendGridMailService;
import com.kairos.config.env.EnvConfig;
import com.kairos.dto.activity.wta.IntervalBalance;
import com.kairos.dto.activity.wta.WorkTimeAgreementBalance;
import com.kairos.enums.wta.WTATemplateType;
import com.kairos.persistence.model.activity.Activity;
import com.kairos.persistence.model.staff.personal_details.StaffPersonalDetail;
import com.kairos.persistence.model.staff_settings.StaffActivitySetting;
import com.kairos.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.persistence.repository.staff_settings.StaffActivitySettingRepository;
import com.kairos.rest_client.UserIntegrationService;
import com.kairos.service.wta.WorkTimeAgreementBalancesCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.kairos.commons.utils.DateUtils.*;
import static com.kairos.commons.utils.ObjectUtils.*;
import static com.kairos.constants.AppConstants.*;
import static com.kairos.constants.CommonConstants.DEFAULT_EMAIL_TEMPLATE;
import static com.kairos.enums.wta.WTATemplateType.CHILD_CARE_DAYS_CHECK;
import static com.kairos.enums.wta.WTATemplateType.WTA_FOR_CARE_DAYS;

/**
 * Created By G.P.Ranjan on 7/2/20
 **/
@Service
public class ActivityReminderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShiftReminderService.class);

    @Inject
    private SendGridMailService sendGridMailService;
    @Inject
    private EnvConfig envConfig;
    @Inject
    private ActivityMongoRepository activityMongoRepository;
    @Inject
    private UserIntegrationService userIntegrationService;
    @Inject
    private StaffActivitySettingRepository staffActivitySettingRepository;
    @Inject
    private WorkTimeAgreementBalancesCalculationService workTimeAgreementBalancesCalculationService;

    public void sendActivityCutoffReminderViaEmail(Long unitId, BigInteger entityId) {
        Activity activity = activityMongoRepository.findOne(entityId);
        if (isNull(activity)) {
            LOGGER.info("Unable to find activity by id {}", entityId);
        }
        List<StaffActivitySetting> staffActivitySettings = staffActivitySettingRepository.findByActivityIdAndDeletedFalse(activity.getId());
        Set<Long> staffId = staffActivitySettings.stream().map(StaffActivitySetting::getStaffId).collect(Collectors.toSet());
        List<StaffPersonalDetail> staffPersonalDetails = userIntegrationService.getStaffDetailByIds(unitId, staffId);
        for (StaffPersonalDetail staffPersonalDetail : staffPersonalDetails) {
            if(isNotNull(staffPersonalDetail.getMainEmploymentId()) && isNotNull(staffPersonalDetail.getPrivateEmail())) {
                try {
                    WorkTimeAgreementBalance workTimeAgreementBalance = workTimeAgreementBalancesCalculationService.getWorkTimeAgreementBalance(unitId, staffPersonalDetail.getMainEmploymentId(), getCurrentLocalDate(), getCurrentLocalDate(), newHashSet(WTATemplateType.SENIOR_DAYS_PER_YEAR,CHILD_CARE_DAYS_CHECK,WTA_FOR_CARE_DAYS), activity.getId());
                    List<IntervalBalance> intervalBalances = workTimeAgreementBalance.getWorkTimeAgreementRuleTemplateBalances().stream().flatMap(workTimeAgreementRuleTemplateBalancesDTO -> workTimeAgreementRuleTemplateBalancesDTO.getIntervalBalances().stream()).filter(intervalBalance -> (int) intervalBalance.getAvailable()>0).collect(Collectors.toList());
                    for (IntervalBalance intervalBalance : intervalBalances) {
                        sendEmail(staffPersonalDetail, activity, intervalBalance);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendEmail(StaffPersonalDetail staffPersonalDetail, Activity activity, IntervalBalance intervalBalance) {
        String description = String.format(ABSENCE_ACTIVITY_REMINDER_EMAIL_BODY, activity.getName(), intervalBalance.getEndDate(), intervalBalance.getAvailable());
        Map<String,Object> templateParam = new HashMap<>();
        templateParam.put("receiverName",staffPersonalDetail.getFullName());
        templateParam.put("description", description);
        if(isNotNull(staffPersonalDetail.getProfilePic())) {
            templateParam.put("receiverImage",envConfig.getServerHost() + FORWARD_SLASH + envConfig.getImagesPath()+staffPersonalDetail.getProfilePic());
        }
        sendGridMailService.sendMailWithSendGrid(DEFAULT_EMAIL_TEMPLATE,templateParam, null, ACTIVITY_REMINDER,staffPersonalDetail.getPrivateEmail());
    }
}
