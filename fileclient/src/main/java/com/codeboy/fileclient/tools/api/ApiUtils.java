package com.codeboy.fileclient.tools.api;



import com.codeboy.fileclient.tools.*;
import org.springframework.http.ResponseEntity;

public class ApiUtils {

	public static ResponseEntity<BaseResponse> errorOf(BaseErrorCode errorCode) {
		return ResponseEntity.ok(new BaseResponse(errorCode));
	}

	public static void errorIfEmpty(BaseErrorCode errorCode, Object... params) {
		if (RequestUtils.emptyRequestParameter(params)) {
			throw ErrorException.of(errorCode);
		}
	}
	
	public static ResponseEntity<Object> emptyResponse() {
		return ResponseEntity.ok(new BaseResponse());
	}
	
	public static <T> ResponseEntity<SimpleResponse<T>> responseOf(T data) {
		return ResponseEntity.ok(new SimpleResponse<>(data));
	}

	public static <T extends BaseResponse> ResponseEntity<T> responseOf(T response) {
		return ResponseEntity.ok(response);
	}
}
