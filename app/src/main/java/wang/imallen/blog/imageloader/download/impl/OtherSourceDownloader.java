package wang.imallen.blog.imageloader.download.impl;

import java.io.IOException;
import java.io.InputStream;

import wang.imallen.blog.imageloader.download.ImageDownloader;

/**
 * Created by allen on 15-9-14.
 */
public class OtherSourceDownloader implements ImageDownloader{

    private static final String ERROR_UNSUPPORTED_SCHEME = "UIL doesn't support scheme(protocol) by default [%s]. " + "You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";

    @Override
    public InputStream getStream(String imageUri) throws IOException {
        throw new UnsupportedOperationException(String.format(ERROR_UNSUPPORTED_SCHEME,imageUri));
    }
}
