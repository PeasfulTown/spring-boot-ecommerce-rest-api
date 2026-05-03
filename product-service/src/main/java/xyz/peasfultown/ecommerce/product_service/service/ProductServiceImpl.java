package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.ecommerce.product_api.model.*;
import xyz.peasfultown.ecommerce.product_service.dto.ProductStockUpdateMessage;
import xyz.peasfultown.ecommerce.product_service.entity.CategoryEntity;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.exception.CategoryNotFoundException;
import xyz.peasfultown.ecommerce.product_service.exception.ProductNotFoundException;
import xyz.peasfultown.ecommerce.product_service.mapper.ProductMapper;
import xyz.peasfultown.ecommerce.product_service.repository.CategoryRepository;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static xyz.peasfultown.ecommerce.product_service.repository.specification.ProductSpecification.*;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;
    private final CategoryRepository caRepo;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repo, CategoryRepository caRepo, ProductMapper mapper) {
        this.repo = repo;
        this.caRepo = caRepo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Product> queryProducts(String name,
                                       String category,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       List<StockStatus> stockStatus,
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
                        .and(hasStockStatus(stockStatus)),
                pageable
        ).map(mapper::toModel);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Product> getProducts(BatchProductIdRequest req) {
        if (req.getIds() == null || req.getIds().isEmpty())
            return mapper.entityListToModelList(repo.findAll());

        List<UUID> uuids = req.getIds().stream().map(p -> UUID.fromString(p)).toList();
        return mapper.entityListToModelList(repo.findAll(hasIdsIn(uuids)));
    }

    @Override
    public Product createProduct(ProductCreateRequest newProductReq) {
        CategoryEntity ce = caRepo.findCategoryByName(newProductReq.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(String.format(
                        "Category not found by name: %s", newProductReq.getCategory()
                )));
        ProductEntity pe = ProductEntity.builder()
                .name(newProductReq.getName())
                .description(newProductReq.getDescription())
                .imageUrls(newProductReq.getImageUrls())
                .price(newProductReq.getPrice())
                .stock(newProductReq.getStock() == null ? 0 : newProductReq.getStock())
                .category(ce)
                .build();
        return mapper.toModel(repo.save(pe));
    }

    @Override
    public Product updateProductById(String productId, ProductUpdateRequest req) {
        ProductEntity pe = repo.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ProductNotFoundException(String.format(
                        "Product not found by ID: %s", productId
                )));

        if (Objects.nonNull(req.getName()))
            pe.setName(req.getName());
        if (Objects.nonNull(req.getDescription()))
            pe.setDescription(req.getDescription());
        if (Objects.nonNull(req.getPrice()))
            pe.setPrice(req.getPrice());
        if (Objects.nonNull(req.getImageUrls()))
            pe.setImageUrls(req.getImageUrls());
        if (Objects.nonNull(req.getActiveStatus()))
            pe.setActiveStatus(
                    ProductEntity.ActiveStatus.fromValue(req.getActiveStatus().getValue()));
        if (Objects.nonNull(req.getStock())) {
            pe.setStock(req.getStock());
        }
        if (Objects.nonNull(req.getCategory())) {
            CategoryEntity ce = caRepo.findCategoryByName(req.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(String.format(
                            "Category not found by name: %s", req.getCategory()
                    )));
            pe.setCategory(ce);
        }
        pe.setUpdatedAt(Instant.now());
        pe = repo.save(pe);
        return mapper.toModel(pe);
    }

    @Override
    public void deleteProductById(String id) {
        repo.deleteById(UUID.fromString(id));
    }

    @Transactional(readOnly = true)
    @Override
    public Product getProductById(String id) {
        ProductEntity pe = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ProductNotFoundException(String.format(
                        "Product not found by ID: %s", id
                )));

        return mapper.toModel(pe);
    }

    @Override
    public void updateProductStock(ProductStockUpdateMessage dto) {
        List<ProductEntity> pes = repo.findAllById(
        dto.getProductIdStockMap().keySet().stream().map(UUID::fromString).toList());
        assert pes.size() == dto.getProductIdStockMap().size();
        pes.forEach(p -> {
            p.setStock(p.getStock() - dto.getProductIdStockMap().get(p.getId().toString()));
        });
        repo.saveAll(pes);
    }
}
