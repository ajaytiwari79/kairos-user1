package com.kairos.activity.persistence.repository.counter;

import com.kairos.activity.persistence.enums.counter.CounterType;
import com.kairos.activity.persistence.model.counter.*;
import com.kairos.activity.response.dto.counter.*;
import io.jsonwebtoken.lang.Assert;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

@Repository
public class CounterRepository {

    @Inject
    private MongoTemplate mongoTemplate;

    //get counter by type
    public Counter getCounterByType(CounterType type) {
        Query query = new Query(Criteria.where("type").is(type));
        return mongoTemplate.findOne(query, Counter.class);
    }


    //get ModuleWiseCounters List by country
    public List<ModuleWiseCounter> getModulewiseCountersForCountry(BigInteger countryId) {
        Query query = new Query(Criteria.where("countryId").is(countryId));
        return mongoTemplate.find(query, ModuleWiseCounter.class);
    }

    //get modulewise countersIds for a country
    public List<ModulewiseCounterGroupingDTO> getModulewiseCounterDTOsForCountry(BigInteger countryId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("countryId").is(countryId)),
                Aggregation.group("moduleId").addToSet("counterId").as("counterIds"),
                Aggregation.project("counterIds")
        );
        AggregationResults<ModulewiseCounterGroupingDTO> results = mongoTemplate.aggregate(aggregation, ModuleWiseCounter.class, ModulewiseCounterGroupingDTO.class);
        return results.getMappedResults();
    }

    //get role and moduleCounterId mapping for unit
    public List<RolewiseCounterDTO> getRoleAndModuleCounterIdMapping(BigInteger unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("unitId").is(unitId)),
                Aggregation.group("roleId").addToSet("modulewiseCounterId").as("modulewiseCounterIds"),
                Aggregation.project("modulewiseCounterIds")

        );

        AggregationResults<RolewiseCounterDTO> results = mongoTemplate.aggregate(aggregation, UnitRoleWiseCounter.class, RolewiseCounterDTO.class);
        return results.getMappedResults();
    }

    //public


    //public void setCustomCounterSetting


    /// old code

    //getCounterModuleLink
    public ModuleWiseCounter getCounterModuleLink(String moduleId, BigInteger counterDefinitionId) {
        Assert.notNull(moduleId, "Module Id can't be null!");
        Query query = new Query(Criteria.where("moduleId").is(moduleId).and("counterDefinitionId").is(counterDefinitionId));
        ModuleWiseCounter moduleWiseCounter = mongoTemplate.findOne(query, ModuleWiseCounter.class);
        return moduleWiseCounter;
    }

    //deleteModuleWiseCounter
    public void deleteCounterModuleLink(BigInteger moduleId, BigInteger counterDefinitionId) {
        Query query = new Query(Criteria.where("moduleId").is(moduleId).and("counterDefinitionId").is(counterDefinitionId));
        mongoTemplate.findAllAndRemove(query, ModuleWiseCounter.class);
    }

    public void removeAccessiblitiesById(List<BigInteger> ids) {
        Query query = new Query(Criteria.where("_id").in(ids));
        mongoTemplate.findAllAndRemove(query, UnitRoleWiseCounter.class);
    }



    //get item by Id
    public Object getItemById(BigInteger id, Class claz){
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, claz);
    }

    //remove item by Id
    public void removeItemById(BigInteger id, Class claz){
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, claz);
    }

    //test cases..
    //getCounterListByType for testcases
    public List getEntityItemList(Class claz){
        return mongoTemplate.findAll(claz);
    }


    public void removeCustomCounterProfiles(List<BigInteger> accessiblityIds) {
        Query query = new Query(Criteria.where("_id").in(accessiblityIds));
    }

    public List<RefCounterDefDTO> getRolewiseCounterTypeDetails(BigInteger roleId, BigInteger unitId, String moduleId){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("roleId").is(roleId).and("unitId").is(unitId)),
                Aggregation.lookup("moduleWiseCounter","refCounterId", "_id", "refCounter"),
                Aggregation.project().and("refCounter").arrayElementAt(0).as("refCounter"),
                Aggregation.match(Criteria.where("refCounter.moduleId").is(moduleId)),
                Aggregation.lookup("counter", "refCounter.counterId", "_id","counterDef" ),
                Aggregation.project().and("counterDef").arrayElementAt(0).as("counterDef"),
                Aggregation.project().and("counterDef.type").as("counterType")
        );

        AggregationResults<RefCounterDefDTO> results = mongoTemplate.aggregate(aggregation, UnitRoleWiseCounter.class, RefCounterDefDTO.class);
        return results.getMappedResults();
    }

    public List<RefCounterDefDTO> getModuleWiseCounterDetails(String moduleId, BigInteger countryId){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("moduleId").is(moduleId).and("countryId").is(countryId)),
                Aggregation.lookup("counter", "counterId","_id","counterType"),
                Aggregation.project().and("counterType").arrayElementAt(0).as("counterType")
        );

        AggregationResults<RefCounterDefDTO> results = mongoTemplate.aggregate(aggregation, ModuleWiseCounter.class, RefCounterDefDTO.class);
        return results.getMappedResults();
    }

    public List<CounterOrderDTO> getOrderedCountersListForCountry(BigInteger countryId, String moduleId){
        Criteria criteria = Criteria.where("countryId").is(countryId).and("moduleId").is(moduleId);
        if(moduleId == null)
            criteria = Criteria.where("countryId").is(countryId);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria)
        );
        AggregationResults<CounterOrderDTO> results = mongoTemplate.aggregate(aggregation, DefaultCounterOrder.class, CounterOrderDTO.class);
        return results.getMappedResults();
    }

    public List<CounterOrderDTO> getOrderedCountersListForUnit(BigInteger unitId, String moduleId){
        Criteria criteria = Criteria.where("unitId").is(unitId).and("moduleId").is(moduleId);
        if(moduleId == null)
            criteria = Criteria.where("unitId").is(unitId);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("unitId").is(unitId).and("moduleId").is(moduleId))
        );
        AggregationResults<CounterOrderDTO> results = mongoTemplate.aggregate(aggregation, UnitWiseCounterOrder.class, CounterOrderDTO.class);
        return results.getMappedResults();
    }

    public List<CounterOrderDTO> getOrderedCountersListForUser(BigInteger unitId, BigInteger staffId, String moduleId){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("unitId").is(unitId).and("staffId").is(staffId).and("moduleId").is(moduleId))
        );
        AggregationResults<CounterOrderDTO> results = mongoTemplate.aggregate(aggregation, UserWiseCounterOrder.class, CounterOrderDTO.class);
        return results.getMappedResults();
    }
}
