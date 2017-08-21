package com.kairos.persistence.repository.user.country;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import com.kairos.persistence.model.user.country.CountryHolidayCalender;

/**
 * Created by oodles on 20/9/16.
 */

@Repository
public interface CountryHolidayCalenderGraphRepository extends GraphRepository<CountryHolidayCalender> {
    List<CountryHolidayCalender> findAll();

    @Query("MATCH (ch:CountryHolidayCalender) WHERE id(ch) ={0}   SET ch.disabled = true return ch")
    CountryHolidayCalender safeDelete(Long id);

    @Query("MATCH (c:Country)-[:HAS_HOLIDAY]->(ch:CountryHolidayCalender{isEnable:true})  WHERE ch.googleCalId={0} AND  id(c)={1} return count(ch) ")
    int checkIfHolidayExist(String id, Long countryId);

    @Query("MATCH (c:Country)-[:HAS_HOLIDAY]->(ch:CountryHolidayCalender)  WHERE ch.googleCalId={0} AND  id(c)={1} return ch ")
    CountryHolidayCalender getExistingHoliday(String id, Long countryId);
}
