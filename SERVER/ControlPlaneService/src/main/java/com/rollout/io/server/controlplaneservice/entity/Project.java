package com.rollout.io.server.controlplaneservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "projects")
@Data
@CompoundIndex(
        name = "user_project_unique",
        def = "{'createdByUid': 1, 'name': 1}",
        unique = true
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    private String id;

    private String name;

    private String description;

    private String createdByUid;

    private Instant createdAt;

}

