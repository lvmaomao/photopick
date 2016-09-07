package com.qingsongchou.library.photopick.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qingsongchou.library.photopick.R;
import com.qingsongchou.library.photopick.entity.PhotoDirectory;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PopupDirectoryListAdapter extends BaseAdapter {

    private Context context;
    private int select;

    private List<PhotoDirectory> directories = new ArrayList<>();

    private LayoutInflater mLayoutInflater;

    public PopupDirectoryListAdapter(Context context) {
        this(context, null);
    }

    public PopupDirectoryListAdapter(Context context, List<PhotoDirectory> directories) {
        this.context = context;
        this.directories = new ArrayList<>();
        if(directories != null && !directories.isEmpty()) {
            this.directories.addAll(directories);
        }

        mLayoutInflater = LayoutInflater.from(context);
    }

    public void update(List<PhotoDirectory> directories) {
        if(directories == null) {
            return;
        }
        this.directories.clear();
        this.directories.addAll(directories);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return directories.size();
    }


    @Override
    public PhotoDirectory getItem(int position) {
        return directories.get(position);
    }


    @Override
    public long getItemId(int position) {
        return directories.get(position).hashCode();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_directory, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindData(directories.get(position));

        return convertView;
    }


    private class ViewHolder {

        public ImageView ivCover;
        public TextView tvName;

        public ViewHolder(View rootView) {
            ivCover = (ImageView) rootView.findViewById(R.id.iv_dir_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_dir_name);
        }

        public void bindData(PhotoDirectory directory) {
            if(TextUtils.isEmpty(directory.getCoverPath())) {
                ivCover.setImageResource(R.drawable.ic_directory_default);
            } else {
                Picasso.with(context)
                        .load(new File(directory.getCoverPath()))
                        .error(R.drawable.ic_directory_default)
                        .placeholder(R.drawable.ic_directory_default)
                        .resize(90, 90)
                        .into(ivCover);
            }
            tvName.setText(directory.getName());
        }
    }

}
