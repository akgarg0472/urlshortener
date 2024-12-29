package com.akgarg.urlshortener.url.v1;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "urls")
@Getter
@Setter
@ToString
public final class Url {

    @Id
    @Field(name = "short_url")
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

    @Field(name = "is_custom_alias")
    private Boolean isCustomAlias;

    public static Url fromShortUrl(final String shortUrl) {
        final Url url = new Url();
        url.setShortUrl(shortUrl);
        return url;
    }

}
