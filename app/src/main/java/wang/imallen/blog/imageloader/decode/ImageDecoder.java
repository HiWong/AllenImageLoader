package wang.imallen.blog.imageloader.decode;

import android.graphics.Bitmap;

import java.io.IOException;

import wang.imallen.blog.imageloader.info.ImageLoadingInfo;

/**
 * Created by allen on 15-9-12.
 */
public interface ImageDecoder {

    Bitmap decode(ImageDecodingInfo info) throws IOException;

}
