package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageDTO<T> implements Slice<T> {
    List<T> data;
    PagingDTO paging;

    @Override
    public int getNumber() {
        return paging.getNumber();
    }

    @Override
    public int getSize() {
        return paging.getSize();
    }

    @Override
    public int getNumberOfElements() {
        return paging.getTotalElements();
    }

    @Override
    public List<T> getContent() {
        return data;
    }

    @Override
    public boolean hasContent() {
        return !data.isEmpty();
    }

    @Override
    public Sort getSort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    public boolean isLast() {
        return !hasNext();
    }

    @Override
    public boolean hasNext() {
        return getNumber() < paging.getTotalPages();
    }

    public int getTotalPages() {
        return paging.getTotalPages();
    }

    @Override
    public boolean hasPrevious() {
        return getNumber() > 0;
    }

    @Override
    public Pageable nextPageable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pageable previousPageable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U> Slice<U> map(Function<? super T, ? extends U> converter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }
}
