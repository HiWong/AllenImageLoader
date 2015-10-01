package wang.imallen.blog.imageloader.core;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import wang.imallen.blog.imageloader.cache.disk.DiskCache;
import wang.imallen.blog.imageloader.cache.disk.impl.LruDiskCache;
import wang.imallen.blog.imageloader.cache.disk.impl.UnlimitedDiskCache;
import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;
import wang.imallen.blog.imageloader.cache.disk.naming.impl.HashCodeFileNameGenerator;
import wang.imallen.blog.imageloader.cache.memory.impl.LruMemoryCache;
import wang.imallen.blog.imageloader.cache.memory.MemoryCache;
import wang.imallen.blog.imageloader.constant.QueueProcessingType;
import wang.imallen.blog.imageloader.deque.LIFOLinkedBlockingDeque;
import wang.imallen.blog.imageloader.display.BitmapDisplayer;
import wang.imallen.blog.imageloader.display.impl.SimpleBitmapDisplayer;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.utils.StorageUtils;

/**
 * Created by allen on 15-9-12.
 */
public final class DefaultConfigFactory {

    private static final String TAG=DefaultConfigFactory.class.getSimpleName();

    public static final String TASK_THREAD_NAME_FIX="allenuil-pool-";
    public static final String TASK_DISTRIBUTOR_THREAD_NAME_FIX="allenuil-pool-d-";


    public static Executor createExecutor(int threadPoolSize,int threadPriority,
                                          QueueProcessingType queueProcessingType)
    {
        boolean lifoFlag=queueProcessingType==QueueProcessingType.LIFO;
        BlockingQueue<Runnable>taskQueue=lifoFlag?new LIFOLinkedBlockingDeque<Runnable>():
                new LinkedBlockingQueue<Runnable>();

        return new ThreadPoolExecutor(threadPoolSize,threadPoolSize,0L,
                TimeUnit.MILLISECONDS,taskQueue,createThreadFactory(threadPriority,TASK_THREAD_NAME_FIX));
    }

    private static ThreadFactory createThreadFactory(int threadPriority,String threadNamePrefix)
    {
        return new DefaultThreadFactory(threadPriority,threadNamePrefix);
    }

    public static Executor createTaskDistributor()
    {
        return Executors.newCachedThreadPool(createThreadFactory(Thread.NORM_PRIORITY,
                TASK_DISTRIBUTOR_THREAD_NAME_FIX));
    }

    public static FileNameGenerator createFileNameGenerator()
    {
        return new HashCodeFileNameGenerator();
    }

    public static BitmapDisplayer createBitmapDisplayer()
    {
        return new SimpleBitmapDisplayer();
    }


    private static class DefaultThreadFactory implements ThreadFactory
    {
        private static final AtomicInteger poolNum=new AtomicInteger(1);

        private final ThreadGroup group;
        private final AtomicInteger threadNum=new AtomicInteger(1);
        private final String namePrefix;
        private final int threadPriority;

        public DefaultThreadFactory(int threadPriority,String threadNamePrefix)
        {
            this.threadPriority=threadPriority;
            group=Thread.currentThread().getThreadGroup();
            namePrefix=threadNamePrefix+poolNum.getAndIncrement()+"-thread-";
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t=new Thread(group,r,namePrefix+threadNum.getAndIncrement(),0);
            if(t.isDaemon())
            {
                t.setDaemon(false);
            }
            t.setPriority(threadPriority);
            return t;
        }

    }

    public static DiskCache createDiskCache(Context context,FileNameGenerator diskCacheFileNameGenerator,
                                            long diskCacheSize,int diskCacheFileCount)
    {
        File reserveCacheDir=createReserveDiskCacheDir(context);
        if(diskCacheSize>0||diskCacheFileCount>0)
        {
            File individualCacheDir=StorageUtils.getIndividualCacheDirectory(context);
            try
            {
                return new LruDiskCache(individualCacheDir,reserveCacheDir,diskCacheFileNameGenerator,
                        diskCacheSize,diskCacheFileCount);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }

        }
        File cacheDir=StorageUtils.getCacheDirectory(context);
        return new UnlimitedDiskCache(cacheDir,reserveCacheDir,
                diskCacheFileNameGenerator);

    }

    private static File createReserveDiskCacheDir(Context context)
    {
        File cacheDir= StorageUtils.getCacheDirectory(context, false);
        File individualDir=new File(cacheDir,"allenuil-images");
        if(individualDir.exists()||individualDir.mkdir())
        {
            cacheDir=individualDir;
        }
        return cacheDir;
    }

    public static MemoryCache createMemoryCache(Context context,int memoryCacheSize)
    {
        if(memoryCacheSize==0)
        {
            ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass=am.getMemoryClass();
            if(hasHoneyComb()&&isLargeHeap(context))
            {
                memoryClass=getLargeMemoryClass(am);
            }
            memoryCacheSize=1024*1024*memoryClass/8;
            Logger.d(TAG, "memoryCacheSize=" + memoryCacheSize);

        }
        return new LruMemoryCache(memoryCacheSize);
    }

    private static boolean hasHoneyComb()
    {
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static boolean isLargeHeap(Context context)
    {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP)!=0;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static int getLargeMemoryClass(ActivityManager am)
    {
        return am.getLargeMemoryClass();
    }

}
