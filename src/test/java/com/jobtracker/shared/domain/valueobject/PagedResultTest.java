package com.jobtracker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResultTest {

    @Test
    void of_createsPagedResult() {
        PagedResult<String> result = PagedResult.of(List.of("a", "b"), 0, 10, 2L);
        assertThat(result.content()).containsExactly("a", "b");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void isEmpty_returnsTrueWhenContentEmpty() {
        PagedResult<String> result = PagedResult.of(List.of(), 0, 10, 0L);
        assertThat(result.content()).isEmpty();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void hasNext_returnsTrueWhenMorePages() {
        PagedResult<String> result = PagedResult.of(List.of("a"), 0, 1, 3L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    void isLast_whenOnLastPage() {
        PagedResult<String> result = PagedResult.of(List.of("c"), 2, 1, 3L);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void toString_containsInfo() {
        PagedResult<String> result = PagedResult.of(List.of("a"), 0, 10, 1L);
        assertThat(result.toString()).contains("page=0").contains("totalPages=1");
    }
}
