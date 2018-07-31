package com.kairos.activity.counter.distribution.tab;

import java.math.BigInteger;
import java.util.List;

public class TabKPIMappingDTO {
    private String tabId;
    private BigInteger kpiId;
    private BigInteger kpiAssignmentId;

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

    public BigInteger getKpiId() {
        return kpiId;
    }

    public void setKpiId(BigInteger kpiId) {
        this.kpiId = kpiId;
    }

    public BigInteger getKpiAssignmentId() {
        return kpiAssignmentId;
    }

    public void setKpiAssignmentId(BigInteger kpiAssignmentId) {
        this.kpiAssignmentId = kpiAssignmentId;
    }
}
