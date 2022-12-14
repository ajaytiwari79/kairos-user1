package com.kairos.service;

import com.kairos.commons.utils.DateTimeInterval;
import com.kairos.commons.utils.ObjectUtils;
import com.kairos.dto.activity.kpi.StaffKpiFilterDTO;
import org.springframework.stereotype.Service;

@Service
public class StaffChildCountKPIService implements KPIService {
    @Override
    public <T> double get(Long staffId, DateTimeInterval dateTimeInterval, KPICalculationRelatedInfo kpiCalculationRelatedInfo, T t) {
        return getChildrenCount(staffId, kpiCalculationRelatedInfo);
    }

    private long getChildrenCount(Long staffId, KPICalculationRelatedInfo kpiCalculationRelatedInfo) {
        StaffKpiFilterDTO staff = kpiCalculationRelatedInfo.getStaffIdAndStaffKpiFilterMap().get(staffId);
        return ObjectUtils.isNotNull(staff) && ObjectUtils.isCollectionNotEmpty(staff.getStaffChildDetails()) ? staff.getStaffChildDetails().size() : 0;
    }
}
