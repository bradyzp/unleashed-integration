package net.jastrab.unleashedintegration.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document
public class ProcessedOrder {
    private String salesOrderId;

    private LocalDateTime processedDate = LocalDateTime.now();

    public ProcessedOrder(String salesOrderId, LocalDateTime processedDate) {
        this.salesOrderId = salesOrderId;
        this.processedDate = processedDate;
    }
}
