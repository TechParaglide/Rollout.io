package com.rollout.io.server.controlplaneservice.repository;

import com.rollout.io.server.controlplaneservice.entity.Flag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.rollout.io.server.controlplaneservice.entity.FlagCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoreFlagRepository extends MongoRepository<Flag, String> {
    
    List<Flag> findAllByEnvironmentIdAndCategory(String environmentId, FlagCategory category);

    Optional<Flag> findByEnvironmentIdAndKey(String environmentId, String key);

    Optional<Flag> findByEnvironmentIdAndDisplayName(String environmentId, String displayName);

}
