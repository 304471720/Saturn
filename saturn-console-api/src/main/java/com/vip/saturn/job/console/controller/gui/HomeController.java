package com.vip.saturn.job.console.controller.gui;

import com.vip.saturn.job.console.controller.SuccessResponseEntity;
import com.vip.saturn.job.console.domain.RegistryCenterConfiguration;
import com.vip.saturn.job.console.domain.RequestResult;
import com.vip.saturn.job.console.exception.SaturnJobConsoleException;
import com.vip.saturn.job.console.exception.SaturnJobConsoleGUIException;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Name space related operations.
 *
 * @author hebelala
 */
@Controller
@RequestMapping("/console/namespaces")
public class HomeController extends AbstractGUIController {

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping
	public SuccessResponseEntity getNamespaces(final HttpServletRequest request)
			throws SaturnJobConsoleException {
		List<String> namespaceList = new ArrayList<>();
		List<String> temp = registryCenterService.getNamespaces();
		if (temp != null) {
			namespaceList.addAll(temp);
		}
		return new SuccessResponseEntity(namespaceList);
	}

	@ApiResponses(value = {@ApiResponse(code = 200, message = "Success/Fail", response = RequestResult.class)})
	@GetMapping(value = "/{namespace:.+}")
	public SuccessResponseEntity getNamespace(final HttpServletRequest request, @PathVariable String namespace)
			throws SaturnJobConsoleException {
		RegistryCenterConfiguration registryCenterConfiguration = registryCenterService
				.findConfigByNamespace(namespace);
		if (registryCenterConfiguration == null) {
			throw new SaturnJobConsoleGUIException("该域名（" + namespace + "）不存在");
		}
		return new SuccessResponseEntity(registryCenterConfiguration);
	}

}
