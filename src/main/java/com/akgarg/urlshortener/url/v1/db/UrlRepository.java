package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<Url, String> {
}
