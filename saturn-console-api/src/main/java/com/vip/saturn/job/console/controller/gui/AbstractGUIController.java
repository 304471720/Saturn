package com.vip.saturn.job.console.controller.gui;

import com.vip.saturn.job.console.controller.AbstractController;
import com.vip.saturn.job.console.domain.RequestResult;
import com.vip.saturn.job.console.domain.RequestResultHelper;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.mybatis.entity.Permission;
import com.vip.saturn.job.console.mybatis.service.AuthorizationService;
import com.vip.saturn.job.console.utils.SessionAttributeKeys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class AbstractGUIController extends AbstractController {

	private static final Logger log = LoggerFactory.getLogger(AbstractGUIController.class);

	private static final String UNKNOWN = "Unkown";

	@Resource
	private AuthorizationService authorizationService;

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<RequestResult> handleException(Throwable ex) {
		log.error("exception happens inside GUI controller operation:" + ex.getMessage(), ex);
		String message = ex.getMessage();
		if (StringUtils.isBlank(message)) {
			message = ex.toString();
		}
		return new ResponseEntity<>(RequestResultHelper.failure(message), HttpStatus.OK);
	}

	private Object getAttributeInSession(String key) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		if (request == null) {
			return null;
		}
		return request.getSession().getAttribute(key);
	}

	public String getUserRealName() {
		String userRealName = (String) getAttributeInSession(SessionAttributeKeys.LOGIN_USER_REAL_NAME);
		return StringUtils.isBlank(userRealName) ? UNKNOWN : userRealName;
	}

	public String getUserName() {
		String userName = (String) getAttributeInSession(SessionAttributeKeys.LOGIN_USER_NAME);
		return StringUtils.isBlank(userName) ? UNKNOWN : userName;
	}

	public void assertIsPermitted(Permission permission, String namespace) throws SaturnJobConsoleException {
		if (!authorizationService.useAuthorization()) {
			return;
		}
		String userOaName = getUserName();
		if (!authorizationService.isPermitted(permission, userOaName, namespace)) {
			throw new SaturnJobConsoleException(String.format("您没有权限，域:%s，权限:%s", namespace, permission.getKey()));
		}
	}

	public void assertIsPermitted(Permission permission) throws SaturnJobConsoleException {
		if (!authorizationService.useAuthorization()) {
			return;
		}
		String userOaName = getUserName();
		if (!authorizationService.isPermitted(permission, userOaName, "")) {
			throw new SaturnJobConsoleException(String.format("您没有权限，权限:%s", permission.getKey()));
		}
	}

	public void assertIsSuper() throws SaturnJobConsoleException {
		if (!authorizationService.useAuthorization()) {
			return;
		}
		String userOaName = getUserName();
		if (!authorizationService.isSuperRole(userOaName)) {
			throw new SaturnJobConsoleException("您不是超级管理员，没有权限");
		}
	}

}
