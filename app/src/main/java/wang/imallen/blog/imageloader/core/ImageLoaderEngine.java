package wang.imallen.blog.imageloader.core;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import wang.imallen.blog.imageloader.config.ImageLoaderConfig;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-12.
 */
public final class ImageLoaderEngine {

    private static final String TAG=ImageLoaderEngine.class.getSimpleName();

    private final ImageLoaderConfig config;
    private Executor taskExecutor;
    private Executor taskExecutorForCachedImages;
    private Executor taskDistributor;

    private final Map<Integer,String> cacheKeysForViewWrapper=
            Collections.synchronizedMap(new HashMap<Integer,String>());
    private final Map<String,ReentrantLock>uriLocks=new WeakHashMap<>();

    private final AtomicBoolean paused=new AtomicBoolean(false);
    private final AtomicBoolean networkDenied=new AtomicBoolean(false);
    private final AtomicBoolean slowNetwork=new AtomicBoolean(false);

    private final Object pauseLock=new Object();

    public ImageLoaderEngine(ImageLoaderConfig config)
    {
        this.config=config;
        taskExecutor=config.getTaskExecutor();
        taskExecutorForCachedImages=config.getTaskExecutorForCachedImages();
        taskDistributor=DefaultConfigFactory.createTaskDistributor();
    }

    public void submit(final LoadAndDisplayImageTask task)
    {
        taskDistributor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                File image=config.getDiskCache().get(task.getLoadingUri());
                boolean isImageCacheOnDisk=image!=null&&image.exists();
                initExecutorsIfNeed();
                if(isImageCacheOnDisk)
                {
                    Logger.d(TAG,"image cache on disk,use takeExecutorForCachedImages");
                    taskExecutorForCachedImages.execute(task);
                }
                else
                {
                    Logger.d(TAG,"image not cache on disk,use taskExecutor");
                    taskExecutor.execute(task);
                }
            }
        });
    }

    private void initExecutorsIfNeed()
    {
        if(!config.isCustomExecutor()&&((ExecutorService)taskExecutor).isShutdown())
        {
            taskExecutor=createTaskExecutor();
        }
        if(!config.isCustomExecutorForCachedImages()&&
                ((ExecutorService)taskExecutorForCachedImages).isShutdown())
        {
            taskExecutorForCachedImages=createTaskExecutor();
        }
    }

    private Executor createTaskExecutor()
    {
        return DefaultConfigFactory.createExecutor(config.getThreadPoolSize(),
                config.getThreadPriority(),
                config.getTasksProcessingType());
    }

    /** Checks whether memory cache key (image URI) for current ImageAware is actual */
    public String getLoadingUriForView(ViewWrapper viewWrapper)
    {
        return cacheKeysForViewWrapper.get(viewWrapper.getId());
    }

    public void cancelDisplayTaskFor(ViewWrapper viewWrapper)
    {
        cacheKeysForViewWrapper.remove(viewWrapper.getId());
    }

    public ImageLoaderConfig getConfig()
    {
        return config;
    }

    public AtomicBoolean getPause()
    {
        return paused;
    }

    public Object getPauseLock()
    {
        return pauseLock;
    }

    public boolean isNetworkDenied()
    {
        return networkDenied.get();
    }

    public boolean isSlowNetwork()
    {
        return slowNetwork.get();
    }

    public void fireCallback(Runnable r)
    {
        Logger.d(TAG,"fireCallback,ThreadName:"+Thread.currentThread().getName());
        taskDistributor.execute(r);
    }

    public void prepareDisplayTaskFor(ViewWrapper viewWrapper,String memoryCacheKey)
    {
        cacheKeysForViewWrapper.put(viewWrapper.getId(),memoryCacheKey);
    }

    public ReentrantLock getLockForUri(String uri)
    {
        ReentrantLock lock=uriLocks.get(uri);
        if(null==lock)
        {
            lock=new ReentrantLock();
            uriLocks.put(uri,lock);
        }
        return lock;
    }

    public void pause()
    {
        paused.set(true);
    }

    public void resume()
    {
        paused.set(false);
        synchronized (pauseLock)
        {
            pauseLock.notifyAll();
        }
    }

    public void stop()
    {
        if(!config.isCustomExecutor())
        {
            ((ExecutorService)taskExecutor).shutdownNow();
        }
        if(!config.isCustomExecutorForCachedImages())
        {
            ((ExecutorService)taskExecutorForCachedImages).shutdownNow();
        }

        cacheKeysForViewWrapper.clear();
        uriLocks.clear();

    }

}
