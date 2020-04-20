package com.kairos.config;

import com.kairos.commons.client.RestTemplateResponseEnvelope;
import com.kairos.enums.IntegrationOperation;
import com.kairos.rest_client.UserRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

public class PermissionSchemaProcessor implements BeanPostProcessor {
    private static final Logger LOGGER= LoggerFactory.getLogger(PermissionSchemaProcessor.class);
    private UserRestClient userRestClient;

    private String userServiceUrl;

    public PermissionSchemaProcessor(List<Map<String, Object>> data, UserRestClient userRestClient, String userServiceUrl, String kpermissionDataPublish) {
        this.userRestClient =userRestClient;
        this.userServiceUrl= userServiceUrl;
        try{
            publishPermissionSchemaToUserService(userRestClient, data);
        }catch (Exception e){
            LOGGER.info("something went wrong while creating permission model");
            LOGGER.error(e.getLocalizedMessage());
        }

    }

    private void publishPermissionSchemaToUserService(UserRestClient userRestClient, List<Map<String, Object>> data){
        try {
            userRestClient.publishRequest(data, userServiceUrl, IntegrationOperation.CREATE, "/create_permission_schema", null, new ParameterizedTypeReference<RestTemplateResponseEnvelope<Object>>() {});
        }catch (Exception e){
           //ignored
        }

    }
}
