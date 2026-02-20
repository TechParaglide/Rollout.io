package com.rollout.io.server.controlplaneservice.logic;

import com.rollout.io.server.controlplaneservice.entity.Flag;
import com.rollout.io.server.controlplaneservice.entity.FlagType;
import com.rollout.io.server.controlplaneservice.exceptions.RolloutError;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FlagHelperLogic {

    public void validateFlagValue(Flag flag) {
        FlagType type = flag.getType();
        Object value = flag.getValue();

        if (type == null) {
            throw new RolloutError("Flag type cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (value == null) {
            // For core flags, maybe null is executed as "false" or "empty"? 
            // But usually core flags should have a default value.
            throw new RolloutError("Flag value cannot be null for Core flags", HttpStatus.BAD_REQUEST);
        }

        try {
            switch (type) {
                case BOOLEAN:
                    if (!(value instanceof Boolean)) {
                        throw new IllegalArgumentException();
                    }
                    break;
                case INTEGER:
                    if (value instanceof Integer || value instanceof Long) {
                        flag.setValue(((Number) value).intValue());
                    } else if (value instanceof String) {
                        flag.setValue(Integer.parseInt((String) value));
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case DOUBLE:
                    if (value instanceof Number) {
                        flag.setValue(((Number) value).doubleValue());
                    } else if (value instanceof String) {
                        flag.setValue(Double.parseDouble((String) value));
                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case STRING:
                    if (!(value instanceof String)) {
                        flag.setValue(String.valueOf(value));
                    }
                    break;
                case JSON:
                    // skip for now
                    break;
            }
        } catch (Exception e) {
            throw new RolloutError("Invalid value for flag type " + type, HttpStatus.BAD_REQUEST);
        }
    }
}
