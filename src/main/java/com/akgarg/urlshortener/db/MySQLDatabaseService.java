package com.akgarg.urlshortener.db;

import com.akgarg.urlshortener.url.UrlMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class MySQLDatabaseService implements DatabaseService, InitializingBean {

    private static final Logger LOGGER = LogManager.getLogger(MySQLDatabaseService.class);

    private static final String GET_URL_SQL = "SELECT * FROM urls WHERE short_url = ?";

    private final JdbcTemplate jdbcTemplate;

    public MySQLDatabaseService(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        jdbcTemplate.execute("""
                                     CREATE TABLE IF NOT EXISTS urls (
                                                 short_url VARCHAR(10) NOT NULL,
                                                 original_url VARCHAR(1000) NOT NULL,
                                                 user_id VARCHAR(50) NOT NULL,
                                                 created_at BIGINT NOT NULL,
                                                 PRIMARY KEY (short_url)
                                             );
                                     """);

        LOGGER.info("Database table(s) initialized successfully");
    }

    @Override
    public boolean saveUrlMetadata(final UrlMetadata urlMetadata) {
        try {
            final int insertedRowsCount = jdbcTemplate.update(
                    """
                            INSERT INTO urls (short_url, original_url, user_id, created_at)
                            VALUES (?, ?, ?, ?);
                            """,
                    urlMetadata.shortUrl(),
                    urlMetadata.originalUrl(),
                    urlMetadata.userId(),
                    urlMetadata.createdAt()
            );
            return insertedRowsCount == 1;
        } catch (Exception e) {
            LOGGER.error("Error occurred while saving url metadata to database", e);
            return false;
        }
    }

    @Override
    public Optional<UrlMetadata> getUrlMetadataByShortUrl(final String shortUrl) {
        Optional<UrlMetadata> urlMetadata;

        try {
            final List<UrlMetadata> selectQueryResult = jdbcTemplate.query(
                    GET_URL_SQL,
                    ps -> ps.setString(1, shortUrl),
                    new UrlMetadataRowMapper()
            );

            if (selectQueryResult.size() != 1) {
                LOGGER.error("No url record found for shortUrl: {}", shortUrl);
                urlMetadata = Optional.empty();
                return urlMetadata;
            }

            urlMetadata = Optional.of(selectQueryResult.getFirst());

            LOGGER.debug("Fetched url record for '{}' is: {}", shortUrl, urlMetadata.get());

        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching url metadata from database", e);
            urlMetadata = Optional.empty();
        }

        return urlMetadata;
    }

    private static class UrlMetadataRowMapper implements RowMapper<UrlMetadata> {
        @Override
        public UrlMetadata mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
            return new UrlMetadata(
                    resultSet.getString("short_url"),
                    resultSet.getString("original_url"),
                    resultSet.getString("user_id"),
                    resultSet.getLong("created_at")
            );
        }
    }

}