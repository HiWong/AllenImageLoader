package wang.imallen.blog.imageloader.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import wang.imallen.blog.imageloader.log.Logger;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by allen on 15-9-5.
 */
public final class StorageUtils {

    private static final String TAG=StorageUtils.class.getSimpleName();

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "uil-images";

    private StorageUtils()
    {

    }

    public static File getCacheDirectory(Context context)
    {
        return getCacheDirectory(context,true);
    }

    public static File getCacheDirectory(Context context,boolean preferExternal)
    {
        File appCacheDir=null;
        String externalStorageState;
        try
        {
            externalStorageState= Environment.getExternalStorageState();
        }
        catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue #989)
            externalStorageState = "";
        }

        if(preferExternal&&Environment.MEDIA_MOUNTED.equals(externalStorageState))
        {
            appCacheDir=getExternalCacheDir(context);
        }
        if(null==appCacheDir)
        {
            appCacheDir=context.getCacheDir();
        }
        if(null==appCacheDir)
        {
            String cacheDirPath="/data/data/"+context.getPackageName()+"/cache/";
            Logger.d("Can't define system cache directory! '%s' will be used.", cacheDirPath);
            appCacheDir=new File(cacheDirPath);
        }
        return appCacheDir;

    }

    public static File getIndividualCacheDirectory(Context context)
    {
        return getIndividualCacheDirectory(context,INDIVIDUAL_DIR_NAME);
    }

    public static File getIndividualCacheDirectory(Context context,String cacheFileName)
    {
        File appCacheDir=getCacheDirectory(context);
        File individualCacheDir=new File(appCacheDir,cacheFileName);
        if(!individualCacheDir.exists())
        {
            if(!individualCacheDir.mkdir())
            {
                individualCacheDir=appCacheDir;
            }
        }
        return individualCacheDir;

    }

    public static File getOwnCacheDirectory(Context context,String cacheDir)
    {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context))
        {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    public static File getOwnCacheDirectory(Context context, String cacheDir, boolean preferExternal) {
        File appCacheDir = null;
        if (preferExternal && MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context)
    {
        File dataDir=new File(new File(Environment.getExternalStorageDirectory(),"Android"),"data");
        File appCacheDir=new File(new File(dataDir,context.getPackageName()),"cache");

        if(!appCacheDir.exists())
        {
            if(!appCacheDir.mkdirs())
            {
                Logger.d(TAG,"Unable to create external cache directory");
                return null;
            }
            try
            {
                new File(appCacheDir,".nomedia").createNewFile();
            }
            catch(IOException e)
            {
                Logger.i(TAG,"Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context)
    {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }




}
