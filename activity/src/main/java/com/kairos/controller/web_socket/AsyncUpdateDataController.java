package com.kairos.controller.web_socket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kairos.constants.ApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by neuron on 12/5/17.
 */

@RestController
public class AsyncUpdateDataController{

    Logger logger = LoggerFactory.getLogger(AsyncUpdateDataController.class);

    @MessageMapping("/planner/dynamic/{unitId}")
    @SendTo(ApiConstants.API_V1+"/ws/dynamic-push/dynamic/{unitId}")
    public CitizenPushedData dynamicTaskSyncResponse(@DestinationVariable String unitId, CitizenPushedData citizenPushedData){
        logger.info(" web socket responding");
        return citizenPushedData;
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class CitizenPushedData{

    private int onEscalation = 0;
    private int notDraggedAndDropped = 0 ;
    private int updatedInfo = 0;
    private int unplannedStatus = 0;
    private int longDrivingTime = 0;
    private int mostDriven = 0;

    private String citizenId;

    public CitizenPushedData(){

    }

    public int getOnEscalation() {
        return onEscalation;
    }

    public void setOnEscalation(int onEscalation) {
        this.onEscalation = onEscalation;
    }

    public int getNotDraggedAndDropped() {
        return notDraggedAndDropped;
    }

    public void setNotDraggedAndDropped(int notDraggedAndDropped) {
        this.notDraggedAndDropped = notDraggedAndDropped;
    }

    public int getUpdatedInfo() {
        return updatedInfo;
    }

    public void setUpdatedInfo(int updatedInfo) {
        this.updatedInfo = updatedInfo;
    }

    public int getUnplannedStatus() {
        return unplannedStatus;
    }

    public void setUnplannedStatus(int unplannedStatus) {
        this.unplannedStatus = unplannedStatus;
    }

    public int getLongDrivingTime() {
        return longDrivingTime;
    }

    public void setLongDrivingTime(int longDrivingTime) {
        this.longDrivingTime = longDrivingTime;
    }

    public int getMostDriven() {
        return mostDriven;
    }

    public void setMostDriven(int mostDriven) {
        this.mostDriven = mostDriven;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public CitizenPushedData(String citizenId, int onEscalation){
        this.citizenId = citizenId;
        this.onEscalation = onEscalation;
    }

    public String getContent() {
        return this.citizenId;
    }
}