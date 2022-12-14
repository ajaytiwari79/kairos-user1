package com.kairos.persistence.repository.user.client;

import com.kairos.persistence.model.client.AccessToLocation;
import com.kairos.persistence.repository.custom_repository.Neo4jBaseRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static com.kairos.persistence.model.constants.RelationshipConstants.ADDRESS_ACCESS_DEAILS;


/**
 * Created by oodles on 23/11/16.
 */
@Repository
public interface AccessToLocationGraphRepository extends Neo4jBaseRepository<AccessToLocation,Long> {


    @Query("MATCH (c:Client)-[r]->(cd:ContactAddress{isEnabled:true}) WHERE id(c)={0}   WITH cd,r " +
            "OPTIONAL MATCH (cd)-[:"+ADDRESS_ACCESS_DEAILS+"]->(ac:AccessToLocation) RETURN  {accessDetails:collect ( DISTINCT {   " +
            "      id:id(ac),  " +
            "      addressId:id(cd),  " +
            "      reasonForDailyPhoneCall:ac.reasonForDailyPhoneCall, " +
            "      serialNumber:ac.serialNumber,  " +
            "      keySystemDescription:ac.keySystemDescription,  " +
            "      reasonForEmergencyCall:ac.reasonForEmergencyCall,   " +
            "      portPhoneNumber:ac.portPhoneNumber,  " +
            "      dailyPhoneCallIsAgreed:ac.dailyPhoneCallIsAgreed,  " +
            "      keySystem:ac.keySystem,  " +
            "      emergencyCallDeviceType:ac.emergencyCallDeviceType,  " +
            "      emergencyCallNumber:ac.emergencyCallNumber,  " +
            "      remarks:ac.remarks,  " +
            "      alarmCode:ac.alarmCode," +
            "      alarmCodeDescription:ac.alarmCodeDescription," +
            "      haveAlarmCode:ac.haveAlarmCode," +
            "      howToAccessAddress:ac.howToAccessAddress," +
            "       profilePic: {1} + ac.accessPhotoURL, "+
            "      addressType:type(r)"+
            "})} as accessDetails")
    Map<String,Object> findHomeAccessToLocation(long clientId, String imageUrl);
}
