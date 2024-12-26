package com.akgarg.urlshortener.customalias.v1;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Getter
@Setter
@ToString
@Table(name = "custom_aliases")
public final class CustomAlias {

    @Id
    private String alias;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

}
