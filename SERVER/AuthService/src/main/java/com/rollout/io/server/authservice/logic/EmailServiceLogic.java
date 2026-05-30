package com.rollout.io.server.authservice.logic;

import com.rollout.io.server.authservice.entity.User;
import com.rollout.io.server.authservice.helpers.GeoLocationHelper;
import com.rollout.io.server.authservice.helpers.UserAgentHelper;
import com.rollout.io.server.authservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link EmailService} that constructs and sends asynchronous
 * email alerts for user sign-in and sign-out activities using Spring Mail.
 * Leverages custom parsed device and geolocation parameters to provide rich security insights.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceLogic implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:resend}")
    private String mailUsername;

    @Value("${app.mail.sender:rollout@paraglide.in}")
    private String senderEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.of("Asia/Kolkata")); // Indian Standard Time (IST)

    /**
     * {@inheritDoc}
     * Resolves client details asynchronously and triggers a custom styled HTML login alert email.
     */
    @Async
    @Override
    public void sendLoginNotification(User user, String ipAddress, String userAgent) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Skipping login notification: User UID {} has no email registered.", user.getFirebaseUid());
            return;
        }

        UserAgentHelper.DeviceDetails device = UserAgentHelper.parse(userAgent);
        String location = GeoLocationHelper.getCityCountry(ipAddress);
        String timeStr = DATE_FORMATTER.format(Instant.now());

        String subject = "Security Alert: New sign-in to Rollout.io";
        String htmlContent = buildLoginEmailHtml(user.getDisplayName(), timeStr, device, location);

        dispatchEmail(email, subject, htmlContent);
    }

    /**
     * {@inheritDoc}
     * Resolves client details asynchronously and triggers a custom styled HTML logout confirmation email.
     */
    @Async
    @Override
    public void sendLogoutNotification(User user, String ipAddress, String userAgent) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("Skipping logout notification: User UID {} has no email registered.", user.getFirebaseUid());
            return;
        }

        UserAgentHelper.DeviceDetails device = UserAgentHelper.parse(userAgent);
        String location = GeoLocationHelper.getCityCountry(ipAddress);
        String timeStr = DATE_FORMATTER.format(Instant.now());

        String subject = "Signed Out: Successfully signed out of Rollout.io";
        String htmlContent = buildLogoutEmailHtml(user.getDisplayName(), timeStr, device, location);

        dispatchEmail(email, subject, htmlContent);
    }

    /**
     * Delivers the composed email payload using JavaMailSender SMTP configurations.
     * Automatically falls back to a clean terminal log preview if credentials are placeholders.
     *
     * @param recipient the target user email address
     * @param subject the subject line of the email
     * @param htmlContent the compiled responsive HTML body
     */
    private void dispatchEmail(String recipient, String subject, String htmlContent) {
        // Checking if the Resend SMTP password/API Key or sender configurations are placeholder
        if (mailUsername == null || mailUsername.isBlank() || mailUsername.equals("resend-placeholder")) {
            logFallback(recipient, subject, htmlContent, "SMTP not configured (Placeholder username detected)");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(senderEmail, "Rollout.io Accounts");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully dispatched email to: {} with subject: {}", recipient, subject);
        } catch (Exception e) {
            log.error("Failed to send email via SMTP to: {}. Exception: {}", recipient, e.getMessage());
            logFallback(recipient, subject, htmlContent, e.getMessage());
        }
    }

    /**
     * Utility fallback logger to print rendered email templates directly to the standard system console
     * for easy local debugging and validation.
     *
     * @param recipient the target user email address
     * @param subject the subject line of the email
     * @param htmlContent the compiled responsive HTML body
     * @param reason the descriptive cause for the fallback trigger
     */
    private void logFallback(String recipient, String subject, String htmlContent, String reason) {
        log.warn("\n========================================================================\n               EMAIL FALLBACK LOGGER - {}\n========================================================================\nRECIPIENT: {}\nSUBJECT:   {}\nCONTENT:\n{}\n========================================================================", reason.toUpperCase(), recipient, subject, htmlContent);
    }

    /**
     * Composes the custom styled, fully responsive HTML template for a successful login notification.
     * Styled with modern dark theme aesthetics to match Rollout.io brand guidelines.
     *
     * @param name the user display name
     * @param time the formatted session timestamp (IST)
     * @param device the resolved client browser and OS parameters
     * @param location the physical city and country of the request
     * @return Raw HTML email payload
     */
    private String buildLoginEmailHtml(String name, String time, UserAgentHelper.DeviceDetails device, String location) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Security Alert: New Sign-In</title>\n" +
                "  <style>\n" +
                "    body { font-family: 'Inter', -apple-system, sans-serif; background-color: #060211; color: #e2e8f0; margin: 0; padding: 0; }\n" +
                "    .container { max-width: 600px; margin: 40px auto; background-color: #0d0720; border: 1px solid #2d1b4e; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }\n" +
                "    .header { background: linear-gradient(135deg, #7c3aed 0%, #3b82f6 100%); padding: 30px; text-align: center; }\n" +
                "    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }\n" +
                "    .content { padding: 40px 30px; line-height: 1.6; }\n" +
                "    .greeting { font-size: 18px; font-weight: 600; color: #ffffff; margin-bottom: 20px; }\n" +
                "    .alert-text { color: #94a3b8; font-size: 15px; margin-bottom: 30px; }\n" +
                "    .details-table { width: 100%; border-collapse: collapse; background-color: #12092d; border-radius: 8px; overflow: hidden; margin-bottom: 30px; }\n" +
                "    .details-table td { padding: 14px 20px; border-bottom: 1px solid #221446; font-size: 14px; }\n" +
                "    .details-table td.label { color: #8b5cf6; font-weight: 600; width: 35%; }\n" +
                "    .details-table td.value { color: #ffffff; word-break: break-word; white-space: normal; overflow-wrap: break-word; }\n" +
                "    .warning-box { background: rgba(239, 68, 68, 0.1); border-left: 4px solid #ef4444; border-radius: 6px; padding: 15px 20px; font-size: 14px; color: #fca5a5; margin-bottom: 30px; }\n" +
                "    .footer { background-color: #0b051b; padding: 20px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #1a0e35; }\n" +
                "    .logo { color: #ffffff; font-weight: 800; font-size: 18px; text-decoration: none; }\n" +
                "    .logo span { color: #7c3aed; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>Rollout<span>.</span>io</h1>\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "      <div class=\"greeting\">Hey " + name + ",</div>\n" +
                "      <div class=\"alert-text\">We noticed a new login attempt to your Rollout.io profile from a new environment:</div>\n" +
                "      <table class=\"details-table\">\n" +
                "        <tr>\n" +
                "          <td class=\"label\">Time (IST)</td>\n" +
                "          <td class=\"value\">" + time + "</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td class=\"label\">Device / OS</td>\n" +
                "          <td class=\"value\">" + device.os() + " (" + device.deviceType() + ")</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td class=\"label\">Browser</td>\n" +
                "          <td class=\"value\">" + device.browser() + "</td>\n" +
                "        </tr>\n" +

                "        <tr>\n" +
                "          <td class=\"label\">Location</td>\n" +
                "          <td class=\"value\">" + location + "</td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      <div class=\"warning-box\">\n" +
                "        <strong>Was this not you?</strong> If this sign-in attempt was not authorized by you, please reset your password immediately inside your profile to safeguard your feature flags configuration.\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "      <a class=\"logo\" href=\"https://rollout.io\">Rollout<span>.</span>io</a><br><br>\n" +
                "      Licensed under the MIT License. &copy; 2026 Rollout.io Project Team.\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Composes the custom styled, fully responsive HTML template for a successful logout notification.
     * Styled with emerald accents to confirm security transitions.
     *
     * @param name the user display name
     * @param time the formatted session timestamp (IST)
     * @param device the resolved client browser and OS parameters
     * @param location the physical city and country of the request
     * @return Raw HTML email payload
     */
    private String buildLogoutEmailHtml(String name, String time, UserAgentHelper.DeviceDetails device, String location) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Successfully Signed Out</title>\n" +
                "  <style>\n" +
                "    body { font-family: 'Inter', -apple-system, sans-serif; background-color: #060211; color: #e2e8f0; margin: 0; padding: 0; }\n" +
                "    .container { max-width: 600px; margin: 40px auto; background-color: #0d0720; border: 1px solid #2d1b4e; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }\n" +
                "    .header { background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 30px; text-align: center; }\n" +
                "    .header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }\n" +
                "    .content { padding: 40px 30px; line-height: 1.6; }\n" +
                "    .greeting { font-size: 18px; font-weight: 600; color: #ffffff; margin-bottom: 20px; }\n" +
                "    .alert-text { color: #94a3b8; font-size: 15px; margin-bottom: 30px; }\n" +
                "    .details-table { width: 100%; border-collapse: collapse; background-color: #12092d; border-radius: 8px; overflow: hidden; margin-bottom: 30px; }\n" +
                "    .details-table td { padding: 14px 20px; border-bottom: 1px solid #221446; font-size: 14px; }\n" +
                "    .details-table td.label { color: #10b981; font-weight: 600; width: 35%; }\n" +
                "    .details-table td.value { color: #ffffff; word-break: break-word; white-space: normal; overflow-wrap: break-word; }\n" +
                "    .footer { background-color: #0b051b; padding: 20px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #1a0e35; }\n" +
                "    .logo { color: #ffffff; font-weight: 800; font-size: 18px; text-decoration: none; }\n" +
                "    .logo span { color: #10b981; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>Rollout<span>.</span>io</h1>\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "      <div class=\"greeting\">Hey " + name + ",</div>\n" +
                "      <div class=\"alert-text\">This email confirms that you have successfully logged out of your Rollout.io account:</div>\n" +
                "      <table class=\"details-table\">\n" +
                "        <tr>\n" +
                "          <td class=\"label\">Time (IST)</td>\n" +
                "          <td class=\"value\">" + time + "</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td class=\"label\">Device / OS</td>\n" +
                "          <td class=\"value\">" + device.os() + " (" + device.deviceType() + ")</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td class=\"label\">Browser</td>\n" +
                "          <td class=\"value\">" + device.browser() + "</td>\n" +
                "        </tr>\n" +

                "        <tr>\n" +
                "          <td class=\"label\">Location</td>\n" +
                "          <td class=\"value\">" + location + "</td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      <p style=\"font-size: 14px; color: #94a3b8;\">Thank you for developing with Rollout.io! We look forward to seeing you back inside your dashboard workspace soon.</p>\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "      <a class=\"logo\" href=\"https://rollout.io\">Rollout<span>.</span>io</a><br><br>\n" +
                "      Licensed under the MIT License. &copy; 2026 Rollout.io Project Team.\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
}
