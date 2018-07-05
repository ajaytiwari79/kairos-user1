package com.kairos.controller.counters;


import com.kairos.activity.enums.counter.ChartType;
import com.kairos.activity.enums.counter.CounterSize;
import com.kairos.activity.enums.counter.RepresentationUnit;
import com.kairos.enums.CounterType;
import com.kairos.persistence.model.counter.KPI;
import com.kairos.persistence.model.counter.chart.BaseChart;
import com.kairos.persistence.model.counter.chart.GaugeChart;
import com.kairos.persistence.model.counter.chart.SingleNumberChart;
import com.kairos.service.counter.CounterDataService;
import com.kairos.util.response.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.kairos.constants.ApiConstants.COUNTER_DATA_URL;

/*
 * @author: mohit.shakya@oodlestechnologies.com
 * @dated: Jun/27/2018
 */

@RestController
@RequestMapping(COUNTER_DATA_URL)
public class CounterDataController {

    @Inject
    CounterDataService counterDataService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCounterInitialData(@RequestParam BigInteger solverConfigId, @PathVariable Long unitId) {
        //TODO: TO BE MODIFIED, CURRENTLY MOCK ONLY
        /*
    return map keys( List<BigInteger> order, metaData )
    { block1, block2, block3 }
    { order }
    { size:same }
    { filter }
         */
        KPI kpi;
        BaseChart baseChart;
        ArrayList<KPI> kpiList = new ArrayList<>();
        //TODO: TO COMPLETE
        kpiList.addAll(counterDataService.getCountersData(unitId, solverConfigId));

        return ResponseHandler.generateResponse(HttpStatus.OK, true, kpiList);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetaData(@RequestParam String moduleId){
        //TODO: TO BE MODIFIED CURRENTLY MOCK ONLY
        Map<String, Object> respData = new HashMap<>();
        Map<String, BigInteger> tabData = new HashMap<>();
        tabData.put("tab1", BigInteger.valueOf(1));
        tabData.put("tab1", BigInteger.valueOf(2));
        tabData.put("tab1", BigInteger.valueOf(3));
        tabData.put("tab1", BigInteger.valueOf(4));
        tabData.put("tab1", BigInteger.valueOf(5));
        tabData.put("tab1", BigInteger.valueOf(6));
        tabData.put("tab1", BigInteger.valueOf(7));
        tabData.put("tab1", BigInteger.valueOf(8));
        tabData.put("tab2", BigInteger.valueOf(9));
        tabData.put("tab2", BigInteger.valueOf(10));
        tabData.put("tab2", BigInteger.valueOf(11));
        tabData.put("tab2", BigInteger.valueOf(12));
        respData.put("tabDistribution", tabData);
        return ResponseHandler.generateResponse(HttpStatus.ACCEPTED, true, respData);
    }
}
