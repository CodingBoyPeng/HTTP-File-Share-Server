package com.codeboy.fileclient.tools;

import java.util.concurrent.ConcurrentHashMap;

// code range 200 - 999
public class BaseErrorCode {
    
	private static final ConcurrentHashMap<Integer, BaseErrorCode> instances = new ConcurrentHashMap<>();
    
    public static final BaseErrorCode SUCCESS                 = new BaseErrorCode(200, "Success");
    
    // HTTP status compatible error codes
    public static final BaseErrorCode INVALID_REQUEST           = new BaseErrorCode(403, "Invalid Request");
    public static final BaseErrorCode DOWNLOAD_ERROR             = new BaseErrorCode(410, "Download Error");
    public static final BaseErrorCode INTERNAL_SERVER_ERROR   = new BaseErrorCode(500, "Internal Server Error");
    public static final BaseErrorCode UPLOAD_ERROR             = new BaseErrorCode(411, "Upload Error");
    // Non standard HTTP status
    public static final BaseErrorCode NOT_FOUND        = new BaseErrorCode(412, "Not Found");
    public static final BaseErrorCode EMPTY_PARAMETER         = new BaseErrorCode(600, "Empty Parameter");
    public static final BaseErrorCode BAD_PARAMETER           = new BaseErrorCode(601, "Bad Parameters");

    private int code;
    private String error;

    public static BaseErrorCode instancesOf(int code) {
    	return instances.get(code);
    }
    
    protected BaseErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
        instances.putIfAbsent(code, this);
    }

    public BaseResponse buildResponse() {
        return new BaseResponse(this);
    }
    
    public ErrorException buildException() {
        return ErrorException.of(this);
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;
    }

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BaseErrorCode)) {
			return false;
		}
		
		return this.code == ((BaseErrorCode) obj).getCode();
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public String toString() {
    	return String.format("BaseErrorCode {\"code\":%d, \"error\":\"%s\"}",
    			code, error);
	}
}
