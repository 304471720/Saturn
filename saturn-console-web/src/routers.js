import Vue from 'vue';
import Router from 'vue-router';
import RouterMapper from './utils/router_mapper';
import JobManage from './pages/job_manage/job_manage';
import JobList from './pages/job_list/job_list';
import JobOverview from './pages/job_list/job_overview';
import ExecutorOverview from './pages/job_list/executor_overview';
import JobDetail from './pages/job_detail/job_detail';
import JobSetting from './pages/job_detail/job_setting';
import JobSharding from './pages/job_detail/job_sharding';
import JobExecution from './pages/job_detail/job_execution';
import JobStatistics from './pages/job_detail/job_statistics';
import DashboardManage from './pages/dashboard_manage/dashboard_manage';
import DashboardDetail from './pages/dashboard_manage/dashboard_detail';
import DomainStatistic from './pages/dashboard_manage/domain_statistic';
import ExecutorStatistic from './pages/dashboard_manage/executor_statistic';
import JobStatistic from './pages/dashboard_manage/job_statistic';
import RegistryManage from './pages/registry_manage/registry_manage';

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: RouterMapper.GetPath('jobManage'),
      name: 'job_manage',
      component: JobManage,
    }, {
      path: '/',
      redirect: 'job_manage',
    }, {
      path: RouterMapper.GetPath('jobList'),
      component: JobList,
      children: [
        { path: '', redirect: 'job_overview' },
        { name: 'job_overview', path: RouterMapper.GetPath('jobOverview'), component: JobOverview },
        { name: 'executor_overview', path: RouterMapper.GetPath('executorOverview'), component: ExecutorOverview },
      ],
    }, {
      path: RouterMapper.GetPath('jobDetail'),
      component: JobDetail,
      children: [
        { path: '', redirect: 'job_setting' },
        { name: 'job_setting', path: RouterMapper.GetPath('jobSetting'), component: JobSetting },
        { name: 'job_sharding', path: RouterMapper.GetPath('jobSharding'), component: JobSharding },
        { name: 'job_execution', path: RouterMapper.GetPath('jobExecution'), component: JobExecution },
        { name: 'job_statistics', path: RouterMapper.GetPath('jobStatistics'), component: JobStatistics },
      ],
    }, {
      path: RouterMapper.GetPath('dashboardManage'),
      name: 'dashboard_manage',
      component: DashboardManage,
    }, {
      path: RouterMapper.GetPath('dashboardDetail'),
      component: DashboardDetail,
      children: [
        { path: '', redirect: 'domain_statistic' },
        { name: 'domain_statistic', path: RouterMapper.GetPath('domainStatistic'), component: DomainStatistic },
        { name: 'executor_statistic', path: RouterMapper.GetPath('executorStatistic'), component: ExecutorStatistic },
        { name: 'job_statistic', path: RouterMapper.GetPath('jobStatistic'), component: JobStatistic },
      ],
    }, {
      path: RouterMapper.GetPath('registryManage'),
      name: 'registry_manage',
      component: RegistryManage,
    },
  ],
});
