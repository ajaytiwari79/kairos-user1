package com.kairos.persistance.repository.data_inventory.Assessment;


import com.kairos.persistance.model.data_inventory.assessment.Assessment;
import com.kairos.persistance.repository.custom_repository.MongoBaseRepository;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
@JaversSpringDataAuditable
public interface AssessmentMongoRepository extends MongoBaseRepository<Assessment,BigInteger>,CustomAssessmentRepository {




    @Query("{deleted:false,organizationId:?0,_id:?1}")
    Assessment findByIdAndNonDeleted(Long unitId,BigInteger assessmenytId);


}
