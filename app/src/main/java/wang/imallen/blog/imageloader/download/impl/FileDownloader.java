package wang.imallen.blog.imageloader.download.impl;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import wang.imallen.blog.imageloader.assist.ContentLengthInputStream;
import wang.imallen.blog.imageloader.constant.Schema;
import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.utils.BitmapUtils;

/**
 * Created by allen on 15-9-14.
 */
public class FileDownloader implements ImageDownloader{

    protected static final int BUFFER_SIZE=32*1024;

    @Override
    public InputStream getStream(String imageUri) throws IOException {
        String filePath= Schema.FILE.crop(imageUri);
        if(isVideoFileUri(imageUri))
        {
            return getVideoThumbnailStream(filePath);
        }
        else
        {
            BufferedInputStream imageStream=new BufferedInputStream(new FileInputStream(filePath),
                    BUFFER_SIZE);
            return new ContentLengthInputStream(imageStream,(int)new File(filePath).length());
        }
    }

    private boolean isVideoFileUri(String uri)
    {
        String extension= MimeTypeMap.getFileExtensionFromUrl(uri);
        String mimeType=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType!=null&&mimeType.startsWith("video/");

    }


    @TargetApi(Build.VERSION_CODES.FROYO)
    private InputStream getVideoThumbnailStream(String filePath)
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.FROYO)
        {
            Bitmap bitmap= ThumbnailUtils.createVideoThumbnail(filePath,
                    MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
           return BitmapUtils.getCompressInputStream(bitmap);
        }
        return null;
    }




}
