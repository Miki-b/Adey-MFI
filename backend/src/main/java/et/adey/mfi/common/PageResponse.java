package et.adey.mfi.common;

import java.util.List;
import java.util.function.Function;

/** One page of results. page is 0-based. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <S, T> PageResponse<T> of(List<S> items, Function<S, T> mapper, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PageResponse<>(items.stream().map(mapper).toList(), page, size, total, totalPages);
    }
}
