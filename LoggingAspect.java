package org.kp.smu.application.aop.logging;

import java.util.Arrays;

import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.kp.smu.application.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * @author Q014787 Aspect for logging execution of service and repository Spring
 *         components. By default, it only runs with the "dev" profile.
 */
@Aspect
public class LoggingAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

	@Inject
	private Environment env;

	/**
	 * Pointcut that matches all repositories, services and Web REST endpoints.
	 */
	@Pointcut("within(org.kp.smu.application.repository..*) || within(org.kp.smu.application.service..*) || within(org.kp.smu.application.web.rest..*)")
	public void loggingPointcut() {
		// Method is empty as this is just a Poincut, the implementations are in the
		// advices.
	}

	/**
	 * Advice that logs methods throwing exceptions.
	 */
	@AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
		if (env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
			LOGGER.error("Exception in {}.{}() with cause = \'{}\' and exception = \'{}\'",
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
					e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);

		} else {
			LOGGER.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
		}
	}

	/**
	 * Advice that logs when a method is entered and exited.
	 */
	@Around("loggingPointcut()")
	public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
		}
		try {
			Object result = joinPoint.proceed();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), result);
			}
			return result;
		} catch (IllegalArgumentException e) {
			LOGGER.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

			throw e;
		}
	}
}
