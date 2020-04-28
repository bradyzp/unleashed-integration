package net.jastrab.unleashedintegration.repository;

import net.jastrab.unleashed.api.models.Product;
import net.jastrab.unleashedintegration.model.ProcessedItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UnleashedItemsRepo extends MongoRepository<Product, String> {
    boolean existsByProductCode(String productCode);
}
