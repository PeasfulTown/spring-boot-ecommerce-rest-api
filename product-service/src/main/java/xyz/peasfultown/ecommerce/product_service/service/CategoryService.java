package xyz.peasfultown.ecommerce.product_service.service;

import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.CategoryCreateRequest;
import xyz.peasfultown.ecommerce.product_api.model.CategoryUpdateRequest;
import xyz.peasfultown.ecommerce.product_api.model.Product;

import java.util.List;

public interface CategoryService {
    Category createNewCategory(CategoryCreateRequest updateReq);

    void deleteCategoryById(String id);

    List<Category> getAllCategories();

    List<Product> getProductsByCategory(String id);

    Category updateCategoryById(String id, CategoryUpdateRequest updateReq);
}
