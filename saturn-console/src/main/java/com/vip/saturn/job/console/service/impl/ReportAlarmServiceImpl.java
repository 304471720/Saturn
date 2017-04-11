package com.vip.saturn.job.console.service.impl;

import com.vip.saturn.job.integrate.exception.ReportAlarmException;
import com.vip.saturn.job.integrate.service.ReportAlarmService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hebelala
 */
@Service
public class ReportAlarmServiceImpl implements ReportAlarmService {

    @Override
    public void allShardingError(String namespace, String hostValue) throws ReportAlarmException {

    }

    @Override
    public void dashboardContainerInstancesMismatch(String namespace, String taskId, int configInstances, int runningInstances) throws ReportAlarmException {

    }

    @Override
    public void dashboardAbnormalJob(String namespace, String jobName, String shouldFiredTime) throws ReportAlarmException {

    }

    @Override
    public void dashboardTimeout4AlarmJob(String namespace, String jobName, List<Integer> timeoutItems, int timeout4AlarmSeconds) throws ReportAlarmException {

    }
}
