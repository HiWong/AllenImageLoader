package wang.imallen.blog.imageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import wang.imallen.blog.imageloader.assist.FailReason;
import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.config.ImageLoaderConfig;
import wang.imallen.blog.imageloader.constant.ImageScaleType;
import wang.imallen.blog.imageloader.constant.LoadedFromType;
import wang.imallen.blog.imageloader.constant.Schema;
import wang.imallen.blog.imageloader.constant.ViewScaleType;
import wang.imallen.blog.imageloader.decode.ImageDecoder;
import wang.imallen.blog.imageloader.decode.ImageDecodingInfo;
import wang.imallen.blog.imageloader.decode.impl.BaseImageDecoder;
import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.download.ImageDownloaderManager;
import wang.imallen.blog.imageloader.exception.TaskCancelledException;
import wang.imallen.blog.imageloader.helper.LoadAndDisplayHelper;
import wang.imallen.blog.imageloader.info.DisplayImageOptions;
import wang.imallen.blog.imageloader.info.ImageLoadingInfo;
import wang.imallen.blog.imageloader.listener.ImageLoadingListener;
import wang.imallen.blog.imageloader.listener.ImageLoadingProgressListener;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.utils.IOUtils;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-13.
 */
public class LoadAndDisplayImageTask implements Runnable,IOUtils.CopyListener{

    private static final String TAG=LoadAndDisplayImageTask.class.getSimpleName();

    /*
    private static final String LOG_WAITING_FOR_RESUME = "ImageLoader is paused. Waiting...  [%s]";
    private static final String LOG_RESUME_AFTER_PAUSE = ".. Resume loading [%s]";
    private static final String LOG_DELAY_BEFORE_LOADING = "Delay %d ms before loading...  [%s]";
    private static final String LOG_START_DISPLAY_IMAGE_TASK = "Start display image task [%s]";
    private static final String LOG_WAITING_FOR_IMAGE_LOADED = "Image already is loading. Waiting... [%s]";
    private static final String LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING = "...Get cached bitmap from memory after waiting. [%s]";
    private static final String LOG_LOAD_IMAGE_FROM_NETWORK = "Load image from network [%s]";
    private static final String LOG_LOAD_IMAGE_FROM_DISK_CACHE = "Load image from disk cache [%s]";
    private static final String LOG_RESIZE_CACHED_IMAGE_FILE = "Resize image in disk cache [%s]";
    private static final String LOG_PREPROCESS_IMAGE = "PreProcess image before caching in memory [%s]";
    private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";
    private static final String LOG_CACHE_IMAGE_IN_MEMORY = "Cache image in memory [%s]";
    private static final String LOG_CACHE_IMAGE_ON_DISK = "Cache image on disk [%s]";
    private static final String LOG_PROCESS_IMAGE_BEFORE_CACHE_ON_DISK = "Process image before cache on disk [%s]";
    private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
    private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";
    private static final String LOG_TASK_INTERRUPTED = "Task was interrupted [%s]";

    private static final String ERROR_NO_IMAGE_STREAM = "No stream for image [%s]";
    private static final String ERROR_PRE_PROCESSOR_NULL = "Pre-processor returned null [%s]";
    private static final String ERROR_POST_PROCESSOR_NULL = "Post-processor returned null [%s]";
    private static final String ERROR_PROCESSOR_FOR_DISK_CACHE_NULL = "Bitmap processor for disk cache returned null [%s]";
    */


    private final ImageLoaderEngine engine;
    private final ImageLoadingInfo imageLoadingInfo;
    private final Handler handler;

    private final ImageLoaderConfig config;

    /////////downloader related//////////////
    private ImageDownloader downloadManager;

    //private final ImageDownloader downloader;
    //private final ImageDownloader networkDeniedDownloader;
    //private final ImageDownloader slowNetworkDownloader;

   ///////////////////////////////////////////
    private final ImageDecoder decoder;
    private final String uri;
    private final String memoryCacheKey;
    private final ViewWrapper viewWrapper;
    private final ImageSize targetSize;
    private final DisplayImageOptions options;
    private final ImageLoadingListener listener;
    private final ImageLoadingProgressListener progressListener;

    private LoadedFromType loadedFromType=LoadedFromType.NETWORK;

    public LoadAndDisplayImageTask(ImageLoaderEngine engine,
                                   ImageLoadingInfo info,
                                   Handler handler)
    {
        this.engine=engine;
        this.imageLoadingInfo=info;
        //this handler is usually defined by AllenImageLoader instead of outside
        this.handler=handler;

        this.config=engine.getConfig();
        //we cannot build a new ImageDownloaderManager every time
        /*
        this.downloadManager=new ImageDownloaderManager(config.getContext(),
                engine.isNetworkDenied(),engine.isSlowNetwork());
        */
        this.downloadManager=ImageDownloaderManager.getInstance(config.getContext(),
                engine.isNetworkDenied(),engine.isSlowNetwork());

        this.decoder=BaseImageDecoder.getInstance(true);

        uri=imageLoadingInfo.getUri();
        memoryCacheKey=imageLoadingInfo.getMemoryCacheKey();
        viewWrapper=imageLoadingInfo.getViewWrapper();
        targetSize=imageLoadingInfo.getTargetSize();
        options=imageLoadingInfo.getDisplayImageOptions();
        listener=imageLoadingInfo.getListener();
        progressListener=imageLoadingInfo.getProgressListener();

    }

    @Override
    public boolean onBytesCopied(int current, int total)
    {
        return fireProgressEvent(current,total);
    }

    @Override
    public void run()
    {
       if(waifIfPaused())
       {
           return;
       }
       if(delayIfNeed())
       {
           return;
       }

      ReentrantLock loadFromUriLock=imageLoadingInfo.getLoadFromUriLock();
      if(loadFromUriLock.isLocked())
      {
         Logger.d(TAG, "loadFromUriLock is locked");
      }

      loadFromUriLock.lock();
      Bitmap bmp;
      try
      {
          checkTaskNotActual();
          //this may be not necessary cause it has been checked no memory cache in ImageLoader
          bmp=config.getMemoryCache().get(memoryCacheKey);
          if(null==bmp||bmp.isRecycled())
          {
              //bmp=tryLoadBitmap();
              //bmp=tryLoadBitmapEfficiently();
              bmp=tryLoadBitmapEffectively();
              if(null==bmp)
              {
                  return;
              }
              checkTaskNotActual();
              checkTaskInterrupted();

              //no pre process here
              //this is not the right logics
              //if(options.shouldCacheOnDisk())
              if(options.shouldCacheInMemory())
              {
                  config.getMemoryCache().put(memoryCacheKey,bmp);
              }
          }
          else
          {
              loadedFromType=LoadedFromType.MEMORY_CACHE;
          }

          //no post process here
          checkTaskNotActual();
          checkTaskInterrupted();
      }
      catch(TaskCancelledException ex)
      {
          fireCancelEvent();
          return;
      }
      finally
      {
          loadFromUriLock.unlock();
      }
        DisplayBitmapTask displayBitmapTask=new DisplayBitmapTask(bmp,imageLoadingInfo,
                engine,loadedFromType);
        LoadAndDisplayHelper.runTask(displayBitmapTask,handler,engine);
    }

    /**
     * the workflow is as follows:
     * download image stream-->decodeImage-->diskCache save file
     * with this method, we only need to decode once and in this way a lot of time will be saved.
     * @return
     * @throws TaskCancelledException
     */
    private Bitmap tryLoadBitmapEfficiently() throws TaskCancelledException
    {
        Bitmap bitmap=null;
        try
        {
            File imageFile=config.getDiskCache().get(uri);
            if(null!=imageFile&&imageFile.exists()&&imageFile.length()>0)
            {
                Logger.d(TAG,"load from diskCache");
                loadedFromType=LoadedFromType.DISK_CACHE;
                checkTaskNotActual();
                bitmap=decodeImage(Schema.FILE.wrap(imageFile.getAbsolutePath()));
            }
            if(null==bitmap||bitmap.getWidth()<=0||bitmap.getHeight()<=0)
            {
                Logger.d(TAG,"load from network or some other resources");
                loadedFromType=LoadedFromType.NETWORK;
                //decode from network stream directly
                bitmap=decodeImage(uri);
                //then we need to cache it on disk
                //But attention,we do not decode again! so we can not use cacheImageOnDisk() here
                if(null!=bitmap)
                {
                    if(options.shouldCacheOnDisk())
                    {
                        config.getDiskCache().save(uri,bitmap);
                    }
                }
                else
                {
                    fireFailEvent(FailReason.FailType.DECODING_ERROR,null);
                }
            }
        }
        catch(IllegalStateException ex)
        {
            fireFailEvent(FailReason.FailType.NETWORK_DENIED, null);
        }
        catch(TaskCancelledException e)
        {
            throw e;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.IO_ERROR,e);
        }
        catch(OutOfMemoryError e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.OUT_OF_MEMORY,e);
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.UNKNOWN,e);
        }
        return bitmap;

    }

    /**
     * the workflow is as follows:
     * download image stream-->save original file-->decode
     * @return
     * @throws TaskCancelledException
     */
    private Bitmap tryLoadBitmapEffectively() throws TaskCancelledException
    {
        Logger.d(TAG,"tryLoadBitmapEffectively,uri="+uri);
        Bitmap bitmap=null;
        try
        {
            File imageFile = config.getDiskCache().get(uri);
            if (null != imageFile && imageFile.exists() && imageFile.length() > 0)
            {
                Logger.d(TAG, "load from diskcache,uri="+uri);
                loadedFromType = LoadedFromType.DISK_CACHE;
                checkTaskNotActual();
                bitmap = decodeImage(Schema.FILE.wrap(imageFile.getAbsolutePath()));
            }

            //if no imageFile on disk, then we need to load image from network
            if (null == bitmap || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0)
            {
                Logger.d(TAG, "load from network or some other resource,uri="+uri);
                //then we need to load bitmap from network
                loadedFromType = LoadedFromType.NETWORK;
                String imageUriForDecoding = uri;
                boolean downloadSucceedFlag = downloadAndSaveOriginalImageFile();
                Logger.d(TAG,"downloadSucceedFlag="+downloadSucceedFlag+",uri="+uri);

                if(!downloadSucceedFlag)
                {
                    fireFailEvent(FailReason.FailType.UNKNOWN,null);
                }

                if(downloadSucceedFlag&&options.shouldCacheOnDisk())
                {
                    imageFile=config.getDiskCache().get(uri);
                    if(null!=imageFile)
                    {
                        Logger.d(TAG,"imageFile is not null, it implies that disk cache exists");
                        imageUriForDecoding=Schema.FILE.wrap(imageFile.getAbsolutePath());
                    }
                }


                checkTaskNotActual();

                //if image has been downloaded, then imageUriForDecoding is related to file now
                //but actually we decoded and get bitmap once in cacheImageOnDisk() already
                //so why we have to decode twice?
                //or what's the difference between two decode actions?
                //bitmap=decodeImage(imageUriForDecoding);
                int width=config.getMaxImageWidthForDiskCache();
                int height=config.getMaxImageHeightForDiskCache();
                if(width<=0||height<=0)
                {
                    width=targetSize.getWidth();
                    height=targetSize.getHeight();
                }
                bitmap=decodeImage(imageUriForDecoding,width,height);

                if(null==bitmap||bitmap.getWidth()<=0
                        ||bitmap.getHeight()<=0)
                {
                    fireFailEvent(FailReason.FailType.DECODING_ERROR, null);
                }

            }

        }
        catch(IllegalStateException ex)
        {
            fireFailEvent(FailReason.FailType.NETWORK_DENIED, null);
        }
        catch(TaskCancelledException e)
        {
            throw e;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.IO_ERROR,e);
        }
        catch(OutOfMemoryError e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.OUT_OF_MEMORY,e);
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.UNKNOWN,e);
        }
        return bitmap;
    }

    /**
     * actullay its name should be tryLoadBitmapByDiskCachOrNetwork()
     * @return
     * @throws TaskCancelledException
     */
    private Bitmap tryLoadBitmap() throws TaskCancelledException
    {
        Bitmap bitmap=null;
        try
        {
            File imageFile=config.getDiskCache().get(uri);
            if(null!=imageFile&&imageFile.exists()&&imageFile.length()>0)
            {
                Logger.d(TAG,"load from diskcache");
                loadedFromType=LoadedFromType.DISK_CACHE;
                checkTaskNotActual();
                bitmap=decodeImage(Schema.FILE.wrap(imageFile.getAbsolutePath()));
            }
            //if no imageFile on disk, then we need to load image from network
            if(null==bitmap||bitmap.getWidth()<=0||bitmap.getHeight()<=0)
            {
                Logger.d(TAG,"load from network");
                //then we need to load bitmap from network
                loadedFromType=LoadedFromType.NETWORK;
                String imageUriForDecoding=uri;
                boolean downloadSucceedFlag=downloadAndSaveOriginalImageFile();

                if(downloadSucceedFlag&&options.shouldCacheOnDisk())
                {
                    //in cacheImageOnDisk() we will decode once
                    //and we will save file to disk cache again
                    //so why we have to save file twice other than using
                    // this workflow:download image-->resize and decode,then we get the bitmap-->save file to disk cache?
                    cacheImageOnDisk();
                    imageFile=config.getDiskCache().get(uri);
                    if(null!=imageFile)
                    {
                        imageUriForDecoding=Schema.FILE.wrap(imageFile.getAbsolutePath());
                    }
                }

                checkTaskNotActual();

                //if image has been downloaded, then imageUriForDecoding is related to file now
                //but actually we decoded and get bitmap once in cacheImageOnDisk() already
                //so why we have to decode twice?
                //or what's the difference between two decode actions?
                bitmap=decodeImage(imageUriForDecoding);

                if(null==bitmap||bitmap.getWidth()<=0
                        ||bitmap.getHeight()<=0)
                {
                    fireFailEvent(FailReason.FailType.DECODING_ERROR,null);
                }

            }

        }
        catch(IllegalStateException ex)
        {
            fireFailEvent(FailReason.FailType.NETWORK_DENIED, null);
        }
        catch(TaskCancelledException e)
        {
            throw e;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.IO_ERROR,e);
        }
        catch(OutOfMemoryError e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.OUT_OF_MEMORY,e);
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            fireFailEvent(FailReason.FailType.UNKNOWN,e);
        }
        return bitmap;
    }

    private void fireFailEvent(final FailReason.FailType failType,final Throwable failCause)
    {
        Logger.d(TAG, "fireFailEvent");
        if(isTaskInterrupted()||isTaskNotActual())
        {
            return;
        }
        Runnable r=new Runnable()
        {
            @Override
            public void run()
            {
                if(options.shouldShowImageOnFail())
                {
                    viewWrapper.setImageRes(options.getImageResOnFail());
                }
                listener.onLoadingFailed(uri,viewWrapper.getWrappedView(),
                        new FailReason(failType,failCause));
            }
        };
        LoadAndDisplayHelper.runTask(r,handler,engine);
    }


    private boolean cacheImageOnDisk() throws TaskCancelledException
    {
        try
        {
            int width=config.getMaxImageWidthForDiskCache();
            int height=config.getMaxImageHeightForDiskCache();
            if(width>0||height>0)
            {
                resizeAndSaveImage(width, height);
                return true;
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    private Bitmap decodeImage(String imageUri,int width,int height) throws IOException
    {
        Logger.d(TAG, "decodeImage,imageUri=" + imageUri);

        ViewScaleType viewScaleType=viewWrapper.getScaleType();

        ImageDecodingInfo decodingInfo=new ImageDecodingInfo(
                memoryCacheKey,
                imageUri,
                uri,
                new ImageSize(width,height),
                viewScaleType,
                downloadManager,
                options);

        //default decoder is BaseImageDecoder
        return decoder.decode(decodingInfo);
    }

    private Bitmap decodeImage(String imageUri) throws IOException
    {
        Logger.d(TAG, "decodeImage,imageUri=" + imageUri);

        ViewScaleType viewScaleType=viewWrapper.getScaleType();

        ImageDecodingInfo decodingInfo=new ImageDecodingInfo(
                memoryCacheKey,
                imageUri,
                uri,
                targetSize,
                viewScaleType,
                downloadManager,
                options);

        //default decoder is BaseImageDecoder
        return decoder.decode(decodingInfo);
    }

    /**
     * download image file and save it to disk
     * but actually we could save only once
     * @return
     * @throws IOException
     */
    private boolean downloadAndSaveOriginalImageFile() throws IOException
    {
        Logger.d(TAG,"downloadAndSaveOriginalImageFile,uri="+uri);
        InputStream is=downloadManager.getStream(uri);
        if(null==is)
        {
            Logger.e(TAG,"memoryCacheKey:"+memoryCacheKey);
            return false;
        }
        else
        {
            try
            {
                return config.getDiskCache().save(uri,is,this);
            }
            finally
            {
                IOUtils.closeSilently(is);
            }
        }
    }

    /**
     * we decode once here
     * @param maxWidth
     * @param maxHeight
     * @return
     * @throws IOException
     */
    private boolean resizeAndSaveImage(int maxWidth,int maxHeight) throws IOException
    {
        boolean saved=false;
        File targetFile=config.getDiskCache().get(uri);

        if(null!=targetFile&&targetFile.exists())
        {
            ImageSize targetImageSize=new ImageSize(maxWidth,maxHeight);

            DisplayImageOptions specialOptions=new DisplayImageOptions.Builder()
                    .cloneFrom(options).setImageScaleType(ImageScaleType.IN_SAMPLE_INT).build();

            ImageDecodingInfo decodingInfo=new ImageDecodingInfo(memoryCacheKey,
                    //Schema.FILE.wrap(targetFile.getPath()),
                    Schema.FILE.wrap(targetFile.getAbsolutePath()),
                    uri,
                    targetImageSize,
                    ViewScaleType.FIT_INSIDE,
                    downloadManager,
                    specialOptions);

            Bitmap bmp=decoder.decode(decodingInfo);

            if(null!=bmp)
            {
                //this time,we will save it as a image file such as fileName.PNG
                saved=config.getDiskCache().save(uri,bmp);
                bmp.recycle();
            }

        }
        return saved;

    }


    private boolean fireProgressEvent(final int current,final int total)
    {
        if(isTaskInterrupted()||isTaskNotActual())
        {
            return false;
        }
        if(null!=progressListener)
        {
            Runnable r=new Runnable()
            {
                @Override
                public void run()
                {
                    progressListener.onProgressUpdate(uri,viewWrapper.getWrappedView(),current,total);
                }
            };
            runTask(r,false,handler,engine);
        }
        return true;
    }


    private void fireCancelEvent()
    {
        Logger.d(TAG,"fireCancelEvent");
        if(isTaskInterrupted())
        {
            return;
        }
        Runnable r=new Runnable() {
            @Override
            public void run()
            {
                listener.onLoadingCancelled(uri,viewWrapper.getWrappedView());
            }
        };
        LoadAndDisplayHelper.runTask(r,handler,engine);
    }

    private void checkTaskInterrupted() throws TaskCancelledException
    {
        if(isTaskInterrupted())
        {
            throw new TaskCancelledException();
        }
    }

    private boolean isTaskInterrupted()
    {
        return Thread.interrupted();
    }

    private void checkTaskNotActual() throws TaskCancelledException
    {
        checkViewCollected();
        checkViewReused();
    }

    private void checkViewCollected() throws TaskCancelledException
    {
       if(isViewCollected())
       {
           throw new TaskCancelledException();
       }
    }

    private void checkViewReused() throws TaskCancelledException
    {
        if(isViewReused())
        {
            throw new TaskCancelledException();
        }
    }

    /**
     * use handler will be better
     * @return
     */
    private boolean delayIfNeed()
    {
        if(options.shouldDelayBeforeLoading())
        {
            try
            {
                Thread.sleep(options.getDelayBeforeLoading());
            }
            catch(InterruptedException ex)
            {
                return true;
            }
            return isTaskNotActual();
        }
        return false;
    }


    private boolean waifIfPaused()
    {
        AtomicBoolean pause=engine.getPause();
        if(pause.get())
        {
            synchronized (engine.getPauseLock())
            {
                //if(pause.get())    //while is better than if
                while(pause.get())
                {
                    try
                    {
                        engine.getPauseLock().wait();
                    }
                    catch(InterruptedException ex)
                    {
                        return true;
                    }
                }
            }
        }
        return isTaskNotActual();
    }

    private boolean isTaskNotActual()
    {
        return isViewCollected()||isViewReused();
    }

    private boolean isViewCollected()
    {
        return viewWrapper.isCollected();
    }

    private boolean isViewReused()
    {
        String currentCacheKey=engine.getLoadingUriForView(viewWrapper);
        // Check whether memory cache key (image URI) for current ImageAware is actual.
        // If ImageAware is reused for another task then current task should be cancelled.
        return !memoryCacheKey.equals(currentCacheKey);
    }

    public String getLoadingUri()
    {
        return uri;
    }


    public static void runTask(Runnable r,boolean sync,Handler handler,ImageLoaderEngine engine)
    {
        if(sync)
        {
            r.run();
        }
        else if(null==handler)
        {
            engine.fireCallback(r);
        }
        else
        {
            handler.post(r);
        }
    }


}
