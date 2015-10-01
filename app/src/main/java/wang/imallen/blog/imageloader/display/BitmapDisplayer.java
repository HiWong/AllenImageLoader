package wang.imallen.blog.imageloader.display;

import android.graphics.Bitmap;

import wang.imallen.blog.imageloader.constant.LoadedFromType;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-12.
 */
public interface BitmapDisplayer {

    void display(Bitmap bitmap,ViewWrapper viewWare,LoadedFromType loadedFromType);

}
