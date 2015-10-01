package wang.imallen.blog.imageloader.download;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import wang.imallen.blog.imageloader.assist.ContentLengthInputStream;
import wang.imallen.blog.imageloader.assist.FlushedInputStream;
import wang.imallen.blog.imageloader.constant.Schema;
import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.download.impl.AssetsDownloader;
import wang.imallen.blog.imageloader.download.impl.ContentDownloader;
import wang.imallen.blog.imageloader.download.impl.DrawableDownloader;
import wang.imallen.blog.imageloader.download.impl.FileDownloader;
import wang.imallen.blog.imageloader.download.impl.OtherSourceDownloader;
import wang.imallen.blog.imageloader.download.impl.UrlDownloader;
import wang.imallen.blog.imageloader.utils.IOUtils;


/**
 * Created by allen on 15-9-14.
 */
public final class ImageDownloaderManager implements ImageDownloader{

    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT=5*1000;
    public static final int DEFAULT_HTTP_READ_TIMEOUT=20*1000;
    public static final int BUFFER_SIZE=32*1024;
    protected static final String ALLOWED_URI_CHARS="@#&=*+-_.,:!?()/~'%";
    protected static final int MAX_REDIRECT_COUNT=5;

    private static ImageDownloaderManager instance;


    private static final String ERROR_UNSUPPORT_SCHEME="UIL doesn't support scheme(protocol) by default [%s]. "
            + "You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";

    protected final Context context;
    protected final int connectTimeout;
    protected final int readTimeout;

    private ImageDownloader urlDownloader;
    private ImageDownloader fileDownloader;
    private ImageDownloader contentDownloader;
    private ImageDownloader assetsDownloader;
    private ImageDownloader drawableDownloader;
    private ImageDownloader otherSourceDownloader;

    private boolean isNetworkDenied;
    private boolean isSlowNetwork;

    public static ImageDownloaderManager getInstance(Context context,boolean isNetworkDenied,
                                                     boolean isSlowNetwork)
    {
        if(null==instance)
        {
            synchronized (ImageDownloaderManager.class)
            {
                if(null==instance)
                {
                    instance=new ImageDownloaderManager(context,isNetworkDenied,isSlowNetwork);
                }
                else
                {
                    instance.isNetworkDenied=isNetworkDenied;
                    instance.isSlowNetwork=isSlowNetwork;
                }
            }
        }
        return instance;
    }

    private ImageDownloaderManager(Context context)
    {
        this(context,DEFAULT_HTTP_CONNECT_TIMEOUT,DEFAULT_HTTP_READ_TIMEOUT,false,false);
    }

    private ImageDownloaderManager(Context context,boolean isNetworkDenied,boolean isSlowNetwork)
    {
        this(context,DEFAULT_HTTP_CONNECT_TIMEOUT,DEFAULT_HTTP_READ_TIMEOUT,isNetworkDenied,isSlowNetwork);
    }

    public ImageDownloaderManager(Context context, int connectTimeout, int readTimeout,
                                  boolean isNetworkDenied,boolean isSlowNetwork)
    {
        //attention!we need to use ApplicationContext in case of OOM
        this.context=context.getApplicationContext();
        this.connectTimeout=connectTimeout;
        this.readTimeout=readTimeout;
        this.isNetworkDenied=isNetworkDenied;
        this.isSlowNetwork=isSlowNetwork;
    }

    //public InputStream getStream(String imageUri,boolean isNetworkDenied,boolean isSlowNetwork) throws IOException
    @Override
    public InputStream getStream(String imageUri) throws IOException
    {
        switch(Schema.ofUri(imageUri))
        {
            case HTTP:
            case HTTPS:
                return getStreamFromNetwork(imageUri);
            case FILE:
                return getStreamFromFile(imageUri);
            case CONTENT:
                return getStreamFromContent(imageUri);
            case ASSETS:
                return getStreamFromAssets(imageUri);
            case DRAWABLE:
                return getStreamFromDrawable(imageUri);
            case UNKNOWN:
                default:
                    return getStreamFromOtherSource(imageUri);
        }

    }

    protected InputStream getStreamFromNetwork(String imageUri) throws IOException
    {
        if(isNetworkDenied)
        {
            throw new IllegalStateException();
        }

        if(null==urlDownloader)
        {
            urlDownloader=new UrlDownloader(connectTimeout,readTimeout);
        }

        if(isSlowNetwork)
        {
            return new FlushedInputStream(urlDownloader.getStream(imageUri));
        }
        return urlDownloader.getStream(imageUri);
    }

    protected InputStream getStreamFromFile(String imageUri) throws IOException
    {
        if(null==fileDownloader)
        {
            fileDownloader=new FileDownloader();
        }
        return fileDownloader.getStream(imageUri);
    }

    protected InputStream getStreamFromContent(String imageUri) throws IOException
    {
        if(null==contentDownloader)
        {
            contentDownloader=new ContentDownloader(context);
        }
        return contentDownloader.getStream(imageUri);
    }

    protected InputStream getStreamFromAssets(String imageUri) throws IOException
    {
        if(null==assetsDownloader)
        {
            assetsDownloader=new AssetsDownloader(context);
        }
        return assetsDownloader.getStream(imageUri);
    }

    protected InputStream getStreamFromDrawable(String imageUri) throws IOException
    {
        if(null==drawableDownloader)
        {
            drawableDownloader=new DrawableDownloader(context);
        }
        return drawableDownloader.getStream(imageUri);
    }

    protected InputStream getStreamFromOtherSource(String imageUri) throws IOException
    {
        if(null==otherSourceDownloader)
        {
            otherSourceDownloader=new OtherSourceDownloader();
        }
        return otherSourceDownloader.getStream(imageUri);
    }

}
