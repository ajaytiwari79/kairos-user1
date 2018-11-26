package com.kairos.persistence.repository.data_inventory.Assessment;


import com.kairos.enums.gdpr.AssessmentStatus;
import com.kairos.persistence.model.data_inventory.assessment.Assessment;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
@JaversSpringDataAuditable
public interface AssessmentMongoRepository extends MongoBaseRepository<Assessment,BigInteger>,CustomAssessmentRepository {



    @Query("{deleted:false,organizationId:?0,_id:?1}")
    Assessment findByUnitIdAndId(Long unitId, BigInteger assessmentId);

    @Query("{deleted:false,organizationId:?0,_id:?1,assessmentStatus:?2}")
    Assessment findByUnitIdAndIdAndAssessmentStatus(Long unitId, BigInteger assessmentId, AssessmentStatus assessmentStatus);



}
