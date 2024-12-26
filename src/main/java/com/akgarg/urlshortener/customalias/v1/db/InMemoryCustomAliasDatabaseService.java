package com.akgarg.urlshortener.customalias.v1.db;

import com.akgarg.urlshortener.customalias.v1.CustomAlias;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Profile("dev")
@Service
public class InMemoryCustomAliasDatabaseService implements CustomAliasDatabaseService {

    private final Map<String, List<CustomAlias>> db = new ConcurrentHashMap<>();

    @Override
    public boolean addCustomAlias(final CustomAlias customAlias) {
        final var userAlias = db.getOrDefault(customAlias.getUserId(), new ArrayList<>());
        userAlias.add(customAlias);
        db.put(customAlias.getUserId(), userAlias);
        return true;
    }

    @Override
    public long getConsumedCustomAliasForUserIdSince(final String userId, final long timestampSince) {
        final var userAlias = db.getOrDefault(userId, new ArrayList<>());
        return userAlias
                .stream()
                .filter(alias -> alias.getCreatedAt() >= timestampSince)
                .count();
    }

}
