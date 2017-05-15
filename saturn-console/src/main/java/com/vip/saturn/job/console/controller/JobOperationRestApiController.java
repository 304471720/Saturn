package com.vip.saturn.job.console.controller;

import com.alibaba.fastjson.JSON;
import com.vip.saturn.job.console.domain.JobConfig;
import com.vip.saturn.job.console.domain.RestApiErrorResult;
import com.vip.saturn.job.console.domain.RestApiJobInfo;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.exception.SaturnJobConsoleHttpException;
import com.vip.saturn.job.console.service.JobOperationService;
import com.vip.saturn.job.console.service.RestApiService;
import com.vip.saturn.job.console.utils.ControllerUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author hebelala
 */
@Controller
@RequestMapping("/rest/v1")
public class JobOperationRestApiController {

    public final static String BAD_REQ_MSG_PREFIX =  "Invalid request.";

    public final static String INVALID_REQUEST_MSG = BAD_REQ_MSG_PREFIX + " Parameter: {%s} %s";

    public final static String MISSING_REQUEST_MSG = BAD_REQ_MSG_PREFIX + " Missing parameter: {%s}";

    public final static String NOT_EXISTED_PREFIX = "does not exists";

    private final static Logger logger = LoggerFactory.getLogger(JobOperationRestApiController.class);

    @Resource
    private RestApiService restApiService;

    @Resource
    private JobOperationService jobOperationService;

    @RequestMapping(value = "/{namespace}/jobs", method = RequestMethod.POST)
    public ResponseEntity<String> create(@PathVariable("namespace") String namespace, @RequestBody Map<String, Object> reqParams) {
        try{
            JobConfig jobConfig = constructJobConfig(namespace, reqParams);
            jobOperationService.validateJobConfig(jobConfig);

            restApiService.createJob(namespace, jobConfig);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e){
            return constructOtherResponses(e);
        }
    }

    @RequestMapping(value = "/{namespace}/jobs/{jobName}", method = RequestMethod.GET)
    public ResponseEntity<String> query(@PathVariable("namespace") String namespace, @PathVariable("jobName") String jobName) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try{
            if(StringUtils.isBlank(namespace)){
                throw new SaturnJobConsoleHttpException(HttpStatus.BAD_REQUEST.value(), String.format(MISSING_REQUEST_MSG, "namespace"));
            }
            if(StringUtils.isBlank(jobName)){
                throw new SaturnJobConsoleHttpException(HttpStatus.BAD_REQUEST.value(), String.format(MISSING_REQUEST_MSG, "jobName"));
            }

            RestApiJobInfo restAPIJobInfo = restApiService.getRestAPIJobInfo(namespace, jobName);

            return new ResponseEntity<>(JSON.toJSONString(restAPIJobInfo), httpHeaders, HttpStatus.OK);
        } catch (Exception e){
            return constructOtherResponses(e);
        }
    }

    @RequestMapping(value = "/{namespace}/jobs", method = RequestMethod.GET)
    public ResponseEntity<String> queryAll(@PathVariable("namespace") String namespace, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            if (namespace == null || namespace.trim().length() == 0) {
                throw new SaturnJobConsoleException("The namespace of parameter is required");
            }
            List<RestApiJobInfo> restApiJobInfos = restApiService.getRestApiJobInfos(namespace);
            return new ResponseEntity<>(JSON.toJSONString(restApiJobInfos), httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return constructOtherResponses(e);
        }
    }

    @RequestMapping(value = {"/{namespace}/{jobName}/enable", "/{namespace}/jobs/{jobName}/enable"}, method = RequestMethod.POST)
    public ResponseEntity<String> enable(@PathVariable("namespace") String namespace, @PathVariable("jobName") String jobName, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            if (namespace == null || namespace.trim().length() == 0) {
                throw new SaturnJobConsoleException("The namespace of parameter is required");
            }
            if (jobName == null || jobName.trim().length() == 0) {
                throw new SaturnJobConsoleException("The jobName of parameter is required");
            }
            restApiService.enableJob(namespace, jobName);
            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return constructOtherResponses(e);
        }
    }

    @RequestMapping(value = {"/{namespace}/{jobName}/disable", "/{namespace}/jobs/{jobName}/disable"}, method = RequestMethod.POST)
    public ResponseEntity<String> disable(@PathVariable("namespace") String namespace, @PathVariable("jobName") String jobName, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            if (namespace == null || namespace.trim().length() == 0) {
                throw new SaturnJobConsoleException("The namespace of parameter is required");
            }
            if (jobName == null || jobName.trim().length() == 0) {
                throw new SaturnJobConsoleException("The jobName of parameter is required");
            }
            restApiService.disableJob(namespace, jobName);
            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            return constructOtherResponses(e);
        }
    }

    private ResponseEntity<String> constructOtherResponses(Exception e){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        if (e instanceof SaturnJobConsoleHttpException){
            SaturnJobConsoleHttpException saturnJobConsoleHttpException = (SaturnJobConsoleHttpException) e;
            int statusCode = saturnJobConsoleHttpException.getStatusCode();
            switch (statusCode) {
                case 201:
                    return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
                default:
                    return constructErrorResponse(e.getMessage(), HttpStatus.valueOf(statusCode));
            }
        } else if (e instanceof SaturnJobConsoleException){
            if (e.getMessage().contains(NOT_EXISTED_PREFIX)){
                return constructErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }

        String message = null;
        if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
            message = e.toString();
        } else {
            message = e.getMessage();
        }

        RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
        restApiErrorResult.setMessage(message);
        return new ResponseEntity<>(JSON.toJSONString(restApiErrorResult), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<String> constructErrorResponse(String errorMsg, HttpStatus status){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
        restApiErrorResult.setMessage(errorMsg);

        return new ResponseEntity<>(JSON.toJSONString(restApiErrorResult), httpHeaders, status);
    }


    private JobConfig constructJobConfig(String namespace, Map<String, Object> reqParams) throws SaturnJobConsoleException {
        JobConfig jobConfig = new JobConfig();

        if(StringUtils.isBlank(namespace)){
            throw new SaturnJobConsoleHttpException(HttpStatus.BAD_REQUEST.value(), String.format(MISSING_REQUEST_MSG, "namespace"));
        }
        jobConfig.setNamespace(namespace);

        if(!reqParams.containsKey("jobConfig")){
            throw new SaturnJobConsoleHttpException(HttpStatus.BAD_REQUEST.value(), String.format(INVALID_REQUEST_MSG, "jobConfig", "cannot be blank"));
        }
        Map<String, Object> configParams = (Map<String, Object>) reqParams.get("jobConfig");

        jobConfig.setJobName(ControllerUtils.checkAndGetParametersValueAsString(reqParams, "jobName", true));

        jobConfig.setDescription(ControllerUtils.checkAndGetParametersValueAsString(reqParams, "description", false));

        jobConfig.setChannelName(ControllerUtils.checkAndGetParametersValueAsString(configParams, "channelName", false));

        jobConfig.setCron(ControllerUtils.checkAndGetParametersValueAsString(configParams, "cron", false));

        jobConfig.setJobClass(ControllerUtils.checkAndGetParametersValueAsString(configParams, "jobClass", false));

        jobConfig.setJobParameter(ControllerUtils.checkAndGetParametersValueAsString(configParams, "jobParameter", false));

        jobConfig.setJobType(ControllerUtils.checkAndGetParametersValueAsString(configParams, "jobType", true));

        jobConfig.setLoadLevel(ControllerUtils.checkAndGetParametersValueAsInteger(configParams, "loadLevel", false));

        jobConfig.setLocalMode(ControllerUtils.checkAndGetParametersValueAsBoolean(configParams, "localMode", false));

        jobConfig.setPausePeriodDate(ControllerUtils.checkAndGetParametersValueAsString(configParams, "pausePeriodDate", false));

        jobConfig.setPausePeriodTime(ControllerUtils.checkAndGetParametersValueAsString(configParams, "pausePeriodTime", false));

        jobConfig.setPreferList(ControllerUtils.checkAndGetParametersValueAsString(configParams, "preferList", false));

        jobConfig.setQueueName(ControllerUtils.checkAndGetParametersValueAsString(configParams, "queueName", false));

        jobConfig.setShardingItemParameters(ControllerUtils.checkAndGetParametersValueAsString(configParams, "shardingItemParameters", true));

        jobConfig.setShardingTotalCount(ControllerUtils.checkAndGetParametersValueAsInteger(configParams, "shardingTotalCount", true));

        jobConfig.setTimeout4AlarmSeconds(ControllerUtils.checkAndGetParametersValueAsInteger(configParams, "timeout4AlarmSeconds", false));

        jobConfig.setTimeoutSeconds(ControllerUtils.checkAndGetParametersValueAsInteger(configParams, "timeout4Seconds", false));

        jobConfig.setUseDispreferList(ControllerUtils.checkAndGetParametersValueAsBoolean(configParams, "useDispreferList", false));

        jobConfig.setUseSerial(ControllerUtils.checkAndGetParametersValueAsBoolean(configParams, "useSerial", false));

        jobConfig.setJobDegree(ControllerUtils.checkAndGetParametersValueAsInteger(configParams, "jobDegree", false));

        jobConfig.setDependencies(ControllerUtils.checkAndGetParametersValueAsString(configParams, "dependencies", false));

        return jobConfig;
    }


}
