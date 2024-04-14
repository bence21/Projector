package com.bence.projector.server.api;

public class ResponseMessage {

    private String data;
    private String message;

    public ResponseMessage(String data, String message) {
        this.message = message;
        this.data = data;
    }

    public ResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
