package com.kairos.persistence.repository.user.pay_level;


import com.kairos.persistence.model.user.pay_group_area.PayGroupArea;
import com.kairos.persistence.model.user.pay_group_area.PayGroupAreaQueryResult;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.kairos.persistence.model.constants.RelationshipConstants.*;

/**
 * Created by prabjot on 21/12/17.
 */
@Repository
public interface PayGroupAreaGraphRepository extends Neo4jBaseRepository<PayGroupArea, Long> {

    @Query("match(country:Country)-[:" + HAS_LEVEL + "]->(level:Level)  where id(country) IN {0}\n" +
            "match(level)-[:" + IN_LEVEL + "]-(p:PayGroupArea{deleted:false})-[rel:" + HAS_MUNICIPALITY + "]-(municipality:Municipality)\n" +
            "RETURN  id(municipality) as id,municipality.name as name,municipality.description as description, id(municipality) as municipalityId" +
            "id(level) as levelId,rel.endDateMillis as endDateMillis,rel.startDateMillis as startDateMillis")
    List<PayGroupAreaQueryResult> getPayGroupAreaByCountry(Long countryId);
}
