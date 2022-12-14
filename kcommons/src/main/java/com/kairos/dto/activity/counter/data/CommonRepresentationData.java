package com.kairos.dto.activity.counter.data;

import com.kairos.dto.activity.counter.chart.CommonKpiDataUnit;
import com.kairos.dto.activity.counter.enums.ChartType;
import com.kairos.dto.activity.counter.enums.RepresentationUnit;
import com.kairos.dto.activity.counter.enums.XAxisConfig;

import java.math.BigInteger;
import java.util.List;

public class CommonRepresentationData {
    protected BigInteger counterId;
    private String title;
    private ChartType chartType;
    private String displayUnit;
    private RepresentationUnit unit;
    protected List<CommonKpiDataUnit> dataList;

    public CommonRepresentationData(){

    }

    public CommonRepresentationData(BigInteger counterId, String title, ChartType chartType, XAxisConfig XAxisConfig, RepresentationUnit unit, List<CommonKpiDataUnit> dataList){

        this.counterId = counterId;
        this.title = title;
        this.chartType = chartType;
        this.displayUnit = XAxisConfig.getDisplayValue();
        this.unit = unit;
        this.dataList = dataList;

    }

    public BigInteger getCounterId() {
        return counterId;
    }

    public void setCounterId(BigInteger counterId) {
        this.counterId = counterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayUnit() {
        return displayUnit;
    }

    public void setDisplayUnit(String displayUnit) {
        this.displayUnit = displayUnit;
    }

    public RepresentationUnit getUnit() {
        return unit;
    }

    public void setUnit(RepresentationUnit unit) {
        this.unit = unit;
    }

    public List<CommonKpiDataUnit> getDataList() {
        return dataList;
    }

    public void setDataList(List<CommonKpiDataUnit> dataList) {
        this.dataList = dataList;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }


}
