package com.bence.projector.server.api.dto;

import java.util.Date;

public class StatisticsDTO extends BaseDTO {
    private Date accessedDate;
    private String method;
    private String remoteAddress;
    private String uri;

    public Date getAccessedDate() {
        return accessedDate == null ? null : (Date) accessedDate.clone();
    }

    public void setAccessedDate(Date accessedDate) {
        this.accessedDate = accessedDate == null ? null : (Date) accessedDate.clone();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
