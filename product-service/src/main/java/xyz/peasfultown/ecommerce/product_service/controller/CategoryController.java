package xyz.peasfultown.ecommerce.product_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.product_api.CategoryApi;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.NewCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.PatchCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;
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

    @Override
    public ResponseEntity<Category> createCategory(@Valid NewCategoryReq newCategoryReq) throws Exception {
        return status(HttpStatus.CREATED).body(service.createNewCategory(newCategoryReq));
    }

    @Override
    public ResponseEntity<Void> deleteCategoryById(String id) throws Exception {
        service.deleteCategoryById(id);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategories() throws Exception {
        return ok(service.getAllCategories());
    }

    @Override
    public ResponseEntity<List<Product>> getProductsByCategory(String id) throws Exception {
        return ok(service.getProductsByCategory(id));
    }

    @Override
    public ResponseEntity<Category> updateCategoryById(String id, PatchCategoryReq patchCategoryReq) throws Exception {
        return status(HttpStatus.ACCEPTED).body(service.updateCategoryById(id, patchCategoryReq));
    }
}
