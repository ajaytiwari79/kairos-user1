package com.kairos.persistence.repository.cta;

import com.kairos.commons.utils.DateUtils;
import com.kairos.commons.utils.ObjectMapperUtils;
import com.kairos.constants.CommonConstants;
import com.kairos.dto.activity.cta.CTAResponseDTO;
import com.kairos.dto.activity.cta.CTARuleTemplateDTO;
import com.kairos.persistence.model.cta.CostTimeAgreement;
import com.kairos.persistence.model.wta.WorkingTimeAgreement;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.kairos.constants.CommonConstants.COUNTRY_ID;
import static com.kairos.constants.CommonConstants.DELETED;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author pradeep
 * @date - 3/8/18
 */

public class CostTimeAgreementRepositoryImpl implements CustomCostTimeAgreementRepository {

    public static final String C_TA_RULE_TEMPLATE = "cTARuleTemplate";
    public static final String RULE_TEMPLATE_IDS = "ruleTemplateIds";
    public static final String RULE_TEMPLATES = "ruleTemplates";
    public static final String ORGANIZATION_ID = "organization._id";
    public static final String EMPLOYMENT_ID = "employmentId";
    public static final String PARENT_ID = "parentId";
    public static final String ORGANIZATION_PARENT_ID = "organizationParentId";
    public static final String DESCRIPTION = "description";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    @Inject
    private MongoTemplate mongoTemplate;

    @Override

    public List<CTAResponseDTO> findCTAByCountryId(Long countryId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(COUNTRY_ID).is(countryId).and(DELETED).is(false).and(CommonConstants.DISABLED).is(false).and(ORGANIZATION_ID).exists(false)),
                lookup("tag", "tags", "_id", "tags")
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults();

    }

    @Override
    public CTAResponseDTO getOneCtaById(BigInteger ctaId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("_id").is(ctaId).and(DELETED).is(false)),
                lookup("tag", "tags", "_id", "tags"),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES)
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults().isEmpty() ? null : result.getMappedResults().get(0);
    }

    @Override
    public CostTimeAgreement getCTAByIdAndOrganizationSubTypeAndCountryId(Long organizationSubTypeId, Long countryId, BigInteger ctaId) {
        Query query = new Query(Criteria.where("organizationSubType._id").is(organizationSubTypeId).and("_id").is(ctaId).and(COUNTRY_ID).is(countryId).and(DELETED).is(false).and(CommonConstants.DISABLED).is(false));
        return mongoTemplate.findOne(query, CostTimeAgreement.class);
    }

    @Override
    public CTAResponseDTO findCTAById(BigInteger ctaId){
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("_id").is(ctaId)),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES)
        );
        return mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class).getMappedResults().get(0);
    }

    @Override
    public List<CTAResponseDTO> getAllCTAByOrganizationSubType(Long countryId, Long organizationSubTypeId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where("organizationSubType._id").is(organizationSubTypeId).and(COUNTRY_ID).is(countryId).and(ORGANIZATION_ID).exists(false).and(DELETED).is(false).and(CommonConstants.DISABLED).is(false)),
                lookup("tag", "tags", "_id", "tags"),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES)
                );
        return mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class).getMappedResults();
    }

    @Override
    public List<CTAResponseDTO> findCTAByUnitId(Long unitId) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(ORGANIZATION_ID).is(unitId).and(DELETED).is(false).and(CommonConstants.DISABLED).is(false).and(EMPLOYMENT_ID).exists(false)),
                lookup("tag", "tags", "_id", "tags")
        );
        return mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class).getMappedResults();
    }

    @Override
    public Boolean isCTAExistWithSameNameInUnit(Long unitId, String name, BigInteger ctaId) {
        Query query = new Query(Criteria.where(ORGANIZATION_ID).is(unitId).and("name").is(name).and("_id").ne(ctaId).and(DELETED).is(false).and(CommonConstants.DISABLED).is(false).and(EMPLOYMENT_ID).exists(false));
        return mongoTemplate.exists(query, CostTimeAgreement.class);
    }

    @Override
    public List<CTAResponseDTO> getDefaultCTAOfExpertiseAndDate(Long unitId, Long expertiseId,LocalDate selectedDate) {
        Query query = new Query(Criteria.where(ORGANIZATION_ID).is(unitId).and("expertise._id").is(expertiseId).and(DELETED).is(false).and(EMPLOYMENT_ID).exists(false).orOperator(Criteria.where(START_DATE).lte(selectedDate).and(END_DATE).gte(selectedDate), Criteria.where(END_DATE).exists(false).and(START_DATE).lte(selectedDate)));
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(mongoTemplate.find(query,CostTimeAgreement.class),CTAResponseDTO.class);
    }

    @Override
    public List<CTAResponseDTO> getDefaultCTA(Long unitId, Long expertiseId) {
        Query query = new Query(Criteria.where(ORGANIZATION_ID).is(unitId).and("expertise._id").is(expertiseId).and(DELETED).is(false).and(EMPLOYMENT_ID).exists(false));
        query.fields().include("name").include(DESCRIPTION).include(EMPLOYMENT_ID).include(START_DATE).include(END_DATE).include(PARENT_ID).include(ORGANIZATION_PARENT_ID);
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(mongoTemplate.find(query, CostTimeAgreement.class), CTAResponseDTO.class);
    }

    @Override
    public List<CTAResponseDTO> getCTAByUpIds(Set<Long> employmentIds) {
        Query query = new Query(Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).in(employmentIds));
        return ObjectMapperUtils.copyCollectionPropertiesByMapper(mongoTemplate.find(query,CostTimeAgreement.class),CTAResponseDTO.class);
    }


    @Override
    public List<CTAResponseDTO> getParentCTAByUpIds(List<Long> employmentIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).in(employmentIds).and(CommonConstants.DISABLED).is(false)),
                        lookup("tag", "tags", "_id", "tags"),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES),
                sort(Sort.Direction.DESC, START_DATE)
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<CTAResponseDTO> getEmploymentIdsByAccountTypes(Set<Long> employmentIds,Set<String> accountTypes) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).in(employmentIds)),
                lookup("tag", "tags", "_id", "tags"),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES),
                match(Criteria.where("plannedTimeWithFactor.accountType").in(accountTypes)),
                project("EMPLOYMENT_ID")
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public CTAResponseDTO getCTAByEmploymentIdAndDate(Long employmentId, Date date) {
        Criteria criteria = Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).is(employmentId)
                .orOperator(Criteria.where(START_DATE).lte(date).and(END_DATE).gte(date),Criteria.where(END_DATE).exists(false).and(START_DATE).lte(date));
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup("tag", "tags", "_id", "tags"),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES)
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults().isEmpty() ? null : result.getMappedResults().get(0);
    }

    @Override
    public List<CTARuleTemplateDTO> getCTARultemplateByEmploymentId(Long employmentId) {
        Criteria criteria = Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).is(employmentId);
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES),
                unwind(RULE_TEMPLATES),
                match(Criteria.where("ruleTemplates.calculationFor").ne("SCHEDULED_HOURS")),
                lookup("ruleTemplateCategory", "ruleTemplates.ruleTemplateCategoryId", "_id", "ruleTemplates.ruleTemplateCategory"),
                project().and("ruleTemplates.name").as("name").and("ruleTemplates._id").as("_id").and("ruleTemplates.ruleTemplateCategory").arrayElementAt(0).as("ruleTemplateCategory"),
                project("_id","name").and("ruleTemplateCategory._id").as("ruleTemplateCategoryId").and("ruleTemplateCategory.name").as("ruleTemplateCategoryName")
        );
        AggregationResults<CTARuleTemplateDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTARuleTemplateDTO.class);
        return result.getMappedResults();
    }


    @Override
    public List<CTAResponseDTO> getCTAByEmploymentIdBetweenDate(Long employmentId, Date startDate, Date endDate) {
        Criteria criteria = Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).is(employmentId).orOperator(Criteria.where(START_DATE).lte(endDate).and(END_DATE).gte(startDate),Criteria.where(END_DATE).exists(false).and(START_DATE).lte(endDate));
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES),
                unwind(RULE_TEMPLATES,true),
                group(EMPLOYMENT_ID).push(RULE_TEMPLATES).as(RULE_TEMPLATES)
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<CTAResponseDTO> getCTAByEmploymentIds(Collection<Long> employmentIds, Date date) {

        Criteria criteria = date==null?(Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).in(employmentIds)) : (Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).in(employmentIds).orOperator(Criteria.where(START_DATE).lte(date).and(END_DATE).gte(date),Criteria.where(END_DATE).exists(false).and(START_DATE).lte(date)));
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES),
                project("name", DESCRIPTION, CommonConstants.DISABLED, "expertise", ORGANIZATION_PARENT_ID, "organization", PARENT_ID, START_DATE, END_DATE, RULE_TEMPLATES, EMPLOYMENT_ID)
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public boolean isEmploymentCTAExistsOnDate(Long employmentId,LocalDate localDate,BigInteger ctaId){
        Criteria criteria = (Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).is(employmentId).and("_id").ne(ctaId).orOperator(Criteria.where(START_DATE).lte(localDate).and(END_DATE).gte(localDate),Criteria.where(END_DATE).exists(false).and(START_DATE).lte(localDate)));
        return mongoTemplate.exists(new Query(criteria),CostTimeAgreement.class);
    }

    @Override
    public boolean isGapExistsInEmploymentCTA(Long employmentId,LocalDate localDate,BigInteger ctaId){
        Criteria criteria = Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).is(employmentId).and("_id").ne(ctaId).and(START_DATE).gte(localDate).ne(localDate);
        return mongoTemplate.exists(new Query(criteria),CostTimeAgreement.class);
    }

    @Override
    public List<CTAResponseDTO> getCTAByEmploymentIdsAndDate(List<Long> employmentIds, Date startDate, Date endDate) {
        Criteria criteria = Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).in(employmentIds).orOperator(Criteria.where(START_DATE).lte(endDate).and(END_DATE).gte(startDate),Criteria.where(END_DATE).exists(false).and(START_DATE).lte(endDate));
        Aggregation aggregation = Aggregation.newAggregation(
                match(criteria),
                lookup(C_TA_RULE_TEMPLATE, RULE_TEMPLATE_IDS, "_id", RULE_TEMPLATES),
                project("name", DESCRIPTION, CommonConstants.DISABLED, "expertise", ORGANIZATION_PARENT_ID, "organization", PARENT_ID, START_DATE, END_DATE, RULE_TEMPLATES, EMPLOYMENT_ID)
        );
        AggregationResults<CTAResponseDTO> result = mongoTemplate.aggregate(aggregation, CostTimeAgreement.class, CTAResponseDTO.class);
        return result.getMappedResults();
    }

    @Override
    public List<CostTimeAgreement> getCTAByEmployment(Long employmentId) {
        Criteria criteria = Criteria.where(DELETED).is(false).and(EMPLOYMENT_ID).is(employmentId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, CostTimeAgreement.class);
    }

    @Override
    public void disableOldCta(BigInteger oldctaId, LocalDate endDate) {
        Update update = Update.update(END_DATE, DateUtils.asDate(endDate)).set(CommonConstants.DISABLED, true);
        mongoTemplate.findAndModify(new Query(Criteria.where("id").is(oldctaId)), update, CostTimeAgreement.class);


    }

    @Override
    public void setEndDateToCTAOfEmployment(Long employmentId, LocalDate endDate){
        Update update=Update.update(END_DATE,DateUtils.asDate(endDate));
        Query query = new Query(Criteria.where(EMPLOYMENT_ID).is(employmentId)).with(Sort.by(Sort.Direction.DESC,START_DATE)).limit(1);
        mongoTemplate.findAndModify(query,update,CostTimeAgreement.class);
    }


    @Override
    public boolean existsOngoingCTAByEmployment(Long employmentId,Date endDate){
        Criteria criteria = Criteria.where(EMPLOYMENT_ID).is(employmentId).orOperator(Criteria.where(END_DATE).exists(false),Criteria.where(START_DATE).gte(endDate));
        return mongoTemplate.exists(new Query(criteria), WorkingTimeAgreement.class);
    }
}
