package com.kairos.service.requestComponent;

import com.kairos.client.OrganizationRestClient;
import com.kairos.client.StaffRestClient;
import com.kairos.activity.organization.OrganizationDTO;
import com.kairos.activity.dto.RequestComponent;
import com.kairos.activity.dto.RequestType;
import com.kairos.service.MongoBaseService;
import com.kairos.service.activity_stream.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static com.kairos.constants.AppConstants.*;


/**
 * Created by oodles on 22/8/17.
 */
@Service
@Transactional
public class RequestComponentService extends MongoBaseService {

    @Inject
    private NotificationService notificationService;
    @Inject
    private OrganizationRestClient organizationRestClient;
    @Inject
    private StaffRestClient staffRestClient;

    private static final Logger logger = LoggerFactory.getLogger(RequestComponentService.class);

    public RequestComponent createRequest(Long organizationId, RequestComponent requestComponent) {
        try {
            OrganizationDTO organization = null;
            String source="";
            switch (requestComponent.getRequestSentTo()) {
                case HUB:
                    List<Long> staffs = staffRestClient.getCountryAdminsIds(organizationId);
                    source= TAB_45;
                    for(Long staff : staffs){
                        notificationService.addNewRequestNotification(organizationId, requestComponent, staff, source);
                    }

                    break;
                case ORGANIZATION:
                    organization = organizationRestClient.getParentOfOrganization(requestComponent.getRequestSentId());

                    source=MODULE_3;
                    break;
            }
            if (organization != null) {

                List<Long> unitManagerList = staffRestClient.getUnitManagerIds(organization.getId());
                if (unitManagerList.size() != 0) {
                    for (Long unitManager : unitManagerList) {
                        notificationService.addNewRequestNotification(organization.getId(), requestComponent, unitManager, source);
                    }
                }
            }
            return requestComponent;
        }catch (Exception exception){
            return null;
        }
    }

    public RequestType[] fetchRequestTypes(){
        return RequestType.values();
    }


}
