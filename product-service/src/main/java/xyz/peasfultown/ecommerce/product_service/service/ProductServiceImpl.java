package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.product_api.model.Product;
import xyz.peasfultown.ecommerce.product_service.entity.ProductEntity;
import xyz.peasfultown.ecommerce.product_service.mapper.ProductMapper;
import xyz.peasfultown.ecommerce.product_service.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static xyz.peasfultown.ecommerce.product_service.repository.specification.ProductSpecification.hasCategoryName;
import static xyz.peasfultown.ecommerce.product_service.repository.specification.ProductSpecification.hasName;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repo;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repo, ProductMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<Product> queryProducts(String name,
                                       String category,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       List<String> stockStatus,
                                       String sortBy,
                                       String sortDir,
                                       Integer page,
                                       Integer size) {

        List<ProductEntity> pe = repo.findAll(hasName(name).and(hasCategoryName(category)));
        return mapper.entityListToModelList(pe);
    }
}
