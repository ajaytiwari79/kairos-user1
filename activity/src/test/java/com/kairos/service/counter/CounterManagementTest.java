package com.kairos.service.counter;
import com.kairos.KairosActivityApplication;
import com.kairos.rest_client.RestTemplateResponseEnvelope;
import com.kairos.enums.CounterType;
import com.kairos.persistence.model.counter.Counter;
import com.kairos.persistence.model.counter.ModuleCounter;
import com.kairos.persistence.model.counter.UnitRoleCounter;
import com.kairos.service.MongoBaseService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KairosActivityApplication.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CounterManagementTest extends MongoBaseService {
    @Inject
    private TestRestTemplate restTemplate;
    @Inject
    private MongoTemplate mongoTemplate;

    @Test
    public void testRestClient(){
        String url = "http://xyz.example.com/kairos/user/api/v1/organization/349/country/4/tab";
        url = "http://xyz.example.com/kairos/user/api/v1/organization/349/unit/349/access_group";
        ParameterizedTypeReference<RestTemplateResponseEnvelope<List<Map<String, Object>>>> typeReference =
                new ParameterizedTypeReference<RestTemplateResponseEnvelope<List<Map<String, Object>>>>() {
                };

        ResponseEntity<RestTemplateResponseEnvelope<List<Map<String, Object>>>> response = restTemplate
                .exchange(url, HttpMethod.GET, null, typeReference);
                //.exchange(baseUrl + "/activity", HttpMethod.GET, null, typeReference);

        System.out.println("resp: "+response.getBody().getData());
    }

    @Test
    public void storeRandom(){
        String moduleId = "module_1";
        String moduleId2 = "module_2";
        BigInteger counterId = BigInteger.valueOf(0);
        BigInteger countryId = BigInteger.valueOf(1);
        BigInteger unitId = BigInteger.valueOf(2);
        BigInteger roleId = BigInteger.valueOf(3);
        BigInteger modulewiseCounterId = BigInteger.valueOf(4);
        BigInteger modulewiseCounterId2 = BigInteger.valueOf(12);
        BigInteger unitRoleCounterId = BigInteger.valueOf(5);
        BigInteger unitRoleCounterId2 = BigInteger.valueOf(13);


        Counter ctr = new Counter();
        ctr.setId(counterId);
        ctr.setType(CounterType.RESTING_HOURS_PER_PRESENCE_DAY);
        ctr = save(ctr);

        ModuleCounter moduleCounter = new ModuleCounter(counterId, moduleId, ctr.getId());
        moduleCounter.setId(modulewiseCounterId);
        moduleCounter = save(moduleCounter);

        moduleCounter.setId(modulewiseCounterId2);
        moduleCounter.setCountryId(countryId);
        moduleCounter.setCounterId(ctr.getId());
        moduleCounter.setModuleId(moduleId2);
        moduleCounter = save(moduleCounter);

        UnitRoleCounter unitRoleCounter = new UnitRoleCounter(unitId, roleId, modulewiseCounterId);
        unitRoleCounter.setId(unitRoleCounterId);
        unitRoleCounter = save(unitRoleCounter);

        unitRoleCounter = new UnitRoleCounter(unitId, roleId, modulewiseCounterId2);
        unitRoleCounter = save(unitRoleCounter);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("roleId").is(roleId).and("unitId").is(unitId)),
                Aggregation.lookup("moduleCounter","refCounterId", "_id", "refCounter"),
                Aggregation.project().and("refCounter").arrayElementAt(0).as("refCounter"),
                Aggregation.match(Criteria.where("refCounter.moduleId").is("module_1")),
                Aggregation.lookup("counter", "refCounter.counterId", "_id","counterDef" ),
                Aggregation.project().and("counterDef").arrayElementAt(0).as("counterDef"),
                Aggregation.project().and("counterDef.type").as("counterType")

        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, UnitRoleCounter.class, Map.class);
        ObjectMapper om = new ObjectMapper();
        System.out.println("results: "+om.convertValue(results.getMappedResults().get(0), Map.class));

    }
}
