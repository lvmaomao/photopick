package com.qingsongchou.library.photopick;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.qingsongchou.library.photopick.entity.Photo;
import com.qingsongchou.library.photopick.event.OnItemCheckListener;
import com.qingsongchou.library.photopick.fragment.ImagePagerFragment;
import com.qingsongchou.library.photopick.fragment.PhotoPickerFragment;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class PhotoPickerActivity extends AppCompatActivity {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;

    public final static String EXTRA_MAX_COUNT = "MAX_COUNT";
    public final static String EXTRA_SHOW_CAMERA = "SHOW_CAMERA";
    public final static String EXTRA_SOURCE_VIEW_ID = "SOURCE_VIEW_ID";
    public final static String KEY_SELECTED_PHOTOS = "SELECTED_PHOTOS";
    private int mSourceViewId;

    private MenuItem menuDoneItem;

    public final static int DEFAULT_MAX_COUNT = 8;

//    private int maxCount = DEFAULT_MAX_COUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_picker);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.images);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }

        int maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        mSourceViewId = getIntent().getIntExtra(EXTRA_SOURCE_VIEW_ID, 0);
        pickerFragment =
                (PhotoPickerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPickerFragment);

        pickerFragment.getPhotoGridAdapter().setShowCamera(showCamera);
        pickerFragment.getPhotoGridAdapter().setMaxCount(maxCount);

        pickerFragment.setListener(new PhotoPickerFragment.Listener() {
            @Override
            public void onPhotoCheck(int total, int maxCount) {
                if (maxCount == 1) {
                    menuDoneItem.setTitle(R.string.done);
                    menuDoneItem.setEnabled(total > 0);
                } else if (maxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.done_with_count, total, maxCount));
                    menuDoneItem.setEnabled(total > 0);
                }
            }
        });


//        //TODO:user click the photo which need to upload
//        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
//
//            @Override
//            public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {
//
//                int total = selectedItemCount + (isCheck ? -1 : 1);
//
//                menuDoneItem.setEnabled(total > 0);
//
//                if (maxCount <= 1) {
//                    List<Photo> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
//                    photos.clear();
//                    pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
//                    return true;
//                }
//
//                if (total > maxCount) {
//                    Toast.makeText(getActivity(), getString(R.string.over_max_count_tips, maxCount),
//                            LENGTH_LONG).show();
//                    return false;
//                }
//                menuDoneItem.setTitle(getString(R.string.done_with_count, total, maxCount));
//                return true;
//            }
//        });

    }


    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picker, menu);
        menuDoneItem = menu.findItem(R.id.done);
        menuDoneItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            this.finish();
            return true;
        }

        //TODO: process the upload operation
        // "/storage/emulated/0/DCIM/P60315-132714.jpg"
        // "/storage/emulated/0/DCIM/P60315-132718.jpg"
        if (item.getItemId() == R.id.done) {
            Intent intent = new Intent();
            List<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getCheckedPath();
            intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, (ArrayList<String>) selectedPhotos);
            intent.putExtra(EXTRA_SOURCE_VIEW_ID, mSourceViewId);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }
}
