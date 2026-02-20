package com.rollout.io.server.controlplaneservice.repository;

import com.rollout.io.server.controlplaneservice.entity.Environment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends MongoRepository<Environment, String> {

    List<Environment> findAllByProjectId(String projectId);

    Optional<Environment> findByProjectIdAndName(String projectId, String name);

    Optional<Environment> findBySdkKey(String sdkKey);

}
