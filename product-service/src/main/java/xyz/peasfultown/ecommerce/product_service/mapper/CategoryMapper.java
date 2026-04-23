package xyz.peasfultown.ecommerce.product_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toModel(CategoryEntity entity);
    CategoryEntity toEntity(Category model);

    List<Category> toModel(List<CategoryEntity> entities);
    List<CategoryEntity> toEntity(List<Category> models);
}
