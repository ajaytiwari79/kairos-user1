package com.kairos.persistence.repository.time_bank;

import com.kairos.persistence.model.time_bank.DailyTimeBankEntry;
import com.kairos.persistence.repository.custom_repository.MongoBaseRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/*
* Created By Pradeep singh rajawat
*  Date-27/01/2018
*
* */

@Repository
public interface TimeBankRepository extends MongoBaseRepository<DailyTimeBankEntry,BigInteger> ,CustomTimeBankRepository{

    @Query("{employmentId:?0,deleted:false,date:{$gte:?1 , $lte:?2}}")
    List<DailyTimeBankEntry> findAllByEmploymentAndDate(Long employmentId, Date startDate, Date endDate);

    @Query(value = "{employmentId:?0,deleted:false,date:{$lte:?1}}")
    List<DailyTimeBankEntry> findAllByEmploymentIdAndBeforeDate(Long employmentId, Date timeBankDate);

    @Query("{deleted:false}")
    List<DailyTimeBankEntry> findAllAndDeletedFalse();

    @Query("{employmentId:?0,deleted:false,date:{$lte:?1}}")
    List<DailyTimeBankEntry> findAllByEmploymentIdAndStartDate(Long employmentId, Date timeBankDate);

    @Query("{employmentId:?0,deleted:false,date:?1}")
    DailyTimeBankEntry findByEmploymentAndDate(Long employmentId, LocalDate startDate);

    @Query("{employmentId:{$in:?0},deleted:false,date:{$gte:?1 , $lte:?2}}")
    List<DailyTimeBankEntry> findAllByEmploymentIdsAndBetweenDate(Collection<Long> employmentIds, Date startDate, Date endDate);

}
