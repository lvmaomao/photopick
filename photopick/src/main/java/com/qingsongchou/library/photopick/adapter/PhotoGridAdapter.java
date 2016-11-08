package com.qingsongchou.library.photopick.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.qingsongchou.library.photopick.R;
import com.qingsongchou.library.photopick.entity.Photo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoGridAdapter extends RecyclerView.Adapter {

    public interface Listener {
        void onCameraClick();

        void onPhotoClick(View v, int index, List<String> pathList);

        void onPhotoCheck(int total, int maxCount);
    }

    public final static int VIEW_TYPE_CAMERA = 100;
    public final static int VIEW_TYPE_PHOTO = 101;

    private boolean displayCamera;

    private Context context;
    private Listener listener;

    private List<Photo> photos;

    private int maxCount;
    private int lastPosition;

    public PhotoGridAdapter(Context context) {
        this(context, null);
    }

    public PhotoGridAdapter(Context context, List<Photo> data) {
        this.context = context;
        this.displayCamera = true;
        this.photos = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            this.photos.addAll(data);
        }
        this.maxCount = -1;
        this.lastPosition = -1;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setShowCamera(boolean hasCamera) {
        this.displayCamera = hasCamera;
    }

    public void addAll(List<Photo> data) {
        if (data == null) {
            return;
        }
        int start = this.photos.size();
        this.photos.addAll(data);
        notifyItemRangeInserted(start + getOffset(), data.size());
    }

    public void clear() {
        if (this.photos.isEmpty()) {
            return;
        }
        int size = this.photos.size();
        this.photos.clear();
        notifyItemRangeRemoved(getOffset(), size);
    }

    public void update(List<Photo> data) {
        if (data == null) {
            return;
        }
        if (this.photos.isEmpty()) {
            if (data.isEmpty()) {
                return;
            }
            this.photos.addAll(data);
            notifyItemRangeInserted(getOffset(), data.size());
        } else {
            if (data.isEmpty()) {
                int size = this.photos.size();
                this.photos.clear();
                notifyItemRangeRemoved(getOffset(), size);
            } else {
                for (Photo photo : data) {
                    if (this.photos.contains(photo)) {
                        continue;
                    }
                    //add camera photo
                    this.photos.add(0, photo);
                    this.notifyItemInserted(getOffset());
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CAMERA) {
            View camera = inflater.inflate(R.layout.item_carera, parent, false);
            return new VHCamera(camera);
        } else {
            View photo = inflater.inflate(R.layout.item_photo, parent, false);
            return new VHPhoto(photo);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof VHPhoto) {
            VHPhoto vhPhoto = (VHPhoto) holder;
            Photo photo = getPhoto(position);
            if (photo != null) {
                String uri = "file://" + photo.getPath();
                ImageLoader.getInstance().displayImage(uri, vhPhoto.ivPhoto);
                vhPhoto.ivPhoto.setTag(uri);
                vhPhoto.ivSelected.setSelected(photo.isChecked());
            }
//            Picasso.with(context)
//                    .load(new File(photo.getPath()))
//                    .placeholder(R.drawable.ic_photo_black_48dp)
//                    .error(R.drawable.ic_broken_image_black_48dp)
//                    .resize(220, 220)
//                    .centerCrop()
//                    .into(vhPhoto.ivPhoto);
        }
    }

    private Photo getPhoto(int position) {
        if (position > photos.size() + 1) {
            return null;
        }
        return photos.get(displayCamera ? position - 1 : position);
    }

    private int getOffset() {
        int offset = 0;
        if (displayCamera) {
            offset++;
        }
        return offset;
    }

    @Override
    public int getItemCount() {
        int offset = getOffset();
        return photos.size() + offset;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionCamera(position)) {
            return VIEW_TYPE_CAMERA;
        } else {
            return VIEW_TYPE_PHOTO;
        }
    }

    private boolean isPositionCamera(int position) {
        return (displayCamera && position == 0);
    }

    private int getCheckedCount() {
        int count = 0;
        for (Photo photo : photos) {
            if (photo.isChecked()) {
                count++;
            }
        }
        return count;
    }

    public List<String> getCheckedPath() {
        List<String> pathList = new ArrayList<>();
        for (Photo photo : photos) {
            if (photo.isChecked()) {
                pathList.add(photo.getPath());
            }
        }
        return pathList;
    }

    private List<String> getPathList() {
        List<String> pathList = new ArrayList<>();
        for (Photo photo : photos) {
            pathList.add(photo.getPath());
        }
        return pathList;
    }

    class VHPhoto extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivPhoto;
        View ivSelected;

        public VHPhoto(View view) {
            super(view);
            ivPhoto = (ImageView) view.findViewById(R.id.iv_photo);
            ivSelected = view.findViewById(R.id.iv_selected);

            ivPhoto.setOnClickListener(this);
            ivSelected.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.iv_photo) {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    int index = displayCamera ? position - 1 : position;
                    listener.onPhotoClick(ivPhoto, index, getPathList());
                }
            } else if (id == R.id.iv_selected) {
                int newPosition = getAdapterPosition();
                if (newPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                Photo newPhoto = getPhoto(newPosition);
                if (maxCount == 1) {
                    if (newPhoto.isChecked()) {
                        newPhoto.setChecked(false);
                        notifyItemChanged(newPosition);
                    } else {
                        if (lastPosition != -1) {
                            Photo oldPhoto = getPhoto(lastPosition);
                            if (oldPhoto != null) {
                                oldPhoto.setChecked(false);
                                notifyItemChanged(lastPosition);
                            }
                        }
                        newPhoto.setChecked(true);
                        notifyItemChanged(newPosition);
                        lastPosition = newPosition;
                    }
                } else if (maxCount > 1) {
                    if (newPhoto.isChecked()) {
                        newPhoto.setChecked(false);
                        notifyItemChanged(newPosition);
                    } else {
                        int oldCount = getCheckedCount();
                        if (oldCount == maxCount) {
                            return;
                        }
                        newPhoto.setChecked(true);
                        notifyItemChanged(newPosition);
                    }
                }
                if (listener != null) {
                    int newCount = getCheckedCount();
                    listener.onPhotoCheck(newCount, maxCount);
                }
            }
        }
    }

    class VHCamera extends RecyclerView.ViewHolder implements View.OnClickListener {

        public VHCamera(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener == null) {
                return;
            }
            listener.onCameraClick();
        }
    }
}
