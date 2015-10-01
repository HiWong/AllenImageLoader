package wang.imallen.blog.imageloader.display.impl;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import wang.imallen.blog.imageloader.constant.LoadedFromType;
import wang.imallen.blog.imageloader.display.BitmapDisplayer;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-12.
 */
public class FadeInBitmapDisplayer implements BitmapDisplayer {

    private final int durationMillis;
    private final boolean animateFromNetwork;
    private final boolean animateFromDisk;
    private final boolean animateFromMemory;

    public FadeInBitmapDisplayer(int durationMillis)
    {
        this(durationMillis,true,true,true);
    }

    public FadeInBitmapDisplayer(int durationMillis,boolean animateFromNetwork,boolean animateFromDisk,
                                 boolean animateFromMemory)
    {
        this.durationMillis=durationMillis;
        this.animateFromNetwork=animateFromNetwork;
        this.animateFromDisk=animateFromDisk;
        this.animateFromMemory=animateFromMemory;
    }

    @Override
    public void display(Bitmap bitmap,ViewWrapper viewWrapper,LoadedFromType loadedFromType)
    {
        viewWrapper.setImageBitmap(bitmap);
        if ((animateFromNetwork && loadedFromType == LoadedFromType.NETWORK) ||
                (animateFromDisk && loadedFromType == LoadedFromType.DISK_CACHE) ||
                (animateFromMemory && loadedFromType == LoadedFromType.MEMORY_CACHE))
        {
            animate(viewWrapper.getWrappedView(), durationMillis);
        }
    }

    public static void animate(View view,int durationMillis)
    {
        if(null!=view)
        {
            AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
            fadeImage.setDuration(durationMillis);
            fadeImage.setInterpolator(new DecelerateInterpolator());
            view.startAnimation(fadeImage);
        }
    }


}
