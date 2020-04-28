package net.jastrab.unleashedintegration.model;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "token")
public class OAuthTokenDTO {
    @Indexed(unique = true)
    private String tokenId;
    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime accessTokenExpiration;
    private final LocalDateTime refreshTokenExpiration;

    public OAuthTokenDTO(String accessToken, String refreshToken, LocalDateTime accessTokenExpiration, LocalDateTime refreshTokenExpiration) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

}
