package net.jastrab.unleashedintegration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * OAuthToken represents the token object provided by the OAuth Authorization Server
 */
@Value
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OAuthToken {
    @JsonIgnore
    private final LocalDateTime issued;
    private final String accessToken;
    private final String refreshToken;
    private final int expiresIn;
    private final int refreshTokenExpiresIn;
    private final String tokenType;

    @JsonCreator
    public OAuthToken(@JsonProperty("access_token") String accessToken,
                      @JsonProperty("refresh_token") String refreshToken,
                      @JsonProperty("expires_in") int expiresIn,
                      @JsonProperty("refresh_token_expires_in") int refreshTokenExpiresIn,
                      @JsonProperty("token_type") String tokenType) {
        this.issued = LocalDateTime.now();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.tokenType = tokenType;
    }

    public OAuthTokenDTO toTokenDTO() {
        return new OAuthTokenDTO(accessToken,
                refreshToken,
                issued.plusSeconds(expiresIn),
                issued.plusSeconds(refreshTokenExpiresIn));
    }
}
