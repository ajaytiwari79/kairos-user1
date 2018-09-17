package com.kairos.persistence.repository.phase;


import com.kairos.persistence.model.phase.Phase;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;
import com.kairos.dto.activity.phase.PhaseDTO;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by vipul on 25/9/17.
 */
@Repository
public interface PhaseMongoRepository extends MongoBaseRepository<Phase, BigInteger>, CustomPhaseMongoRepository {
    @Query(value = "{organizationId:?0 ,name:?1}")
    Phase findByName(Long unitId, String name);

    @Query(value = "{sequence:?0 ,countryId:?1 ,deleted:false}", count = true)
    long findBySequenceAndCountryIdAndDeletedFalse(Integer sequence, Long countryId);

//    @Query(value = "{sequence:?0 ,organizationId:?1 ,deleted:false}")
//    Phase findBySequenceAndOrganizationIdAndDeletedFalse(Integer sequence, Long organizationId);

    List<PhaseDTO> findByCountryIdAndDeletedFalseOrderByPhaseTypeDescSequenceAsc(Long countryId);

    List<Phase> findByOrganizationIdAndPhaseTypeAndDeletedFalse(Long unitId, String PhaseType);

    List<Phase> findByOrganizationIdAndDeletedFalse(Long unitId);

}
