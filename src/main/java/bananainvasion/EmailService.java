package bananainvasion;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // Replace with your Gmail
    private static final String FROM_EMAIL = "wijerathnechanul@gmail.com";

    // Gmail App Password (NOT normal password)
    private static final String PASSWORD = "spod mblo dqcn hnri";

    public static void sendOTP(String toEmail, String code) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Connect to Gmail SMTP
        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));

            message.setSubject("Verification Code");

            // OTP sent here
            message.setText("Your OTP: " + code);

            Transport.send(message);

        } catch (Exception e) {
            System.out.println("Email error: " + e.getMessage());
        }
    }
}