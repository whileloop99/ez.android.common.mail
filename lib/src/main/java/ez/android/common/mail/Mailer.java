package ez.android.common.mail;

import android.text.TextUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 */
public class Mailer {
    private boolean mDebuggable = false;
    private String mSslPort;
    private String mSubject = "";
    private String mContent = "";
    private Set<String> mToEmails = new HashSet<>();
    private Set<String> mCcEmails = new HashSet<>();
    private Set<String> mBccEmails = new HashSet<>();
    private String mFromEmail;
    private Multipart mMultipart;
    private String mEncoding = "ISO-2022-JP";
    private String mUsername;
    private String mPassword;
    private MailTransport mTransport;

    /**
     *
     */
    public enum MailTransport {
        smtp
    }

    /**
     *
     * @param username
     * @param password
     * @return
     */
    public Mailer authenticate(String username, String password) {
        this.mUsername = username;
        this.mPassword = password;
        return this;
    }

    /**
     *
     * @param transport
     * @return
     */
    public Mailer transport(MailTransport transport) {
        this.mTransport = transport;
        return this;
    }

    /**
     *
     * @param debuggable
     * @return
     */
    public Mailer debugable(boolean debuggable) {
        mDebuggable = debuggable;
        return this;
    }

    /**
     *
     * @param sslPort
     * @return
     */
    public Mailer sslPort(String sslPort) {
        mSslPort = sslPort;
        return this;
    }

    /**
     *
     */
    public void send() {
        Session session = Session.getInstance(buildSessionProperties(), new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mUsername, mPassword);
            }
        });
        try {
            // Build message;
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mFromEmail));
            message.setSubject(mSubject);
            message.setText(mContent);

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TextUtils.join(",", mToEmails)));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(TextUtils.join(",", mCcEmails)));
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(TextUtils.join(",", mBccEmails)));
//            for (String email : mToEmails) {
//                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
//            }
//            for (String email : mCcEmails) {
//                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(email));
//            }
//            for (String email : mBccEmails) {
//                message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(email));
//            }

            // Attachments
            BodyPart msgBodyPart = new MimeBodyPart();
            msgBodyPart.setText(mContent);
            mMultipart.addBodyPart(msgBodyPart);

            message.setContent(mMultipart, mEncoding);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public static Mailer createInstance() {
        Mailer mailer = new Mailer();
        mailer.mMultipart = new MimeMultipart();
        return mailer;
    }

    /**
     *
     * @return
     */
    private Properties buildSessionProperties() {
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        if (mDebuggable) {
            props.put("mail.debug", "true");
        }

        if (mUsername != null || mPassword != null) {
            props.put("mail.smtp.auth", "true");
        }

        if (mSslPort != null) {
            props.put("mail.smtp.socketFactory.port", mSslPort);
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        return props;
    }

    /**
     *
     * @param subject
     * @return
     */
    public Mailer subject(String subject) {
        mSubject = subject;
        return this;
    }

    /**
     *
     * @param content
     * @return
     */
    public Mailer content(String content) {
        mContent = content;
        return this;
    }

    /**
     *
     * @param toEmails
     * @return
     */
    public Mailer to(String... toEmails) {
        mToEmails.addAll(Arrays.asList(toEmails));
        return this;
    }

    /**
     *
     * @param toEmails
     * @return
     */
    public Mailer to(Collection<String> toEmails) {
        mToEmails.addAll(toEmails);
        return this;
    }

    /**
     *
     * @param ccEmails
     * @return
     */
    public Mailer cc(String... ccEmails) {
        mCcEmails.addAll(Arrays.asList(ccEmails));
        return this;
    }

    /**
     *
     * @param ccEmails
     * @return
     */
    public Mailer cc(Collection<String> ccEmails) {
        mCcEmails.addAll(ccEmails);
        return this;
    }

    /**
     *
     * @param bccEmails
     * @return
     */
    public Mailer bcc(String... bccEmails) {
        mBccEmails.addAll(Arrays.asList(bccEmails));
        return this;
    }

    /**
     *
     * @param bccEmails
     * @return
     */
    public Mailer bcc(Collection<String> bccEmails) {
        mBccEmails.addAll(bccEmails);
        return this;
    }

    /**
     *
     * @param fromEmail
     * @return
     */
    public Mailer from(String fromEmail) {
        mFromEmail = fromEmail;
        return this;
    }

    /**
     *
     * @param encoding
     * @return
     */
    public Mailer encoding(String encoding) {
        mEncoding = encoding;
        return this;
    }

    /**
     *
     * @param file
     * @return
     * @throws Exception
     */
    public Mailer attach(File file) throws Exception {
        if(file == null) return this;

        BodyPart msgBodyPart = new MimeBodyPart();

        DataSource source = new FileDataSource(file);
        msgBodyPart.setDataHandler(new DataHandler(source));

        msgBodyPart.setFileName(file.getName());

        mMultipart.addBodyPart(msgBodyPart);
        return this;
    }
}
