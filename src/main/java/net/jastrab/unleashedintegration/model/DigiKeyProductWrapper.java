package net.jastrab.unleashedintegration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.jastrab.digikey.model.ordersupport.LineItem;
import net.jastrab.unleashed.api.models.Product;
import net.jastrab.unleashed.api.models.PurchaseOrderLine;
import net.jastrab.unleashed.api.models.Supplier;
import net.jastrab.unleashed.api.models.SupplierWithProductCode;

/**
 * Wrapping class for integration flow
 */
@Data
@AllArgsConstructor
public class DigiKeyProductWrapper {
    private String salesOrderId;
    private LineItem lineItem;

    public String getProductCode() {
        return lineItem.getManufacturerPartNumber();
    }

    public Product toUnleashedProduct(Supplier supplier) {
        Product product = new Product(this.lineItem.getManufacturerPartNumber());
        product.setProductDescription(this.lineItem.getProductDescription());
        product.setBarcode(this.lineItem.getManufacturerPartNumber());
        product.setComponent(true);
        product.setDefaultPurchasePrice(this.lineItem.getUnitPrice());

        SupplierWithProductCode supplierWithProductCode = SupplierWithProductCode.fromSupplier(supplier);
        supplierWithProductCode.setSupplierProductCode(lineItem.getDigiKeyPartNumber());
        supplierWithProductCode.setSupplierProductDescription(lineItem.getProductDescription());
        supplierWithProductCode.setSupplierProductPrice(lineItem.getUnitPrice());
        product.setSupplier(supplierWithProductCode);

        return product;
    }

    public PurchaseOrderLine toPurchaseOrderLine(Product product) {
        return new PurchaseOrderLine(product, this.lineItem.getUnitPrice(), this.lineItem.getQuantity());
    }

}
