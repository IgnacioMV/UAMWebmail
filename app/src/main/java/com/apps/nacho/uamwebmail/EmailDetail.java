package com.apps.nacho.uamwebmail;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.app.LoaderManager.LoaderCallbacks;

import com.apps.nacho.uamwebmail.sqlite.dao.AttachmentDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.ContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.FolderDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.UserDAO;
import com.apps.nacho.uamwebmail.sqlite.model.Attachment;
import com.apps.nacho.uamwebmail.sqlite.model.Contact;
import com.apps.nacho.uamwebmail.sqlite.model.Folder;
import com.apps.nacho.uamwebmail.sqlite.model.MessageContact;
import com.apps.nacho.uamwebmail.sqlite.model.MyMessage;
import com.apps.nacho.uamwebmail.sqlite.model.User;
import com.sun.mail.imap.IMAPBodyPart;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class EmailDetail extends AppCompatActivity {


    private MessageDAO messageDao;
    AttachmentDAO attachmentDao;

    private MyAttachmentsAdapter mAttachmentsAdapter;

    private User mUser;

    private MyMessage mMessage;

    private WebView mBodyWebView;
    private ProgressDialog mProgressDialog;

    private boolean showDetails = false;
    private String mailBody = "";
    private String mailEncoding = "";
    private boolean isSlidingDrawerOpen = false;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FolderDAO folderDao;
        UserDAO userDao;
        ContactDAO contactDao;
        MessageContactDAO messageContactDao;

        Toolbar mToolbar;
        TextView mSubjectText;
        TextView mFolderText;
        TextView mFromText;
        TextView mToText;
        TextView mDetailsFromText;
        TextView mDetailsToText;
        TextView mDetailsDateText;
        Folder mFolder;
        RecyclerView mAttachmentsRecyclerView;
        RecyclerView.LayoutManager mAttachmentsLayoutManager;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_email_detail);

        mContext = this;

        final SlidingDrawer slidingdrawer;
        final Button slidingButton;
        LinearLayout slidingLayout;


        slidingdrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
        slidingLayout = (LinearLayout) findViewById(R.id.handle);
        slidingButton = (Button) findViewById(R.id.sliding_drawer_button);

        slidingdrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                isSlidingDrawerOpen = true;
                mBodyWebView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                //slidingdrawer.setBackgroundColor(Color.parseColor("#FFFFFF"));
                slidingButton.setBackgroundResource(R.drawable.ic_expand_more_24dp);
            }
        });

        slidingdrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {

            public void onDrawerClosed() {
                isSlidingDrawerOpen = false;
                mBodyWebView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                //slidingdrawer.setBackgroundColor(0x0000FF00);
                slidingButton.setBackgroundResource(R.drawable.ic_expand_less_24dp);
            }

        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent myIntent = getIntent();

        messageDao = new MessageDAO(this);
        folderDao = new FolderDAO(this);
        userDao = new UserDAO(this);
        contactDao = new ContactDAO(this);
        messageContactDao = new MessageContactDAO(this);
        attachmentDao = new AttachmentDAO(this);

        messageDao.open();
        mMessage = messageDao.getMessageById(myIntent.getLongExtra("message_id", 0));
        messageDao.close();

        folderDao.open();
        mFolder = folderDao.getFolderById(mMessage.getFolderId());
        folderDao.close();

        userDao.open();
        mUser = userDao.getActiveUser();
        userDao.close();

        mSubjectText = (TextView) findViewById(R.id.email_detail_subject);
        mSubjectText.setText(mMessage.getSubject());

        mFolderText = (TextView) findViewById(R.id.email_detail_folder);
        mFolderText.setText(mFolder.getName());

        mFromText = (TextView) findViewById(R.id.message_detail_from);

        mToText = (TextView) findViewById(R.id.message_detail_to);


        mDetailsFromText = (TextView) findViewById(R.id.more_details_from);

        mDetailsToText = (TextView) findViewById(R.id.more_details_to);

        mDetailsDateText = (TextView) findViewById(R.id.more_details_date);


        messageContactDao = new MessageContactDAO(this);

        messageContactDao.open();
        contactDao.open();
        List<MessageContact> fromMsgContact = messageContactDao.getMessageContactsByMessageId(mMessage.getId());

        for (MessageContact msgC : fromMsgContact) {
            if (msgC.getType() == 0) {
                Contact fromContact = contactDao.getContactById(msgC.getContactId());
                String name = fromContact.getName();
                String email = fromContact.getEmail();
                if (!fromContact.getName().equals("")) {
                    mFromText.setText(name);
                } else {
                    mFromText.setText(email);
                }
                mDetailsFromText.setText(Html.fromHtml(name + " <font color=blue>" + email));
                break;
            }
        }

        for (MessageContact msgC : fromMsgContact) {
            if (msgC.getType() == 1) {
                Contact fromContact = contactDao.getContactById(msgC.getContactId());
                String name = fromContact.getName();
                String email = fromContact.getEmail();
                if (!fromContact.getName().equals("")) {
                    mToText.setText("para " + name);
                } else {
                    mToText.setText("para " + email);
                }
                mDetailsToText.setText(Html.fromHtml(name + " <font color=blue>" + email));
                break;
            }
        }

        messageContactDao.close();
        contactDao.close();


        Date sentDate = new Date(mMessage.getSentDate());

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date());
        cal2.setTime(sentDate);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        mDetailsDateText.setText(DateFormat.getLongDateFormat(this).format(sentDate) + " " + DateFormat.getTimeFormat(this).format(sentDate));

        mBodyWebView = (WebView) findViewById(R.id.body_web_view);
        mBodyWebView.getSettings().setLoadsImagesAutomatically(mMessage.getShowImages() == 1 ? true : false);
        mBodyWebView.getSettings().setUseWideViewPort(true);
        mBodyWebView.getSettings().setSupportZoom(true);
        mBodyWebView.getSettings().setBuiltInZoomControls(true);
        mBodyWebView.getSettings().setDisplayZoomControls(false);
        mBodyWebView.getSettings().setLoadWithOverviewMode(true);
        mBodyWebView.loadData("loading...", "text/html", null);

        setShowDetails(!showDetails);

        cal1.setTime(new Date());
        cal2.setTime(sentDate);
        System.out.println(mMessage.getShowImages());
        final CheckedTextView ctv = (CheckedTextView) findViewById(R.id.showImages);
        ctv.setChecked((mMessage.getShowImages() == 1) ? true : false);
        ctv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv.isChecked()) {
                    ctv.setChecked(false);
                    mBodyWebView.getSettings().setLoadsImagesAutomatically(false);
                    if (!mailBody.equals("")) {
                        mBodyWebView.loadData(mailBody, mailEncoding, "base64");
                    }
                    messageDao.open();
                    mMessage.setShowImages(0);
                    messageDao.updateMessage(mMessage);
                    messageDao.close();
                } else {
                    ctv.setChecked(true);
                    mBodyWebView.getSettings().setLoadsImagesAutomatically(true);
                    messageDao.open();
                    mMessage.setShowImages(1);
                    messageDao.updateMessage(mMessage);
                    messageDao.close();
                }
                System.out.println(mMessage.getShowImages());
            }
        });

        attachmentDao.open();
        List<Attachment> listAttachments = attachmentDao.getAllMessageAttachments(mMessage.getId());
        attachmentDao.close();

        mAttachmentsRecyclerView = (RecyclerView) findViewById(R.id.email_detail_recycler_view);
        mAttachmentsLayoutManager = new LinearLayoutManager(this);
        mAttachmentsRecyclerView.setLayoutManager(mAttachmentsLayoutManager);
        mAttachmentsAdapter = new MyAttachmentsAdapter(this, listAttachments);
        mAttachmentsRecyclerView.setAdapter(mAttachmentsAdapter);

        new DownloadMailBodyByUIDTask(mUser, mFolder).execute(mMessage.getUid());

        // declare the dialog as a member field of your activity


        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(EmailDetail.this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
    }

    @Override
    public void onBackPressed() {
        SlidingDrawer slidingdrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
        if (isSlidingDrawerOpen) {
            isSlidingDrawerOpen = false;
            slidingdrawer.close();
        } else {
            super.onBackPressed();
        }
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class DownloadMailBodyByUIDTask extends AsyncTask<Long, Void, Boolean> {

        private final Folder mFolder;
        private String mimeEncoding = "";
        private String total = "";

        DownloadMailBodyByUIDTask(User user, Folder folder) {
            mUser = user;
            mFolder = folder;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Long... params) {

            if (isOnline()) {
                try {
                    long uid = params[0];

                    IMAPStore store = logIn();
                    store.connect();
                    if (store.isConnected()) {

                        IMAPFolder[] f = (IMAPFolder[]) store.getDefaultFolder().list();
                        IMAPFolder folder = (IMAPFolder) store.getFolder(mFolder.getName());
                        folder.open(IMAPFolder.READ_ONLY);

                        Message message = folder.getMessageByUID(uid);
                        Object content = message.getContent();
                        Enumeration headers = message.getAllHeaders();
                        while (headers.hasMoreElements()) {
                            Header h = (Header) headers.nextElement();
                            System.out.println(h.getName() + ": " + h.getValue());
                        }
                        System.out.println("message - " + message.getContentType());
                        if (message.isMimeType("text/*")) {

                            System.out.println("plain");

                            mimeEncoding = message.getContentType();
                            String[] bpContentTypeParts = mimeEncoding.split("; ");

                            InputStream is = message.getInputStream();
                            BufferedReader reader;
                            reader = new BufferedReader(new InputStreamReader(is, bpContentTypeParts[1].split("=")[1].toUpperCase()));
                            StringBuilder builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                builder.append(line + "\n");
                            }
                            total = content.toString();
                            return true;
                        }

                        if (message.isMimeType("multipart/*")) {
                            //System.out.println(((Multipart) content).getContentType());
                            //System.out.println("multipart");
                            String boundary = "";
                            if (((Multipart) content).getContentType().contains("boundary")) {
                                boundary = ((Multipart) content).getContentType().split("boundary=")[1];
                                //System.out.println("BOUNDARY: " + boundary);
                            }
                            MimeMultipart mmp = (MimeMultipart) content;
                            System.out.println(content.getClass());
                            attachmentDao.open();
                            decodeMultipart(mmp);
                            attachmentDao.close();

                            String sb = total;
                            String TAG = "SB";
                            System.out.println(total);
//                            while (total.contains("cid:")) {
//
//                                String pattern = "(?i)(<img.*?>)(.+?)()";
//                                total = total.replaceAll(pattern, "$2");
//                            }
//                            //System.out.println("Body Parts: " + mmp.getCount());
//                            StringBuilder builder = new StringBuilder();
//                            StringBuilder plainBuilder = new StringBuilder();
//                            StringBuilder htmlBuilder = new StringBuilder();
//                            Utils utils = new Utils();
//
//                            for (int i = 0; i < mmp.getCount(); i++) {
//
//
//                                //System.out.println("Next Part: " + i);
//                                boolean htmlFound = false;
//                                BodyPart bp = mmp.getBodyPart(i);
//                                String bpContentType = bp.getContentType();
//                                //System.out.println("BP Content Type: " + bpContentType);
//                                if (bpContentType.toLowerCase().contains("boundary")) {
//                                    //System.out.println("partception");
//                                }
//                                seeMimeMultipart(bp);
//
//                                //if (bpContentType.contains("multipart")) {
//                                //System.out.println(bp.getClass());
//                                if (bp instanceof IMAPBodyPart) {
//                                    //System.out.println("Is IMAPBodyPart");
//                                    IMAPBodyPart imapbp = (IMAPBodyPart) bp;
//                                    InputStream is = imapbp.getMimeStream();
//                                    BufferedReader reader;
//                                    reader = new BufferedReader(new InputStreamReader(is));
//
//                                    String line;
//                                    builder.append("----------------- PART " + i + "-----------------" + '\n');
//                                    builder.append(imapbp.getDisposition() + '\n');
//                                    String disposition = imapbp.getDisposition();
//                                    //System.out.println("DISPOSITION: " + disposition);
//                                    if (disposition != null && disposition.contains("attachment")) {
//                                        String attContType = imapbp.getContentType().split(";")[0];
//                                        String attEnc = imapbp.getEncoding();
//
//                                        String attFileName = null;
//                                        try {
//                                            attFileName = utils.decodeString(imapbp.getFileName());
//                                        } catch (DecoderException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        int attSize = imapbp.getSize();
//                                        builder.append("Content T: " + attContType + '\n');
//                                        builder.append("Enc: " + attEnc + '\n');
//                                        builder.append("File Name: " + imapbp.getFileName() + '\n');
//                                        builder.append("Decoded File Name: " + attFileName + '\n');
//                                        builder.append("Size: " + attSize + '\n');
//                                        //System.out.println(builder.toString());
//
//                                        File attFile = new File(Environment.getExternalStoragePublicDirectory(
//                                                Environment.DIRECTORY_DOWNLOADS), attFileName);
//                                        imapbp.saveFile(attFile);
//                                        //System.out.println(attFile.getAbsolutePath());
//                                    } else {
//                                        if (bpContentType.toLowerCase().contains("html")) {
//                                            htmlFound = true;
//                                        }
//                                        int k = 0;
//                                        while ((line = reader.readLine()) != null) {
//                                            k++;
//                                            //System.out.println(k+"--->"+line);
//                                            if (line.equals(boundary) || line.toLowerCase().contains("content-type")) {
//                                                continue;
//                                            }
//                                            if (line.toLowerCase().contains("content-transfer-encoding")) {
//                                                isQuotedP = true;
//                                                continue;
//                                            }
//                                            if (htmlFound) {
//                                                htmlBuilder.append(k + line + '\n');
//                                            } else {
//                                                plainBuilder.append(k + line + '\n');
//                                            }
//                                        }
//                                    }
//
//
//                                    //System.out.println();
//                                }
//
//                                //System.out.println(bp.getHeader()[0]);
//                                //}
//                            }
//                            total = builder.toString();
//                            if (htmlBuilder.length() > 0) {
//                                total += htmlBuilder.toString();
//                            } else if (plainBuilder.length() > 0) {
//                                total += plainBuilder.toString();
//                            }
                            return true;

/*
                            if (mmp.getCount() == 2) {
                                BodyPart bp1 = mmp.getBodyPart(0);
                                BodyPart bp2 = mmp.getBodyPart(1);
                                String mimeEnc1 = bp1.getContentType();
                                String mimeEnc2 = bp2.getContentType();
                                String[] bp1ContentParts = mimeEnc1.split("; ");
                                String mimeType1 = bp1ContentParts[0];
                                String encoding1 = bp1ContentParts[1].split("=")[1].toUpperCase();
                                String[] bp2ContentParts = mimeEnc2.split("; ");
                                String mimeType2 = bp2ContentParts[0];
                                String encoding2 = bp2ContentParts[1].split("=")[1].toUpperCase();

                                if (mimeType1.equals("text/plain") && mimeType2.equals("text/html")) {
                                    encoding = encoding2;
                                    mimeType = mimeType2;
                                    mimeEncoding = mimeEnc2;

                                    InputStream is = bp2.getInputStream();
                                    BufferedReader reader;
                                    reader = new BufferedReader(new InputStreamReader(is, encoding));

                                    StringBuilder builder = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                    total = builder.toString();
                                    return true;
                                }
                            }

                            BodyPart bp = mmp.getBodyPart(0);
                            mimeEncoding = bp.getContentType();
                            System.out.println(mimeEncoding);
                            String[] bpContentTypeParts = mimeEncoding.split("; ");
                            mimeType = bpContentTypeParts[0];
                            encoding = bpContentTypeParts[1].split("=")[1].toUpperCase();
                            System.out.println("parts: " + mmp.getCount());
                            if (mimeType.equals("text/plain")) {
                                InputStream is = bp.getInputStream();
                                BufferedReader reader;
                                reader = new BufferedReader(new InputStreamReader(is, encoding));

                                StringBuilder builder = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    builder.append(line + '\n');
                                }
                                total = builder.toString();
                                //System.out.println(total);
                            }
                            if (mimeType.equals("multipart/alternative")) {
                                System.out.println("ding ding");
                                InputStream is = bp.getInputStream();
                                BufferedReader reader;
                                System.out.println(encoding);
                                reader = new BufferedReader(new InputStreamReader(is, encoding));
                                StringBuilder plainBuilder = new StringBuilder();
                                StringBuilder htmlBuilder = new StringBuilder();
                                String line;

                                boolean toPlain = false;
                                boolean toHtml = false;
                                while ((line = reader.readLine()) != null) {

                                    if (line.contains("Content-Type: text/plain")) {
                                        System.out.println(line);
                                        toPlain = true;
                                        toHtml = false;
                                        continue;
                                    }

                                    if (line.contains("Content-Type: text/html")) {
                                        System.out.println(line);
                                        toHtml = true;
                                        toPlain = false;
                                        mimeType = "text/html";
                                        mimeEncoding = "text/html; charset=utf-8";
                                        continue;
                                    }

                                    if (line.contains("Content-Transfer-Encoding")) {
                                        continue;
                                    }

                                    if (toPlain) {
                                        plainBuilder.append(line);
                                    }

                                    if (toHtml) {
                                        //System.out.println(line);
                                        htmlBuilder.append(line);
                                    }

                                    if (line.contains("2d38aff9fafb2597c36fe7a6f2264c4e")) {
                                        System.out.println("-----------------------");
                                        toHtml = false;
                                        toPlain = false;
                                    }
                                }

                                if (htmlBuilder.toString().equals("")) {
                                    total = plainBuilder.toString();
                                } else {
                                    total = htmlBuilder.toString();
                                }


                            }*/
//                            for (int i = 0; i < mmp.getCount(); i++) {
//                                Part bp = mmp.getBodyPart(i);
//                                String bpContentType = bp.getContentType();
//                                String[] bpContentTypeParts = bpContentType.split("; ");
//                                mimeType = bpContentTypeParts[0];
//                                encoding = bpContentTypeParts[1].split("=")[1];
//                                System.out.println(mimeType+encoding);
//
//                                if (Pattern
//                                        .compile(Pattern.quote("boundary"),
//                                                Pattern.CASE_INSENSITIVE)
//                                        .matcher(bp.getContentType()).find()) {
//                                    System.out.println("BOUNDARY MESSAGE");
//                                    InputStream is = bp.getInputStream();
//                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//                                    String line;
//                                    String charset = "";
//                                    int inlineImgs = 0;
//                                    Boolean write = false;
//                                    BufferedReader reader2;
//
//                                    while ((line = reader.readLine()) != null) {
//                                        if (Pattern.compile(Pattern.quote("iso-8859-1"), Pattern.CASE_INSENSITIVE).matcher(line).find()) {
//                                            System.out.println("ISO FOUND");
//                                            charset = "iso-8859-1";
//                                            reader2 = new BufferedReader((new InputStreamReader(is)));
//                                            StringBuilder strBuilder = new StringBuilder();
//                                            while ((line = reader2.readLine()) != null) {
//                                                if (line.equals("Content-Type: text/html;")) {
//                                                    write = true;
//                                                }
//                                                if (line.contains("_NextPart_") || line.contains("Apple-Mail=") || line.contains("image/jpeg") || line.contains("image/png")) {
//                                                    System.out.println("APPLE MAIL APPLE MAIL APPLE MAIL APPLE MAIL APPLE MAIL APPLE MAIL APPLE MAIL ");
//                                                    inlineImgs++;
//                                                    write = false;
//                                                }
//                                                if (write) {
//                                                    strBuilder.append(line).append('\n');
//                                                }
//                                            }
//                                            total = strBuilder.toString();
//                                            break;
//                                        }
//                                        if (Pattern.compile(Pattern.quote("utf-8"), Pattern.CASE_INSENSITIVE).matcher(line).find()) {
//                                            System.out.println("UTF FOUND");
//                                            charset = "utf-8";
//                                            reader2 = new BufferedReader((new InputStreamReader(is)));
//                                            StringBuilder strBuilder = new StringBuilder();
//                                            while ((line = reader2.readLine()) != null) {
//                                                //System.out.println(line);
//                                                if (line.equals("Content-Type: text/html;")) {
//                                                    write = true;
//                                                }
//                                                if (line.contains("_NextPart_") || line.contains("Apple-Mail=_")) {
//                                                    inlineImgs++;
//                                                    write = false;
//                                                }
//                                                if (write) {
//                                                    strBuilder.append(line).append('\n');
//                                                }
//                                            }
//                                            total = strBuilder.toString();
//                                            break;
//                                        }
//                                    }
//
//                                }
//
//                                if (bp.getContentType().startsWith("text/html")) {
//                                    InputStream is = bp.getInputStream();
//                                    BufferedReader reader;
//                                    reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1));
//
//                                    String line;
//                                    while ((line = reader.readLine()) != null) {
//                                        total += line;
//                                    }
//                                }
//                            }
                        }


                        store.close();
                        return true;
                    }

                    store.close();
                    return false;

                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                System.out.println("SUCCESSFUL DOWNLOAD");
//                byte[] b = new byte[0];
//                try {
//                    b = total.getBytes(encoding);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                String s = "";
//                try {
//                    s = new String(b, "US-ASCII");
//                    System.out.println(s);
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                //System.out.println(s);
                //System.out.println(total);

                /*
                System.out.println(mimeEncoding);

                Charset charset = StandardCharsets.UTF_8;

                if (mimeEncoding.toLowerCase().contains("iso")) {
                    charset = StandardCharsets.ISO_8859_1;
                }

                String base64 = android.util.Base64.encodeToString(total.getBytes(charset), android.util.Base64.DEFAULT);
                mailBody = base64;
                mailEncoding = mimeEncoding;*/
                Log.v("TOTAL", total);
                WebSettings settings = mBodyWebView.getSettings();
                settings.setDefaultTextEncodingName("utf-8");

                mBodyWebView.loadDataWithBaseURL(null, total, "text/html", "utf-8", null);
                attachmentDao.open();
                List<Attachment> listAttachments = attachmentDao.getAllMessageAttachments(mMessage.getId());
                attachmentDao.close();
                Log.v("ATTN", String.valueOf(listAttachments.size()));
                for (Attachment atc : listAttachments) {
                    Log.v("ATT", atc.getName()+"."+atc.getFormat());
                }
                mAttachmentsAdapter.swap(listAttachments);


            } else {
                System.out.println("ERROR");
            }
        }

        @Override
        protected void onCancelled() {
        }

        private void decodeMultipart(MimeMultipart mmp) throws MessagingException, IOException {

            System.out.println("START OF DECODE BODY");
            System.out.println(mmp.getContentType());
            if (mmp.getContentType().toLowerCase().contains("multipart/mixed")) {
                Log.v("CONTENT-TYPE", "mixed");
                System.out.println("mixed - Number of parts: " + mmp.getCount());
                for (int i = 0; i < mmp.getCount(); i++) {
                    Log.v("MIXED", mmp.getBodyPart(i).getContent().toString());
                    String disposition = mmp.getBodyPart(i).getDisposition();
                    System.out.println("mixed - " + disposition);
                    if (mmp.getBodyPart(i).getContent() instanceof MimeMultipart) {
                        decodeMultipart((MimeMultipart) mmp.getBodyPart(i).getContent());
                    }
                    if ((disposition != null && disposition.contains("attachment")) || mmp.getBodyPart(i).isMimeType("*/*")) {
                        BodyPart bp = mmp.getBodyPart(i);
                        String attContType = bp.getContentType();
                        System.out.println("mixed attachment - " + attContType);
                        String filename = bp.getFileName();
                        Log.v("FILE", "filename: " + filename);
                        Utils utils = new Utils();
                        try {
                            filename = utils.decodeString(filename);
                        } catch (DecoderException e) {
                            e.printStackTrace();
                        }
                        Log.v("FILE", "filename: " + filename);
                        String[] chunks = filename.split("\\.");
                        Log.v("FILE", "format: " + chunks[chunks.length - 1]);
                        String name = "";
                        for (int j = 0; j < chunks.length - 1; j++) {
                            name += chunks[j];
                        }
                        String format = chunks[chunks.length - 1];

                        //attachmentDao.open();
                        List<Attachment> lstAtt = attachmentDao.getAllMessageAttachments(mMessage.getId());
                        boolean exists = false;
                        for (Attachment att : lstAtt) {
                            if (attachmentDao.existsAttachment(name, format, mMessage.getId())) {
                                exists = true;
                            }
                        }
                        if (!exists) {
                            Log.v("MESSAGEID", String.valueOf(mMessage.getId()));
                            Attachment newAttachment = attachmentDao.createAttachment(name, format, mMessage.getId());
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getId()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getName()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getFormat()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getPath()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getMsgId()));
                        }
                    }
                }
            }

            if (mmp.getContentType().toLowerCase().contains("multipart/alternative")) {
                Log.v("CONTENT-TYPE", "alternative");
                System.out.println("alternative - Number of parts: " + mmp.getCount());
                for (int i = 0; i < mmp.getCount(); i++) {
                    Log.v("ALTERNATIVE", mmp.getBodyPart(i).getContent().toString());
                    String disposition = mmp.getBodyPart(i).getDisposition();
                    System.out.println("alternative - " + disposition);
                    total = mmp.getBodyPart(i).getContent().toString();
                }
            }

            if (mmp.getContentType().toLowerCase().contains("multipart/related")) {
                Log.v("CONTENT-TYPE", "related");
                System.out.println("related - Number of parts: " + mmp.getCount());
                for (int i = 0; i < mmp.getCount(); i++) {
                    Log.v("RELATED", mmp.getBodyPart(i).getContent().toString());
                    String disposition = mmp.getBodyPart(i).getDisposition();
                    System.out.println("related - " + disposition);
                    if (mmp.getBodyPart(i).getContent() instanceof MimeMultipart) {
                        decodeMultipart((MimeMultipart) mmp.getBodyPart(i).getContent());
                    }
                    Log.v("RELATED SUBPART", mmp.getBodyPart(i).getContentType());
                    Log.v("BPCT",mmp.getBodyPart(i).getContentType());
                    Log.v("BPCT",String.valueOf(mmp.getBodyPart(i).isMimeType("image/jpeg")));
                    if ((disposition != null && disposition.contains("attachment")) || containsAttachment(mmp.getBodyPart(i).getContentType())) {
                        BodyPart bp = mmp.getBodyPart(i);
                        String attContType = bp.getContentType();
                        System.out.println("related attachment - " + attContType);
                        String filename = bp.getFileName();
                        Log.v("FILE", "filename: " + filename);
                        Utils utils = new Utils();
                        try {
                            filename = utils.decodeString(filename);
                        } catch (DecoderException e) {
                            e.printStackTrace();
                        }
                        Log.v("FILE", "filename: " + filename);
                        String[] chunks = filename.split("\\.");
                        Log.v("FILE", "format: " + chunks[chunks.length - 1]);
                        String name = "";
                        for (int j = 0; j < chunks.length - 1; j++) {
                            name += chunks[j];
                        }
                        String format = chunks[chunks.length - 1];

                        //attachmentDao.open();
                        List<Attachment> lstAtt = attachmentDao.getAllMessageAttachments(mMessage.getId());
                        boolean exists = false;
                        for (Attachment att : lstAtt) {
                            if (attachmentDao.existsAttachment(name, format, mMessage.getId())) {
                                System.out.println("attachment " + name + "." + format + " exists");
                                exists = true;
                            }
                        }
                        if (!exists) {
                            Log.v("MESSAGEID", String.valueOf(mMessage.getId()));
                            Attachment newAttachment = attachmentDao.createAttachment(name, format, mMessage.getId());
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getId()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getName()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getFormat()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getPath()));
                            Log.v("SAVED_MSG", String.valueOf(newAttachment.getMsgId()));
                        }
                        //attachmentDao.close();
                    }
                }
            }
        }
    }

    private IMAPStore logIn() {
        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties smtpProps = new Properties();
        Session session = Session.getDefaultInstance(smtpProps);

        smtpProps.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        smtpProps.setProperty("mail.smtp.socketFactory.fallback", "false");
        smtpProps.setProperty("mail.smtp.port", "993");
        smtpProps.setProperty("mail.smtp.socketFactory.port", "993");

        URLName url = new URLName("smtp", "correo.uam.es", 993, "",
                mUser.getEmail(), mUser.getPassword());

        session = Session.getInstance(smtpProps, null);

        return new IMAPSSLStore(session, url);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /*
            System.out.println(((Multipart) content).getContentType());
            System.out.println("multipart");
            String boundary = "";
            if (((Multipart) content).getContentType().contains("boundary")) {
                boundary = ((Multipart) content).getContentType().split("boundary=")[1];
                System.out.println("BOUNDARY: " + boundary);
            }
            MimeMultipart mmp = (MimeMultipart) content;
            System.out.println("Body Parts: " + mmp.getCount());
            StringBuilder builder = new StringBuilder();
            StringBuilder plainBuilder = new StringBuilder();
            StringBuilder htmlBuilder = new StringBuilder();
            Utils utils = new Utils();

            for (int i = 0; i < mmp.getCount(); i++) {

                System.out.println("Next Part: " + i);
                boolean htmlFound = false;
                BodyPart bp = mmp.getBodyPart(i);
                String bpContentType = bp.getContentType();
                System.out.println("BP Content Type: " + bpContentType);
                if (bpContentType.toLowerCase().contains("boundary")) {
                    System.out.println("partception");
                }
                seeMimeMultipart(bp);

                //if (bpContentType.contains("multipart")) {
                System.out.println(bp.getClass());
                if (bp instanceof IMAPBodyPart) {
                    System.out.println("Is IMAPBodyPart");
                    IMAPBodyPart imapbp = (IMAPBodyPart) bp;
                    InputStream is = imapbp.getMimeStream();
                    BufferedReader reader;
                    reader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    builder.append("----------------- PART " + i + "-----------------" + '\n');
                    builder.append(imapbp.getDisposition() + '\n');
                    String disposition = imapbp.getDisposition();
                    System.out.println("DISPOSITION: " + disposition);
                    if (disposition != null && disposition.contains("attachment")) {
                        String attContType = imapbp.getContentType().split(";")[0];
                        String attEnc = imapbp.getEncoding();

                        String attFileName = null;
                        try {
                            attFileName = utils.decodeString(imapbp.getFileName());
                        } catch (DecoderException e) {
                            e.printStackTrace();
                        }

                        int attSize = imapbp.getSize();
                        builder.append("Content T: " + attContType + '\n');
                        builder.append("Enc: " + attEnc + '\n');
                        builder.append("File Name: " + imapbp.getFileName() + '\n');
                        builder.append("Decoded File Name: " + attFileName + '\n');
                        builder.append("Size: " + attSize + '\n');
                        //System.out.println(builder.toString());

                        File attFile = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), attFileName);
                        imapbp.saveFile(attFile);
                        //System.out.println(attFile.getAbsolutePath());
                    } else {
                        if (bpContentType.toLowerCase().contains("html")) {
                            htmlFound = true;
                        }
                        int k = 0;
                        while ((line = reader.readLine()) != null) {
                            k++;
                            //System.out.println(k+"--->"+line);
                            if (line.equals(boundary) || line.toLowerCase().contains("content-type")) {
                                continue;
                            }
                            if (line.toLowerCase().contains("content-transfer-encoding")) {
                                isQuotedP = true;
                                continue;
                            }
                            if (htmlFound) {
                                htmlBuilder.append(k + line + '\n');
                            } else {
                                plainBuilder.append(k + line + '\n');
                            }
                        }
                    }
                }
            }
            total = builder.toString();
            if (htmlBuilder.length() > 0) {
                total += htmlBuilder.toString();
            } else if (plainBuilder.length() > 0) {
                total += plainBuilder.toString();
            }
            return true;
            store.close();
            return true;
        }

    */
    public void startDownloadAttachmentTask(long attchid) {
        System.out.println("touched");

        // execute this when the downloader must be fired
        final DownloadAttachmentTask downloadAttachmentTask = new DownloadAttachmentTask(EmailDetail.this);
        downloadAttachmentTask.execute(attchid);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadAttachmentTask.cancel(true);
            }
        });
    }

    private class DownloadAttachmentTask extends AsyncTask<Long, Void, Boolean> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadAttachmentTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            if (isOnline()) {
                try {
                    long attchId = params[0];

                    IMAPStore store = logIn();
                    store.connect();
                    if (store.isConnected()) {
                        AttachmentDAO attchDao = new AttachmentDAO(mContext);
                        MessageDAO msgDao = new MessageDAO(mContext);
                        FolderDAO folderDao = new FolderDAO(mContext);

                        attchDao.open();
                        Attachment attch = attchDao.getAttachmentById(attchId);
                        attchDao.close();

                        msgDao.open();
                        MyMessage myMesg = msgDao.getMessageById(attch.getMsgId());
                        msgDao.close();

                        folderDao.open();
                        Folder mFolder = folderDao.getFolderById(myMesg.getFolderId());
                        folderDao.close();

                        IMAPFolder[] f = (IMAPFolder[]) store.getDefaultFolder().list();
                        IMAPFolder folder = (IMAPFolder) store.getFolder(mFolder.getName());
                        folder.open(IMAPFolder.READ_ONLY);

                        Message message = folder.getMessageByUID(myMesg.getUid());
                        Object content = message.getContent();

                        if (message.isMimeType("multipart/*")) {
                            System.out.println("multipart");

                            MimeMultipart mmp = (MimeMultipart) content;

                            String filename = attch.getName() + "." + attch.getFormat();
                            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/uamwebmail");
                            if (!dir.isDirectory()) {
                                dir.mkdir();
                            }
                            String saveName = String.valueOf(myMesg.getId()) + "_" + String.valueOf(attch.getId() + "." + attch.getFormat());
                            File attFile = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) + "/uamwebmail/", saveName);

                            if (attFile.exists()) {
                                System.out.println("file exists");
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + attFile), getDataType(attFile));
                                startActivity(intent);
                                return true;
                            } else {
                                BodyPart bodyPartAttachment = findAttachment(filename, mmp);
                                if (bodyPartAttachment != null) {


                                    Log.v("SAVENAME", saveName);
                                    IMAPBodyPart imapbp = (IMAPBodyPart) bodyPartAttachment;

                                    imapbp.saveFile(attFile);
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse("file://" + attFile), getDataType(attFile));
                                    startActivity(intent);

                                    return true;
                                }
                                else {
                                    System.out.println("null bp");
                                }
                            }
                        } else {
                            return false;
                        }
                    }
                } catch (DecoderException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        private BodyPart findAttachment(String filename, MimeMultipart mmp) throws MessagingException, IOException, DecoderException {
            String contentType = mmp.getContentType().toLowerCase();
            Utils utils = new Utils();
            if (contentType.contains("multipart/mixed") || contentType.contains("multipart/related")) {
                for (int i = 0; i < mmp.getCount(); i++) {
                    String disposition = mmp.getBodyPart(i).getDisposition();
                    if (mmp.getBodyPart(i).getContent() instanceof MimeMultipart) {
                        findAttachment(filename, (MimeMultipart) mmp.getBodyPart(i).getContent());
                    }
                    System.out.println(mmp.getBodyPart(i).getContentType());
                    if ((disposition != null && disposition.contains("attachment")) || containsAttachment(mmp.getBodyPart(i).getContentType())) {
                        BodyPart tempBp = mmp.getBodyPart(i);

                        if (utils.decodeString(tempBp.getFileName()).equals(filename)) {
                            return tempBp;
                        }
                    }
                }
            }
            return null;
        }

        private String getDataType(File file) {
            String[] imgExtensions = new String[]{"bmp", "jpg", "png", "jpeg", "gif", "webp"};
            String[] videoExtensions = new String[]{"mp4", "webm"};
            String[] audioExtensions = new String[]{"mp3", "3gp", "flac", "wav", "ogg", "m4a", "aac"};
            String[] textExtensions = new String[]{"xml", "txt"};
            for (String extension : imgExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return "image/*";
                }
            }
            for (String extension : videoExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return "video/*";
                }
            }
            for (String extension : audioExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return "audio/*";
                }
            }
            for (String extension : textExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return "text/*";
                }
            }
            if (file.getName().toLowerCase().endsWith("pdf")) {
                return "application/pdf";
            }
            if (file.getName().toLowerCase().endsWith("html")) {
                return "text/html";
            }
            return "*/*";
        }
    }

    private boolean containsAttachment(String contentType) {
        String[] extensions = new String[]{"text/", "image/", "audio/", "video", "application", "model"};
        for (String extension : extensions) {
            if (contentType.toLowerCase().startsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}