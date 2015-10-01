package wang.imallen.blog.imageloader.info;

import java.util.concurrent.locks.ReentrantLock;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.listener.ImageLoadingListener;
import wang.imallen.blog.imageloader.listener.ImageLoadingProgressListener;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-11.
 */
public final class ImageLoadingInfo {

    private final String uri;
    private final String memoryCacheKey;

    private final ViewWrapper viewWrapper;
    private final ImageSize targetSize;

    private final DisplayImageOptions displayImageOptions;

    private final ImageLoadingListener listener;
    private final ImageLoadingProgressListener progressListener;

    private final ReentrantLock loadFromUriLock;

    public ImageLoadingInfo(String uri,ViewWrapper viewWrapper,ImageSize targetSize,String memoryCacheKey,
                            DisplayImageOptions displayImageOptions,ImageLoadingListener listener,
                            ImageLoadingProgressListener progressListener,ReentrantLock loadFromUriLock)
    {
        this.uri=uri;
        this.viewWrapper=viewWrapper;
        this.targetSize=targetSize;
        this.memoryCacheKey=memoryCacheKey;
        this.displayImageOptions=displayImageOptions;
        this.listener=listener;
        this.progressListener=progressListener;
        this.loadFromUriLock=loadFromUriLock;
  
    }

    public ReentrantLock getLoadFromUriLock()
    {
        return loadFromUriLock;
    }

    public String getUri() {
        return uri;
    }

    public ViewWrapper getViewWrapper() {
        return viewWrapper;
    }

    public ImageSize getTargetSize() {
        return targetSize;
    }

    public DisplayImageOptions getDisplayImageOptions() {
        return displayImageOptions;
    }

    public ImageLoadingListener getListener() {
        return listener;
    }

    public ImageLoadingProgressListener getProgressListener() {
        return progressListener;
    }

    public String getMemoryCacheKey()
    {
        return memoryCacheKey;
    }

}
