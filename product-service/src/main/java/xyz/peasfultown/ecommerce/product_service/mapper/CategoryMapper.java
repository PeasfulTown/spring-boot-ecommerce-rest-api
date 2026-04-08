package xyz.peasfultown.ecommerce.product_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class CategoryMapper {
    public Category entityToModel(CategoryEntity entity) {
        return new Category()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription());
    }

    public CategoryEntity modelToEntity(Category model) {
        return CategoryEntity.builder()
                .id(model.getId() != null ? UUID.fromString(model.getId()) : null)
                .name(model.getName())
                .description(model.getDescription())
                .build();
    }
}
