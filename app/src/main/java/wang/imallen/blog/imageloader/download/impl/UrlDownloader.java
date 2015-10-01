package wang.imallen.blog.imageloader.download.impl;

import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import wang.imallen.blog.imageloader.assist.ContentLengthInputStream;
import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.utils.IOUtils;

/**
 * Created by allen on 15-9-14.
 */
public class UrlDownloader implements ImageDownloader {

    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT=5*1000;
    public static final int DEFAULT_HTTP_READ_TIMEOUT=20*1000;
    public static final int BUFFER_SIZE=32*1024;
    protected static final String ALLOWED_URI_CHARS="@#&=*+-_.,:!?()/~'%";
    protected static final int MAX_REDIRECT_COUNT=5;


    private int connectTimeout;
    private int readTimeout;

    public UrlDownloader()
    {
        this(DEFAULT_HTTP_CONNECT_TIMEOUT,DEFAULT_HTTP_READ_TIMEOUT);
    }

    public UrlDownloader(int connectTimeout,int readTimeout)
    {
        this.connectTimeout=connectTimeout;
        this.readTimeout=readTimeout;
    }

    @Override
    public InputStream getStream(String imageUri) throws IOException {
        HttpURLConnection conn=createConnection(imageUri);
        int redirectCount=0;
        while(conn.getResponseCode()/100==3&&redirectCount<MAX_REDIRECT_COUNT)
        {
            conn=createConnection(conn.getHeaderField("Location"));
            redirectCount++;
        }

        InputStream imageStream;
        try
        {
            imageStream=conn.getInputStream();
        }
        catch(IOException ex)
        {
            IOUtils.readAndCloseStream(conn.getErrorStream());
            throw ex;
        }

        if(!shouldBeProcessed(conn))
        {
            IOUtils.closeSilently(imageStream);
            throw new IOException("Image request failed with response code "+conn.getResponseCode());
        }

        return new ContentLengthInputStream(new BufferedInputStream(imageStream),BUFFER_SIZE);
    }

    protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException
    {
        return conn.getResponseCode()==200;
    }

    protected HttpURLConnection createConnection(String url) throws IOException
    {
        String encodeUrl= Uri.encode(url, ALLOWED_URI_CHARS);
        HttpURLConnection conn=(HttpURLConnection)new URL(encodeUrl).openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return conn;
    }

}
