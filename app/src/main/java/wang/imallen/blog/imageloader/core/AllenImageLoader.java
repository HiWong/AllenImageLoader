package wang.imallen.blog.imageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.config.ImageLoaderConfig;
import wang.imallen.blog.imageloader.constant.LoadedFromType;
import wang.imallen.blog.imageloader.constant.ViewScaleType;
import wang.imallen.blog.imageloader.info.DisplayImageOptions;
import wang.imallen.blog.imageloader.info.ImageLoadingInfo;
import wang.imallen.blog.imageloader.listener.ImageLoadingListener;
import wang.imallen.blog.imageloader.listener.ImageLoadingProgressListener;
import wang.imallen.blog.imageloader.listener.SimpleImageLoadingListener;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.utils.ImageSizeUtils;
import wang.imallen.blog.imageloader.utils.MemoryCacheUtils;
import wang.imallen.blog.imageloader.wrap.ImageViewWrapper;
import wang.imallen.blog.imageloader.wrap.NonViewWrapper;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-12.
 */
public final class AllenImageLoader {

    private static final String TAG=AllenImageLoader.class.getSimpleName();

    private static final String LOG_INIT_CONFIG="Init ImageLoader Config";
    private static final String LOG_DESTROY="Destroy ImageLoader";
    private static final String LOG_LOAD_IMAGE_FROM_MEMORY_CACHE="Load Image From Memory Cache";

    private static final String WARNING_RE_ININ_CONFIG="Re-init Config";
    private static final String ERROR_WRONG_ARGUMENTS="Error:Wrong arguments";
    private static final String ERROR_NOT_INIT="Error:has not been inited";
    private static final String ERROR_INIT_CONFIG_WITH_NULL="Error:Init config with null";

    private ImageLoaderConfig config;
    private ImageLoaderEngine engine;

    private ImageLoadingListener defaultListener=new SimpleImageLoadingListener();

    //private volatile static AllenImageLoader instance;
    private static AllenImageLoader instance;

    public static AllenImageLoader getInstance()
    {
        if(null==instance)
        {
            synchronized (AllenImageLoader.class)
            {
                if(null==instance)
                {
                    instance=new AllenImageLoader();
                }
            }
        }
        return instance;
    }

    private AllenImageLoader()
    {

    }

    public synchronized void init(ImageLoaderConfig config)
    {
        if(null==config)
        {
            throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
        }
        if(null==this.config)
        {
            Logger.d(TAG, LOG_INIT_CONFIG);
            engine=new ImageLoaderEngine(config);
            this.config=config;
        }
        else
        {
            Logger.d(TAG,WARNING_RE_ININ_CONFIG);
        }
    }

    public boolean isInited()
    {
        return config!=null;
    }

    public void displayImage(String uri,ViewWrapper viewWrapper,DisplayImageOptions options,
                             ImageLoadingListener listener,
                             ImageLoadingProgressListener progressListener)
    {
        checkConfig();
        if(null==viewWrapper)
        {
            throw new IllegalArgumentException(ERROR_WRONG_ARGUMENTS);
        }
        if(null==listener)
        {
            listener=defaultListener;
        }
        if(null==options)
        {
            options=config.getDefaultDisplayImageOptions();
        }

        if(TextUtils.isEmpty(uri))
        {
            //why cancel here? there should no display task here
            //is this not necessary? because imageAware may be reused such as in ListView.
            engine.cancelDisplayTaskFor(viewWrapper);
            listener.onLoadingStarted(uri,viewWrapper.getWrappedView());
            if(options.shouldShowImageForEmptyUri())
            {
                viewWrapper.setImageRes(options.getImageResForEmptyUri());
            }
            else
            {
                viewWrapper.setImageRes(0);
            }
            listener.onLoadingComplete(uri,viewWrapper.getWrappedView(),null);
            return;
        }

        ImageSize targetSize= ImageSizeUtils.defineTargetSizeForView(viewWrapper,
                config.getMaxImageSize());
        String memoryCacheKey= MemoryCacheUtils.generateKey(uri,targetSize);
        engine.prepareDisplayTaskFor(viewWrapper,memoryCacheKey);

        listener.onLoadingStarted(uri,viewWrapper.getWrappedView());

        Bitmap bmp=config.getMemoryCache().get(memoryCacheKey);
        if(bmp!=null&&!bmp.isRecycled())
        {
            Logger.d(LOG_LOAD_IMAGE_FROM_MEMORY_CACHE, memoryCacheKey);

            options.getDisplayer().display(bmp,viewWrapper, LoadedFromType.MEMORY_CACHE);

            listener.onLoadingComplete(uri,viewWrapper.getWrappedView(),bmp);
        }
        else
        {
            if(options.shouldShowImageOnLoading())
            {
                viewWrapper.setImageRes(options.getImageResOnLoading());
            }
            /*
            else if(options.isResetViewBeforeLoading())
            {
                viewWrapper.setImageDrawable(null);
            }
            */
            ImageLoadingInfo imageLoadingInfo=new ImageLoadingInfo(uri,viewWrapper,targetSize,
                    memoryCacheKey,options,listener,progressListener,engine.getLockForUri(uri));
            LoadAndDisplayImageTask displayTask=new LoadAndDisplayImageTask(engine,imageLoadingInfo,
                    defineHandler(options));
            engine.submit(displayTask);
        }

    }

    public void displayImage(String uri,ViewWrapper viewWrapper,DisplayImageOptions options,
                             ImageLoadingListener listener)
    {
        displayImage(uri,viewWrapper,options,listener,null);
    }

    public void displayImage(String uri,ImageView imageView,DisplayImageOptions options)
    {
        displayImage(uri,imageView,options,null);
    }

    public void displayImage(String uri,ImageView imageView,DisplayImageOptions options,
                             ImageLoadingListener listener)
    {
        displayImage(uri,imageView,options,listener,null);
    }

    public void displayImage(String uri,ImageView imageView,DisplayImageOptions options,ImageLoadingListener listener,
                             ImageLoadingProgressListener progressListener)
    {
        displayImage(uri,new ImageViewWrapper(imageView),options,listener,progressListener);
    }

    public void displayImage(String uri,ViewWrapper viewWrapper)
    {
        displayImage(uri,viewWrapper,null,null,null);
    }

    public void displayImage(String uri,ViewWrapper viewWrapper,ImageLoadingListener listener)
    {
        displayImage(uri,viewWrapper,null,listener,null);
    }
    public void displayImage(String uri,ViewWrapper viewWrapper,DisplayImageOptions options)
    {
        displayImage(uri,viewWrapper,options,null,null);
    }



    public void loadImage(String uri,ImageLoadingListener listener)
    {
        loadImage(uri,null,null,listener,null);
    }

    public void loadImage(String uri,ImageSize targetSize,ImageLoadingListener listener)
    {
        loadImage(uri,targetSize,null,listener,null);
    }

    public void loadImage(String uri,DisplayImageOptions options,ImageLoadingListener listener)
    {
        loadImage(uri,null,options,listener,null);
    }

    public void loadImage(String uri,ImageSize targetSize,DisplayImageOptions options,
                          ImageLoadingListener listener)
    {
        loadImage(uri,targetSize,options,listener,null);
    }

    public void loadImage(String uri,ImageSize targetSize,DisplayImageOptions options,
                          ImageLoadingListener listener,ImageLoadingProgressListener progressListener)
    {
        checkConfig();
        if(null==targetSize)
        {
            targetSize=config.getMaxImageSize();
        }
        if(null==options)
        {
            options=config.getDefaultDisplayImageOptions();
        }

        NonViewWrapper viewWrapper=new NonViewWrapper(uri,targetSize, ViewScaleType.CROP);
        displayImage(uri,viewWrapper,options,listener,progressListener);

    }

    public void resume()
    {
       engine.resume();
    }

    public void pause()
    {
       engine.pause();
    }

    public void stop()
    {
       engine.stop();
    }

    public void destroy()
    {
        if(null!=config)
        {
            Logger.d(TAG,LOG_DESTROY);
        }
        stop();
        config.getDiskCache().close();
        engine=null;
        config=null;
    }

    public void clearMemoryCache()
    {
        checkConfig();
        config.getMemoryCache().clear();
    }



    ///////////////////start of utility methods////////////////
    private void checkConfig()
    {
        if(null==config)
        {
            throw new IllegalStateException(ERROR_NOT_INIT);
        }
    }

    public void clearDiskCache()
    {
        checkConfig();
        config.getDiskCache().clear();
    }


    private static Handler defineHandler(DisplayImageOptions options)
    {
        Handler handler=options.getHandler();
        if(handler==null&& Looper.myLooper()==Looper.getMainLooper())
        {
            handler=new Handler();
        }
        else
        {
            throw new RuntimeException("AllenImageLoader can only be called by Main thread(UI thread)");
        }
        return handler;
    }

    /////////////////////end of utility methods//////////////////



}
