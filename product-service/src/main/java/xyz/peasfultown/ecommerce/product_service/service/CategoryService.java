package xyz.peasfultown.ecommerce.product_service.service;

import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.NewCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.PatchCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;

import java.util.List;

public interface CategoryService {
    Category createNewCategory(NewCategoryReq newCategoryReq);

    void deleteCategoryById(String id);

    List<Category> getAllCategories();

    List<Product> getProductsByCategory(String id);

    Category updateCategoryById(String id, PatchCategoryReq patchCategoryReq);
}
