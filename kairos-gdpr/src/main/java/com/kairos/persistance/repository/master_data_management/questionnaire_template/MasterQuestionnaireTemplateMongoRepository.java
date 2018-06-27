package com.kairos.persistance.repository.master_data_management.questionnaire_template;


import com.kairos.persistance.model.master_data_management.questionnaire_template.MasterQuestionnaireTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface MasterQuestionnaireTemplateMongoRepository extends MongoRepository<MasterQuestionnaireTemplate,BigInteger> ,CustomQuestionnaireTemplateRepository {

    @Query("{deleted:false,countryId:?0,organizationId:?1,_id:?2}")
    MasterQuestionnaireTemplate findByIdAndNonDeleted(Long countryId,Long organizationId,BigInteger id);

    @Query("{'name':?1, 'deleted':false, 'countryId':?0,organizationId:?1}")
    MasterQuestionnaireTemplate findByCountryIdAndName(Long countryId,Long organizationId,String name);

    MasterQuestionnaireTemplate findByid(BigInteger id);

}
