package wang.imallen.blog.ui.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import wang.imallen.blog.R;
import wang.imallen.blog.imageloader.core.AllenImageLoader;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.startup.Constants;
import wang.imallen.blog.ui.fragment.ImageGalleryFragment;
import wang.imallen.blog.ui.fragment.ImageGridFragment;
import wang.imallen.blog.ui.fragment.ImageListFragment;
import wang.imallen.blog.ui.fragment.ImagePagerFragment;


public class MainActivity extends ActionBarActivity {

    private static final String TAG=MainActivity.class.getSimpleName();

    private static final String TEST_FILE_NAME = "Universal Image Loader @#&=+-_.,!()~'%20.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_home);

        File testImageOnSdCard = new File("/mnt/sdcard", TEST_FILE_NAME);
        if (!testImageOnSdCard.exists()) {
            copyTestImageToSdCard(testImageOnSdCard);
        }
    }

    public void onImageListClick(View view) {
        Intent intent = new Intent(this, SimpleImageActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageListFragment.INDEX);
        startActivity(intent);
    }

    public void onImageGridClick(View view) {
        Intent intent = new Intent(this, SimpleImageActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageGridFragment.INDEX);
        startActivity(intent);
    }

    public void onImagePagerClick(View view) {
        Intent intent = new Intent(this, SimpleImageActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImagePagerFragment.INDEX);
        startActivity(intent);
    }

    public void onImageGalleryClick(View view) {
        Intent intent = new Intent(this, SimpleImageActivity.class);
        intent.putExtra(Constants.Extra.FRAGMENT_INDEX, ImageGalleryFragment.INDEX);
        startActivity(intent);
    }

    public void onFragmentsClick(View view) {
        Intent intent = new Intent(this, ComplexImageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AllenImageLoader.getInstance().stop();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_clear_memory_cache:
                AllenImageLoader.getInstance().clearMemoryCache();
                return true;
            case R.id.item_clear_disc_cache:
                AllenImageLoader.getInstance().clearDiskCache();
                return true;
            default:
                return false;
        }
    }

    private void copyTestImageToSdCard(final File testImageOnSdCard) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = getAssets().open(TEST_FILE_NAME);
                    FileOutputStream fos = new FileOutputStream(testImageOnSdCard);
                    byte[] buffer = new byte[8192];
                    int read;
                    try {
                        while ((read = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    } finally {
                        fos.flush();
                        fos.close();
                        is.close();
                    }
                } catch (IOException e) {
                    Logger.d(TAG, "Can't copy test image onto SD card");
                }
            }
        }).start();
    }
}
