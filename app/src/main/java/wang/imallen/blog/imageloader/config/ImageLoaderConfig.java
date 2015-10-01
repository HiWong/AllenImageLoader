package wang.imallen.blog.imageloader.config;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.concurrent.Executor;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.cache.disk.DiskCache;
import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;
import wang.imallen.blog.imageloader.cache.memory.MemoryCache;
import wang.imallen.blog.imageloader.cache.memory.impl.FuzzyKeyMemoryCache;
import wang.imallen.blog.imageloader.constant.QueueProcessingType;
import wang.imallen.blog.imageloader.core.DefaultConfigFactory;
import wang.imallen.blog.imageloader.decode.ImageDecoder;
import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.info.DisplayImageOptions;
import wang.imallen.blog.imageloader.log.Logger;

/**
 * Created by allen on 15-9-12.
 */
public final class ImageLoaderConfig {

    private static final String TAG=ImageLoaderConfig.class.getSimpleName();

    //this context is ApplicationContext instead of normal context!
    // OOM will occurred if normal context is used.
    private Context context;

    private Resources resources;

    private final int maxImageWidthForMemoryCache;
    private final int maxImageHeightForMemoryCache;
    private final int maxImageWidthForDiskCache;
    private final int maxImageHeightForDiskCache;

    private final Executor taskExecutor;
    private final Executor taskExecutorForCachedImages;
    private final boolean isCustomExecutor;
    private final boolean isCustomExecutorForCachedImages;

    private final int threadPoolSize;
    private final int threadPriority;
    private final QueueProcessingType tasksProcessingType;

    private final MemoryCache memoryCache;
    private final DiskCache diskCache;

    //decode image in diskCache
    //private final ImageDecoder imageDecoder;

    private final DisplayImageOptions defaultDisplayImageOptions;

    private ImageLoaderConfig(final Builder builder)
    {
        this.context=builder.context.getApplicationContext();

        resources=builder.context.getResources();
        maxImageWidthForMemoryCache=builder.maxImageWidthForMemoryCache;
        maxImageHeightForMemoryCache=builder.maxImageHeightForMemoryCache;
        maxImageWidthForDiskCache=builder.maxImageWidthForDiskCache;
        maxImageHeightForDiskCache=builder.maxImageHeightForDiskCache;

        taskExecutor=builder.taskExecutor;
        taskExecutorForCachedImages=builder.taskExecutorForCachedImages;

        threadPoolSize=builder.threadPoolSize;
        threadPriority=builder.threadPriority;
        tasksProcessingType=builder.tasksProcessingType;

        diskCache=builder.diskCache;
        memoryCache=builder.memoryCache;
        defaultDisplayImageOptions=builder.defaultDisplayImageOptions;

        isCustomExecutor=builder.isCustomExecutor;
        isCustomExecutorForCachedImages=builder.isCustomExecutorForCachedImages;

    }

    public Context getContext()
    {
        return context;
    }

    public ImageSize getMaxImageSize()
    {
        DisplayMetrics displayMetrics=resources.getDisplayMetrics();
        int width=maxImageWidthForMemoryCache;
        if(width<=0)
        {
            width=displayMetrics.widthPixels;
        }
        int height=maxImageHeightForMemoryCache;
        if(height<=0)
        {
            height=displayMetrics.heightPixels;
        }
        return new ImageSize(width,height);
    }



    public static class Builder
    {
        private static final String WARNING_OVERLAP_DISK_CACHE_PARAMS = "diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other";
        private static final String WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR = "diskCache() and diskCacheFileNameGenerator() calls overlap each other";
        private static final String WARNING_OVERLAP_MEMORY_CACHE = "memoryCache() and memoryCacheSize() calls overlap each other";
        private static final String WARNING_OVERLAP_EXECUTOR = "threadPoolSize(), threadPriority() and tasksProcessingOrder() calls "
                + "can overlap taskExecutor() and taskExecutorForCachedImages() calls.";

        public static final int DEFAULT_THREAD_POOL_SIZE=3;

        public static final int DEFAULT_THREAD_PRIORITY=Thread.NORM_PRIORITY;

        public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE=QueueProcessingType.FIFO;

        private Context context;

        private int maxImageWidthForMemoryCache=0;
        private int maxImageHeightForMemoryCache=0;
        private int maxImageWidthForDiskCache=0;
        private int maxImageHeightForDiskCache=0;

        private Executor taskExecutor=null;
        private Executor taskExecutorForCachedImages=null;
        private boolean isCustomExecutor=false;
        private boolean isCustomExecutorForCachedImages=false;

        private int threadPoolSize=DEFAULT_THREAD_POOL_SIZE;
        private int threadPriority=DEFAULT_THREAD_PRIORITY;
        private boolean denyCacheImageMultiSizesInMemory=false;
        private QueueProcessingType tasksProcessingType=DEFAULT_TASK_PROCESSING_TYPE;

        private int memoryCacheSize=0;
        private long diskCacheSize=0;
        private int diskCacheFileCount=0;

        private MemoryCache memoryCache;
        private DiskCache diskCache;
        //private BitmapCache bitmapCache;

        //we will put ImageDownloader(we may use UrlLoader,LocalLoader...),ImageDecoder into Task

        private FileNameGenerator diskCacheFileNameGenerator=null;


        private DisplayImageOptions defaultDisplayImageOptions;

        public Builder(Context context)
        {
            this.context=context.getApplicationContext();
        }

        public Builder setMemoryCacheExtraOptions(int maxImageWidthForMemoryCache, int maxImageHeightForMemoryCache) {
            this.maxImageWidthForMemoryCache = maxImageWidthForMemoryCache;
            this.maxImageHeightForMemoryCache = maxImageHeightForMemoryCache;
            return this;
        }

        public Builder setDiskCacheExtraOptions(int maxImageWidthForDiskCache,int maxImageHeightForDiskCache)
        {
            this.maxImageWidthForDiskCache=maxImageWidthForDiskCache;
            this.maxImageHeightForDiskCache=maxImageHeightForDiskCache;
            return this;
        }

        public Builder setTaskExecutor(Executor executor)
        {
            if (threadPoolSize != DEFAULT_THREAD_POOL_SIZE || threadPriority != DEFAULT_THREAD_PRIORITY || tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
                Logger.e(TAG, WARNING_OVERLAP_EXECUTOR);
            }

            this.taskExecutor=executor;
            return this;
        }

        public Builder setTaskExecutorForCachedImages(Executor executorForCachedImages)
        {
            if (threadPoolSize != DEFAULT_THREAD_POOL_SIZE || threadPriority != DEFAULT_THREAD_PRIORITY || tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
                Logger.e(TAG,WARNING_OVERLAP_EXECUTOR);
            }
            this.taskExecutorForCachedImages=taskExecutorForCachedImages;
            return this;
        }

        public Builder setThreadPoolSize(int poolSize)
        {
            this.threadPoolSize=poolSize;
            return this;
        }

        public Builder setThreadPriority(int threadPriority)
        {
            if(null!=taskExecutor||null!=taskExecutorForCachedImages)
            {
                Logger.e(TAG,WARNING_OVERLAP_EXECUTOR);
            }

            if(threadPriority<Thread.MIN_PRIORITY)
            {
                this.threadPriority=Thread.MIN_PRIORITY;
            }
            else
            {
                if(threadPriority>Thread.MAX_PRIORITY)
                {
                    this.threadPriority=Thread.MAX_PRIORITY;
                }
                else
                {
                    this.threadPriority=threadPriority;
                }
            }
            return this;
        }

        public Builder setDenyCachedImageMultiSizesInMemory(boolean flag)
        {
            this.denyCacheImageMultiSizesInMemory=flag;
            return this;
        }

        public Builder setTasksProcessingType(QueueProcessingType tasksProcessingType)
        {
            if (taskExecutor != null || taskExecutorForCachedImages != null) {
                Logger.e(TAG,WARNING_OVERLAP_EXECUTOR);
            }
            this.tasksProcessingType=tasksProcessingType;
            return this;
        }

        public Builder setMemoryCacheSize(int memoryCacheSize)
        {
            if(memoryCacheSize<=0)
            {
                throw new IllegalArgumentException("memory cache size must be a positive number");
            }
            if(null!=memoryCache)
            {
                Logger.d(WARNING_OVERLAP_MEMORY_CACHE);
            }
            this.memoryCacheSize=memoryCacheSize;
            return this;
        }

        public Builder setMemoryCacheSizePercentage(int availableMemoryPercent)
        {
            if (availableMemoryPercent <= 0 || availableMemoryPercent >= 100)
            {
                throw new IllegalArgumentException("availableMemoryPercent must be in range (0 < % < 100)");
            }

            if (memoryCache != null)
            {
                Logger.d(WARNING_OVERLAP_MEMORY_CACHE);
            }

            long availableMemory = Runtime.getRuntime().maxMemory();
            memoryCacheSize = (int) (availableMemory * (availableMemoryPercent / 100f));
            return this;
        }

        public Builder setMemoryCache(MemoryCache memoryCache)
        {
            if(memoryCacheSize!=0)
            {
                Logger.d(TAG,WARNING_OVERLAP_MEMORY_CACHE);
            }
            this.memoryCache=memoryCache;
            return this;
        }

        public Builder setDiskCacheSize(int maxCacheSize)
        {
            if(maxCacheSize<=0)
            {
                throw new IllegalArgumentException("maxCacheSize must be a positive number");
            }
            if(diskCache!=null)
            {
                Logger.d(TAG,WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }
            this.diskCacheSize=maxCacheSize;
            return this;
        }

        public Builder setDiskCacheFileCount(int maxFileCount)
        {
            if (maxFileCount <= 0)
            {
                throw new IllegalArgumentException("maxFileCount must be a positive number");
            }
            if (diskCache != null)
            {
                Logger.d(WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }
            this.diskCacheFileCount=maxFileCount;
            return this;
        }

        public Builder setDiskCacheFileNameGenerator(FileNameGenerator fileNameGenerator)
        {
            if (diskCache != null)
            {
                Logger.d(WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
            }
            this.diskCacheFileNameGenerator=fileNameGenerator;
            return this;
        }

        public Builder setDiskCache(DiskCache diskCache)
        {
            if (diskCacheSize > 0 || diskCacheFileCount > 0)
            {
                Logger.d(WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }
            if (diskCacheFileNameGenerator != null)
            {
                Logger.d(WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
            }
            this.diskCache=diskCache;
            return this;
        }

        public Builder setDisplayImageOptions(DisplayImageOptions options)
        {
            this.defaultDisplayImageOptions=options;
            return this;
        }

        public ImageLoaderConfig build()
        {
            initEmptyFieldsWithDefaultValues();
            return new ImageLoaderConfig(this);
        }

        private void initEmptyFieldsWithDefaultValues()
        {
            if(taskExecutor==null)
            {
                taskExecutor= DefaultConfigFactory.createExecutor(threadPoolSize,threadPriority,
                        tasksProcessingType);

            }
            else
            {
                isCustomExecutor=true;
            }

            if(taskExecutorForCachedImages==null)
            {
                taskExecutorForCachedImages=DefaultConfigFactory.createExecutor(threadPoolSize,
                        threadPriority,tasksProcessingType);
            }
            else
            {
                isCustomExecutorForCachedImages=true;
            }

            if(null==diskCache)
            {
                if(diskCacheFileNameGenerator==null)
                {
                    diskCacheFileNameGenerator=DefaultConfigFactory.createFileNameGenerator();
                }
                diskCache=DefaultConfigFactory.createDiskCache(context,diskCacheFileNameGenerator,diskCacheSize,diskCacheFileCount);
            }

            if(null==memoryCache)
            {
                memoryCache=DefaultConfigFactory.createMemoryCache(context,memoryCacheSize);
            }

            if(denyCacheImageMultiSizesInMemory)
            {
                memoryCache=new FuzzyKeyMemoryCache(memoryCache);
            }

            if(null==defaultDisplayImageOptions)
            {
                defaultDisplayImageOptions=DisplayImageOptions.createSimple();
            }

        }

    }

    ///////////////////////////start of setter and getter////////////////////

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public int getMaxImageWidthForMemoryCache() {
        return maxImageWidthForMemoryCache;
    }

    public int getMaxImageHeightForMemoryCache() {
        return maxImageHeightForMemoryCache;
    }

    public int getMaxImageWidthForDiskCache() {
        return maxImageWidthForDiskCache;
    }

    public int getMaxImageHeightForDiskCache() {
        return maxImageHeightForDiskCache;
    }

    public Executor getTaskExecutor() {
        return taskExecutor;
    }

    public Executor getTaskExecutorForCachedImages() {
        return taskExecutorForCachedImages;
    }

    public boolean isCustomExecutor() {
        return isCustomExecutor;
    }

    public boolean isCustomExecutorForCachedImages() {
        return isCustomExecutorForCachedImages;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public QueueProcessingType getTasksProcessingType() {
        return tasksProcessingType;
    }

    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    public DiskCache getDiskCache() {
        return diskCache;
    }

    public DisplayImageOptions getDefaultDisplayImageOptions() {
        return defaultDisplayImageOptions;
    }
    ////////////////////////////////////////////////////////////////////////

}
