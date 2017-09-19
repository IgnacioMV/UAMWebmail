package com.apps.nacho.uamwebmail;

import android.os.AsyncTask;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;


public class Email extends AsyncTask {

    private final String email;
    private final String password;

    public Email(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public Map readMessageByUID(String folderName, long UID) {

        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties smtpProps = new Properties();
        Session session = Session.getDefaultInstance(smtpProps);

        smtpProps.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        smtpProps.setProperty("mail.smtp.socketFactory.fallback", "false");
        smtpProps.setProperty("mail.smtp.port", "993");
        smtpProps.setProperty("mail.smtp.socketFactory.port", "993");

        URLName url = new URLName("smtp", "correo.uam.es", 993, "",
                email, password);

        session = Session.getInstance(smtpProps, null);


        String nUID = "";
        String from = "";
        String subject = "";
        String sentDate = "";
        String read = "";

        Map<String, Object> msgMap = new HashMap<String, Object>();

        try {
            IMAPStore store = new IMAPSSLStore(session, url);
            store.connect();

            IMAPFolder[] f = (IMAPFolder[]) store.getDefaultFolder().list();

            IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);

            // try to open read/write and if that fails try read-only
            folder.open(IMAPFolder.READ_ONLY);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.CONTENT_INFO);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add("Message-ID");

            Message message = folder.getMessageByUID(UID);
            if (message == null) {
                return null;
            }

            msgMap.put("uid", folder.getUID(message));

            // Quoted Printable & Base64
            QuotedPrintableCodec qpc = new QuotedPrintableCodec();
            Base64 base64 = new Base64();

            String parts0 = "";
            String parts1 = "";

            from = message.getFrom()[0].toString();
            if (from.contains("=?utf-8?Q?") || from.contains("=?UTF-8?Q?")) {
                String[] fromParts;
                if (from.contains("=?utf-8?Q?")) {
                    fromParts = from.split(Pattern.quote("=?utf-8?Q?"));
                } else {
                    fromParts = from.split(Pattern.quote("=?UTF-8?Q?"));
                }
                for (int j = 0; j < fromParts.length; j++) {
                    String str = fromParts[j];
                    str = str.replace("?= ", "");
                    str = qpc.decode(str);
                    str = str.replace("_", " ");
                    fromParts[j] = str;
                }
                String fromDecoded = "";
                for (String str : fromParts) {
                    fromDecoded += str;
                }
                fromParts = fromDecoded.split(Pattern.quote("<"));
                fromParts[1] = "<" + fromParts[1];
                parts0 = fromParts[0];
                parts1 = fromParts[1];
            } else if (from.contains("=?utf-8?B?") || from.contains("=?UTF-8?B?")) {
                String[] fromParts;
                if (from.contains("=?utf-8?B?")) {
                    fromParts = from.split(Pattern.quote("=?utf-8?B?"));
                } else if (from.contains("=?UTF-8?B?")) {
                    fromParts = from.split(Pattern.quote("=?UTF-8?B?"));
                } else {
                    fromParts = from.split(Pattern.quote("=?utf-8?b?"));
                }
                from = from.substring(10);
                fromParts = from.split(Pattern.quote("?= "));
                for (int k = 0; k < fromParts.length; k++) {
                    String str = fromParts[k];
                    if (!str.contains("@"))
                        str = new String(base64.decode(str.getBytes()));
                    fromParts[k] = str;
                }
                parts0 = fromParts[0];
                parts1 = fromParts[1];
            } else if (from.contains("=?iso-8859-1?Q?") || from.contains("=?ISO-8859-1?Q?") || from.contains("=?iso-8859-1?B?") || from.contains("=?ISO-8859-1?B?") || from.contains("=?iso-8859-1?b?")) {
                String[] fromParts;
                if (from.substring(0, 1).equals("\""))
                    from = from.substring(1);
                if (from.contains("=?\""))
                    from = from.replace("=?\"", "=)");
                if (from.contains("=?iso-8859-1?Q?")) {
                    fromParts = from.split(Pattern.quote("=?iso-8859-1?Q?"));
                } else if (from.contains("=?ISO-8859-1?Q?")) {
                    fromParts = from.split(Pattern.quote("=?ISO-8859-1?Q?"));
                } else if (from.contains("=?iso-8859-1?B?")) {
                    fromParts = from.split(Pattern.quote("=?iso-8859-1?B?"));
                } else if (from.contains("=?ISO-8859-1?B?")) {
                    fromParts = from.split(Pattern.quote("=?ISO-8859-1?B?"));
                } else {
                    fromParts = from.split(Pattern.quote("=?iso-8859-1?b?"));
                }

                for (int l = 0; l < fromParts.length; l++) {
                    String str = fromParts[l];
                    str = MimeUtility.decodeText(from);
                    System.out.println("String: " + str);
                    fromParts[l] = str;
                }
                String fromDecoded = "";
                for (String str : fromParts)
                    fromDecoded += str;
                fromParts = fromDecoded.split(Pattern.quote("<"));
                fromParts[1] = "<" + fromParts[1];
                parts0 = fromParts[0];
                parts1 = fromParts[1];
            } else {
                if (from.substring(0, 1).equals("<") || !from.split(Pattern.quote("@"))[0].contains(" ")) {
                    if (!from.split(Pattern.quote("@"))[0].contains(" "))
                        from = "<" + from + ">";
                    String[] fromParts = {"", from};
                    parts0 = fromParts[0];
                    parts1 = fromParts[1];
                } else {
                    String[] fromParts = from.split(Pattern.quote(" <"));
                    fromParts[1] = "<" + fromParts[1];
                    parts0 = fromParts[0];
                    parts1 = fromParts[1];
                }
            }
            parts0 = "0";
            parts1 = "1";
            msgMap.put("fromName", parts0);
            msgMap.put("fromAddress", parts1);

            Address[] to = message.getRecipients(Message.RecipientType.TO);
            String strTo = "";
            if (to != null) {
                for (Address addr : to) {
                    if (strTo.length() != 0)
                        strTo += ";";
                    strTo += addr;
                }
            }
            msgMap.put("to", strTo);

            Address[] cc = message.getRecipients(Message.RecipientType.CC);
            String strCc = "";
            if (cc != null) {
                for (Address addr : cc) {
                    if (strCc.length() != 0)
                        strCc += ";";
                    strCc += addr;
                }
            }
            msgMap.put("cc", strCc);

            Address[] bcc = message.getRecipients(Message.RecipientType.BCC);
            String strBcc = "";
            if (bcc != null) {
                for (Address addr : bcc) {
                    if (strBcc.length() != 0)
                        strBcc += ";";
                    strBcc += addr;
                }
            }
            msgMap.put("bcc", strBcc);

            msgMap.put("subject", message.getSubject());
            msgMap.put("sentDate", String.valueOf(message.getSentDate().getTime()));
            msgMap.put("seen", String.valueOf(message.getFlags().contains(Flags.Flag.SEEN)));

            if (folder.isOpen()) {
                folder.close(true);
                store.close();
            }


        } catch (MessagingException e) {
            if (e instanceof AuthenticationFailedException) {

            }
        } catch (DecoderException e) {
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msgMap;
    }


    private void sendMail() {
        final String username = "ignacio.martin.velasco@alumnos.upm.es";
        final String password = "Picmajo00";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.upm.es");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("ignacio.martin.velasco@alumnos.upm.es"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("nmvmm22@gmail.com"));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler,"
                    + "\n\n No spam to my email, please!");

//            MimeBodyPart messageBodyPart = new MimeBodyPart();
//
//            Multipart multipart = new MimeMultipart();
//
//            messageBodyPart = new MimeBodyPart();
//            String file = "path of file to be attached";
//            String fileName = "attachmentName";
//            DataSource source = new FileDataSource(file);
//            messageBodyPart.setDataHandler(new DataHandler(source));
//            messageBodyPart.setFileName(fileName);
//            multipart.addBodyPart(messageBodyPart);
//
//            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object doInBackground(Object[] params) {
        return null;
    }
}