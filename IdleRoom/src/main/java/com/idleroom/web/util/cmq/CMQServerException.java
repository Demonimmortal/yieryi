package com.idleroom.web.util.cmq;

public class CMQServerException extends RuntimeException {

	private int httpStatus = 200;
    private int errorCode = 0;
	private String errorMessage = "";
    private String requestId ="";

    public CMQServerException(int status){
		super(Integer.toString(status));
        this.httpStatus = status;
	}
    public CMQServerException(int errorCode, String errorMessage, String requestId){
        super("code:" + errorCode
             + ", message:" + errorMessage
             + ", requestId:" + requestId);
        this.errorCode = errorCode;
		this.errorMessage = errorMessage;
        this.requestId = requestId;
    }

	
    public int getErrorCode() {
        return errorCode;
    }
	
	public String getErrorMessage() {
        return errorMessage;
    }

    public String getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
	if(httpStatus != 200)
		return "http status:" + httpStatus;
	else
        	return "code:" + errorCode
                + ", message:" + errorMessage
				+ ", requestId:" + requestId;
    }
}
