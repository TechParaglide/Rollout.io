package com.rollout.io.server.controlplaneservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "flags")
@CompoundIndex(
        name = "env_flag_unique",
        def = "{'environmentId': 1, 'key': 1}",
        unique = true
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flag {

    @Id
    private String id;

    @Indexed
    private String environmentId;

    private String key;          // unique system identifier

    private String displayName;  // UI friendly name

    private String description;

    private FlagType type; // BOOLEAN, STRING, JSON

    private FlagCategory category; // CORE or DEPENDENT

    private Boolean enabled;

    private Object value;

    // Only used if category = DEPENDENT
    private RuleNode dependency;

    private Integer version;

    private String createdByUid;

    private Instant createdAt;

    private Instant updatedAt;

}