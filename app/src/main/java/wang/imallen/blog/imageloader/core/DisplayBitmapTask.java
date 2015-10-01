package wang.imallen.blog.imageloader.core;

import android.graphics.Bitmap;

import wang.imallen.blog.imageloader.constant.LoadedFromType;
import wang.imallen.blog.imageloader.display.BitmapDisplayer;
import wang.imallen.blog.imageloader.info.ImageLoadingInfo;
import wang.imallen.blog.imageloader.listener.ImageLoadingListener;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-14.
 */
public class DisplayBitmapTask implements Runnable{

    private static final String TAG=DisplayBitmapTask.class.getSimpleName();

    private static final String LOG_DISPLAY_IMAGE_IN_IMAGEAWARE = "Display image in ImageAware (loaded from %1$s) [%2$s]";
    private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
    private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";

    private final Bitmap bitmap;
    private final String imageUri;
    private final ViewWrapper viewWrapper;
    private final String memoryCacheKey;
    private final BitmapDisplayer displayer;
    private final ImageLoadingListener listener;
    private final ImageLoaderEngine engine;
    private final LoadedFromType loadedFromType;

    public DisplayBitmapTask(Bitmap bitmap,ImageLoadingInfo imageLoadingInfo,
                             ImageLoaderEngine engine,LoadedFromType loadedFromType)
    {
        this.bitmap=bitmap;
        this.imageUri=imageLoadingInfo.getUri();
        this.viewWrapper=imageLoadingInfo.getViewWrapper();
        this.memoryCacheKey=imageLoadingInfo.getMemoryCacheKey();
        this.displayer=imageLoadingInfo.getDisplayImageOptions().getDisplayer();
        this.listener=imageLoadingInfo.getListener();
        this.engine=engine;
        this.loadedFromType=loadedFromType;

    }

    /**
     * 首先判断imageAware是否被 GC 回收，如果是直接调用取消加载回调接口ImageLoadingListener.onLoadingCancelled(…)；
     否则判断imageAware是否被复用，如果是直接调用取消加载回调接口ImageLoadingListener.onLoadingCancelled(…)；
     否则调用displayer显示图片，并将imageAware从正在加载的 map 中移除。调用加载成功回调接口ImageLoadingListener.onLoadingComplete(…)。

     对于 ListView 或是 GridView 这类会缓存 Item 的 View 来说，单个 Item 中如果含有 ImageView，
     在滑动过程中可能因为异步加载及 View 复用导致图片错乱，这里对imageAware是否被复用的判断就能很好的解决这个问题。
     原因类似：Android ListView 滑动过程中图片显示重复错位闪烁问题原因及解决方案。
     */
    @Override
    public void run()
    {
        //we know this thread is UI thread(main thread) by log
        Logger.d(TAG,"DiaplayBitmapTask-->run(),ThreadName:"+Thread.currentThread().getName());
        if(viewWrapper.isCollected())
        {
            Logger.d(LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED, memoryCacheKey);
            listener.onLoadingCancelled(imageUri,viewWrapper.getWrappedView());
        }
        else if(isViewReused())
        {
            Logger.d(LOG_TASK_CANCELLED_IMAGEAWARE_REUSED, memoryCacheKey);
            listener.onLoadingCancelled(imageUri,viewWrapper.getWrappedView());
        }
        else
        {
            Logger.d(LOG_DISPLAY_IMAGE_IN_IMAGEAWARE, loadedFromType+","+memoryCacheKey);
            displayer.display(bitmap,viewWrapper,loadedFromType);
            engine.cancelDisplayTaskFor(viewWrapper);
            listener.onLoadingComplete(imageUri,viewWrapper.getWrappedView(),bitmap);
        }
    }

    private boolean isViewReused()
    {
        String currentCacheKey=engine.getLoadingUriForView(viewWrapper);
        return !memoryCacheKey.equals(currentCacheKey);
    }

}
