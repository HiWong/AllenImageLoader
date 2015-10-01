package wang.imallen.blog.imageloader.listener;

import android.graphics.Bitmap;
import android.view.View;

import wang.imallen.blog.imageloader.assist.FailReason;

/**
 * Created by allen on 15-9-12.
 */
public interface ImageLoadingListener {

    void onLoadingStarted(String imageUri,View view);

    void onLoadingFailed(String imageUri,View view,FailReason failReason);
    
    void onLoadingComplete(String imageUri,View view,Bitmap bitmap);

    void onLoadingCancelled(String imageUri,View view);
}
