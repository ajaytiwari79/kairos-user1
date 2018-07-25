package com.kairos.persistence.repository.wta.rule_template;

import com.kairos.activity.wta.rule_template_category.RuleTemplateCategoryTagDTO;
import com.kairos.persistence.model.wta.templates.RuleTemplateCategory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


public class RuleTemplateMongoRepositoryImpl implements CustomRuleTemplateMongoRepository {
    @Inject
    private MongoTemplate mongoTemplate;

    @Override
    public List<RuleTemplateCategoryTagDTO> findAllByCountryId(Long countryId) {

        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("deleted").is(false).and("countryId").is(countryId)),
                unwind("tags",true),
                lookup("tag", "tags", "_id", "tags"),
                project("name", "description", "tags", "ruleTemplateIds")
        );
        AggregationResults<RuleTemplateCategoryTagDTO> result = mongoTemplate.aggregate(aggregation, RuleTemplateCategory.class, RuleTemplateCategoryTagDTO.class);
        return result.getMappedResults();
    }

}
