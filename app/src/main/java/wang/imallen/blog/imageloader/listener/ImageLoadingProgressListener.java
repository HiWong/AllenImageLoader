package wang.imallen.blog.imageloader.listener;

import android.view.View;

/**
 * Created by allen on 15-9-12.
 */
public interface ImageLoadingProgressListener {

    void onProgressUpdate(String imageUri,View view,int current,int total);
}
