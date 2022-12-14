package com.kairos.persistence.repository.reason_code;

import com.kairos.dto.user.reason_code.ReasonCodeDTO;
import com.kairos.enums.reason_code.ReasonCodeType;
import com.kairos.persistence.model.reason_code.ReasonCode;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Repository
public interface ReasonCodeRepository extends MongoBaseRepository<ReasonCode,BigInteger> {

    List<ReasonCodeDTO> findReasonCodesByCountryIdAndReasonCodeTypeAndDeletedFalse(long countryId, ReasonCodeType reasonCodeType);

    @Query(value = "{'countryId':?0,deleted:false,_id:{$ne:?1},reasonCodeType:?3,$or:[{name:?2},{code:4}]}",exists = true)
    boolean existsByCountryIdAndIdNotInAndNameOrReasonCodeTypeOrCode(Long countryId, BigInteger reasonCodeId, String name, ReasonCodeType reasonCodeType, String code);

    List<ReasonCodeDTO> findByUnitIdAndReasonCodeTypeAndDeletedFalse(long unitId, ReasonCodeType reasonCodeType);

    @Query(value = "{'unitId':?0,deleted:false,_id:{$ne:?4},reasonCodeType:?2,$or:[{name:?1},{code:?3}]}",exists = true)
    boolean existsByUnitIdAndNameOrReasonCodeTypeOrCodeAndIdNotIn(Long unitId,  String name, ReasonCodeType reasonCodeType,String code,BigInteger reasonCodeId);

    boolean existsByTimeTypeIdAndDeletedFalse(BigInteger timeTypeId);

    List<ReasonCodeDTO> findReasonCodeByCountryIdAndDeletedFalseOrderByCreatedAt(Long countryId);

    List<ReasonCodeDTO> findAllByIdAndDeletedFalse(Set<BigInteger> ids);

    List<ReasonCodeDTO> findByReasonCodeTypeAndUnitIdNotNull(ReasonCodeType reasonCodeType);
}
