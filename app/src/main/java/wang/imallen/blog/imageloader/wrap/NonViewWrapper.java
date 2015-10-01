package wang.imallen.blog.imageloader.wrap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.constant.ViewScaleType;

/**
 * Created by allen on 15-9-14.
 */
public class NonViewWrapper implements ViewWrapper{

    protected final String imageUri;
    protected final ImageSize imageSize;
    protected final ViewScaleType scaleType;

    public NonViewWrapper(ImageSize imageSize,ViewScaleType scaleType)
    {
        this(null,imageSize,scaleType);
    }

    public NonViewWrapper(String imageUri,ImageSize imageSize,ViewScaleType scaleType)
    {
        if(null==imageSize)
        {
            throw new IllegalArgumentException("imageSize must not be null");
        }
        if(null==scaleType)
        {
            throw new IllegalArgumentException("scaleType cannot be null");
        }
        this.imageUri=imageUri;
        this.imageSize=imageSize;
        this.scaleType=scaleType;

    }

    @Override
    public int getWidth()
    {
        return imageSize.getWidth();
    }

    @Override
    public int getHeight()
    {
        return imageSize.getHeight();
    }

    @Override
    public ViewScaleType getScaleType() {
        return scaleType;
    }

    @Override
    public View getWrappedView() {
        return null;
    }

    @Override
    public boolean isCollected() {
        return false;
    }

    @Override
    public int getId() {
        return TextUtils.isEmpty(imageUri)?super.hashCode():imageUri.hashCode();
    }

    @Override
    public boolean setImageDrawable(Drawable drawable) {
        return false;
    }

    @Override
    public boolean setImageRes(int resId) {
        return false;
    }

    @Override
    public boolean setImageBitmap(Bitmap bitmap) {
        return false;
    }
}
