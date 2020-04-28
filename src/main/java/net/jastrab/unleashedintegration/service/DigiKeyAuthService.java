package net.jastrab.unleashedintegration.service;

import lombok.extern.slf4j.Slf4j;
import net.jastrab.unleashedintegration.configuration.DigiKeyConfigurationProperties;
import net.jastrab.unleashedintegration.exception.AuthenticationException;
import net.jastrab.unleashedintegration.model.OAuthToken;
import net.jastrab.unleashedintegration.model.OAuthTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DigiKeyAuthService {
    private final TemporalAmount minTokenLifetime = Duration.ofMinutes(2);
    private final DigiKeyConfigurationProperties properties;
    private final MongoTemplate mongoTemplate;
    private final RestTemplate authTemplate;

    private final Query getTokenQuery;
    private final HttpHeaders formRequestHeaders = new HttpHeaders();

    @Autowired
    public DigiKeyAuthService(DigiKeyConfigurationProperties properties,
                              MongoTemplate mongoTemplate) {
        this.properties = properties;
        this.mongoTemplate = mongoTemplate;
        this.authTemplate = new RestTemplateBuilder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        getTokenQuery = Query.query(Criteria.where("tokenId").is(properties.getTokenId()));
        formRequestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * Build the URI used to authorize the application with DigiKey's OAuth server
     * @return String http uri which the user must visit to authorize the application
     */
    public String getAuthUri() {
        return UriComponentsBuilder.fromHttpUrl(properties.getAuthUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .build()
                .toUriString();
    }

    /**
     * Exchange a oauth `code` for a OAuthToken, and store it
     *
     * @param code
     */
    public void exchangeCodeForToken(String code) {
        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("code", List.of(code));
        params.put("client_id", List.of(properties.getClientId()));
        params.put("client_secret", List.of(properties.getClientSecret()));
        params.put("redirect_uri", List.of(properties.getRedirectUri()));
        params.put("grant_type", List.of("authorization_code"));

        log.info("Exchanging OAuth code for token");
        ResponseEntity<OAuthToken> response = authTemplate.exchange(properties.getTokenUri(),
                HttpMethod.POST,
                new HttpEntity<>(params, formRequestHeaders),
                OAuthToken.class);

        Optional.ofNullable(response.getBody())
                .map(OAuthToken::toTokenDTO)
                .map(this::saveToken)
                .orElseThrow(() -> new AuthenticationException("OAuth token exchange failed, response status code: " + response.getStatusCode()));
    }

    /**
     * Load a stored OAuthTokenDTO and refresh it if it will expire soon
     * @return Optional OAuthTokenDTO
     */
    public Optional<OAuthTokenDTO> getToken() {
        return loadToken().flatMap(this::refreshToken);
    }

    private boolean willExpire(OAuthTokenDTO token) {
        return token.getAccessTokenExpiration().isBefore(LocalDateTime.now().plus(minTokenLifetime));
    }

    private Optional<OAuthTokenDTO> refreshToken(OAuthTokenDTO token) {
        if(!willExpire(token)) {
            log.info("Token is still valid within the defined lifetime");
            return Optional.of(token);
        }

        log.info("Token will expire within defined lifetime, refreshing");
        final LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("client_id", List.of(properties.getClientId()));
        params.put("client_secret", List.of(properties.getClientSecret()));
        params.put("refresh_token", List.of(token.getRefreshToken()));
        params.put("grant_type", List.of("refresh_token"));

        final ResponseEntity<OAuthToken> response = authTemplate.exchange(
                properties.getTokenUri(),
                HttpMethod.POST,
                new HttpEntity<>(params, formRequestHeaders),
                OAuthToken.class
        );

        return Optional.ofNullable(response.getBody())
                .map(OAuthToken::toTokenDTO)
                .map(this::saveToken);
    }

    private Optional<OAuthTokenDTO> loadToken() {
        return Optional.ofNullable(mongoTemplate.findOne(getTokenQuery, OAuthTokenDTO.class));
    }

    private OAuthTokenDTO saveToken(OAuthTokenDTO token) {
        log.info("Saving token to mongoDb with id: {}", properties.getTokenId());
        token.setTokenId(properties.getTokenId());
        final OAuthTokenDTO savedToken = mongoTemplate.findAndReplace(getTokenQuery, token, FindAndReplaceOptions.options().upsert().returnNew());
        log.info("Saved token: {} to mongo", savedToken);
        return savedToken;
    }

}
