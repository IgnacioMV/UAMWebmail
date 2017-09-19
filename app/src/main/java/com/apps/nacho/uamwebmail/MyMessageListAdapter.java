package com.apps.nacho.uamwebmail;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apps.nacho.uamwebmail.sqlite.dao.ContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageContactDAO;
import com.apps.nacho.uamwebmail.sqlite.model.Contact;
import com.apps.nacho.uamwebmail.sqlite.model.MessageContact;
import com.apps.nacho.uamwebmail.sqlite.model.MyMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by nacho on 10/10/16.
 */

public class MyMessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MyMessage> msgItemList;
    private Context mContext;
    private MessageContactDAO mMessageContactDao;
    private ContactDAO mContactDao;
    private String mFolderName;
    private boolean hasLoadButton = true;
    private boolean isLoading = false;
    private final int TITLE = 0;
    private final int LOAD_MORE = 1;


    public MyMessageListAdapter(Context context, List<MyMessage> msgItemList, String folderName) {
        this.msgItemList = msgItemList;
        this.mContext = context;
        this.mMessageContactDao = new MessageContactDAO(context);
        this.mContactDao = new ContactDAO(context);
        this.mFolderName = folderName;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;

        if (viewType == LOAD_MORE) {

            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.load_more_row, null);

            LoadMoreViewHolder vh = new LoadMoreViewHolder(v);

            return vh;
        }

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_row, null);

        MessageViewHolder mh = new MessageViewHolder(v);

        return mh;
    }

    public boolean isHasLoadButton() {
        return hasLoadButton;
    }

    public void setHasLoadButton(boolean hasLoadButton) {
        this.hasLoadButton = hasLoadButton;
        notifyDataSetChanged();
    }

    public boolean isLoadingMore() {
        return isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
        this.notifyItemChanged(msgItemList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position < (getItemCount()-1)) {
            return TITLE;
        } else {
            return LOAD_MORE;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof LoadMoreViewHolder) {
            LoadMoreViewHolder lmh = (LoadMoreViewHolder) viewHolder;

            if (isLoading) {
                lmh.loadMore.setVisibility(View.GONE);
                lmh.spinner.setVisibility(View.VISIBLE);
            }
            else {
                lmh.loadMore.setVisibility(View.VISIBLE);
                lmh.spinner.setVisibility(View.GONE);
            }

        } else {
            MessageViewHolder mh = (MessageViewHolder) viewHolder;
            MyMessage msgItem = msgItemList.get(position);
            mMessageContactDao.open();
            mContactDao.open();
            List<MessageContact> fromMsgContact = mMessageContactDao.getMessageContactsByMessageId(msgItem.getId());
            for (MessageContact msgC : fromMsgContact) {
                if (mFolderName.equals("Enviados")) {
                    if (msgC.getType() == 1) {
                        Contact fromContact = mContactDao.getContactById(msgC.getContactId());
                        String name = fromContact.getName();
                        String email = fromContact.getEmail();
                        if (!fromContact.getName().equals("")) {
                            mh.from.setText(name);
                        } else {
                            mh.from.setText(email);
                        }
                        break;
                    }
                } else {
                    if (msgC.getType() == 0) {
                        Contact fromContact = mContactDao.getContactById(msgC.getContactId());
                        String name = fromContact.getName();
                        String email = fromContact.getEmail();
                        if (!fromContact.getName().equals("")) {
                            mh.from.setText(name);
                        } else {
                            mh.from.setText(email);
                        }
                        break;
                    }
                }

            }

            mMessageContactDao.close();
            mContactDao.close();


            Date sentDate = new Date(msgItem.getSentDate());
            mh.sentDate.setText(DateFormat.getDateFormat(mContext).format(sentDate));
//
            mh.subject.setText(msgItem.getSubject());
        }
    }

    @Override
    public int getItemCount() {
        if (hasLoadButton) {
            return msgItemList.size() + 1;
        } else {
            return msgItemList.size();
        }
    }

    public void swap(List<MyMessage> msgList) {
        msgItemList.clear();
        msgItemList.addAll(msgList);
        notifyDataSetChanged();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView from;
        protected TextView subject;
        protected TextView sentDate;

        public MessageViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.from = (TextView) view.findViewById(R.id.row_message_from);
            this.subject = (TextView) view.findViewById(R.id.row_message_subject);
            this.sentDate = (TextView) view.findViewById(R.id.row_message_sent_date);
        }

        @Override
        public void onClick(View view) {
            System.out.println("load message...");
            //System.out.println("onClick " + getPosition() + " " + from.getText());
            Intent intent = new Intent(mContext, EmailDetail.class);
            intent.putExtra("message_id", msgItemList.get(getAdapterPosition()).getId());
            mContext.startActivity(intent);

        }
    }

    public class LoadMoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected TextView loadMore;
        protected ProgressBar spinner;

        public LoadMoreViewHolder(View view) {
            super(view);
            System.out.println("create LoadMoreViewHolder");
            this.loadMore = (TextView) view.findViewById(R.id.load_more);
            this.spinner = (ProgressBar) view.findViewById(R.id.load_more_progress);
            this.loadMore.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            System.out.println("load more...");
            setIsLoading(true);
            ((EmailView) mContext).onLoadMoreCalled();
        }
    }
}