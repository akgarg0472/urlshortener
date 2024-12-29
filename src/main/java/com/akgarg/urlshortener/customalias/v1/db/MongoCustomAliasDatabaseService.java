package com.akgarg.urlshortener.customalias.v1.db;

import com.akgarg.urlshortener.customalias.v1.CustomAlias;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class MongoCustomAliasDatabaseService implements CustomAliasDatabaseService {

    private final CustomAliasRepository customAliasRepository;

    @Override
    public boolean addCustomAlias(final CustomAlias customAlias) {
        try {
            customAliasRepository.save(customAlias);
            return true;
        } catch (Exception e) {
            log.error("Error adding/updating custom alias", e);
            return false;
        }
    }

    @Override
    public long getConsumedCustomAliasForUserIdSince(final String userId, final long timestampSince) {
        try {
            return this.customAliasRepository.countByUserIdAndCreatedAtGreaterThanEqual(userId, timestampSince);
        } catch (Exception e) {
            return 0;
        }
    }

}
