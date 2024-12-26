package com.akgarg.urlshortener.customalias.v1.db;

import com.akgarg.urlshortener.customalias.v1.CustomAlias;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomAliasRepository extends JpaRepository<CustomAlias, String> {

    long countByUserIdAndCreatedAtGreaterThanEqual(String userId, long timestampSince);

}