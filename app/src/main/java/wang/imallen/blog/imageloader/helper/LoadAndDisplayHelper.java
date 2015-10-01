package wang.imallen.blog.imageloader.helper;

import android.os.Handler;

import wang.imallen.blog.imageloader.core.ImageLoaderEngine;
import wang.imallen.blog.imageloader.log.Logger;

/**
 * Created by allen on 15-9-16.
 */
public class LoadAndDisplayHelper {

    private static final String TAG=LoadAndDisplayHelper.class.getSimpleName();

    public static void runTask(Runnable r,Handler handler,
                               ImageLoaderEngine engine)
    {
        //we know this is not in main thread,actually it's in allenuil-pool-1-thread-threadnum such as allenuil-pool-1-thread-3
        Logger.d(TAG, "runTask,ThreadName:"+Thread.currentThread().getName());
        if(handler==null)
        {
            engine.fireCallback(r);
        }
        else
        {
            //so this is why bitmap displayed to ImageView
            // The runnable will be run on the thread to which this handler is attached.
            handler.post(r);
        }
    }


}
