package net.jastrab.unleashedintegration.service;

import net.jastrab.unleashed.api.models.ProductGroup;
import net.jastrab.unleashed.api.models.UnitOfMeasure;
import net.jastrab.unleashedspringclient.client.UnleashedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnleashedMetaService {
    private final UnitOfMeasure defaultUoM;
    private final ProductGroup defaultProductGroup;

    @Autowired
    public UnleashedMetaService(UnleashedClient client) {
        this.defaultUoM = client.getUnitsOfMeasure().stream()
                .filter(uom -> uom.getName().equals("EA"))
                .findFirst()
                .orElseThrow();
        this.defaultProductGroup = client.getProductGroups().stream()
                .filter(group -> group.getGroupName().equals("Common Components"))
                .findFirst()
                .orElseThrow();
    }

    public UnitOfMeasure getDefaultUnitOfMeasure() {
        return defaultUoM;
    }

    public ProductGroup getDefaultProductGroup() {
        return defaultProductGroup;

    }
}
