package com.kairos.persistance.repository.data_inventory.processing_activity;

import com.kairos.persistance.model.data_inventory.processing_activity.ProcessingActivity;
import com.kairos.response.dto.data_inventory.ProcessingActivityResponseDTO;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

import static com.kairos.constants.AppConstant.*;

public class ProcessingActivityMongoRepositoryImpl implements CustomProcessingActivityRepository {


    @Inject
    private MongoTemplate mongoTemplate;

    @Override
    public ProcessingActivity findByName( Long organizationId, String name) {
        Query query = new Query(Criteria.where(ORGANIZATION_ID).is(organizationId).and(DELETED).is(false).and("name").is(name).and("isSubProcess").is(false));
        query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary()));
        return mongoTemplate.findOne(query, ProcessingActivity.class);
    }


    @Override
    public List<ProcessingActivityResponseDTO> getAllProcessingActivityWithSubProcessingActivitiesAndMetaData( Long organizationId) {

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(ORGANIZATION_ID).is(organizationId).and(DELETED).is(false).and("subProcess").is(false)),
                lookup("processing_purpose", "processingPurposes", "_id", "processingPurposes"),
                lookup("transfer_method", "sourceTransferMethods", "_id", "sourceTransferMethods"),
                lookup("transfer_method", "destinationTransferMethods", "_id", "destinationTransferMethods"),
                lookup("accessor_party", "accessorParties", "_id", "accessorParties"),
                lookup("dataSource", "dataSources", "_id", "dataSources"),
                lookup("responsibility_type","responsibilityType","_id","responsibilityType")
        );

        AggregationResults<ProcessingActivityResponseDTO> result = mongoTemplate.aggregate(aggregation, ProcessingActivity.class, ProcessingActivityResponseDTO.class);
        return result.getMappedResults();

    }

    @Override
    public ProcessingActivityResponseDTO getProcessingActivityWithSubProcessingActivitiesAndMetaDataById( Long organizationId, BigInteger id) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(ORGANIZATION_ID).is(organizationId).and(DELETED).is(false).and("_id").is(id).and("subProcess").is(false)),
                lookup("processing_purpose", "processingPurposes", "_id", "processingPurposes"),
                lookup("transfer_method", "sourceTransferMethods", "_id", "sourceTransferMethods"),
                lookup("transfer_method", "destinationTransferMethods", "_id", "destinationTransferMethods"),
                lookup("accessor_party", "accessorParties", "_id", "accessorParties"),
                lookup("dataSource", "dataSources", "_id", "dataSources"),
                lookup("responsibility_type","responsibilityType","_id","responsibilityType")
        );

        AggregationResults<ProcessingActivityResponseDTO> result = mongoTemplate.aggregate(aggregation, ProcessingActivity.class, ProcessingActivityResponseDTO.class);
        return result.getUniqueMappedResult();
    }
}
