package net.jastrab.unleashedintegration.controller;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.unleashedintegration.model.UnleashedConfigurationDTO;
import net.jastrab.unleashedintegration.service.UnleashedConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/config/unleashed")
public class UnleashedConfigurationController {
    private final UnleashedConfigService configService;

    @Autowired
    public UnleashedConfigurationController(UnleashedConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/get")
    public UnleashedConfigurationDTO getCurrentConfig() {
        return configService.getCurrentConfiguration();
    }

    @GetMapping("/get/{environment}")
    public UnleashedConfigurationDTO getConfig(
            @PathVariable("environment") UnleashedConfigurationDTO.Environment environment) {
        return configService.getConfiguration(environment);
    }

    @PutMapping("/set/{environment}")
    public UnleashedConfigurationDTO.Environment setCurrentEnvironment(
            @PathVariable("environment") UnleashedConfigurationDTO.Environment environment) {
        return configService.setEnvironment(environment);
    }

    @PostMapping("/load")
    public void loadConfiguration(@RequestBody UnleashedConfigurationDTO config) {
        log.info("Loading unleashed configuration for env: {} into config store", config.getEnvironment());

        configService.loadConfiguration(config);
    }
}
