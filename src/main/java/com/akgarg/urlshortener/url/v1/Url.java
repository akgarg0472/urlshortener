package com.akgarg.urlshortener.url.v1;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Getter
@Setter
@ToString
@Table(name = "urls")
public final class Url {

    @Id
    @Column(name = "short_url")
    private String shortUrl;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "is_custom_alias")
    private Boolean isCustomAlias;

    public static Url fromShortUrl(final String shortUrl) {
        final Url url = new Url();
        url.setShortUrl(shortUrl);
        return url;
    }

}
