package com.rollout.io.server.controlplaneservice.repository;

import com.rollout.io.server.controlplaneservice.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByName(String name);

    List<Project> findAllByCreatedByUid(String createdByUid);

    Optional<Project> findByIdAndCreatedByUid(String id, String createdByUid);

    List<Project> findByCreatedByUidAndNameContainingIgnoreCase(String createdByUid, String name);

}
