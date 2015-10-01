package wang.imallen.blog.imageloader.download.impl;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import wang.imallen.blog.imageloader.constant.Schema;
import wang.imallen.blog.imageloader.download.ImageDownloader;

/**
 * Created by allen on 15-9-14.
 */
public class DrawableDownloader implements ImageDownloader{

    private Context context;

    public DrawableDownloader(Context context)
    {
        this.context=context;
    }

    @Override
    public InputStream getStream(String imageUri) throws IOException {
        String drawableIdStr= Schema.DRAWABLE.crop(imageUri);
        int drawableId=Integer.parseInt(drawableIdStr);
        return context.getResources().openRawResource(drawableId);
    }
}
