package wang.imallen.blog.startup;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import wang.imallen.blog.imageloader.cache.disk.naming.impl.Md5FileNameGenerator;
import wang.imallen.blog.imageloader.config.ImageLoaderConfig;
import wang.imallen.blog.imageloader.constant.QueueProcessingType;
import wang.imallen.blog.imageloader.core.AllenImageLoader;

/**
 * Created by allen on 15-9-16.
 */
public class ImageApplication extends Application{

    @Override
    public void onCreate()
    {
        if(Constants.Config.DEVELOPER_MODE&&
                Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        }
        super.onCreate();

        initImageLoader(getApplicationContext());

    }

    public static void initImageLoader(Context context)
    {
        ImageLoaderConfig.Builder builder=new ImageLoaderConfig.Builder(context);
        builder.setThreadPriority(Thread.NORM_PRIORITY-2);
        builder.setDenyCachedImageMultiSizesInMemory(true);
        builder.setDiskCacheFileNameGenerator(new Md5FileNameGenerator());
        builder.setDiskCacheSize(50*1024*1024);
        builder.setTasksProcessingType(QueueProcessingType.LIFO);

        AllenImageLoader.getInstance().init(builder.build());

    }



}
