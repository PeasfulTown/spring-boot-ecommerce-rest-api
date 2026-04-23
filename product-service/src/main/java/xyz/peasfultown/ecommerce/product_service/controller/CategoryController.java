package xyz.peasfultown.ecommerce.product_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.product_api.CategoryApi;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.CategoryCreateRequest;
import xyz.peasfultown.ecommerce.product_api.model.CategoryUpdateRequest;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.controller.aspect.AdminOnly;
import xyz.peasfultown.ecommerce.product_service.service.CategoryService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class CategoryController implements CategoryApi {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @AdminOnly
    @Override
    public ResponseEntity<Category> createCategory(String userRole, CategoryCreateRequest newCategoryReq) throws Exception {
        return status(HttpStatus.CREATED).body(service.createNewCategory(newCategoryReq));
    }

    @AdminOnly
    @Override
    public ResponseEntity<Category> updateCategoryById(String userRole, String id, CategoryUpdateRequest categoryUpdateRequest) throws Exception {
        return ok(service.updateCategoryById(id, categoryUpdateRequest));
    }

    @AdminOnly
    @Override
    public ResponseEntity<Void> deleteCategoryById(String userRole, String id) throws Exception {
        service.deleteCategoryById(id);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategories() throws Exception {
        return ok(service.getAllCategories());
    }

    @Override
    public ResponseEntity<List<Product>> getProductsByCategoryId(String id) throws Exception {
        return ok(service.getProductsByCategory(id));
    }

}
