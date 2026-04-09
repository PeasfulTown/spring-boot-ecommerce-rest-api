package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.NewCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.PatchCategoryReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryAlreadyExistsException;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryNotFoundException;
import xyz.peasfultown.ecommerce.product_service.mapper.CategoryMapper;
import xyz.peasfultown.ecommerce.product_service.mapper.ProductMapper;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;
    private final ProductRepository pRepo;
    private final CategoryMapper mapper;
    private final ProductMapper pMapper;

    public CategoryServiceImpl(CategoryRepository repo, ProductRepository pRepo, CategoryMapper mapper, ProductMapper pMapper) {
        this.repo = repo;
        this.pRepo = pRepo;
        this.mapper = mapper;
        this.pMapper = pMapper;
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
        CategoryEntity ce = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by ID: %s", id
                )));

        repo.delete(ce);
    }

    @Override
    public List<Category> getAllCategories() {
        List<CategoryEntity> ce = repo.findAll();
        return mapper.entityListToModelList(ce);
    }

    @Override
    public List<Product> getProductsByCategory(String id) {
        repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by ID: %s", id
                )));
        List<ProductEntity> pe =
                repo.findProductsByCategoryId(UUID.fromString(id));

        return pMapper.entityListToModelList(pe);
    }

    @Override
    public Category updateCategoryById(String id, PatchCategoryReq patchCategoryReq) {
        CategoryEntity ce = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by ID: %s", id
                )));
        if (Objects.nonNull(patchCategoryReq.getName()))
            ce.setName(patchCategoryReq.getName());
        if (Objects.nonNull(patchCategoryReq.getDescription()))
            ce.setDescription(patchCategoryReq.getDescription());

        return mapper.entityToModel(repo.save(ce));
    }
}
