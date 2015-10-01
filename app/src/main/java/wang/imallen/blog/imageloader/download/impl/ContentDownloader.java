package wang.imallen.blog.imageloader.download.impl;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;

import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.utils.BitmapUtils;

/**
 * Created by allen on 15-9-14.
 */
public class ContentDownloader implements ImageDownloader{

    protected static final String CONTENT_CONTACTS_URI_PREFIX="content://com.android.contacts/";

    private Context context;
    public ContentDownloader(Context context)
    {
        this.context=context;
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is accessed using {@link android.content.ContentResolver}).
     *
     * @param imageUri Image URI
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws java.io.FileNotFoundException if the provided URI could not be opened
     */
    @Override
    public InputStream getStream(String imageUri) throws FileNotFoundException {

        ContentResolver resolver=context.getContentResolver();
        Uri uri= Uri.parse(imageUri);
        if(isVideoContentUri(uri))
        {
            //video thumbnail
            Long origId=Long.valueOf(uri.getLastPathSegment());
            Bitmap bitmap= MediaStore.Video.Thumbnails.getThumbnail(resolver,
                    origId,MediaStore.Images.Thumbnails.MINI_KIND,null);
            return BitmapUtils.getCompressInputStream(bitmap);
        }
        else if(imageUri.startsWith(CONTENT_CONTACTS_URI_PREFIX))
        {
            return getContactPhotoStream(uri);
        }
        return resolver.openInputStream(uri);
    }

    private boolean isVideoContentUri(Uri uri)
    {
        String mimeType=context.getContentResolver().getType(uri);
        return mimeType!=null&&mimeType.startsWith("video/");
    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected InputStream getContactPhotoStream(Uri uri)
    {
        ContentResolver resolver=context.getContentResolver();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            return ContactsContract.Contacts.openContactPhotoInputStream(resolver,uri,true);
        }
        else
        {
            return ContactsContract.Contacts.openContactPhotoInputStream(resolver,uri);
        }
    }

}
