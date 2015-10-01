package wang.imallen.blog.imageloader.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by allen on 15-9-13.
 */
public final class IOUtils {

    private static final String TAG= IOUtils.class.getSimpleName();

    private static final int EOF_FLAG=-1;

    /**
     * 32 KB
     */
    public static final int DEFAULT_BUFFER_SIZE=32*1024;
    /**
     * 500KB
     */
    public static final int DEFAULT_IMAGE_TOTAL_SIZE=500*1024;

    public static final int CONTINUE_LOADING_PERCENTAGE=75;

    private IOUtils(){

    }

    public static boolean copyStream(InputStream is,OutputStream os,CopyListener listener) throws IOException
    {
        return copyStream(is,os,listener,DEFAULT_BUFFER_SIZE);
    }

    /**
     * actually maybe BufferedInputStream and BufferedOutputStream will be better
     * @param is
     * @param os
     * @param listener
     * @param bufferSize
     * @return
     * @throws IOException
     */
    public static boolean copyStream(InputStream is,OutputStream os,
                                     CopyListener listener,int bufferSize) throws IOException
    {
        int current=0;
        int total=is.available();
        if(total<=0)
        {
            total=DEFAULT_IMAGE_TOTAL_SIZE;
        }
        final byte[]bytes=new byte[bufferSize];
        int count;
        if(shouldStopLoading(listener,current,total))
        {
            return false;
        }
        //EOF_FLAG应该为-1,当时粗心把它写成-11,导致LruDiskCache完全不起作用
        while((count=is.read(bytes,0,bufferSize))!=EOF_FLAG)
        {
            os.write(bytes,0,count);
            current+=count;
            if(shouldStopLoading(listener,current,total))
            {
                return false;
            }
        }
        os.flush();
        return true;
    }

    private static boolean shouldStopLoading(CopyListener listener,int current,int total)
    {
        if(null!=listener)
        {
            boolean shouldContinue=listener.onBytesCopied(current,total);
            if(!shouldContinue)
            {
                if(100*current/total<CONTINUE_LOADING_PERCENTAGE)
                {
                    return true;
                }
            }
        }
        // if loaded more than 75% then continue loading anyway
        return false;
    }

    public static void readAndCloseStream(InputStream is)
    {
        final byte[]bytes=new byte[DEFAULT_BUFFER_SIZE];
        try
        {
            while(is.read(bytes,0,DEFAULT_BUFFER_SIZE)!=-1);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            closeSilently(is);
        }
    }

    public static void closeSilently(Closeable closeable)
    {
        if(null!=closeable)
        {
            try
            {
                closeable.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }


    public static interface CopyListener
    {
        boolean onBytesCopied(int current,int total);
    }

}
