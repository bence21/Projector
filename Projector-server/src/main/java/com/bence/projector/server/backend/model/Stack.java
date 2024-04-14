package com.bence.projector.server.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

import static com.bence.projector.common.util.StringUtils.trimLongString;
import static com.bence.projector.common.util.StringUtils.trimLongString255;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class Stack extends AbstractModel {

    private static final int MAX_STACK_LENGTH = 4000;
    @Column(length = MAX_STACK_LENGTH)
    private String stackTrace;
    private String message;
    private String email;
    private Date createdDate;
    private int count;
    private String version;

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = trimLongString(stackTrace, MAX_STACK_LENGTH);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = trimLongString255(message);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = trimLongString255(email);
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = trimLongString255(version);
    }
}
