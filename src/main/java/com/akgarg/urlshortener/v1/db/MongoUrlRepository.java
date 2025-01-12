package com.akgarg.urlshortener.v1.db;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoUrlRepository extends MongoRepository<Url, String> {

    Optional<Url> findByShortUrl(String shortUrl);

}
