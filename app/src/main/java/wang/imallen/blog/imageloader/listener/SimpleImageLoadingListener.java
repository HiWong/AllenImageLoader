package wang.imallen.blog.imageloader.listener;

import android.graphics.Bitmap;
import android.view.View;

import wang.imallen.blog.imageloader.assist.FailReason;


/**
 *  A convenient class to extend when you only want to listen for a subset of all the image loading events. This
 * implements all methods in the {@link wang.imallen.blog.imageloader.listener.ImageLoadingListener} but does
 * nothing.
 * Created by allen on 15-9-8.
 */
public class SimpleImageLoadingListener implements ImageLoadingListener{

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }
}
