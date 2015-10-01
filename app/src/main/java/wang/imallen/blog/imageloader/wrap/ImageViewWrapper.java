package wang.imallen.blog.imageloader.wrap;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import wang.imallen.blog.imageloader.constant.ViewScaleType;

/**
 * Created by allen on 15-9-11.
 */
public class ImageViewWrapper extends BasicViewWrapper {

    public ImageViewWrapper(ImageView imageView)
    {
        super(imageView);
    }

    public ImageViewWrapper(ImageView imageView, boolean checkActualViewSize)
    {
        super(imageView,checkActualViewSize);
    }

    @Override
    public int getWidth()
    {
        int width=super.getWidth();
        if(width<=0)
        {
            ImageView imageView=(ImageView)viewRef.get();
            if(null!=imageView)
            {
                width=imageView.getMaxWidth();
            }
        }
        return width;
    }

    @Override
    public int getHeight()
    {
        int height=super.getHeight();
        if(height<=0)
        {
            ImageView imageView=(ImageView)viewRef.get();
            if(null!=imageView)
            {
                height=imageView.getMaxHeight();
            }
        }
        return height;
    }

    @Override
    public ViewScaleType getScaleType()
    {
        ImageView imageView=(ImageView)viewRef.get();
        if(null!=imageView)
        {
            return ViewScaleType.fromImageView(imageView);
        }
        return super.getScaleType();
    }

    @Override
    public ImageView getWrappedView()
    {
        return (ImageView)viewRef.get();
    }

    @Override
    protected void setImageDrawableInto(Drawable drawable,View view)
    {
        ((ImageView)view).setImageDrawable(drawable);
        if(drawable instanceof AnimationDrawable)
        {
            ((AnimationDrawable)drawable).start();
        }
    }

    @Override
    protected void setImageBitmapInto(Bitmap bitmap,View view)
    {
        ((ImageView)view).setImageBitmap(bitmap);
    }

    @Override
    protected void setImageResInto(int resId, View view) {
        ((ImageView)view).setImageResource(resId);
    }
}
