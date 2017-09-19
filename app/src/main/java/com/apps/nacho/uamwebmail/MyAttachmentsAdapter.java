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

import com.apps.nacho.uamwebmail.sqlite.dao.AttachmentDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.ContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageContactDAO;
import com.apps.nacho.uamwebmail.sqlite.dao.MessageDAO;
import com.apps.nacho.uamwebmail.sqlite.model.Attachment;
import com.apps.nacho.uamwebmail.sqlite.model.Contact;
import com.apps.nacho.uamwebmail.sqlite.model.MessageContact;
import com.apps.nacho.uamwebmail.sqlite.model.MyMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by nacho on 10/10/16.
 */

public class MyAttachmentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Attachment> atchItemList;
    private Context mContext;
    private AttachmentDAO mAttachmentDao;


    public MyAttachmentsAdapter(Context context, List<Attachment> atchItemList) {
        this.atchItemList = atchItemList;
        this.mContext = context;
        //this.mAttachmentDao = new AttachmentDAO(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v;

        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attachment_row, null);

        AttachmentViewHolder ah = new AttachmentViewHolder(v);

        return ah;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        AttachmentViewHolder ah = (AttachmentViewHolder) viewHolder;
        Attachment atchItem = atchItemList.get(position);
        ah.name.setText(atchItem.getName() + "." + atchItem.getFormat());
        ah.attchId = atchItem.getId();
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return atchItemList.size();
    }

    public void swap(List<Attachment> atchList) {
        atchItemList.clear();
        atchItemList.addAll(atchList);
        notifyDataSetChanged();
    }

    public class AttachmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView name;
        protected long attchId;

        public AttachmentViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.name = (TextView) view.findViewById(R.id.row_attachment_name);
        }

        @Override
        public void onClick(View view) {
            System.out.println("Download attachment...");

            ((EmailDetail) mContext).startDownloadAttachmentTask(attchId);
        }
    }
}