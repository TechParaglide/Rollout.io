package com.rollout.io.server.controlplaneservice.helpers;

import com.rollout.io.server.controlplaneservice.exceptions.RolloutError;
import com.rollout.io.server.controlplaneservice.objects.Helper;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

@Helper
public class JwtHelper {

    private JwtHelper() {
        // Private constructor to hide the implicit public one in utility classes
    }

    public static String getUidFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new RolloutError("Invalid authentication token", HttpStatus.UNAUTHORIZED);
        }
        String uid = jwt.getSubject();
        if (uid == null || uid.isBlank()) {
            throw new RolloutError("Invalid token: UID missing", HttpStatus.UNAUTHORIZED);
        }
        return uid;
    }

}
