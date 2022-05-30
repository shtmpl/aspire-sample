package me.sample.mapper;

import me.sample.dto.CategoryDTO;
import me.sample.domain.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Optional;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    CategoryDTO toDto(Category entity);

    Category toEntity(CategoryDTO dto);

    @OnlyId
    default Category toEntityOnlyId(CategoryDTO dto) {
        return Optional.ofNullable(dto)
                .map(d -> Category.builder()
                        .id(dto.getId())
                        .build())
                .orElse(null);
    }
}
