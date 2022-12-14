package com.kairos.rest_client;

import com.kairos.commons.client.RestTemplateResponseEnvelope;
import com.kairos.dto.user.country.agreement.cta.cta_response.PhaseResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by vipul on 25/9/17.
 */
@Component
public class PhaseRestClient {
    private static final Logger logger = LoggerFactory.getLogger(PhaseRestClient.class);

    @Inject
    RestTemplate restTemplate;



    public List<PhaseResponseDTO> getPhases (Long countryId){

        final String baseUrl= RestClientURLUtil.getBaseUrl(false);
        try {
            ParameterizedTypeReference<RestTemplateResponseEnvelope<List<PhaseResponseDTO>>> typeReference =
                    new ParameterizedTypeReference<RestTemplateResponseEnvelope<List<PhaseResponseDTO>>>() {};
            ResponseEntity<RestTemplateResponseEnvelope<List<PhaseResponseDTO>>> restExchange =
                    restTemplate.exchange(
                            baseUrl + "/country/"+countryId+"/phase/all",
                            HttpMethod.GET,
                            null, typeReference,countryId);

            RestTemplateResponseEnvelope<List<PhaseResponseDTO>> response = restExchange.getBody();
            if (restExchange.getStatusCode().is2xxSuccessful()) {
                return response.getData();
            } else {
                throw new RuntimeException(response.getMessage());
            }
        }catch (HttpClientErrorException e) {

            logger.info("status {}",e.getStatusCode());
            logger.info("response {}",e.getResponseBodyAsString());
            throw new RuntimeException("exception occurred in task micro service "+e.getMessage());
        }

    }
    /**
     * A temp restclient and to be replaced with rest client in other branch
     */
    @Async
    public void initialOptaplannerSync (Long unitId){

        final String baseUrl= RestClientURLUtil.getBaseUrl(false);

        try {
            ParameterizedTypeReference<RestTemplateResponseEnvelope<Boolean>> typeReference =
                    new ParameterizedTypeReference<RestTemplateResponseEnvelope<Boolean>>() {};
            ResponseEntity<RestTemplateResponseEnvelope<Boolean>> restExchange =
                    restTemplate.exchange(
                            baseUrl + "/unit/{unitId}/planner_integration",
                            HttpMethod.POST,
                            null, typeReference, unitId);

            RestTemplateResponseEnvelope<Boolean> response = restExchange.getBody();
            if (restExchange.getStatusCode().is2xxSuccessful()) {
                logger.info("RestExchange",restExchange);
            } else {
                throw new RuntimeException(response.getMessage());
            }
        }catch (HttpClientErrorException e) {

            logger.info("status {}",e.getStatusCode());
            logger.info("response {}",e.getResponseBodyAsString());
            throw new RuntimeException("exception occurred in task micro service "+e.getMessage());
        }

    }
}
