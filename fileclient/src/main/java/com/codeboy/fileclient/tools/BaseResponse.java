package com.codeboy.fileclient.tools;

public class BaseResponse {
    private Integer code;
	private String error;

    public BaseResponse() {
        this.error = null;
        this.code = BaseErrorCode.SUCCESS.getCode();
    }

    public BaseResponse(BaseErrorCode errorCode) {
        this.code = errorCode.getCode();
    	this.error = errorCode.getError();
    }

    public BaseResponse(Integer code, String error) {
        this.code = code;
    	this.error = error;
    }

    public boolean success() {
    	return code != null && code == BaseErrorCode.SUCCESS.getCode();
    }
    
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
    public BaseErrorCode toErrorCode() {
    	if (success()) {
    		return null;
    	}
    	
    	// All error code encapsulate with BaseErrorCode
    	return new BaseErrorCode(code, error);
    }
    
    public String toString() {
    	return String.format("BaseResponse {\"code\":%d, \"error\":\"%s\"}",
    			code, error);
    }
}
