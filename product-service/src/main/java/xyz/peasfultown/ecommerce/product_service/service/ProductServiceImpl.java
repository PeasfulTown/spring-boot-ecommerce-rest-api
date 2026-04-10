package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.product_api.model.NewProductReq;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_api.model.ProductStockStatus;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryNotFoundException;
import xyz.peasfultown.ecommerce.product_service.exception.ProductNotFoundException;
import xyz.peasfultown.ecommerce.product_service.mapper.ProductMapper;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static xyz.peasfultown.ecommerce.product_service.repository.specification.ProductSpecification.*;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;
    private final CategoryRepository caRepo;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repo, CategoryRepository caRepo, ProductMapper mapper) {
        this.repo = repo;
        this.caRepo = caRepo;
        this.mapper = mapper;
    }

    @Override
    public Page<Product> queryProducts(String name,
                                       String category,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       List<ProductStockStatus> stockStatus,
                                       String sortBy,
                                       String sortDir,
                                       Integer page,
                                       Integer size) {
        if (sortBy == null)
            sortBy = "name";
        if (sortDir == null)
            sortDir = "asc";

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return repo.findAll(
                hasName(name)
                        .and(hasCategoryName(category))
                        .and(hasPriceLowerThan(maxPrice))
                        .and(hasPriceGreaterThan(minPrice))
                        .and(hasStockStatus(stockStatusListOf(stockStatus))),
                pageable
        ).map(mapper::entityToModel);
    }

    private List<ProductEntity.StockStatus> stockStatusListOf(List<ProductStockStatus> pssl) {
        if (pssl == null || pssl.isEmpty()) return null;
        return pssl.stream().map(this::stockStatusOf).toList();
    }

    private ProductEntity.StockStatus stockStatusOf(ProductStockStatus pss) {
        return ProductEntity.StockStatus.fromValue(pss.getValue());
    }

    @Override
    public Product createProduct(NewProductReq newProductReq) {
        CategoryEntity ce = caRepo.findCategoryByName(newProductReq.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by name: %s", newProductReq.getCategory()
                )));
        ProductEntity pe = ProductEntity.builder()
                .name(newProductReq.getName())
                .description(newProductReq.getDescription())
                .imageUrls(newProductReq.getImageUrls())
                .price(newProductReq.getPrice())
                .category(ce)
                .build();
        return mapper.entityToModel(repo.save(pe));
    }

    @Override
    public void deleteProductById(String id) {
        repo.deleteById(UUID.fromString(id));
    }

    @Override
    public Product getProductById(String id) {
        ProductEntity pe = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ProductNotFoundException(String.format(
                        "Product not found by ID: %s", id
                )));

        return mapper.entityToModel(pe);
    }
}
