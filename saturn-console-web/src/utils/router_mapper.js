const routermapperlist = [
    { name: 'jobManage', path: '/job_manage' },
    { name: 'jobList', path: '/job_list/:domain' },
    { name: 'jobOverview', path: '/job_list/:domain/job_overview' },
    { name: 'executorOverview', path: '/job_list/:domain/executor_overview' },
    { name: 'namespaceAlarmCenter', path: '/job_list/:domain/namespace_alarm_center' },
    { name: 'namespaceAbnormalJobs', path: '/job_list/:domain/namespace_alarm_center/namespace_abnormal_jobs' },
    { name: 'namespaceTimeoutJobs', path: '/job_list/:domain/namespace_alarm_center/namespace_timeout_jobs' },
    { name: 'namespaceFailoverJobs', path: '/job_list/:domain/namespace_alarm_center/namespace_failover_jobs' },
    { name: 'namespaceAbnormalContainers', path: '/job_list/:domain/namespace_alarm_center/namespace_abnormal_containers' },
    { name: 'jobDetail', path: '/job_detail/:domain/:jobName' },
    { name: 'jobSetting', path: '/job_detail/:domain/:jobName/job_setting' },
    { name: 'jobSharding', path: '/job_detail/:domain/:jobName/job_sharding' },
    { name: 'jobExecution', path: '/job_detail/:domain/:jobName/job_execution' },
    { name: 'jobStatistics', path: '/job_detail/:domain/:jobName/job_statistics' },
    { name: 'jobAlarmCenter', path: '/job_detail/:domain/:jobName/job_alarm_center' },
    { name: 'jobAbnormalJobs', path: '/job_detail/:domain/:jobName/job_alarm_center/job_abnormal_jobs' },
    { name: 'jobTimeoutJobs', path: '/job_detail/:domain/:jobName/job_alarm_center/job_timeout_jobs' },
    { name: 'jobFailoverJobs', path: '/job_detail/:domain/:jobName/job_alarm_center/job_failover_jobs' },
    { name: 'dashboardManage', path: '/dashboard_manage' },
    { name: 'dashboardDetail', path: '/dashboard_detail' },
    { name: 'domainStatistic', path: '/dashboard_detail/domain_statistic' },
    { name: 'executorStatistic', path: '/dashboard_detail/executor_statistic' },
    { name: 'jobStatistic', path: '/dashboard_detail/job_statistic' },
    { name: 'alarmManage', path: '/alarm_manage' },
    { name: 'alarmAbnormalJobs', path: '/alarm_manage/alarm_abnormal_jobs' },
    { name: 'alarmTimeoutJobs', path: '/alarm_manage/alarm_timeout_jobs' },
    { name: 'unableFailoverJobs', path: '/alarm_manage/unable_failover_jobs' },
    { name: 'alarmAbnormalContainers', path: '/alarm_manage/alarm_abnormal_containers' },
    { name: 'registryManage', path: '/registry_manage' },
];

export default {
  GetPath(name) {
    const items = routermapperlist.filter(x => x.name === name);
    if (items !== undefined) {
      return items[0].path;
    }
    throw new Error('can not find route path ');
  },
};
