package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.product_api.model.Category;
import xyz.peasfultown.ecommerce.product_api.model.CategoryCreateRequest;
import xyz.peasfultown.ecommerce.product_api.model.CategoryUpdateRequest;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryAlreadyExistsException;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryNotFoundException;
import xyz.peasfultown.ecommerce.product_service.mapper.CategoryMapper;
import xyz.peasfultown.ecommerce.product_service.mapper.ProductMapper;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.time.Instant;
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
    public Category createNewCategory(CategoryCreateRequest createReq) {
        repo.findCategoryByName(createReq.getName())
            .ifPresent(c -> {
                throw new CategoryAlreadyExistsException(String.format(
                        "Category already exists by name: %s", c.getName()
                ));
            });
        CategoryEntity ce = CategoryEntity.builder()
                .name(createReq.getName())
                .description(createReq.getDescription())
                .build();
        ce = repo.save(ce);
        return mapper.toModel(ce);
    }

    @Override
    public void deleteCategoryById(String id) {
        CategoryEntity ce = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by ID: %s", id
                )));
        List<ProductEntity> pes = repo.findProductsByCategoryId(ce.getId());
        CategoryEntity dc = getDefaultCategoryEntity();
        pes.forEach(p -> {
            p.setCategory(dc);
            p.setUpdatedAt(Instant.now());
        });
        pRepo.saveAll(pes);
        repo.delete(ce);
    }

    @Override
    public List<Category> getAllCategories() {
        List<CategoryEntity> ce = repo.findAll();
        return mapper.toModel(ce);
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
    public Category updateCategoryById(String id, CategoryUpdateRequest updateReq) {
        CategoryEntity ce = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by ID: %s", id
                )));
        if (Objects.nonNull(updateReq.getName()))
            ce.setName(updateReq.getName());
        if (Objects.nonNull(updateReq.getDescription()))
            ce.setDescription(updateReq.getDescription());

        return mapper.toModel(repo.save(ce));
    }

    private CategoryEntity getDefaultCategoryEntity() {
        CategoryEntity ce = repo.findCategoryByName("Uncategorized")
                .orElseGet(() -> repo.save(CategoryEntity.builder()
                        .id(UUID.randomUUID())
                        .name("Uncategorized")
                        .description("Uncategorized products")
                        .build()));
        return ce;
    }
}
