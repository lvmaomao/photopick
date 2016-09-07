package com.qingsongchou.library.photopick.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.qingsongchou.library.photopick.PhotoPickerActivity;
import com.qingsongchou.library.photopick.R;
import com.qingsongchou.library.photopick.adapter.PhotoGridAdapter;
import com.qingsongchou.library.photopick.adapter.PopupDirectoryListAdapter;
import com.qingsongchou.library.photopick.entity.PhotoDirectory;
import com.qingsongchou.library.photopick.utils.ImageCaptureManager;
import com.qingsongchou.library.photopick.utils.MediaStoreHelper;

import java.io.IOException;
import java.util.List;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.app.Activity.RESULT_OK;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoPickerFragment extends Fragment {

    public interface Listener {
        void onPhotoCheck(int total, int maxCount);
    }

    private static final int REQUEST_EXTERNAL_STORE_PERMISSION = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ImageCaptureManager captureManager;
    private PhotoGridAdapter photoGridAdapter;

    private PopupDirectoryListAdapter directoryAdapter;
    private Listener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        captureManager = new ImageCaptureManager(getActivity());

        requestExternalStorePermission();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    public void requestExternalStorePermission() {
        PermissionGen.with(this)
                .addRequestCode(REQUEST_EXTERNAL_STORE_PERMISSION)
                .permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .request();
    }

    @PermissionSuccess(requestCode = REQUEST_EXTERNAL_STORE_PERMISSION)
    public void requestExternalStorePermissionSuccess() {
        MediaStoreHelper.getPhotoDirs(getActivity(),
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> directories) {
                        directoryAdapter.update(directories);
                        photoGridAdapter.update(directories.get(MediaStoreHelper.INDEX_ALL_PHOTOS).getPhotos());
                    }
                });
    }

    @PermissionFail(requestCode = REQUEST_EXTERNAL_STORE_PERMISSION)
    public void requestExternalStorePermissionError() {
//        Toast. makeText(this, "Camera permission is not granted", Toast.LENGTH_SHORT).show();
    }

    public void requestCameraPermission() {
        PermissionGen.with(this)
                .addRequestCode(REQUEST_CAMERA_PERMISSION)
                .permissions(
                        Manifest.permission.CAMERA)
                .request();
    }

    @PermissionSuccess(requestCode = REQUEST_CAMERA_PERMISSION)
    public void requestCameraPermissionSuccess() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PermissionFail(requestCode = REQUEST_EXTERNAL_STORE_PERMISSION)
    public void requestCameraPermissionError() {
//        Toast. makeText(this, "Camera permission is not granted", Toast.LENGTH_SHORT).show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_picker, container, false);
    }

    private void initViews(final View rootView) {
        photoGridAdapter = new PhotoGridAdapter(getActivity());
        photoGridAdapter.setListener(new PhotoGridAdapter.Listener() {
            @Override
            public void onCameraClick() {
                requestCameraPermission();
            }

            @Override
            public void onPhotoClick(View v, int index, List<String> pathList) {
                int[] screenLocation = new int[2];
                v.getLocationOnScreen(screenLocation);
                ImagePagerFragment imagePagerFragment =
                        ImagePagerFragment.newInstance(pathList, index, screenLocation,
                                v.getWidth(), v.getHeight());

                ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);
            }

            @Override
            public void onPhotoCheck(int total, int maxCount) {
                if (listener == null) {
                    return;
                }
                listener.onPhotoCheck(total, maxCount);
            }
        });
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final Button btSwitchDirectory = (Button) rootView.findViewById(R.id.button);

        directoryAdapter = new PopupDirectoryListAdapter(getActivity());
        final ListPopupWindow listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setAnchorView(btSwitchDirectory);
        listPopupWindow.setAdapter(directoryAdapter);
        listPopupWindow.setModal(true);
        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);
        listPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                listPopupWindow.dismiss();
                PhotoDirectory directory = directoryAdapter.getItem(position);
                btSwitchDirectory.setText(directory.getName());
                photoGridAdapter.clear();
                photoGridAdapter.addAll(directory.getPhotos());
            }
        });

        btSwitchDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else {
                    listPopupWindow.setHeight(Math.round(rootView.getHeight() * 0.8f));
                    listPopupWindow.show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            captureManager.galleryAddPic();
        }
    }


    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        captureManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        captureManager.onRestoreInstanceState(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }
}
