package com.kairos.persistence.repository.staffing_level;

import com.kairos.persistence.model.staffing_level.StaffingLevel;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Repository

public interface StaffingLevelMongoRepository extends MongoBaseRepository<StaffingLevel,BigInteger>,StaffingLevelCustomRepository{

    StaffingLevel findByUnitIdAndCurrentDateAndDeletedFalse(Long unitId, Date currentDate);

    boolean existsByUnitIdAndCurrentDateAndDeletedFalse(Long unitId, Date currentDate);

    @Query("{deleted:false,unitId:?0,currentDate:{$gte:?1,$lte:?2}}")
    List<StaffingLevel> findByUnitIdAndDates(Long unitId, Date startDate, Date endDate);

    @Query("{deleted:false,unitId:?0,currentDate:{$in:?1}}")
    List<StaffingLevel> findByUnitIdAndDates(Long unitId, Set<LocalDate> localDates);

    @Query("{deleted:false,currentDate:{$gt:?1},presenceStaffingLevelInterval:{$elemMatch:{staffingLevelActivities:{$elemMatch:{activityId:?0}}}}}")
    List<StaffingLevel> findPresenceStaffingLevelsByActivityId(BigInteger activityId,Date startDate);

    @Query("{deleted:false,currentDate:{$gt:?1},absenceStaffingLevelInterval:{$elemMatch:{staffingLevelActivities:{$elemMatch:{activityId:?0}}}}}")
    List<StaffingLevel> findAbsenceStaffingLevelsByActivityId(BigInteger activityId,Date startDate);

    List<StaffingLevel> findByUnitIdAndCurrentDateGreaterThanEqualAndCurrentDateLessThanEqualAndDeletedFalseOrderByCurrentDate(Long unitId, Date startDate, Date endDate);

    @Query("{deleted:false,unitId:?0,currentDate:{$gte:?1,$lte:?2}}")
    List<StaffingLevel> findByUnitIdBetweenDates(Long unitId, LocalDate startDate, LocalDate endDate);

    List<StaffingLevel> findAllByDeletedFalse();

    @Query(value = "{deleted:false,unitId:?0,currentDate:{$gte:?1,$lte:?2},$or:[{'presenceStaffingLevelInterval.staffingLevelActivities.activityId':?3},{'absenceStaffingLevelInterval.staffingLevelActivities.activityId':?3}]}")
    List<StaffingLevel> findByUnitIdAndActivityIdBetweenDates(Long unitId, Date startDate, Date endDate,BigInteger activityId);


    @Query(value = "{deleted:false,unitId:?0,$or:[{presenceStaffingLevelInterval:{$elemMatch:{staffingLevelActivities:{$elemMatch:{activityId:?1}}}}},{absenceStaffingLevelInterval:{$elemMatch:{staffingLevelActivities:{$elemMatch:{activityId:?1}}}}}]}",exists = true)
    boolean activityExistsInStaffingLevel(Long unitId,BigInteger activityId);
}
