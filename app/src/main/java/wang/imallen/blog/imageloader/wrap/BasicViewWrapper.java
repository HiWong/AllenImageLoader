package wang.imallen.blog.imageloader.wrap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import wang.imallen.blog.imageloader.constant.ViewScaleType;
import wang.imallen.blog.imageloader.log.Logger;

/**
 * Created by allen on 15-9-11.
 */
public abstract class BasicViewWrapper implements ViewWrapper {

    private static final String TAG=BasicViewWrapper.class.getSimpleName();


    public static final String WARN_CANT_SET_DRAWABLE = "Can't set a drawable into view. You should call ImageLoader on UI thread for it.";
    public static final String WARN_CANT_SET_BITMAP = "Can't set a bitmap into view. You should call ImageLoader on UI thread for it.";

    protected Reference<View>viewRef;
    protected boolean checkActualViewSize;

    public BasicViewWrapper(View view)
    {
        this(view,true);
    }

    public BasicViewWrapper(View view, boolean checkActualViewSize)
    {
        if(null==view)
        {
            throw new IllegalArgumentException();
        }
        //will softReference be better?
        this.viewRef=new WeakReference<>(view);
        this.checkActualViewSize=checkActualViewSize;
    }

    @Override
    public int getWidth() {
        View view=viewRef.get();
        if(null==view)
        {
            return 0;
        }
        final ViewGroup.LayoutParams params=view.getLayoutParams();
        int width=0;
        if(checkActualViewSize
                &&null!=params
                &&params.width!=ViewGroup.LayoutParams.WRAP_CONTENT)
        {
            width=view.getWidth();
        }
        if(width<=0&&null!=params)
        {
            width=params.width;
        }

        return width;

    }

    @Override
    public int getHeight() {

        View view=viewRef.get();
        if(null==view)
        {
            return 0;
        }
        final ViewGroup.LayoutParams params=view.getLayoutParams();
        int height=0;
        if(checkActualViewSize
                &&params!=null
                &&params.height!=ViewGroup.LayoutParams.WRAP_CONTENT)
        {
            height=view.getHeight();
        }
        if(height<=0&&null!=params)
        {
            height=params.height;
        }

        return height;

    }

    @Override
    public ViewScaleType getScaleType()
    {
        return ViewScaleType.CROP;
    }

    @Override
    public View getWrappedView() {
        return viewRef.get();
    }

    @Override
    public boolean isCollected() {
        return viewRef.get()==null;
    }

    @Override
    public int getId() {
       View view=viewRef.get();
        return view==null?super.hashCode():view.hashCode();
    }

    @Override
    public boolean setImageDrawable(Drawable drawable) {
        if(Looper.myLooper()==Looper.getMainLooper())
        {
            View view=viewRef.get();
            if(null!=view)
            {
                setImageDrawableInto(drawable,view);
                return true;
            }
        }
        else
        {
            Logger.d(TAG,WARN_CANT_SET_DRAWABLE);
        }
        return false;
    }

    @Override
    public boolean setImageRes(int resId) {
        if(resId<=0||viewRef.get()==null)
        {
            return false;
        }
        setImageResInto(resId,viewRef.get());
        return true;
    }

    @Override
    public boolean setImageBitmap(Bitmap bitmap) {
        //if this is UI thread
        if(Looper.myLooper()==Looper.getMainLooper())
        {
            View view=viewRef.get();
            if(null!=view)
            {
                setImageBitmapInto(bitmap,view);
                return true;
            }
        }
        else
        {
            Logger.d(TAG,WARN_CANT_SET_BITMAP);
        }
        return false;
    }

    /**
     * Should set drawable into incoming view. Incoming view is guaranteed not null.<br />
     * This method is called on UI thread.
     */
    protected abstract void setImageDrawableInto(Drawable drawable,View view);

    /**
     * Should set Bitmap into incoming view. Incoming view is guaranteed not null.< br />
     * This method is called on UI thread.
     */
    protected abstract void setImageBitmapInto(Bitmap bitmap, View view);

    protected abstract void setImageResInto(int resId,View view);

}
