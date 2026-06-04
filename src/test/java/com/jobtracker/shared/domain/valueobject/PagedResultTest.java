package com.jobtracker.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PagedResult}.
 */
class PagedResultTest {

    // ── Basic construction ────────────────────────────────────────────────────

    @Test
    void of_constructsPagedResultCorrectly() {
        var result = PagedResult.of(List.of("a", "b", "c"), 0, 20, 3L);
        assertThat(result.content()).containsExactly("a", "b", "c");
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(3L);
    }

    // ── totalPages computation ────────────────────────────────────────────────

    @Test
    void totalPages_isZero_whenNoElements() {
        var result = PagedResult.of(List.of(), 0, 20, 0L);
        assertThat(result.totalPages()).isEqualTo(0);
    }

    @Test
    void totalPages_isOne_whenElementsFitExactlyOnOnePage() {
        var result = PagedResult.of(List.of(), 0, 20, 20L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void totalPages_roundsUp_whenNotExactMultiple() {
        // 153 elements / 20 per page = 7.65 → ceil → 8
        var result = PagedResult.of(List.of(), 0, 20, 153L);
        assertThat(result.totalPages()).isEqualTo(8);
    }

    @Test
    void totalPages_isOne_whenSingleElement() {
        var result = PagedResult.of(List.of("x"), 0, 20, 1L);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void totalPages_computedCorrectly_forLargeDataset() {
        // 1000 elements / 10 per page = 100 pages exactly
        var result = PagedResult.of(List.of(), 0, 10, 1000L);
        assertThat(result.totalPages()).isEqualTo(100);
    }

    // ── hasNext / isFirst / isLast ────────────────────────────────────────────

    @Test
    void hasNext_isFalse_onSinglePage() {
        var result = PagedResult.of(List.of("a"), 0, 20, 1L);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void hasNext_isTrue_onFirstOfMultiplePages() {
        var result = PagedResult.of(List.of(), 0, 20, 100L);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void hasNext_isFalse_onLastPage() {
        // page=4 (zero-based), totalPages=5 → last page
        var result = PagedResult.of(List.of(), 4, 20, 100L);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void isFirst_isTrue_forPageZero() {
        var result = PagedResult.of(List.of(), 0, 20, 100L);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    void isFirst_isFalse_forPageGreaterThanZero() {
        var result = PagedResult.of(List.of(), 2, 20, 100L);
        assertThat(result.isFirst()).isFalse();
    }

    @Test
    void isLast_isTrue_onFinalPage() {
        var result = PagedResult.of(List.of(), 4, 20, 100L);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void isEmpty_isTrue_forEmptyContent() {
        var result = PagedResult.of(List.of(), 0, 20, 0L);
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void isEmpty_isFalse_whenContentPresent() {
        var result = PagedResult.of(List.of("x"), 0, 20, 1L);
        assertThat(result.isEmpty()).isFalse();
    }

    // ── Immutability ──────────────────────────────────────────────────────────

    @Test
    void content_isUnmodifiable() {
        var result = PagedResult.of(List.of("a", "b"), 0, 20, 2L);
        assertThatThrownBy(() -> result.content().add("c"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mutatingOriginalList_doesNotAffectPagedResult() {
        var original = new ArrayList<>(List.of("a", "b"));
        var result   = PagedResult.of(original, 0, 20, 2L);
        original.add("c");
        assertThat(result.content()).hasSize(2).containsExactly("a", "b");
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    void of_rejectsNegativePageIndex() {
        assertThatThrownBy(() -> PagedResult.of(List.of(), -1, 20, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page index must be >= 0");
    }

    @Test
    void of_rejectsZeroPageSize() {
        assertThatThrownBy(() -> PagedResult.of(List.of(), 0, 0, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size must be >= 1");
    }

    @Test
    void of_rejectsNegativeTotalElements() {
        assertThatThrownBy(() -> PagedResult.of(List.of(), 0, 20, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Total elements must be >= 0");
    }

    @Test
    void of_rejectsNullContentList() {
        assertThatThrownBy(() -> PagedResult.of(null, 0, 20, 0L))
                .isInstanceOf(NullPointerException.class);
    }
}