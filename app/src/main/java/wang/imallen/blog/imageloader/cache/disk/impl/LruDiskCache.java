package wang.imallen.blog.imageloader.cache.disk.impl;

import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import wang.imallen.blog.imageloader.cache.disk.DiskCache;
import wang.imallen.blog.imageloader.cache.disk.disklrucache.DiskLruCache;
import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.utils.IOUtils;

/**
 * Created by allen on 15-9-13.
 */
public class LruDiskCache implements DiskCache{

    private static final String TAG=LruDiskCache.class.getSimpleName();
    //32 kb
    public static final int DEFAULT_BUFFER_SIZE=32*1024;

    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT=Bitmap.CompressFormat.PNG;

    public static final int DEFAULT_COMPRESS_QUALITY=100;

    private static final String ERROR_ARG_NULL = " argument must be not null";
    private static final String ERROR_ARG_NEGATIVE = " argument must be positive number";

    protected DiskLruCache cache;
    private File reserveCacheDir;

    protected final FileNameGenerator fileNameGenerator;

    protected int bufferSize=DEFAULT_BUFFER_SIZE;

    protected Bitmap.CompressFormat compressFormat=DEFAULT_COMPRESS_FORMAT;

    protected int compressQuality=DEFAULT_COMPRESS_QUALITY;

    public LruDiskCache(File cacheDir,FileNameGenerator fileNameGenerator,long cacheMaxSize)
        throws IOException
    {
        this(cacheDir,null,fileNameGenerator,cacheMaxSize,0);
    }

    public LruDiskCache(File cacheDir,File reserveCacheDir,FileNameGenerator fileNameGenerator,
                        long cacheMaxSize,int cacheMaxFileCount) throws IOException
    {
        if (cacheDir == null)
        {
            throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
        }
        if (cacheMaxSize < 0)
        {
            throw new IllegalArgumentException("cacheMaxSize" + ERROR_ARG_NEGATIVE);
        }
        if (cacheMaxFileCount < 0)
        {
            throw new IllegalArgumentException("cacheMaxFileCount" + ERROR_ARG_NEGATIVE);
        }
        if (fileNameGenerator == null)
        {
            throw new IllegalArgumentException("fileNameGenerator" + ERROR_ARG_NULL);
        }

        if(cacheMaxSize==0)
        {
            cacheMaxSize=Long.MAX_VALUE;
        }

        if(cacheMaxFileCount==0)
        {
            cacheMaxFileCount=Integer.MAX_VALUE;
        }

        this.reserveCacheDir=reserveCacheDir;
        this.fileNameGenerator=fileNameGenerator;
        initCache(cacheDir,reserveCacheDir,cacheMaxSize,cacheMaxFileCount);

    }

    private void initCache(File cacheDir,File reserveCacheDir,long cacheMaxSize,int cacheMaxFileCount)
       throws IOException
    {
        try
        {
            cache=DiskLruCache.open(cacheDir,1,1,cacheMaxSize,cacheMaxFileCount);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            if (reserveCacheDir != null)
            {
                initCache(reserveCacheDir, null, cacheMaxSize, cacheMaxFileCount);
            }
            if (cache == null) {
                throw ex; //new RuntimeException("Can't initialize disk cache", e);
            }
        }
    }

    @Override
    public File get(String imageUri)
    {
        Logger.d(TAG,"LruDiskCache-->get(imageUri),imageUri="+imageUri);
        DiskLruCache.Snapshot snapshot=null;
        try
        {
            snapshot=cache.get(getKey(imageUri));
            return snapshot==null?null:snapshot.getFile(0);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        finally
        {
            if(null!=snapshot)
            {
                snapshot.close();
            }
        }

    }

    private String getKey(String imageUri)
    {
        return fileNameGenerator.generate(imageUri);
    }

    @Override
    public boolean save(String imageUri, InputStream imageStream, IOUtils.CopyListener listener) throws IOException {
        Logger.d(TAG,"save(imageUri,imageStream,listener),imageUri="+imageUri);
        DiskLruCache.Editor editor=cache.edit(getKey(imageUri));
        Logger.d(TAG,"getKey(imageUri)="+getKey(imageUri));
        if(null==editor)
        {
            return false;
        }

        OutputStream os=new BufferedOutputStream(editor.newOutputStream(0),bufferSize);
        boolean copyFlag=false;
        try
        {
            copyFlag=IOUtils.copyStream(imageStream,os,listener,bufferSize);
        }
        finally
        {
            IOUtils.closeSilently(os);
            if(copyFlag)
            {
                editor.commit();
            }
            else
            {
                editor.abort();
            }
        }
        //copyFlag is always false, it means there is something wrong.
        Logger.d(TAG,"copyFlag:"+copyFlag+",uri="+imageUri);
        return copyFlag;

    }

    @Override
    public boolean save(String imageUri, Bitmap bitmap) throws IOException
    {
        Logger.d(TAG,"save(imageUri,bitmap),imageUri="+imageUri);
        DiskLruCache.Editor editor=cache.edit(getKey(imageUri));
        Logger.d(TAG,"getKey(imageUri)="+getKey(imageUri));
        if(null==editor)
        {
            return false;
        }
        OutputStream os=new BufferedOutputStream(editor.newOutputStream(0));
        boolean savedSuccessfully=false;
        try
        {
            savedSuccessfully=bitmap.compress(compressFormat,compressQuality,os);
        }
        finally
        {
            IOUtils.closeSilently(os);
        }
        if(savedSuccessfully)
        {
            editor.commit();
        }
        else
        {
            editor.abort();
        }
        Logger.d(TAG,"savedSuccessfully="+savedSuccessfully);
        return savedSuccessfully;
    }

    @Override
    public boolean remove(String imageUri) {
        try
        {
            return cache.remove(getKey(imageUri));
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
         try
         {
             cache.close();
         }
         catch(IOException ex)
         {
             ex.printStackTrace();
         }
         cache=null;
    }

    @Override
    public void clear()
    {
         try
         {
             cache.delete();
         }
         catch(IOException ex)
         {
             ex.printStackTrace();
         }
         try
         {
             initCache(cache.getDirectory(),reserveCacheDir,cache.getMaxSize(),cache.getMaxFileCount());
         }
         catch(IOException ex)
         {
             ex.printStackTrace();
         }
    }


    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
    }

}
