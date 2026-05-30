package com.rollout.io.server.authservice.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/**
 * Utility class resolving IP addresses to physical city/country representations.
 */
@Slf4j
public class GeoLocationHelper {

    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * Resolves the city and country associated with the given IP address.
     * Includes detection for common local/private networking ranges to prevent external DNS requests.
     *
     * @param ip the resolved request IP address
     * @return Formatted "City, Country" description
     */
    public static String getCityCountry(String ip) {
        if (ip == null || ip.isBlank()) {
            return "Unknown Location";
        }

        // Clean IPv6 localhost or default lookups
        String cleanIp = ip.trim();
        if (cleanIp.equals("127.0.0.1") || cleanIp.equals("0:0:0:0:0:0:0:1") || cleanIp.startsWith("::1")) {
            return "Localhost (Development)";
        }

        // Match private LAN IPv4 ranges: 192.168.x.x, 10.x.x.x, 172.16-31.x.x
        if (cleanIp.startsWith("192.168.") || cleanIp.startsWith("10.")) {
            return "Private Campus Network (Local)";
        }
        if (cleanIp.startsWith("172.")) {
            try {
                String[] parts = cleanIp.split("\\.");
                if (parts.length >= 2) {
                    int secondOctet = Integer.parseInt(parts[1]);
                    if (secondOctet >= 16 && secondOctet <= 31) {
                        return "Private Cloud Network (Local)";
                    }
                }
            } catch (Exception ignored) {}
        }

        // Call the free Geolocation API for public IPs
        try {
            String url = "http://ip-api.com/json/" + cleanIp;
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                String city = (String) response.get("city");
                String country = (String) response.get("country");
                if (city != null && country != null) {
                    return city + ", " + country;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch geolocation details for IP: {}. Exception: {}", cleanIp, e.getMessage());
        }

        return "Unknown Location (Public IP)";
    }

    /**
     * Extracts the real client IP address from the request, prioritizing proxy headers
     * such as X-Forwarded-For before falling back to the standard remote address.
     *
     * @param request the incoming HTTP Servlet Request
     * @return the resolved remote client IP address
     */
    public static String resolveClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank() || "unknown".equalsIgnoreCase(ipAddress)) {
            return request.getRemoteAddr();
        }
        return ipAddress.split(",")[0].trim();
    }
    
}
