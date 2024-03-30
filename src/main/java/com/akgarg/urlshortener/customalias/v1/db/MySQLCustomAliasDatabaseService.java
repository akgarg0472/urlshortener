package com.akgarg.urlshortener.customalias.v1.db;

import com.akgarg.urlshortener.customalias.v1.CustomAlias;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("prod")
@Service
@RequiredArgsConstructor
public class MySQLCustomAliasDatabaseService implements CustomAliasDatabaseService {

    private static final Logger LOGGER = LogManager.getLogger(MySQLCustomAliasDatabaseService.class);

    private final CustomAliasRepository customAliasRepository;

    @Override
    public boolean addCustomAlias(final CustomAlias customAlias) {
        try {
            customAliasRepository.save(customAlias);
            return true;
        } catch (Exception e) {
            LOGGER.error("");
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
