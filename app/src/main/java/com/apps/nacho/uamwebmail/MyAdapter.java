package com.apps.nacho.uamwebmail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by nacho on 14/10/16.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.DrawerViewHolder> {
    private String[] mDataset;
    private DrawerLayout dr;
    private Toolbar mToolbar;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class DrawerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView mTextView;

        public DrawerViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            mTextView = (TextView) v.findViewById(R.id.folder_name);
        }

        @Override
        public void onClick(View view) {
            dr.closeDrawers();
            mToolbar.setTitle(mTextView.getText());
            Intent intent = new Intent(mContext, EmailView.class);
            intent.putExtra("folder", mTextView.getText());
            mContext.startActivity(intent);
            ((Activity) mContext).finish();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(String[] myDataset, DrawerLayout dr, Toolbar mToolbar, Context context) {
        this.mDataset = myDataset;
        this.dr = dr;
        this.mToolbar = mToolbar;
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_text_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        DrawerViewHolder vh = new MyAdapter.DrawerViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DrawerViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}


