package net.jastrab.unleashedintegration.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class ProcessedItem {
    @Indexed(unique = true)
    private String productCode;

    private LocalDateTime processedDate;

    public ProcessedItem() {
    }

    public ProcessedItem(String productCode) {
        this.productCode = productCode;
        this.processedDate = LocalDateTime.now();
    }

}
