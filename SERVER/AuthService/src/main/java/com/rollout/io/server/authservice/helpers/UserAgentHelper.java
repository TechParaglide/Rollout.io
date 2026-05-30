package com.rollout.io.server.authservice.helpers;

/**
 * A lightweight utility to parse User-Agent header strings into friendly OS, Browser, and Device types.
 */
public class UserAgentHelper {

    /**
     * Immutable data carrier representing parsed client environment details.
     * Implemented as a modern Java 17 Record.
     */
    public record DeviceDetails(String os, String browser, String deviceType) {}

    /**
     * Parses the incoming raw User-Agent header value into standard structured info.
     *
     * @param userAgent the raw User-Agent string from the client request headers
     * @return Resolved DeviceDetails mapping
     */
    public static DeviceDetails parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return new DeviceDetails("Unknown OS", "Unknown Browser", "Unknown Device");
        }

        String os = "Unknown OS";
        String browser = "Unknown Browser";
        String deviceType = "Desktop";

        String uaUpper = userAgent.toUpperCase();

        // OS Detection
        if (uaUpper.contains("WINDOWS")) {
            os = "Windows";
        } else if (uaUpper.contains("MACINTOSH") || uaUpper.contains("MAC OS")) {
            os = "macOS";
        } else if (uaUpper.contains("IPHONE")) {
            os = "iOS (iPhone)";
            deviceType = "Mobile";
        } else if (uaUpper.contains("IPAD")) {
            os = "iOS (iPad)";
            deviceType = "Tablet";
        } else if (uaUpper.contains("ANDROID")) {
            os = "Android";
            deviceType = "Mobile";
        } else if (uaUpper.contains("LINUX")) {
            os = "Linux";
        }

        // Browser Detection
        if (uaUpper.contains("EDG/")) {
            browser = "Microsoft Edge";
        } else if (uaUpper.contains("CHROME") && !uaUpper.contains("CHROMIUM")) {
            browser = "Google Chrome";
        } else if (uaUpper.contains("SAFARI") && !uaUpper.contains("CHROME")) {
            browser = "Apple Safari";
        } else if (uaUpper.contains("FIREFOX")) {
            browser = "Mozilla Firefox";
        } else if (uaUpper.contains("OPR/") || uaUpper.contains("OPERA")) {
            browser = "Opera";
        } else if (uaUpper.contains("TRIDENT") || uaUpper.contains("MSIE")) {
            browser = "Internet Explorer";
        }

        return new DeviceDetails(os, browser, deviceType);
    }

}
