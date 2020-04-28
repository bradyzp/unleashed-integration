package net.jastrab.unleashedintegration.controller;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.unleashedintegration.model.OAuthTokenDTO;
import net.jastrab.unleashedintegration.service.DigiKeyAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth/digikey")
public class DigiKeyAuthController {

    private final DigiKeyAuthService authService;

    public DigiKeyAuthController(DigiKeyAuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/url")
    public String getAuthUrl() {
        return authService.getAuthUri();
    }

    @GetMapping("/authorize")
    public ResponseEntity<String> authorizeApplication() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", authService.getAuthUri());
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/code")
    public void authCallbackHandler(@RequestParam("code") String code) {
        log.info("Received OAuth code {}", code);
        authService.exchangeCodeForToken(code);
    }

    @GetMapping("/token")
    public OAuthTokenDTO displayCurrentToken() {
        return authService.getToken().orElseThrow();

    }


}
