package com.vip.saturn.job.console.service.impl;

import com.vip.saturn.job.integrate.entity.AlarmInfo;
import com.vip.saturn.job.integrate.exception.ReportAlarmException;
import com.vip.saturn.job.integrate.service.ReportAlarmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * 
 * @author hebelala
 */
@Service
public class ReportAlarmWithLoggerServiceImpl implements ReportAlarmService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportAlarmWithLoggerServiceImpl.class);

	@Override
	public void allShardingError(String namespace, String hostValue) throws ReportAlarmException {
		LOGGER.error("allShardingError, namespace is {}, hostValue is {}", namespace, hostValue);
	}

	@Override
	public void dashboardContainerInstancesMismatch(String namespace, String taskId, int configInstances,
			int runningInstances) throws ReportAlarmException {
		LOGGER.error(
				"dashboardContainerInstancesMismatch, namespace is {}, taskId is {}, configInstances is {}, runningInstances is {}",
				namespace, taskId, configInstances, runningInstances);
	}

	@Override
	public void dashboardAbnormalJob(String namespace, String jobName, String timeZone, long shouldFiredTime)
			throws ReportAlarmException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone(timeZone));
		String shouldFiredTimeFormatted = timeZone + " " + format.format(shouldFiredTime);
		LOGGER.error("dashboardAbnormalJob, namespace is {}, jobName is {}, timeZone is {}, shouldFiredTime is {}",
				namespace, jobName, timeZone, shouldFiredTimeFormatted);
	}

	@Override
	public void dashboardTimeout4AlarmJob(String namespace, String jobName, List<Integer> timeoutItems,
			int timeout4AlarmSeconds) throws ReportAlarmException {
		LOGGER.error(
				"dashboardTimeout4AlarmJob, namespace is {}, jobName is {}, timeoutItems is {}, timeout4AlarmSeconds is {}",
				namespace, jobName, timeoutItems, timeout4AlarmSeconds);
	}

	@Override
	public void raise(String namespace, String jobName, String executorName, Integer shardItem, AlarmInfo alarmInfo)
			throws ReportAlarmException {
		LOGGER.error("raise, namespace is {}, jobName is {}, executorName is {}, shardItem is {}, alarmInfo is {}",
				namespace, jobName, executorName, shardItem, alarmInfo);
	}

}
