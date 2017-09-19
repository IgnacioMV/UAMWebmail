package com.apps.nacho.uamwebmail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.nacho.uamwebmail.sqlite.dao.ContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.FolderDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.UserDAO;
import com.apps.nacho.uamwebmail.sqlite.model.Folder;
import com.apps.nacho.uamwebmail.sqlite.model.MyMessage;
import com.apps.nacho.uamwebmail.sqlite.model.User;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.URLName;

public class NewEmail extends AppCompatActivity {

    private User mUser;
    private Context mContext;

    private MyMessage mMessage;

    private String mailBody = "";
    private String mailEncoding = "";

    private boolean showCc = false;

    private static int ATTACHMENT_REQUEST = 1;
    private static int PICK_CONTACT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FolderDAO folderDao;
        UserDAO userDao;
        ContactDAO contactDao;
        MessageContactDAO messageContactDao;
        MessageDAO messageDao;
        Toolbar mToolbar;
        Folder mFolder;

        TextView newEmailFromText;
        AutoCompleteTextView newEmailAutoPara;
        AutoCompleteTextView newEmailCc;
        AutoCompleteTextView newEmailCco;
        final ImageButton newEmailShowCc;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_email);

        mContext = this;
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        newEmailFromText = (TextView) findViewById(R.id.new_email_from_text);

        newEmailShowCc = (ImageButton) findViewById(R.id.new_email_show_cc);
        newEmailShowCc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout newEmailCcCcoLayout = (LinearLayout) findViewById(R.id.new_email_cc_cco_layout);
                if (showCc) {
                    newEmailShowCc.setImageResource(R.drawable.ic_expand_more_24dp);
                    newEmailCcCcoLayout.setVisibility(View.GONE);
                    showCc = !showCc;
                } else {
                    newEmailShowCc.setImageResource(R.drawable.ic_expand_less_24dp);
                    newEmailCcCcoLayout.setVisibility(View.VISIBLE);
                    showCc = !showCc;
                }
            }
        });

        messageDao = new MessageDAO(this);
        folderDao = new FolderDAO(this);
        userDao = new UserDAO(this);
        contactDao = new ContactDAO(this);
        messageContactDao = new MessageContactDAO(this);

        Intent myIntent = getIntent();

        if (myIntent.hasExtra("message_id")) {
            messageDao.open();
            mMessage = messageDao.getMessageById(myIntent.getLongExtra("message_id", 0));
            messageDao.close();
        }
        folderDao.open();
        //mFolder = folderDao.getFolderById(mMessage.getFolderId());
        folderDao.close();

        userDao.open();
        mUser = userDao.getActiveUser();
        userDao.close();

        newEmailFromText.setText(mUser.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_email, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_attach:


                // custom dialog
                CharSequence colors[] = new CharSequence[]{"Foto", "Música", "Vídeo", "Contacto", "Archivo"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Elegir archivo adjunto");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        switch (which) {
                            case 0:
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("image/*");
                                startActivityForResult(intent, ATTACHMENT_REQUEST);
                                break;
                            case 1:
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("audio/*");
                                startActivityForResult(intent, ATTACHMENT_REQUEST);
                                break;
                            case 2:
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("video/*");
                                startActivityForResult(intent, ATTACHMENT_REQUEST);
                                break;
                            case 3:
                                intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                                startActivityForResult(intent, PICK_CONTACT);
                                break;
                            case 4:
                                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*");
                                startActivityForResult(intent, ATTACHMENT_REQUEST);
                                break;
                        }
                    }
                });
                builder.show();



                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == ATTACHMENT_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                System.out.println("Uri: " + uri.toString());
                dumpMetaData(uri);

            }
        }
        if (requestCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                System.out.println("Uri: " + uri.toString());
                System.out.println("pre dumping");
                dumpMetaData(uri);

            }
        }
    }

    public void dumpMetaData(Uri uri) {
        System.out.println("dumping metadata");
        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                System.out.println("Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                System.out.println("Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

}

