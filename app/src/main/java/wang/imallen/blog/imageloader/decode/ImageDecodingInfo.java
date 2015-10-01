package wang.imallen.blog.imageloader.decode;

import android.annotation.TargetApi;
import android.graphics.BitmapFactory;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.constant.ImageScaleType;
import wang.imallen.blog.imageloader.constant.ViewScaleType;
import wang.imallen.blog.imageloader.download.ImageDownloader;
import wang.imallen.blog.imageloader.info.DisplayImageOptions;

/**
 * Created by allen on 15-9-14.
 */
public final class ImageDecodingInfo {

    private final String imageKey;
    private final String imageUri;
    private final String originalImageUri;
    private final ImageSize targetSize;

    private final ImageScaleType imageScaleType;
    private final ViewScaleType viewScaleType;

    private final ImageDownloader downloader;
    //private final Object extraForDownloader;

    private final boolean considerExifParams;
    private final BitmapFactory.Options decodingOptions;

    public ImageDecodingInfo(String imageKey,String imageUri,String originalImageUri,
                             ImageSize targetSize,ViewScaleType viewScaleType,
                             ImageDownloader downloader,DisplayImageOptions displayImageOptions)
    {
        this.imageKey=imageKey;
        this.imageUri=imageUri;
        this.originalImageUri=originalImageUri;
        this.targetSize=targetSize;

        this.imageScaleType=displayImageOptions.getImageScaleType();
        this.viewScaleType=viewScaleType;

        this.downloader=downloader;

        considerExifParams=displayImageOptions.isConsiderExifParams();
        decodingOptions=new BitmapFactory.Options();

        copyOptions(displayImageOptions.getDecodingOptions(),decodingOptions);
    }

    @TargetApi(10)
    private void copyOptions(BitmapFactory.Options srcOptions,BitmapFactory.Options desOptions)
    {
        desOptions.inPreferQualityOverSpeed=srcOptions.inPreferQualityOverSpeed;
    }

    public String getImageKey() {
        return imageKey;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getOriginalImageUri() {
        return originalImageUri;
    }

    public ImageSize getTargetSize() {
        return targetSize;
    }

    public ImageScaleType getImageScaleType() {
        return imageScaleType;
    }

    public ViewScaleType getViewScaleType() {
        return viewScaleType;
    }

    public ImageDownloader getDownloader() {
        return downloader;
    }

    public boolean isConsiderExifParams() {
        return considerExifParams;
    }

    public BitmapFactory.Options getDecodingOptions() {
        return decodingOptions;
    }

    public boolean shouldConsiderExifParams()
    {
        return considerExifParams;
    }

}
