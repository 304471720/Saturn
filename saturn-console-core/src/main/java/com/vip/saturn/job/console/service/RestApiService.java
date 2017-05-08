package com.vip.saturn.job.console.service;

import com.vip.saturn.job.console.domain.JobConfig;
import com.vip.saturn.job.console.domain.RestApiJobInfo;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;

import java.util.List;

/**
 * @author hebelala
 */
public interface RestApiService {

    /**
     * Create a new job.
     *
     * @param jobConfig construct from the request.
     *
     * @throws SaturnJobConsoleException once the exception is thrown, which means the creation is not successfully. the caller should handle the exception by itself.
     */
    void createJob(String namespace, JobConfig jobConfig) throws SaturnJobConsoleException;

    /**
     * Get the jobs info under the namespace
     */
    List<RestApiJobInfo> getRestApiJobInfos(String namespace) throws SaturnJobConsoleException;

    /**
     * Enable the job if the job is disable.
     *
     * Nothing will return once the the job is enable successfully;
     *
     * @throws SaturnJobConsoleException for below scenarios:
     * <li> The job was already enabled (statusCode = 201) </li>
     * <li> The update interval time cannot less than 3 seconds (statusCode = 403)</li>
     * <li> Enable the job after creation within 10 seconds (statusCode = 403)</li>
     * <li> Other exceptions (statusCode = 500) </li>
     *
     */
    void enableJob(String namespace, String jobName) throws SaturnJobConsoleException;

    /**
     * Disable the job if the job is enable.<br>
     *
     * Nothing will return when disable successfully;
     *
     * @throws SaturnJobConsoleException for below scenarios:
     * <li> The job was already disabled (statusCode = 201) </li>
     * <li> The update interval time cannot less than 3 seconds (statusCode = 403)</li>
     * <li> Other exceptions (statusCode = 500) </li>
     *
     */
    void disableJob(String namespace, String jobName) throws SaturnJobConsoleException;

}
