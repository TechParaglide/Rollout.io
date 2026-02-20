package com.rollout.io.server.controlplaneservice.logic;

import com.rollout.io.server.controlplaneservice.entity.Environment;
import com.rollout.io.server.controlplaneservice.entity.Flag;
import com.rollout.io.server.controlplaneservice.entity.FlagCategory;
import com.rollout.io.server.controlplaneservice.exceptions.RolloutError;
import com.rollout.io.server.controlplaneservice.repository.CoreFlagRepository;
import com.rollout.io.server.controlplaneservice.service.EnvironmentService;
import com.rollout.io.server.controlplaneservice.service.CoreFlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.rollout.io.server.controlplaneservice.helpers.JwtHelper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CoreFlagServiceLogic implements CoreFlagService {

    private final CoreFlagRepository coreFlagRepository;
    private final EnvironmentService environmentService;
    private final FlagHelperLogic flagHelperLogic;

    @Override
    public Flag createCoreFlag(Jwt jwt, String environmentId, Flag flag) {
        // Validate access to environment
        environmentService.getEnvironmentById(jwt, environmentId);

        if (coreFlagRepository.findByEnvironmentIdAndKey(environmentId, flag.getKey()).isPresent()) {
            throw new RolloutError("Flag with this key already exists in the environment", HttpStatus.CONFLICT);
        }

        if (flag.getDisplayName() != null && coreFlagRepository.findByEnvironmentIdAndDisplayName(environmentId, flag.getDisplayName()).isPresent()) {
            throw new RolloutError("Flag with this name already exists in the environment", HttpStatus.CONFLICT);
        }

        flagHelperLogic.validateFlagValue(flag);

        flag.setEnvironmentId(environmentId);
        flag.setCategory(FlagCategory.CORE);
        flag.setDependency(null); // Ensure dependency is null for CORE flags
        flag.setVersion(1);
        flag.setCreatedAt(Instant.now());
        flag.setUpdatedAt(Instant.now());
        flag.setCreatedByUid(JwtHelper.getUidFromJwt(jwt));

        if (flag.getEnabled() == null) {
            flag.setEnabled(false);
        }

        return coreFlagRepository.save(flag);
    }

    @Override
    public List<Flag> getCoreFlags(Jwt jwt, String environmentId) {
        environmentService.getEnvironmentById(jwt, environmentId);
        return coreFlagRepository.findAllByEnvironmentIdAndCategory(environmentId, FlagCategory.CORE);
    }

    @Override
    public Flag getCoreFlag(Jwt jwt, String flagId) {
        Flag flag = coreFlagRepository.findById(flagId)
                .orElseThrow(() -> new RolloutError("Flag not found", HttpStatus.NOT_FOUND));
        
        // Validate access
        environmentService.getEnvironmentById(jwt, flag.getEnvironmentId());

        return flag;
    }

    @Override
    public List<Flag> getCoreFlagsBySdkKey(String sdkKey) {
        // Find environment using the SDK key (public access endpoint concept)
        Environment environment = environmentService.getEnvironmentBySdkKey(sdkKey);

        return coreFlagRepository.findAllByEnvironmentIdAndCategory(environment.getId(), FlagCategory.CORE);
    }

    @Override
    public Flag updateCoreFlag(Jwt jwt, String flagId, Flag updateRequest) {
        Flag existingFlag = getCoreFlag(jwt, flagId); // Handles access check

        // Update basic fields
        if (updateRequest.getKey() != null && !updateRequest.getKey().equals(existingFlag.getKey())) {
             throw new RolloutError("Flag key is immutable and cannot be changed", HttpStatus.BAD_REQUEST);
        }

        if (updateRequest.getDisplayName() != null && !updateRequest.getDisplayName().equals(existingFlag.getDisplayName())) {
            if (coreFlagRepository.findByEnvironmentIdAndDisplayName(existingFlag.getEnvironmentId(), updateRequest.getDisplayName()).isPresent()) {
                throw new RolloutError("Flag with this name already exists", HttpStatus.CONFLICT);
            }
            existingFlag.setDisplayName(updateRequest.getDisplayName());
        }

        if (updateRequest.getDescription() != null) {
            existingFlag.setDescription(updateRequest.getDescription());
        }

        // Only allow core updates for now
        if (existingFlag.getCategory() != FlagCategory.CORE) {
             throw new RolloutError("Only Core flags can be updated via this endpoint", HttpStatus.BAD_REQUEST);
        }

        // Handle Type and Value updates
        boolean valueChanged = false;
        
        if (updateRequest.getType() != null && updateRequest.getType() != existingFlag.getType()) {
             throw new RolloutError("Flag type is immutable and cannot be changed", HttpStatus.BAD_REQUEST);
        }

        if (updateRequest.getValue() != null && !Objects.equals(updateRequest.getValue(), existingFlag.getValue())) {
            existingFlag.setValue(updateRequest.getValue());
            valueChanged = true;
        }
        
        if (valueChanged) {
            flagHelperLogic.validateFlagValue(existingFlag);
            existingFlag.setVersion(existingFlag.getVersion() + 1); // Increment version on value change
        }

        if (updateRequest.getEnabled() != null && !updateRequest.getEnabled().equals(existingFlag.getEnabled())) {
            throw new RolloutError("Flag 'enabled' status cannot be updated via this endpoint. Use the toggle endpoint instead.", HttpStatus.BAD_REQUEST);
        }

        existingFlag.setUpdatedAt(Instant.now());
        return coreFlagRepository.save(existingFlag);
    }

    @Override
    public void deleteCoreFlag(Jwt jwt, String flagId) {
        Flag flag = getCoreFlag(jwt, flagId); // Handles access check
        coreFlagRepository.delete(flag);
    }

    @Override
    public Flag toggleCoreFlag(Jwt jwt, String flagId) {
        Flag flag = getCoreFlag(jwt, flagId);
        flag.setEnabled(!Boolean.TRUE.equals(flag.getEnabled()));
        flag.setVersion(flag.getVersion() + 1); // Increment version when toggled
        flag.setUpdatedAt(Instant.now());
        return coreFlagRepository.save(flag);
    }

    // Delegated to JwtHelper
    
}
