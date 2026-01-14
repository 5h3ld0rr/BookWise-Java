package bookwise.Mails;

import bookwise.Config;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public abstract class Mail {

    private static final String SMTP_HOST = Config.SMTP.HOST;
    private static final int SMTP_PORT = Config.SMTP.PORT;
    private static final String SMTP_USER = Config.SMTP.USER;
    private static final String SMTP_PASS = Config.SMTP.PASSWORD;

    protected abstract String getSubject();
    protected abstract String getBody();

    public void send(String email) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", SMTP_HOST);
                    props.put("mail.smtp.port", SMTP_PORT);
                    props.put("mail.smtp.ssl.trust", SMTP_HOST); // Sometimes needed

                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                        }
                    });

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(SMTP_USER, "BookWise"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    message.setSubject(getSubject());
                    message.setContent(getBody(), "text/html; charset=utf-8");

                    Transport.send(message);

                } catch (MessagingException e) {
                    throw e;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    String msg = cause != null ? cause.getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(null, "An error occurred: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
