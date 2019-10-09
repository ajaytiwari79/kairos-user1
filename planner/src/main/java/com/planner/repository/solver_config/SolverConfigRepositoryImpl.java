package com.planner.repository.solver_config;

import com.kairos.dto.planner.solverconfig.SolverConfigDTO;
import com.kairos.dto.planner.solverconfig.country.CountrySolverConfigDTO;
import com.kairos.dto.planner.solverconfig.unit.UnitSolverConfigDTO;
import com.planner.domain.solverconfig.common.SolverConfig;
import com.planner.domain.solverconfig.unit.UnitSolverConfig;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.lookup;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

/**
 * @author pradeep
 * @date - 12/11/18
 */
@Repository
public class SolverConfigRepositoryImpl implements CustomSolverConfigRepository {
    public static final String SOLVER_CONFIG = "solverConfig";
    @Inject
    private MongoTemplate mongoTemplate;



        public SolverConfigDTO getSolverConfigWithConstraints(BigInteger solverConfigId){
            Aggregation aggregation = Aggregation.newAggregation(
                    match(Criteria.where("_id").is(solverConfigId)),
                    lookup(SOLVER_CONFIG, "constraintIds", "_id", "constraints"));
            AggregationResults<SolverConfigDTO> result = mongoTemplate.aggregate(aggregation, SolverConfig.class, SolverConfigDTO.class);
            return result.getMappedResults().isEmpty() ? null : result.getMappedResults().get(0);
        }

    public List<SolverConfigDTO> getAllSolverConfigWithConstraints(boolean checkForCountry, Long countryOrUnitId){
        String applicableIdField = checkForCountry ? "countryId" : "unitId";
        Criteria criteria=Criteria.where("deleted").ne(true).and(applicableIdField).is(countryOrUnitId);
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup("constraint", "constraintIds", "_id", "constraints"));
        Class className = checkForCountry ? CountrySolverConfigDTO.class : UnitSolverConfigDTO.class;
        AggregationResults<SolverConfigDTO> result = mongoTemplate.aggregate(aggregation, SolverConfig.class, className);
        return result.getMappedResults();
    }

    public SolverConfig getSolverConfigById(BigInteger solverConfigId,boolean checkForCountry){
        Class className = checkForCountry ? CountrySolverConfigDTO.class : UnitSolverConfigDTO.class;
        return (SolverConfig) mongoTemplate.findOne(new Query(Criteria.where("_id").is(solverConfigId)), className, SOLVER_CONFIG);
    }


    public List<UnitSolverConfig> getAllSolverConfigByParentId(BigInteger solverConfigId){
        return mongoTemplate.find(new Query(Criteria.where("parentCountrySolverConfigId").is(solverConfigId).and("deleted").is(false)), UnitSolverConfig.class, SOLVER_CONFIG);
    }



}
