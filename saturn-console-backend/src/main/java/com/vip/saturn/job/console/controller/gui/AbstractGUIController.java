package com.vip.saturn.job.console.controller.gui;

import com.vip.saturn.job.console.controller.AbstractController;
import com.vip.saturn.job.console.domain.RequestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class AbstractGUIController extends AbstractController{

	private static final Logger log = LoggerFactory.getLogger(AbstractGUIController.class);

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<RequestResult> handleException(Throwable ex)  {
		log.debug("exception happens inside GUI controller operation:", ex.getMessage(), ex);
		return new ResponseEntity<>(new RequestResult(false, ex.getMessage()), HttpStatus.OK);
	}
}
