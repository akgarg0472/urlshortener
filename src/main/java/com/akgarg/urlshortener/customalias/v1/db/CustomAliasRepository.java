package com.akgarg.urlshortener.customalias.v1.db;

import com.akgarg.urlshortener.customalias.v1.CustomAlias;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomAliasRepository extends MongoRepository<CustomAlias, String> {

    long countByUserIdAndCreatedAtGreaterThanEqual(String userId, long timestampSince);

}