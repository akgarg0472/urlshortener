package com.akgarg.urlshortener.customalias.v1.db;

import com.akgarg.urlshortener.customalias.v1.CustomAlias;

public interface CustomAliasDatabaseService {

    boolean addCustomAlias(CustomAlias customAlias);

    /**
     * Retrieves the count of custom aliases consumed by a user since a specified timestamp.
     *
     * @param userId         The identifier of the user to fetch custom aliases for.
     * @param timestampSince The timestamp (in milliseconds) indicating the starting point
     *                       from which to fetch custom aliases.
     * @return The count of custom aliases consumed by the user since the specified timestamp.
     */
    long getConsumedCustomAliasForUserIdSince(String userId, long timestampSince);

}
