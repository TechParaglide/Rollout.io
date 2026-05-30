package com.rollout.io.server.authservice.service;

import com.rollout.io.server.authservice.entity.User;

/**
 * Service orchestrating direct, asynchronous HTML notifications for platform transactions.
 */
public interface EmailService {

    /**
     * Dispatches an asynchronous email notifying the developer of a new login session.
     *
     * @param user the authenticated User record
     * @param ipAddress the remote client IP address
     * @param userAgent the raw HTTP user agent header assert
     */
    void sendLoginNotification(User user, String ipAddress, String userAgent);

    /**
     * Dispatches an asynchronous email notifying the developer of a successful logout.
     *
     * @param user the authenticated User record
     * @param ipAddress the remote client IP address
     * @param userAgent the raw HTTP user agent header assert
     */
    void sendLogoutNotification(User user, String ipAddress, String userAgent);
    
}
