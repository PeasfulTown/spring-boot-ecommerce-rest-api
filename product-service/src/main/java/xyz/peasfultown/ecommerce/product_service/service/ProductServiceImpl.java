package xyz.peasfultown.ecommerce.product_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import xyz.peasfultown.ecommerce.product_api.model.Product;
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
    public Page<Product> queryProducts(String name,
                                       String category,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       List<String> stockStatus,
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
            .and(hasCategoryName(category)),
            pageable
            ).map(mapper::entityToModel);
    }
}
