package com.vip.saturn.job.console.controller.gui;

import com.vip.saturn.job.console.aop.annotation.Audit;
import com.vip.saturn.job.console.aop.annotation.AuditParam;
import com.vip.saturn.job.console.aop.annotation.AuditType;
import com.vip.saturn.job.console.controller.SuccessResponseEntity;
import com.vip.saturn.job.console.domain.DependencyJob;
import com.vip.saturn.job.console.domain.JobConfig;
import com.vip.saturn.job.console.domain.RequestResult;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.exception.SaturnJobConsoleGUIException;
import com.vip.saturn.job.console.service.ExecutorService;
import com.vip.saturn.job.console.service.JobService;
import com.vip.saturn.job.console.utils.AuditInfoContext;
import com.vip.saturn.job.console.utils.SaturnConsoleUtils;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Job overview related operations.
 *
 * @author hebelala
 */
@Controller
@RequestMapping("/console/namespaces/{namespace:.+}/jobs")
public class JobOverviewController extends AbstractGUIController {

	@Resource
	private JobService jobService;

	@Resource
	private ExecutorService executorService;

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping
	public SuccessResponseEntity getJobs(final HttpServletRequest request, @PathVariable String namespace)
			throws SaturnJobConsoleException {
		return new SuccessResponseEntity(jobService.getJobOverviewVo(namespace));
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/groups")
	public SuccessResponseEntity getGroups(final HttpServletRequest request, @PathVariable String namespace)
			throws SaturnJobConsoleException {
		return new SuccessResponseEntity(jobService.getGroups(namespace));
	}

	/**
	 * 获取该作业依赖的所有作业
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/{jobName}/dependency")
	public SuccessResponseEntity getDependingJobs(final HttpServletRequest request,
			@PathVariable String namespace,
			@PathVariable String jobName) throws SaturnJobConsoleException {
		List<DependencyJob> dependencyJobs = jobService.getDependingJobs(namespace, jobName);
		return new SuccessResponseEntity(dependencyJobs);
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/dependency")
	public SuccessResponseEntity batchGetDependingJob(final HttpServletRequest request,
			@PathVariable String namespace,
			@RequestParam List<String> jobNames)
			throws SaturnJobConsoleException {
		Map<String, List<DependencyJob>> dependencyJobsMap = new HashMap<>();
		for (String jobName : jobNames) {
			List<DependencyJob> dependencyJobs = jobService.getDependingJobs(namespace, jobName);
			dependencyJobsMap.put(jobName, dependencyJobs);
		}
		return new SuccessResponseEntity(dependencyJobsMap);
	}

	/**
	 * 获取依赖该作业的所有作业
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/{jobName}/beDependedJobs")
	public SuccessResponseEntity getDependedJobs(final HttpServletRequest request,
			@PathVariable String namespace, @PathVariable String jobName) throws SaturnJobConsoleException {
		List<DependencyJob> dependedJobs = jobService.getDependedJobs(namespace, jobName);
		return new SuccessResponseEntity(dependedJobs);
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/beDependedJobs")
	public SuccessResponseEntity batchGetDependedJobs(final HttpServletRequest request,
			@PathVariable String namespace, @RequestParam List<String> jobNames)
			throws SaturnJobConsoleException {
		Map<String, List<DependencyJob>> dependencyJobsMap = new HashMap<>();
		for (String jobName : jobNames) {
			List<DependencyJob> dependedJobs = jobService.getDependedJobs(namespace, jobName);
			dependencyJobsMap.put(jobName, dependedJobs);
		}
		return new SuccessResponseEntity(dependencyJobsMap);
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/{jobName}/enable")
	public SuccessResponseEntity enableJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobName") @PathVariable String jobName) throws SaturnJobConsoleException {
		jobService.enableJob(namespace, jobName);
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/enable")
	public SuccessResponseEntity batchEnableJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobNames") @RequestParam List<String> jobNames)
			throws SaturnJobConsoleException {
		for (String jobName : jobNames) {
			jobService.enableJob(namespace, jobName);
		}
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/{jobName}/disable")
	public SuccessResponseEntity disableJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobName") @PathVariable String jobName) throws SaturnJobConsoleException {
		jobService.disableJob(namespace, jobName);
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/disable")
	public SuccessResponseEntity batchDisableJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobNames") @RequestParam List<String> jobNames)
			throws SaturnJobConsoleException {
		for (String jobName : jobNames) {
			jobService.disableJob(namespace, jobName);
		}
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@DeleteMapping(value = "/{jobName}")
	public SuccessResponseEntity removeJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobName") @PathVariable String jobName) throws SaturnJobConsoleException {
		jobService.removeJob(namespace, jobName);
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@DeleteMapping
	public SuccessResponseEntity batchRemoveJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobNames") @RequestParam List<String> jobNames) throws SaturnJobConsoleException {
		List<String> successJobNames = new ArrayList<>();
		List<String> failJobNames = new ArrayList<>();
		for (String jobName : jobNames) {
			try {
				jobService.removeJob(namespace, jobName);
				successJobNames.add(jobName);
			} catch (Exception e) {
				failJobNames.add(jobName);
			}
		}
		if (!failJobNames.isEmpty()) {
			StringBuilder message = new StringBuilder();
			message.append("删除成功的作业:" + successJobNames.toString()).append("，").append("删除失败的作业:")
					.append(failJobNames.toString());
			throw new SaturnJobConsoleGUIException(message.toString());
		}
		return new SuccessResponseEntity();
	}

	/**
	 * 批量设置作业的优先Executor
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/preferExecutors")
	public SuccessResponseEntity batchSetPreferExecutors(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobNames") @RequestParam List<String> jobNames,
			@AuditParam("preferList") @RequestParam String preferList)
			throws SaturnJobConsoleException {
		for (String jobName : jobNames) {
			jobService.setPreferList(namespace, jobName, preferList);
		}
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/jobs")
	public SuccessResponseEntity createJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace, JobConfig jobConfig)
			throws SaturnJobConsoleException {
		jobService.addJob(namespace, jobConfig);
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/{jobNameCopied}/copy")
	public SuccessResponseEntity copyJob(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace,
			@AuditParam("jobNameCopied") @PathVariable String jobNameCopied, JobConfig jobConfig)
			throws SaturnJobConsoleException {
		jobService.copyJob(namespace, jobConfig, jobNameCopied);
		return new SuccessResponseEntity();
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@PostMapping(value = "/import")
	public SuccessResponseEntity importJobs(final HttpServletRequest request,
			@AuditParam("namespace") @PathVariable String namespace, @RequestParam("file") MultipartFile file)
			throws SaturnJobConsoleException {
		if (file.isEmpty()) {
			throw new SaturnJobConsoleGUIException("请上传一个有内容的文件");
		}
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || !originalFilename.endsWith(".xls")) {
			throw new SaturnJobConsoleGUIException("仅支持.xls文件导入");
		}
		AuditInfoContext.put("originalFilename", originalFilename);
		return new SuccessResponseEntity(jobService.importJobs(namespace, file));
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@Audit
	@GetMapping(value = "/export")
	public void exportJobs(final HttpServletRequest request, @AuditParam("namespace") @PathVariable String namespace,
			final HttpServletResponse response) throws SaturnJobConsoleException {
		File exportJobFile = jobService.exportJobs(namespace);
		String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String exportFileName = namespace + "_allJobs_" + currentTime + ".xls";
		SaturnConsoleUtils.exportExcelFile(response, exportJobFile, exportFileName, true);
	}

	/**
	 * 获取该作业可选择的优先Executor
	 */
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/{jobName}/executors")
	public SuccessResponseEntity getExecutors(final HttpServletRequest request,
			@PathVariable String namespace, @PathVariable String jobName) throws SaturnJobConsoleException {
		return new SuccessResponseEntity(jobService.getCandidateExecutors(namespace, jobName));
	}

}
