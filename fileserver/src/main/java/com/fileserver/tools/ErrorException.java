package com.fileserver.tools;

import java.util.concurrent.ConcurrentHashMap;

public class ErrorException extends RuntimeException {
    private static final long serialVersionUID = 3281072262669739458L;

    private static final ConcurrentHashMap<String, ErrorException> instances = 
    		new ConcurrentHashMap<>();
    
    private String error;
    private int status;

    public static ErrorException of(BaseErrorCode errorCode) {
    	String key = String.format("%d-%s", errorCode.getCode(), errorCode.getError());
    	ErrorException exception = instances.get(key);
    	if (exception != null) {
    		return exception;
    	}
    	
    	exception = new ErrorException(errorCode);
    	instances.putIfAbsent(key, exception);
    	return exception;
    }
    
    public ErrorException(BaseErrorCode errorCode) {
        this(errorCode.getError(), errorCode.getCode());
    }

    public ErrorException(String error, int status) {
        super(status + ": " + error, null, false, false);
        this.error = error;
        this.status = status;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    
    public BaseErrorCode toErrorCode() {
    	return new BaseErrorCode(status, error);
    }
}
