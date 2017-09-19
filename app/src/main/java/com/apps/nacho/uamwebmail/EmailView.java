package com.apps.nacho.uamwebmail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.apps.nacho.uamwebmail.sqlite.dao.ContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.FolderDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.UserDAO;
import com.apps.nacho.uamwebmail.sqlite.model.Contact;
import com.apps.nacho.uamwebmail.sqlite.model.Folder;
import com.apps.nacho.uamwebmail.sqlite.model.MyMessage;
import com.apps.nacho.uamwebmail.sqlite.model.User;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeUtility;

public class EmailView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private OnLoadingMoreFinished mOnLoadingMoreFinished;

    private UserDAO userDao;
    private FolderDAO folderDao;
    private MessageDAO messageDao;
    private ContactDAO contactDao;
    private MessageContactDAO messageContactDao;

    private User user;
    private Folder folder;

    private MyMessageListAdapter mMsgAdapter;

    List<MyMessage> msgList;
    private SwipeRefreshLayout mRefreshLayout;

    private static List<Long> uids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        DownloadFiftyFirstUIDs mDownloadFiftyFirstUIDsTask = null;
        RecyclerView mDrawerRecyclerView;
        RecyclerView.Adapter mDrawerAdapter;
        RecyclerView mMsgRecyclerView;
        RecyclerView.LayoutManager mDrawerLayoutManager;
        RecyclerView.LayoutManager mMsgLayoutManager;
        Toolbar mToolbar;
        DrawerLayout mDrawer;

        super.onCreate(savedInstanceState);

        Intent myIntent = getIntent();
        String folderName = myIntent.getStringExtra("folder");
        if (folderName == null) {
            folderName = "INBOX";
        }

        setContentView(R.layout.activity_email_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(folderName);
        setSupportActionBar(mToolbar);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        // Iniciar la tarea asíncrona al revelar el indicador
        mRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        System.out.println("refreshing");
                        new DownloadNewUIDs(folder).execute();
                    }
                }
        );
        final Context context = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewEmail.class);
                System.out.println(this.getClass());
                context.startActivity(intent);
                //((Activity) context).finish();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        userDao = new UserDAO(this);
        folderDao = new FolderDAO(this);
        messageDao = new MessageDAO(this);
        contactDao = new ContactDAO(this);
        messageContactDao = new MessageContactDAO(this);


        userDao.open();
        folderDao.open();

        this.user = userDao.getActiveUser();
        System.out.println(folderName);
        List<Folder> folders = folderDao.getAllUserFolders(user.getId());
        System.out.println(folders.size());
        for (Folder fd : folders) {
            System.out.println(fd.getName());
            if (fd.getName().equals(folderName)) {
                this.folder = fd;
            }
        }

        userDao.close();
        folderDao.close();

        messageDao.open();
        this.msgList = messageDao.getAllFolderMessages(folder.getId());

        messageDao.close();

        String[] folderNames = new String[folders.size()];
        for (int i = 0; i < folders.size(); i++) {
            folderNames[folders.size() - i - 1] = folders.get(i).getName();
        }

        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.nav_drawer_recycler_view);
        mMsgRecyclerView = (RecyclerView) findViewById(R.id.content_mail_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mDrawerRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mDrawerLayoutManager = new LinearLayoutManager(this);
        mMsgLayoutManager = new LinearLayoutManager(this);
        mDrawerRecyclerView.setLayoutManager(mDrawerLayoutManager);
        mMsgRecyclerView.setLayoutManager(mMsgLayoutManager);

        // specify an adapter (see also next example)

        mDrawerAdapter = new MyAdapter(folderNames, mDrawer, mToolbar, this);
        mDrawerRecyclerView.setAdapter(mDrawerAdapter);
        mMsgAdapter = new MyMessageListAdapter(this, this.msgList, folderName);
        mMsgRecyclerView.setAdapter(mMsgAdapter);


        if (this.msgList.size() < 1) {
            mMsgAdapter.setIsLoading(true);
            mDownloadFiftyFirstUIDsTask = new DownloadFiftyFirstUIDs(this.folder);
            mDownloadFiftyFirstUIDsTask.execute();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.email_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            userDao.open();
            userDao.setNoActiveUser();
            userDao.close();
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            ((Activity) this).finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public class DownloadMailByUIDTask extends AsyncTask<Long, Void, Boolean> {

        private final User mUser;
        private final Folder mFolder;

        DownloadMailByUIDTask(User user, Folder folder) {
            mUser = user;
            mFolder = folder;
        }

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
                        int numMsg = folder.getMessageCount();

                        messageDao.open();
                        contactDao.open();
                        messageContactDao.open();
                        Message message = folder.getMessageByUID(uid);
                        if (!messageDao.existsMessage(uid, mFolder.getId())) {
                            int seen = (message.getFlags().contains(Flags.Flag.SEEN)) ? 1 : 0;
                            String subject = message.getSubject();
                            if (subject == null) {
                                subject = "Sin Asunto";
                            }
                            int showImages = 0;
                            MyMessage msg = messageDao.createMessage(uid, subject, message.getSentDate().getTime(), seen, showImages, mFolder.getId());

                            Utils utils = new Utils();

                            String from = message.getFrom()[0].toString();
                            String[] addresses = utils.getTranslatedAddress(from);
                            Contact contact;
                            if (contactDao.existsContact(addresses[0])) {
                                // TODO
                            } else {
                                contact = contactDao.createContact(addresses[1], addresses[0]);
                                messageContactDao.createMessageContact(msg.getId(), contact.getId(), 0);
                            }

                            String to = message.getRecipients(Message.RecipientType.TO)[0].toString();
                            String[] toAddresses = utils.getTranslatedAddress(to);
                            if (contactDao.existsContact(addresses[0])) {
                                // TODO
                            } else {
                                contact = contactDao.createContact(toAddresses[1], toAddresses[0]);
                                messageContactDao.createMessageContact(msg.getId(), contact.getId(), 1);
                            }

                            Address[] cc = message.getRecipients(Message.RecipientType.CC);
                            if (cc != null) {
                                for (Address addr : cc) {
                                    addresses = utils.getTranslatedAddress(addr.toString());
                                    if (!contactDao.existsContact(addr.toString())) {
                                        contact = contactDao.createContact(addresses[1], addresses[0]);
                                        messageContactDao.createMessageContact(msg.getId(), contact.getId(), 2);
                                    }
                                }
                            }

                            Address[] bcc = message.getRecipients(Message.RecipientType.BCC);
                            if (bcc != null) {
                                for (Address addr : bcc) {
                                    addresses = utils.getTranslatedAddress(addr.toString());
                                    if (!contactDao.existsContact(addr.toString())) {
                                        contact = contactDao.createContact(addresses[1], addresses[0]);
                                        messageContactDao.createMessageContact(msg.getId(), contact.getId(), 3);
                                    }
                                }
                            }
                        }
                        messageDao.close();
                        contactDao.close();
                        messageContactDao.close();
                        store.close();
                        return true;
                    }

                    store.close();
                    return false;

                } catch (MessagingException | UnsupportedEncodingException | DecoderException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                System.out.println("SUCCESSFUL DOWNLOAD");

                messageDao.open();
                msgList = messageDao.getAllFolderMessages(folder.getId());
                messageDao.close();
                uids.remove(0);
                if (uids.size() == 0) {
                    mMsgAdapter.setIsLoading(false);
                } else {
                    new DownloadMailByUIDTask(user, folder).execute(uids.get(0));
                    mMsgAdapter.setIsLoading(true);
                }
                mMsgAdapter.swap(msgList);

            } else {
                System.out.println("ERROR");
            }
        }

        @Override
        protected void onCancelled() {
        }

//            long msgUid = folder.getUID(msg);
//            RealmResults<MessageItem> result = realm.where(MessageItem.class)
//                    .equalTo("fuid", title+String.valueOf(msgUid)).findAll();
//            final MessageItem msgItem = new MessageItem();
//
//            msgItem.setFuid(folder.getFullName() + String.valueOf(folder.getUID(msg)));
//            msgItem.setUid(folder.getUID(msg));
//
//            String from = msg.getFrom()[0].toString();
//            String[] addresses = getTranslatedAddress(from);
//            msgItem.setFromAddress(addresses[1]);
//            msgItem.setFromName(addresses[0]);
//
//            Address[] to = msg.getRecipients(Message.RecipientType.TO);
//            String strTo = "";
//            if (to != null) {
//                for (Address addr : to) {
//                    if (strTo.length() != 0)
//                        strTo += ";";
//                    strTo += addr;
//                }
//            }
//
//            Address[] cc = msg.getRecipients(Message.RecipientType.CC);
//            String strCc = "";
//            if (cc != null) {
//                for (Address addr : cc) {
//                    if (strCc.length() != 0)
//                        strCc += ";";
//                    strCc += addr;
//                }
//            }
//            msgItem.setCc(strCc);
//
//            Address[] bcc = msg.getRecipients(Message.RecipientType.BCC);
//            String strBcc = "";
//            if (bcc != null) {
//                for (Address addr : bcc) {
//                    if (strBcc.length() != 0)
//                        strBcc += ";";
//                    strBcc += addr;
//                }
//            }
//            msgItem.setBcc(strBcc);
//
//            msgItem.setSubject(msg.getSubject());
//            msgItem.setSentDate(msg.getSentDate());
//            msgItem.setSeen(msg.getFlags().contains(Flags.Flag.SEEN));
//            msgItem.setFolder(folder.getFullName());


//        public class MyAdapter extends RecyclerView.Adapter<MyAdapter.DrawerViewHolder> {
//            private String[] mDataset;
//
//            // Provide a reference to the views for each data item
//            // Complex data items may need more than one view per item, and
//            // you provide access to all the views for a data item in a view holder
//            public class DrawerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//                // each data item is just a string in this case
//                public TextView mTextView;
//
//                public DrawerViewHolder(View v) {
//                    super(v);
//                    v.setOnClickListener(this);
//                    mTextView = (TextView) v.findViewById(R.id.folder_name);
//                }
//
//                @Override
//                public void onClick(View view) {
//                    title = (String) mTextView.getText();
//                    mToolbar.setTitle(mTextView.getText());
//
//                    mDrawer.closeDrawers();
//                }
//            }
//
//            // Provide a suitable constructor (depends on the kind of dataset)
//            public MyAdapter(String[] myDataset) {
//                mDataset = myDataset;
//            }
//
//            // Create new views (invoked by the layout manager)
//            @Override
//            public DrawerViewHolder onCreateViewHolder(ViewGroup parent,
//                                                       int viewType) {
//                // create a new view
//                View v = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.my_text_view, parent, false);
//                // set the view's size, margins, paddings and layout parameters
//
//                DrawerViewHolder vh = new MyAdapter.DrawerViewHolder(v);
//                return vh;
//            }
//
//            // Replace the contents of a view (invoked by the layout manager)
//            @Override
//            public void onBindViewHolder(DrawerViewHolder holder, int position) {
//                // - get element from your dataset at this position
//                // - replace the contents of the view with that element
//                holder.mTextView.setText(mDataset[position]);
//
//            }
//
//            // Return the size of your dataset (invoked by the layout manager)
//            @Override
//            public int getItemCount() {
//                return mDataset.length;
//            }
//
//        }
//
//    }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public IMAPStore logIn() {
        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties smtpProps = new Properties();

        smtpProps.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        smtpProps.setProperty("mail.smtp.socketFactory.fallback", "false");
        smtpProps.setProperty("mail.smtp.port", "993");
        smtpProps.setProperty("mail.smtp.socketFactory.port", "993");

        URLName url = new URLName("smtp", "correo.uam.es", 993, "",
                user.getEmail(), user.getPassword());

        Session session = Session.getInstance(smtpProps, null);

        return new IMAPSSLStore(session, url);
    }




    public class DownloadFiftyFirstUIDs extends AsyncTask<Void, Void, Boolean> {

        private final Folder mFolder;

        DownloadFiftyFirstUIDs(Folder folder) {
            mFolder = folder;
            uids = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            if (isOnline()) {
                try {

                    IMAPStore store = logIn();
                    store.connect();
                    if (store.isConnected()) {

                        IMAPFolder folder = (IMAPFolder) store.getFolder(mFolder.getName());
                        folder.open(IMAPFolder.READ_ONLY);

                        Message[] messages = folder.getMessages();
                        messageDao.open();

                        int limit = 20;
                        if (messages.length < limit) {
                            limit = messages.length;
                        }

                        for (int i = messages.length - 1; i >= (messages.length - limit); i--) {
                            long uid = folder.getUID(messages[i]);
                            if (!messageDao.existsMessage(uid, mFolder.getId())) {
                                uids.add(uid);
                            }
                        }

                        messageDao.close();

                        store.close();
                        return true;
                    }
                    store.close();
                    return false;

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }


            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                System.out.println("SUCCESSFUL 20 UIDS DOWNLOAD");
                if (uids.size() == 0) {
                    mMsgAdapter.setIsLoading(false);
                } else {
                    new DownloadMailByUIDTask(user, folder).execute(uids.get(0));
                }
            } else {
                System.out.println("ERROR 20 UIDS DOWNLOAD");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class DownloadTenNextUIDs extends AsyncTask<Void, Void, Boolean> {

        private final Folder mFolder;

        DownloadTenNextUIDs(Folder folder) {
            mFolder = folder;
            uids = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            if (isOnline()) {
                try {

                    IMAPStore store = logIn();
                    store.connect();
                    if (store.isConnected()) {
                        messageDao.open();
                        long lastUid = messageDao.getMinimumUIDByFolder(mFolder.getId());
                        messageDao.close();
                        IMAPFolder folder = (IMAPFolder) store.getFolder(mFolder.getName());
                        folder.open(IMAPFolder.READ_ONLY);

                        Message[] messages = folder.getMessages();
                        messageDao.open();

                        for (int i = messages.length - 1; i >= 0; i--) {
                            long uid = folder.getUID(messages[i]);
                            if (uid > lastUid) {
                                continue;
                            } else {
                                if (!messageDao.existsMessage(uid, mFolder.getId())) {
                                    uids.add(uid);
                                }

                            }

                            if (uids.size() > 10) {
                                break;
                            }
                        }

                        messageDao.close();

                        store.close();
                        return true;
                    }
                    store.close();
                    return false;

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }


            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                System.out.println("SUCCESSFUL UIDS DOWNLOAD");
                if (uids.size() == 0) {
                    mMsgAdapter.setIsLoading(false);
                } else {
                    new DownloadMailByUIDTask(user, folder).execute(uids.get(0));
                }


            } else {
                System.out.println("ERROR NEXT 10 UIDS DOWNLOAD");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class DownloadNewUIDs extends AsyncTask<Void, Void, Boolean> {

        private final Folder mFolder;

        DownloadNewUIDs(Folder folder) {
            mFolder = folder;
            uids = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (isOnline()) {
                try {

                    IMAPStore store = logIn();
                    store.connect();
                    if (store.isConnected()) {
                        IMAPFolder folder = (IMAPFolder) store.getFolder(mFolder.getName());
                        folder.open(IMAPFolder.READ_ONLY);

                        long onlineLastUid = folder.getUIDNext() - 1;
                        long onlineHmseq = folder.getHighestModSeq();
                        int onlineNumMsg = folder.getMessageCount();
                        int onlineNewMsgNum = folder.getNewMessageCount();

//                        if (onlineHmseq == mFolder.getHmseq()) {
//                            return false;
//                        }

                        Message[] messages = folder.getMessages();
                        messageDao.open();

                        long lastUid = messageDao.getMaximumUIDByFolder(mFolder.getId());

                        for (int i = messages.length - 1; i >= 0; i--) {
                            long uid = folder.getUID(messages[i]);
                            if (uid <= lastUid) {
                                break;
                            } else {
                                if (!messageDao.existsMessage(uid, mFolder.getId())) {
                                    uids.add(uid);
                                }
                            }
                        }

                        mFolder.setLastUID(onlineLastUid);
                        mFolder.setHmseq(onlineHmseq);
                        mFolder.setMessageNumber(onlineNumMsg);
                        mFolder.setNewMessages(onlineNewMsgNum);
                        folderDao.open();
                        folderDao.updateFolder(mFolder);
                        folderDao.close();

                        store.close();
                        return true;
                    }
                    store.close();
                    return false;

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }


            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                System.out.println("SUCCESSFUL NEW UIDS DOWNLOAD");
                new UpdateExistingEmails(folder).execute();
            } else {
                mRefreshLayout.setRefreshing(false);
                System.out.println("ERROR NEW UIDS DOWNLOAD");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public class UpdateExistingEmails extends AsyncTask<Void, Void, Boolean> {

        private final Folder mFolder;

        UpdateExistingEmails(Folder folder) {
            mFolder = folder;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            System.out.println("updating");
            if (isOnline()) {
                try {

                    IMAPStore store = logIn();
                    store.connect();
                    if (store.isConnected()) {
                        IMAPFolder folder = (IMAPFolder) store.getFolder(mFolder.getName());
                        folder.open(IMAPFolder.READ_ONLY);

                        messageDao.open();
                        for (MyMessage msg : msgList) {
                            if (folder.getMessageByUID(msg.getUid()) == null) {
                                messageDao.deleteMessage(msg.getId());
                            }
                        }
                        msgList = messageDao.getAllFolderMessages(mFolder.getId());
                        messageDao.close();

                        store.close();
                        return true;
                    }
                    store.close();
                    return false;

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                System.out.println("SUCCESSFUL UIDS DOWNLOAD");
                mMsgAdapter.swap(msgList);
                if (uids.size() == 0) {
                    mMsgAdapter.setIsLoading(false);
                } else {
                    new DownloadMailByUIDTask(user, folder).execute(uids.get(0));
                }
            } else {
                System.out.println("ERROR NEXT 10 UIDS DOWNLOAD");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public void onLoadMoreCalled() {
        DownloadTenNextUIDs mDownloadTenNextUIDs = new DownloadTenNextUIDs(folder);
        mDownloadTenNextUIDs.execute();
    }

}


