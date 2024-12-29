package com.akgarg.urlshortener.customalias.v1;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@ToString
@Document(collection = "custom_alias")
public class CustomAlias {

    @Id
    private String alias;

    @Field(name = "user_id")
    @NotNull(message = "user_id cannot be null")
    private String userId;

    @Field(name = "created_at")
    @NotNull(message = "created_at cannot be null")
    private Long createdAt;

}
