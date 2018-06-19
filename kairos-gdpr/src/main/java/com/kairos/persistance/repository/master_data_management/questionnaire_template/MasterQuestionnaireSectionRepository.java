package com.kairos.persistance.repository.master_data_management.questionnaire_template;


import com.kairos.persistance.model.master_data_management.questionnaire_template.MasterQuestionnaireSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
public interface MasterQuestionnaireSectionRepository extends MongoRepository<MasterQuestionnaireSection, BigInteger> ,CustomMasterQuestionSectionRepository {


    @Query("{deleted:false,countryId:?0,title:{$regex:?1,$options:'i'}}")
    MasterQuestionnaireSection findMasterQuestionByTitleAndCountryId(Long countryId, String name);

    @Query("{countryId:?0,_id:?1,deleted:false}")
    MasterQuestionnaireSection findByIdAndNonDeleted(Long countryId, BigInteger id);

    @Query("{countryId:?0,_id:{$in:?1},deleted:false}")
    List<MasterQuestionnaireSection> getQuestionnniareSectionListByIds(Long countryId, List<BigInteger> ids);

    MasterQuestionnaireSection findByid(BigInteger id);


}
