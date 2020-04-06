package com.codeboy.fileclient.tools.handler;



import com.codeboy.fileclient.tools.BaseErrorCode;
import com.codeboy.fileclient.tools.BaseResponse;
import com.codeboy.fileclient.tools.ErrorException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * A global exception handler for SpringMVC.
 * Translate ErrorException to corresponding ErrorResponse, and implement ErrorController
 * to render errors which not handler by @ExceptionHandler.
 * 
 * @author panbingzhen
 *
 */
@ControllerAdvice
@RestController
@RequestMapping("/error")
public class GlobalExceptionHandler implements ErrorController {

	private class ExceptionHandlerBridge extends ResponseEntityExceptionHandler {
		protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
                                                                 HttpHeaders headers, HttpStatus status, WebRequest request) {
			return ResponseEntity.ok((Object) new BaseResponse(status.value(), status.getReasonPhrase()));
		}
	}
	
	private ExceptionHandlerBridge handler = new ExceptionHandlerBridge();
	
	@ExceptionHandler(Exception.class)
	@Nullable
	public final ResponseEntity<Object> handleAllException(Exception ex, WebRequest request) throws Exception {
		if (ex instanceof ErrorException) {
			ErrorException errorException = (ErrorException) ex;
			return ResponseEntity.ok((Object) new BaseResponse(errorException.getStatus(), errorException.getError()));
		}
		
		try {
			return handler.handleException(ex, request);
		} catch (Exception e) {
			// Unhandled exception
			return ResponseEntity.ok((Object) new BaseResponse(BaseErrorCode.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}
	
	@RequestMapping
	public ResponseEntity<?> error(HttpServletRequest request) {
		HttpStatus status = getStatus(request);
		
		// HTTP status to BaseErrorCode
		BaseErrorCode errorCode = BaseErrorCode.instancesOf(status.value());
		if (errorCode == null) {
			errorCode = BaseErrorCode.INTERNAL_SERVER_ERROR;
		}
		
		return ResponseEntity.ok(new BaseResponse(errorCode));
	}
	
	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		
		if (statusCode == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		try {
			return HttpStatus.valueOf(statusCode);
		}
		catch (Exception ex) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
}