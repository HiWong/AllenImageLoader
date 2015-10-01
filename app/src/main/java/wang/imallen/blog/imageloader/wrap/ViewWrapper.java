package wang.imallen.blog.imageloader.wrap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import wang.imallen.blog.imageloader.constant.ViewScaleType;

/**
 * Created by allen on 15-9-11.
 */
public interface ViewWrapper {

    int getWidth();

    int getHeight();

    ViewScaleType getScaleType();

    View getWrappedView();

    boolean isCollected();

    int getId();

    boolean setImageDrawable(Drawable drawable);

    boolean setImageRes(int resId);

    boolean setImageBitmap(Bitmap bitmap);

}
