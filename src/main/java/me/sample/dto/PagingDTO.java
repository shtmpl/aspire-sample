package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagingDTO {
    Integer size;
    Integer totalElements;
    Integer totalPages;
    Integer number;
}
