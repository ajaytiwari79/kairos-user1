package com.kairos.service.scheduler;

import com.kairos.config.env.EnvConfig;
import com.kairos.dto.scheduler.queue.KairosSchedulerExecutorDTO;
import com.kairos.persistence.model.organization.Unit;
import com.kairos.persistence.repository.organization.UnitGraphRepository;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.kairos.constants.AppConstants.*;

//import java.io.ByteArrayInputStream;

@Service
public class IntegrationJobsExecutorService {

    @Inject
    private EnvConfig envConfig;
    @Inject
    private UnitGraphRepository unitGraphRepository;
    @Inject private UserSchedulerJobService userSchedulerJobService;
    private static Logger logger = LoggerFactory.getLogger(IntegrationJobsExecutorService.class);

    public void runJob(KairosSchedulerExecutorDTO job) {
        String plainClientCredentials = "cluster:cluster";
        String base64ClientCredentials = new String(Base64.encodeBase64(plainClientCredentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
        headers.add("Authorization", "Basic " + base64ClientCredentials);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        String importShiftStatusXMLURI = envConfig.getCarteServerHost()+KETTLE_TRANS_STATUS;
        Long workplaceId = getWorkPlaceId(job);
        int weeks = 35;
        String uniqueKey = job.getIntegrationSettingsDTO().getUniqueKey();
        logger.info("uniqueKey----> {}",uniqueKey);
        RestTemplate restTemplate = new RestTemplate();
        importData(job, entity, importShiftStatusXMLURI, workplaceId, weeks, uniqueKey, restTemplate);
    }

    private void importData(KairosSchedulerExecutorDTO job, HttpEntity<String> entity, String importShiftStatusXMLURI, Long workplaceId, int weeks, String uniqueKey, RestTemplate restTemplate) {
        switch(uniqueKey){
            case IMPORT_TIMECARE_SHIFTS:
                importTimecareShifts(job, entity, importShiftStatusXMLURI, workplaceId, weeks, restTemplate);
                break;
            case IMPORT_KMD_CITIZEN:
                importKMDCitizen(job, entity, restTemplate);
                break;
            case IMPORT_KMD_CITIZEN_NEXT_TO_KIN:
                importNextToKin(entity, restTemplate);
                break;
            case IMPORT_KMD_CITIZEN_GRANTS:
                importGrants(entity, restTemplate);
                break;
            case IMPORT_KMD_STAFF_AND_WORKING_HOURS:
                importWorkingHours(job, entity, restTemplate);
                break;
            case IMPORT_KMD_TASKS:
                importTasks(job, entity, restTemplate);
                break;
            case IMPORT_KMD_TIME_SLOTS:
                importTimeSlots(job, entity, restTemplate);
                break;
            default:
                break;
        }
    }

    private Long getWorkPlaceId(KairosSchedulerExecutorDTO job) {
        Long workplaceId = Long.valueOf(String.valueOf("15"));
        if(job.getUnitId() != null){
            Unit unit = unitGraphRepository.findOne(job.getUnitId());
            if(unit.getExternalId() != null) workplaceId = Long.valueOf(unit.getExternalId());
        }
        return workplaceId;
    }

    private void importTimeSlots(KairosSchedulerExecutorDTO job, HttpEntity<String> entity, RestTemplate restTemplate) {
        String importShiftURI;
        importShiftURI=envConfig.getServerHost()+API_KMD_CARE_URL+job.getUnitId()+"/getTimeSlots";
        restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
    }

    private void importTasks(KairosSchedulerExecutorDTO job, HttpEntity<String> entity, RestTemplate restTemplate) {
        String importShiftURI;
        importShiftURI=envConfig.getServerHost()+API_KMD_CARE_URL+job.getUnitId()+"/getTasks/"+job.getFilterId();
        restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
    }

    private void importWorkingHours(KairosSchedulerExecutorDTO job, HttpEntity<String> entity, RestTemplate restTemplate) {
        String importShiftURI;
        importShiftURI=envConfig.getServerHost()+API_KMD_CARE_URL+job.getUnitId()+"/getShifts/"+job.getFilterId();
        restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
    }

    private void importGrants(HttpEntity<String> entity, RestTemplate restTemplate) {
        String importShiftURI;
        importShiftURI = envConfig.getServerHost()+API_KMD_CARE_CITIZEN_GRANTS;
        restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
    }

    private void importNextToKin(HttpEntity<String> entity, RestTemplate restTemplate) {
        String importShiftURI;
        importShiftURI = envConfig.getServerHost()+API_KMD_CARE_CITIZEN_RELATIVE_DATA;
        restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
    }

    private void importKMDCitizen(KairosSchedulerExecutorDTO job, HttpEntity<String> entity, RestTemplate restTemplate) {
        String importShiftURI;
        importShiftURI = envConfig.getServerHost()+KMD_CARE_CITIZEN_URL+job.getUnitId();
        restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
    }

    private void importTimecareShifts(KairosSchedulerExecutorDTO job, HttpEntity<String> entity, String importShiftStatusXMLURI, Long workplaceId, int weeks, RestTemplate restTemplate) {
        String importShiftURI;
        logger.info("!!===============Hit to carte server from Kairos==============!!");
        importShiftURI = envConfig.getCarteServerHost()+KETTLE_EXECUTE_TRANS+IMPORT_TIMECARE_SHIFTS_PATH+"&intWorkPlaceId="+workplaceId+"&weeks="+weeks+"&jobId="+job.getId();
        LocalDateTime started = LocalDateTime.now();
        ResponseEntity<String> importResult = restTemplate.exchange(importShiftURI, HttpMethod.GET, entity, String.class);
        if (importResult.getStatusCodeValue() == 200) {
            ResponseEntity<String> resultStatusXml = restTemplate.exchange(importShiftStatusXMLURI, HttpMethod.GET, entity, String.class);
            LocalDateTime stopped = LocalDateTime.now();
            /*try {
                updateJobForTimeCareShifts(job, started, resultStatusXml, stopped);
            } catch (JAXBException | IOException exception ) {
                logger.info("trans status---exception > {}" , exception);
            }*/
        }
        return;
    }

    private void updateJobForTimeCareShifts(KairosSchedulerExecutorDTO job, LocalDateTime started, ResponseEntity<String> resultStatusXml, LocalDateTime stopped) throws IOException {
        /*JAXBContext jaxbContext = JAXBContext.newInstance(Transstatus.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(resultStatusXml.getBody());
        Transstatus transstatus = (Transstatus) jaxbUnmarshaller.unmarshal(reader);
        logger.info("trans status---> {}" , transstatus.getId());
        String loggingString = StringEscapeUtils.escapeHtml4(transstatus.getLogging_string());
        loggingString = loggingString.substring(loggingString.indexOf("[CDATA[")+7,loggingString.indexOf("]]&gt"));
        byte[] bytes = Base64.decodeBase64(loggingString);
        String unzipped;
        try(GZIPInputStream zi = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            unzipped = IOUtils.toString(zi);
        }
        userSchedulerJobService.updateJobForTimecareShift(job, started, stopped, transstatus, unzipped);*/
    }

}
