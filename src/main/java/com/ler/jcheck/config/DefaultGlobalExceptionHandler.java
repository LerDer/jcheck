package com.ler.jcheck.config;

import com.alibaba.fastjson.JSON;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author lww
 */
@Configuration
@ControllerAdvice
public class DefaultGlobalExceptionHandler extends ResponseEntityExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGlobalExceptionHandler.class);

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
		HttpResult httpResult = HttpResult.failure(status.is5xxServerError() ? ErrorCode.serverError.getDesc() : ErrorCode.paramError.getDesc());
		LOGGER.error("handleException, ex caught, contextPath={}, httpResult={}, ex.msg={}", request.getContextPath(), JSON.toJSONString(httpResult), ex.getMessage());
		return super.handleExceptionInternal(ex, httpResult, headers, status, request);
	}

	@ExceptionHandler(Exception.class)
	protected ResponseEntity handleException(HttpServletRequest request, Exception ex) {
		boolean is5xxServerError;
		HttpStatus httpStatus;
		HttpResult httpResult;
		if (ex instanceof UserFriendlyException) {
			UserFriendlyException userFriendlyException = (UserFriendlyException) ex;
			is5xxServerError = userFriendlyException.getHttpStatusCode() >= 500;
			httpStatus = HttpStatus.valueOf(userFriendlyException.getHttpStatusCode());
			httpResult = HttpResult.failure(userFriendlyException.getErrorCode(), userFriendlyException.getMessage());
		} else if (ex instanceof IllegalArgumentException) {
			// 参数判断用到了Spring断言，requireTrue会抛出 IllegalArgumentException，客户端处理不了5xx异常，所以还是返回200
			//XXX 正常逻辑的断言不能用Spring断言，避免出错了不知道
			httpStatus = HttpStatus.OK;
			is5xxServerError = false;
			httpResult = HttpResult.failure("参数校验错误或数据异常！");
		} else {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			is5xxServerError = true;
			httpResult = HttpResult.failure(ErrorCode.serverError.getDesc());
		}
		if (is5xxServerError) {
			LOGGER.error("handleException, ex caught, uri={}, httpResult={}", request.getRequestURI(), JSON.toJSONString(httpResult), ex);
		} else {
			LOGGER.error("handleException, ex caught, uri={}, httpResult={}, ex.msg={}", request.getRequestURI(), JSON.toJSONString(httpResult), ex.getMessage());
		}
		return new ResponseEntity<>(httpResult, httpStatus);
	}

}
