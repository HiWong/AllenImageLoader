package wang.imallen.blog.imageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;

import wang.imallen.blog.imageloader.info.ImageLoadingInfo;
import wang.imallen.blog.imageloader.log.Logger;

/**
 * Created by allen on 15-9-14.
 */
public class ProcessAndDisplayImageTask implements Runnable{

    private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";

    private final ImageLoaderEngine engine;
    private final Bitmap bitmap;
    private final ImageLoadingInfo imageLoadingInfo;
    private final Handler handler;

    public ProcessAndDisplayImageTask(ImageLoaderEngine engine,Bitmap bitmap,
                                      ImageLoadingInfo imageLoadingInfo,Handler handler)
    {
        this.engine=engine;
        this.bitmap=bitmap;
        this.imageLoadingInfo=imageLoadingInfo;
        this.handler=handler;
    }

    @Override
    public void run()
    {
        Logger.d(LOG_POSTPROCESS_IMAGE, imageLoadingInfo.getMemoryCacheKey());

        //we do not use processor, cause this situation is really rare
        //BitmapProcessor processor=imageLoadingInfo.getOptions().getPostProcessor();
       //Bitmap processedBitmap=processor.process(bitmap);



    }



}
