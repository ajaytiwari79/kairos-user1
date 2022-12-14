package com.kairos.persistence.repository.repository_impl;

import com.kairos.dto.activity.activity.TableConfiguration;
import com.kairos.persistence.model.table_settings.TableSetting;
import com.kairos.persistence.repository.table_settings.CustomTableSettingMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class TableSettingMongoRepositoryImpl implements CustomTableSettingMongoRepository {
    @Inject
    private MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public TableSetting findByUserIdAndOrganizationId(Long userId, Long organizationId, String tabId) {

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("deleted").is(false).and("userId").is(userId).and("organizationId").is(organizationId).and("tableConfigurations.tabId").is(tabId)));
        AggregationResults<TableSetting> results = mongoTemplate.aggregate(aggregation, TableSetting.class, TableSetting.class);

        return !results.getMappedResults().isEmpty() ? results.getMappedResults().get(0) : null;
    }

    @Override
    public TableConfiguration findTableConfigurationByUserIdAndOrganizationId(Long userId, Long organizationId, String tabId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("deleted").is(false).and("userId").is(userId).and("organizationId").is(organizationId)),
                unwind("tableConfigurations"),
                match(Criteria.where("tableConfigurations.tabId").is(tabId)),
                replaceRoot("tableConfigurations")
        );
        AggregationResults<TableConfiguration> results = mongoTemplate.aggregate(aggregation, TableSetting.class, TableConfiguration.class);

        return !results.getMappedResults().isEmpty() ? results.getMappedResults().get(0) : null;
    }
}
