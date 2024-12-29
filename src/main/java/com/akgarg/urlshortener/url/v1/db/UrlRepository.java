package com.akgarg.urlshortener.url.v1.db;

import com.akgarg.urlshortener.url.v1.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UrlRepository extends MongoRepository<Url, String> {
}
