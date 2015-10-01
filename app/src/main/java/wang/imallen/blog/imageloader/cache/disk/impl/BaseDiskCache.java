package wang.imallen.blog.imageloader.cache.disk.impl;

import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import wang.imallen.blog.imageloader.cache.disk.DiskCache;
import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;
import wang.imallen.blog.imageloader.core.DefaultConfigFactory;
import wang.imallen.blog.imageloader.utils.IOUtils;


/**
 * Created by allen on 15-9-6.
 */
public class BaseDiskCache implements DiskCache
{

    private static final String TAG=BaseDiskCache.class.getSimpleName();

    /**
     * 32KB
     */
    public static final int DEFAULT_BUFFER_SIZE=32*1024;

    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT=Bitmap.CompressFormat.PNG;

    public static final int DEFAULT_COMPRESS_QUALIty=100;

    private static final String ERROR_ARG_NULL="argument must be not null";
    private static final String TEMP_IMAGE_POSTFIX=".tmp";

    protected final File cacheDir;
    protected final File reserveCacheDir;

    protected final FileNameGenerator fileNameGenerator;

    protected int bufferSize=DEFAULT_BUFFER_SIZE;

    protected Bitmap.CompressFormat compressFormat=DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality=DEFAULT_COMPRESS_QUALIty;

    public BaseDiskCache(File cacheDir)
    {
        this(cacheDir,null);
    }

    public BaseDiskCache(File cacheDir,File reserveCacheDir)
    {
        this(cacheDir,reserveCacheDir, DefaultConfigFactory.createFileNameGenerator());
    }

    public BaseDiskCache(File cacheDir,File reserveCacheDir,FileNameGenerator fileNameGenerator)
    {
        if(null==cacheDir)
        {
            throw new IllegalArgumentException("cacheDir"+ERROR_ARG_NULL);
        }
        if(null==fileNameGenerator)
        {
            throw new IllegalArgumentException("fileNameGenerator"+ERROR_ARG_NULL);
        }
        this.cacheDir=cacheDir;
        this.reserveCacheDir=reserveCacheDir;
        this.fileNameGenerator=fileNameGenerator;
    }

    @Override
    public File get(String imageUri)
    {
        return getFile(imageUri);
    }

    @Override
    public boolean save(String imageUri,InputStream imageStream,IOUtils.CopyListener listener) throws IOException
    {
        File imageFile=getFile(imageUri);
        File tmpFile=new File(imageFile.getAbsolutePath()+TEMP_IMAGE_POSTFIX);
        boolean loaded=false;
        try
        {
            OutputStream os=new BufferedOutputStream(new FileOutputStream(tmpFile),bufferSize);
            try
            {
                loaded=IOUtils.copyStream(imageStream,os,listener,bufferSize);
            }
            finally
            {
                IOUtils.closeSilently(os);
            }
        }
        finally
        {
            if(loaded&&!tmpFile.renameTo(imageFile))
            {
                loaded=false;
            }
            if(!loaded)
            {
                tmpFile.delete();
            }

        }
        return loaded;

    }

    @Override
    public boolean save(String imageUri,Bitmap bitmap) throws IOException
    {
        File imageFile=getFile(imageUri);
        File tmpFile=new File(imageFile.getAbsolutePath()+TEMP_IMAGE_POSTFIX);
        OutputStream os=new BufferedOutputStream(new FileOutputStream(tmpFile),bufferSize);
        boolean savedFlag=false;
        try
        {
            savedFlag=bitmap.compress(compressFormat,compressQuality,os);
        }
        finally
        {
            IOUtils.closeSilently(os);
            if(savedFlag&&!tmpFile.renameTo(imageFile))
            {
                savedFlag=false;
            }
            if(!savedFlag)
            {
                tmpFile.delete();
            }
        }
        bitmap.recycle();
        return savedFlag;
    }

    @Override
    public boolean remove(String imageUri)
    {
        return getFile(imageUri).delete();
    }

    @Override
    public void close()
    {

    }

    @Override
    public void clear()
    {
        File[]files=cacheDir.listFiles();
        if(null!=files)
        {
            for(File f:files)
            {
                f.delete();
            }
        }
    }

    /**
     * this could help
     * @param imageUri
     * @return
     */
    protected File getFile(String imageUri)
    {
        String fileName=fileNameGenerator.generate(imageUri);
        File dir=cacheDir;
        if(!cacheDir.exists()&&!cacheDir.mkdirs())
        {
            if(reserveCacheDir!=null&&(reserveCacheDir.exists()||reserveCacheDir.mkdirs()))
            {
                dir=reserveCacheDir;
            }
        }
        return new File(dir,fileName);
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

