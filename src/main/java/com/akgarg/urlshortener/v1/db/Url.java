package com.akgarg.urlshortener.v1.db;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "urls")
@Getter
@Setter
@ToString
public final class Url {

    @Id
    private String id;

    @Field(name = "short_url")
    @NotNull(message = "shortUrl cannot be null")
    @NotBlank(message = "shortUrl cannot be empty")
    @Indexed(unique = true)
    private String shortUrl;

    @Field(name = "original_url")
    @NotNull(message = "original_url can't be null")
    private String originalUrl;

    @Field(name = "user_id")
    @NotNull(message = "user_id can't be null")
    private String userId;

    @Field(name = "created_at")
    @NotNull(message = "created_at can't be null")
    private Long createdAt;

    @Field(name = "expires_at")
    @NotNull(message = "expires_at can't be null")
    private Long expiresAt;

    @Field(name = "custom_alias")
    private boolean customAlias;

    public static Url fromShortUrl(final String shortUrl) {
        final Url url = new Url();
        url.setShortUrl(shortUrl);
        url.setCustomAlias(false);
        return url;
    }

}
