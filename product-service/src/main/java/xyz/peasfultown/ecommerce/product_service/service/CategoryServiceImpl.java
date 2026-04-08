package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.NewCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.PatchCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryAlreadyExistsException;
import xyz.peasfultown.ecommerce.product_service.mapper.CategoryMapper;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    public CategoryServiceImpl(CategoryRepository repo, CategoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Category createNewCategory(NewCategoryReq newCategoryReq) {
        repo.findCategoryByName(newCategoryReq.getName())
            .ifPresent(c -> {
                throw new CategoryAlreadyExistsException(String.format(
                        "Category already exists by name: %s", c.getName()
                ));
            });
        CategoryEntity ce = CategoryEntity.builder()
                .name(newCategoryReq.getName())
                .description(newCategoryReq.getDescription())
                .build();
        ce = repo.save(ce);
        return mapper.entityToModel(ce);
    }

    @Override
    public void deleteCategoryById(String id) {

    }

    @Override
    public List<Category> getAllCategories() {
        return List.of();
    }

    @Override
    public List<Product> getProductsByCategory(String id) {
        return List.of();
    }

    @Override
    public Category updateCategoryById(String id, PatchCategoryReq patchCategoryReq) {
        return null;
    }
}
