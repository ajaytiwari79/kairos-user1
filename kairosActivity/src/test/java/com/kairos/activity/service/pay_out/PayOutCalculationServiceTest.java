package com.kairos.activity.service.pay_out;

import com.kairos.activity.persistence.model.activity.Activity;
import com.kairos.activity.persistence.model.activity.tabs.BalanceSettingsActivityTab;
import com.kairos.activity.persistence.model.pay_out.DailyPayOutEntry;
import com.kairos.activity.persistence.repository.activity.ActivityMongoRepository;
import com.kairos.activity.response.dto.ActivityDTO;
import com.kairos.activity.response.dto.ShiftQueryResultWithActivity;
import com.kairos.activity.response.dto.pay_out.UnitPositionWithCtaDetailsDTO;
import com.kairos.activity.util.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PayOutCalculationServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(PayOutCalculationServiceTest.class);

    @InjectMocks
    PayOutCalculationService payOutCalculationService;

    //This is for Temp CTA
    @InjectMocks
    PayOutService payOutService;
    @Mock
    ActivityMongoRepository activityMongoRepository;

    List<ShiftQueryResultWithActivity> shifts = new ArrayList<>(3);
    Interval interval = null;
    Activity activity = null;

    @Before
    public void getMockShifts(){
        activity = new Activity(new BalanceSettingsActivityTab(new BigInteger("123")));
        activity.setId(new BigInteger("125"));
        DateTime startDate = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime("22/02/2018 00:00:00");
        DateTime endDate = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime("23/02/2018 00:00:00");
        interval = new Interval(startDate,endDate);
        ShiftQueryResultWithActivity shift = new ShiftQueryResultWithActivity(interval.getStart().minusHours(2).toDate(),interval.getStart().plusMinutes(120).toDate(),activity);
        shifts.add(shift);
        shift = new ShiftQueryResultWithActivity(interval.getStart().plusMinutes(240).toDate(),interval.getStart().plusMinutes(720).toDate(),activity);
        shifts.add(shift);
        shift = new ShiftQueryResultWithActivity(interval.getStart().plusMinutes(1020).toDate(),interval.getStart().plusMinutes(1560).toDate(),activity);
        shifts.add(shift);
    }

    @Test
    public void calculatePayOut(){
        when(activityMongoRepository.findAllActivityByUnitId(Mockito.anyLong())).thenReturn(Arrays.asList(new ActivityDTO(activity.getId())));
        UnitPositionWithCtaDetailsDTO unitPositionWithCtaDetailsDTO = payOutService.getCostTimeAgreement(1225l);
        DailyPayOutEntry dailyPayOutEntry = new DailyPayOutEntry(unitPositionWithCtaDetailsDTO.getUnitPositionId(), unitPositionWithCtaDetailsDTO.getStaffId(), unitPositionWithCtaDetailsDTO.getWorkingDaysPerWeek(), DateUtils.asLocalDate(interval.getStart().toDate()));
        payOutCalculationService.calculateDailyPayOut(interval, unitPositionWithCtaDetailsDTO,shifts, dailyPayOutEntry);
        Assert.assertEquals(dailyPayOutEntry.getTotalPayOutMin(),1130);
        Assert.assertEquals(dailyPayOutEntry.getScheduledMin(),1020);
        Assert.assertEquals(dailyPayOutEntry.getContractualMin(),300);
    }



}
