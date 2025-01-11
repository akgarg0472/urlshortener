package com.akgarg.urlshortener.v1.url.db;

import com.akgarg.urlshortener.v1.url.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoUrlRepository extends MongoRepository<Url, String> {

    Optional<Url> findByShortUrl(String shortUrl);

}
