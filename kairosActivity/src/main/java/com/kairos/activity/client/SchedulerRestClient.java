package com.kairos.activity.client;

import com.kairos.activity.client.dto.RestTemplateResponseEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by oodles on 5/9/17.
 */
@Component
public class SchedulerRestClient {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerRestClient.class);
    @Autowired
    @Qualifier("schedulerRestTemplate")
    private RestTemplate restTemplate;

    public List<Long> getAllOrganizationIds(){
        final String baseUrl=getBaseUrl();

        try {
            ParameterizedTypeReference<RestTemplateResponseEnvelope<List<Long>>> typeReference = new ParameterizedTypeReference<RestTemplateResponseEnvelope<List<Long>>>() {};
            ResponseEntity<RestTemplateResponseEnvelope<List<Long>>> restExchange =
                    restTemplate.exchange(
                            baseUrl +"/ids" ,
                            HttpMethod.GET,
                            null, typeReference);
            RestTemplateResponseEnvelope<List<Long>> response = restExchange.getBody();
            if (restExchange.getStatusCode().is2xxSuccessful()) {

                List<Long> organizations =  response.getData();
                return organizations;
            } else {
                throw new RuntimeException(response.getMessage());
            }
        }catch (HttpClientErrorException e) {
            logger.info("status {}",e.getStatusCode());
            logger.info("response {}",e.getResponseBodyAsString());
            throw new RuntimeException("exception occurred in user micro service "+e.getMessage());
        }

    }


    private final String getBaseUrl(){

            String baseUrl=new StringBuilder("http://zuulservice/kairos/user/api/v1/organization/123").toString();
            return baseUrl;


    }



}
