package com.vip.saturn.job.console.controller;

import com.alibaba.fastjson.JSON;
import com.vip.saturn.job.console.domain.RestApiErrorResult;
import com.vip.saturn.job.console.domain.RestApiJobInfo;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.service.RestApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author hebelala
 */
@Controller
@RequestMapping("/rest/v1")
public class RestApiController {

    private final static Logger logger = LoggerFactory.getLogger(RestApiController.class);

    @Resource
    private RestApiService restApiService;

    @RequestMapping(value = "/{namespace}/jobs", method = RequestMethod.GET)
    public ResponseEntity<String> getJobs(@PathVariable("namespace") String namespace, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            if (namespace == null || namespace.trim().length() == 0) {
                throw new SaturnJobConsoleException("The namespace of parameter is required");
            }
            List<RestApiJobInfo> restApiJobInfos = restApiService.getRestApiJobInfos(namespace);
            return new ResponseEntity<String>(JSON.toJSONString(restApiJobInfos), httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            String message = null;
            if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                message = e.toString();
            } else {
                message = e.getMessage();
            }
            RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
            restApiErrorResult.setMessage(message);
            return new ResponseEntity<String>(JSON.toJSONString(restApiErrorResult), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{namespace}/{jobName}/enable", method = RequestMethod.POST)
    public ResponseEntity<String> enableJob(@PathVariable("namespace") String namespace, @PathVariable("jobName") String jobName, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            if (namespace == null || namespace.trim().length() == 0) {
                throw new SaturnJobConsoleException("The namespace of parameter is required");
            }
            if (jobName == null || jobName.trim().length() == 0) {
                throw new SaturnJobConsoleException("The jobName of parameter is required");
            }
            int code = restApiService.enableJob(namespace, jobName);
            if (code == 201) {
                return new ResponseEntity<String>(httpHeaders, HttpStatus.CREATED);
            } else if (code == 403) {
                RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
                restApiErrorResult.setMessage("The update interval time cannot less than 3 seconds");
                return new ResponseEntity<String>(JSON.toJSONString(restApiErrorResult), httpHeaders, HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<String>(httpHeaders, HttpStatus.OK);
            }
        } catch (Exception e) {
            String message = null;
            if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                message = e.toString();
            } else {
                message = e.getMessage();
            }
            RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
            restApiErrorResult.setMessage(message);
            return new ResponseEntity<String>(JSON.toJSONString(restApiErrorResult), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{namespace}/{jobName}/disable", method = RequestMethod.POST)
    public ResponseEntity<String> disableJob(@PathVariable("namespace") String namespace, @PathVariable("jobName") String jobName, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            if (namespace == null || namespace.trim().length() == 0) {
                throw new SaturnJobConsoleException("The namespace of parameter is required");
            }
            if (jobName == null || jobName.trim().length() == 0) {
                throw new SaturnJobConsoleException("The jobName of parameter is required");
            }
            int code = restApiService.disableJob(namespace, jobName);
            if (code == 201) {
                return new ResponseEntity<String>(httpHeaders, HttpStatus.CREATED);
            } else if (code == 403) {
                RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
                restApiErrorResult.setMessage("The update interval time cannot less than 3 seconds");
                return new ResponseEntity<String>(JSON.toJSONString(restApiErrorResult), httpHeaders, HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<String>(httpHeaders, HttpStatus.OK);
            }
        } catch (Exception e) {
            String message = null;
            if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
                message = e.toString();
            } else {
                message = e.getMessage();
            }
            RestApiErrorResult restApiErrorResult = new RestApiErrorResult();
            restApiErrorResult.setMessage(message);
            return new ResponseEntity<String>(JSON.toJSONString(restApiErrorResult), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
