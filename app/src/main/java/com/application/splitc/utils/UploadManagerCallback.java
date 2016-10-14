package com.application.splitc.utils;

/**
 * Created by nik on 1/10/16.
 */
public interface UploadManagerCallback {
    public void uploadStarted(int requestType, Object data);
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage);
}
