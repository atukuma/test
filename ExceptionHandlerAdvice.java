package org.kp.smu.application.aop;

import java.net.ConnectException;
import java.net.UnknownHostException;

import org.kp.smu.application.constants.SmuConstants;
import org.kp.smu.application.domain.Message;
import org.kp.smu.application.domain.Message.ErrorType;
import org.kp.smu.application.domain.SmuResponse;
import org.kp.smu.application.exception.DBException;
import org.kp.smu.application.exception.SmuSystemException;
import org.kp.smu.application.exception.ValidationException;
import org.kp.smu.application.util.CommonUtil;
import org.kp.smu.application.util.ErrorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * @author Q014787
 * This is ExceptionHandlerAdvice Java class that will logged error in log whenever any functionality get defined exception.
 */
@RestControllerAdvice
public class ExceptionHandlerAdvice {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private CommonUtil commonUtil;

	@ExceptionHandler(Exception.class)
	public ResponseEntity<SmuResponse> handleException(Exception e) {
		logger.error("Exception: {}", e.getMessage(), e);
		if (e.getCause() instanceof InvalidFormatException) {
			Message message = commonUtil.getSmuMessage(SmuConstants.INVALID_REQUEST);
			message.setDetail("Invalid JSON");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorUtil.getErrorGetPrescriptionsResp(message));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorUtil.getErrorGetPrescriptionsResp(
				ErrorType.SYSTEM, SmuConstants.GENERIC_SYSTEM_EXCEPTION_CODE, SmuConstants.UNABLE_TO_CONNECT_APP_SERVICE, e.getMessage()));
		}
	}

	@ExceptionHandler(DBException.class)
	public ResponseEntity<SmuResponse> handleException(DBException e) {
		logger.error("Exception: {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorUtil.getErrorGetPrescriptionsResp(ErrorType.SYSTEM,
						SmuConstants.GENERIC_SYSTEM_EXCEPTION_CODE, SmuConstants.DB_EXCEPTION,
						e.getMessage()));
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<SmuResponse> handleException(IllegalArgumentException e) {
		logger.error("Exception: {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorUtil.getErrorGetPrescriptionsResp(
				ErrorType.VALIDATION, SmuConstants.GENERIC_SYSTEM_EXCEPTION_CODE, SmuConstants.VALIDATION_EXCEPTION, e.getMessage()));
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<SmuResponse> handleException(ValidationException e) {
		logger.error("Exception: {}", e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorUtil.getErrorGetPrescriptionsResp(e.getMessageObj()));
	}

	@ExceptionHandler(SmuSystemException.class)
	public ResponseEntity<SmuResponse> handleException(SmuSystemException e) {
		logger.error("Exception: {}", e.getMessage(), e);
		if (e.getCause().getCause() instanceof ConnectException
				|| e.getCause().getCause() instanceof UnknownHostException) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(ErrorUtil.getErrorGetPrescriptionsResp(ErrorType.SYSTEM,
							SmuConstants.UNABLE_TO_CONNECT_SERVICE_CODE, SmuConstants.UNABLE_TO_CONNECT_APP_SERVICE,
							e.getMessage()));
		} else if (SmuConstants.HTTP_SECURITY_ERROR_CODE == e.getStatusCode()
				|| SmuConstants.INTERNAL_SERVER_ERROR_CODE == e.getStatusCode()
				|| SmuConstants.HTTP_AUTH_ERROR_CODE == e.getStatusCode()) {
			if (SmuConstants.HTTP_SECURITY_ERROR_CODE == e.getStatusCode()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(ErrorUtil.getErrorGetPrescriptionsResp(ErrorType.SYSTEM,
								SmuConstants.UNABLE_TO_CONNECT_SERVICE_CODE, SmuConstants.UNABLE_TO_CONNECT_APP_SERVICE,
								e.getMessage()));
			} else if (SmuConstants.HTTP_AUTH_ERROR_CODE == e.getStatusCode()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(ErrorUtil.getErrorGetPrescriptionsResp(ErrorType.SYSTEM,
								SmuConstants.UNABLE_TO_CONNECT_SERVICE_CODE, SmuConstants.UNABLE_TO_CONNECT_APP_SERVICE,
								e.getMessage()));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(ErrorUtil.getErrorGetPrescriptionsResp(ErrorType.SYSTEM,
								SmuConstants.HEALTHCONNECT_WEBSERVICE_EXCEPTION_ERROR_CODE, SmuConstants.APPLICATION_EXCEPTION,
								e.getMessage()));
			}
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorUtil.getErrorGetPrescriptionsResp(ErrorType.SYSTEM,
						SmuConstants.HEALTHCONNECT_WEBSERVICE_EXCEPTION_ERROR_CODE, SmuConstants.APPLICATION_EXCEPTION,
						e.getMessage()));

	}
}
