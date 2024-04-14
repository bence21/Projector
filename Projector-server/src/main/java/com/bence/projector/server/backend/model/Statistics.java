package com.bence.projector.server.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

@Entity
public class Statistics extends BaseEntity {
    @Transient
    private final int URI_LENGTH = 100;
    @Transient
    private final int REMOTE_ADDRESS_LENGTH = 15;
    private Date accessedDate;
    @Column(length = REMOTE_ADDRESS_LENGTH)
    private String remoteAddress;
    @Column(length = URI_LENGTH)
    private String uri;
    @Column(length = 10)
    private String method;

    public Date getAccessedDate() {
        return accessedDate == null ? null : (Date) accessedDate.clone();
    }

    public void setAccessedDate(Date accessedDate) {
        this.accessedDate = accessedDate == null ? null : (Date) accessedDate.clone();
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress.substring(0, Math.min(remoteAddress.length(), REMOTE_ADDRESS_LENGTH - 1));
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri.substring(0, Math.min(uri.length(), URI_LENGTH - 1));
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
