package com.kairos.client.planner;

import com.kairos.activity.enums.IntegrationOperation;
import com.kairos.activity.response.dto.staffing_level.StaffingLevelDto;
import com.kairos.client.dto.RestTemplateResponseEnvelope;
import com.kairos.client.dto.activity.ActivityNoTabsDTO;
import com.kairos.persistence.model.user.staff.StaffBasicDetailsDTO;
import com.kairos.response.dto.web.UnitPositionWtaDTO;
import com.kairos.response.dto.web.wta.WTAResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;

import static com.kairos.client.RestClientURLUtil.getPlannerBaseUrl;


@Service("optaplannerServiceRestClient")
public class PlannerRestClient {
    private static Logger logger = LoggerFactory.getLogger(PlannerRestClient.class);

    @Autowired
    RestTemplate restTemplate;

    public <T, V> RestTemplateResponseEnvelope<V> publish(T t, Long unitId, IntegrationOperation integrationOperation,Object... pathParams) {
        final String baseUrl = getPlannerBaseUrl();

        try {
            ParameterizedTypeReference<RestTemplateResponseEnvelope<V>> typeReference = new ParameterizedTypeReference<RestTemplateResponseEnvelope<V>>() {
            };
            ResponseEntity<RestTemplateResponseEnvelope<V>> restExchange =
                    restTemplate.exchange(
                            baseUrl + unitId + "/"+ getURI(t,integrationOperation,pathParams),
                            getHttpMethod(integrationOperation),
                            t==null?null:new HttpEntity<>(t), typeReference);
            RestTemplateResponseEnvelope<V> response = restExchange.getBody();
            if (!restExchange.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(response.getMessage());
            }
            return response;
        } catch (HttpClientErrorException e) {
            logger.info("status {}", e.getStatusCode());
            logger.info("response {}", e.getResponseBodyAsString());
            throw new RuntimeException("exception occurred in activity micro service " + e.getMessage());
        }

    }

    public static HttpMethod getHttpMethod(IntegrationOperation integrationOperation) {
        switch (integrationOperation) {
            case CREATE:
                return HttpMethod.POST;
            case DELETE:
                return HttpMethod.DELETE;
            case UPDATE:
                return HttpMethod.PUT;
            default:return null;

        }
    }
    public static <T>String getURI(T t,IntegrationOperation integrationOperation,Object... pathParams){
        String uri="";
        if(t instanceof StaffBasicDetailsDTO){
            uri= "staff/";
        }else if(t instanceof UnitPositionWtaDTO && integrationOperation.equals(IntegrationOperation.CREATE)){
            uri= String.format("staff/%s/unitposition/",pathParams);
        }else if(t instanceof UnitPositionWtaDTO && (integrationOperation.equals(IntegrationOperation.UPDATE)|| integrationOperation.equals(IntegrationOperation.DELETE))){
            uri= String.format("staff/%s/unitposition/%s",pathParams);
        }
        else if(t instanceof WTAResponseDTO){
            uri= String.format("staff/%s/unitposition/%s/wta",pathParams);
        }
        return uri;
    }
}