package com.codeboy.fileclient.tools;

public class SimpleResponse<T> extends BaseResponse {
    private T data;

    public SimpleResponse() {
        super();
    }

    public SimpleResponse(T data) {
        super();
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
