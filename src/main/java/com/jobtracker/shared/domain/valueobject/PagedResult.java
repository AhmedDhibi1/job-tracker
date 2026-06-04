package com.jobtracker.shared.domain.valueobject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PagedResult<T> {

    private final List<T> content;
    private final int     page;
    private final int     size;
    private final long    totalElements;
    private final int     totalPages;

    private PagedResult(List<T> content, int page, int size, long totalElements) {
        Objects.requireNonNull(content, "Content list must not be null");
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page index must be >= 0, got: " + page
            );
        }
        if (size < 1) {
            throw new IllegalArgumentException(
                    "Page size must be >= 1, got: " + size
            );
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "Total elements must be >= 0, got: " + totalElements
            );
        }
        this.content       = Collections.unmodifiableList(List.copyOf(content));
        this.page          = page;
        this.size          = size;
        this.totalElements = totalElements;
        this.totalPages    = (totalElements == 0)
                                ? 0
                                : (int) Math.ceil((double) totalElements / size);
    }

    
    public static <T> PagedResult<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements) {
        return new PagedResult<>(content, page, size, totalElements);
    }

    
    public List<T> content() {
        return content;
    }

    
    public int page() {
        return page;
    }

    
    public int size() {
        return size;
    }

    
    public long totalElements() {
        return totalElements;
    }

    
    public int totalPages() {
        return totalPages;
    }

    
    public boolean hasNext() {
        return (page + 1) < totalPages;
    }

    
    public boolean isFirst() {
        return page == 0;
    }

    
    public boolean isLast() {
        return !hasNext();
    }

    
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public String toString() {
        return "PagedResult{"
                + "page=" + page
                + ", size=" + size
                + ", totalElements=" + totalElements
                + ", totalPages=" + totalPages
                + ", contentSize=" + content.size()
                + "}";
    }
}