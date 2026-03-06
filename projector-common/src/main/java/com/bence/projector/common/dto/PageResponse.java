package com.bence.projector.common.dto;

import java.util.List;

/**
 * Generic page response for paginated API endpoints.
 *
 * @param <T> type of each element in content
 */
public class PageResponse<T> {

    private List<T> content;
    private long totalElements;

    public PageResponse(List<T> content, long totalElements) {
        this.content = content;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}
