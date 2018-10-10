package com.kairos.persistence.repository.staffing_level;/*
 *Created By Pavan on 10/10/18
 *
 */

import com.kairos.persistence.model.staffing_level.StaffingLevelActivityRanking;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;

import java.math.BigInteger;
import java.util.List;

public interface StaffingLevelActivityRankRepository extends MongoBaseRepository<StaffingLevelActivityRanking,BigInteger> {

   List<StaffingLevelActivityRanking> findAllByStaffingLevelIdAndStaffingLevelDateAndDeletedFalse();
}
