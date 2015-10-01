package wang.imallen.blog.imageloader.info;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import wang.imallen.blog.imageloader.constant.ImageScaleType;
import wang.imallen.blog.imageloader.core.DefaultConfigFactory;
import wang.imallen.blog.imageloader.display.BitmapDisplayer;

/**
 * Created by allen on 15-9-12.
 */
public final class DisplayImageOptions {

    private final int imageResOnLoading;
    private final int imageResForEmptyUri;
    private final int imageResOnFail;

    //this is not necessary
    /*
    private final Drawable drawableOnLoading;
    private final Drawable drawableForEmptyUri;
    private final Drawable drawableOnFail;
    */
    private final boolean shouldCacheInMemory;
    private final boolean shouldCacheOnDisk;


    private final BitmapFactory.Options decodingOptions;

    private final int delayBeforeLoading;
    private final ImageScaleType imageScaleType;
    private final boolean considerExifParams;


    private final BitmapDisplayer displayer;
    //maybe we should put handler in ImageLoaderConfig, because one handler is enough
    private Handler handler;

    private DisplayImageOptions(Builder builder)
    {
        this.imageResOnLoading=builder.imageResOnLoading;
        this.imageResForEmptyUri=builder.imageResForEmptyUri;
        this.imageResOnFail=builder.imageResOnFail;

        this.delayBeforeLoading=builder.delayBeforeLoading;

        this.shouldCacheInMemory=builder.shouldCacheInMemory;
        this.shouldCacheOnDisk=builder.shouldCacheOnDisk;

        this.imageScaleType=builder.imageScaleType;
        this.decodingOptions=builder.decodingOptions;
        this.considerExifParams=builder.considerExifParams;
        this.displayer=builder.displayer;
        this.handler=builder.handler;
    }

    public static DisplayImageOptions createSimple()
    {
        return new Builder().build();
    }

    public boolean shouldCacheInMemory()
    {
        return shouldCacheInMemory;
    }

    public boolean shouldCacheOnDisk()
    {
        return shouldCacheOnDisk;
    }

    public int getDelayBeforeLoading()
    {
        return delayBeforeLoading;
    }

    public boolean shouldDelayBeforeLoading()
    {
        return delayBeforeLoading>0;
    }

    public int getImageResOnLoading() {
        return imageResOnLoading;
    }

    public int getImageResForEmptyUri() {
        return imageResForEmptyUri;
    }

    public int getImageResOnFail() {
        return imageResOnFail;
    }

    public ImageScaleType getImageScaleType() {
        return imageScaleType;
    }

    public boolean isConsiderExifParams() {
        return considerExifParams;
    }

    public BitmapDisplayer getDisplayer() {
        return displayer;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public boolean shouldShowImageOnLoading()
    {
        return imageResOnLoading!=0;
    }

    public boolean shouldShowImageForEmptyUri()
    {
        return imageResForEmptyUri!=0;
    }

    public boolean shouldShowImageOnFail()
    {
        return imageResOnFail!=0;
    }

    public BitmapFactory.Options getDecodingOptions()
    {
        return decodingOptions;
    }


    public static class Builder
    {
        private int imageResOnLoading;
        private int imageResForEmptyUri;
        private int imageResOnFail;

        private boolean shouldCacheInMemory;
        private boolean shouldCacheOnDisk;

        private int delayBeforeLoading;
        private BitmapFactory.Options decodingOptions=new BitmapFactory.Options();
        private ImageScaleType imageScaleType;
        private boolean considerExifParams=false;

        private BitmapDisplayer displayer= DefaultConfigFactory.createBitmapDisplayer();
        private Handler handler;

        public int getImageResOnLoading() {
            return imageResOnLoading;
        }

        public int getDelayBeforeLoading() {
            return delayBeforeLoading;
        }

        public Builder setDelayBeforeLoading(int delayBeforeLoading) {
            this.delayBeforeLoading = delayBeforeLoading;
            return this;
        }

        public boolean shouldCacheOnDisk() {
            return shouldCacheOnDisk;
        }

        public Builder setShouldCacheOnDisk(boolean shouldCacheOnDisk) {
            this.shouldCacheOnDisk = shouldCacheOnDisk;
            return this;
        }

        public boolean shouldCacheInMemory() {
            return shouldCacheInMemory;
        }

        public Builder setShouldCacheInMemory(boolean shouldCacheInMemory) {
            this.shouldCacheInMemory = shouldCacheInMemory;
            return this;
        }

        public BitmapFactory.Options getDecodingOptions() {
            return decodingOptions;
        }

        public Builder setImageResOnLoading(int imageResOnLoading) {
            this.imageResOnLoading = imageResOnLoading;
            return this;
        }

        public int getImageResForEmptyUri() {
            return imageResForEmptyUri;
        }

        public Builder setImageResForEmptyUri(int imageResForEmptyUri) {
            this.imageResForEmptyUri = imageResForEmptyUri;
            return this;
        }

        public int getImageResOnFail() {
            return imageResOnFail;
        }

        public Builder setImageResOnFail(int imageResOnFail) {
            this.imageResOnFail = imageResOnFail;
            return this;
        }

        public Builder setBitmapConfig(Bitmap.Config bitmapConfig)
        {
            if(null==bitmapConfig)
            {
                throw new IllegalArgumentException();
            }
            decodingOptions.inPreferredConfig=bitmapConfig;
            return this;
        }


        public Builder setDecodingOptions(BitmapFactory.Options options)
        {
            if(null==options)
            {
                throw new IllegalArgumentException("decodingOptons can not be null");
            }
            this.decodingOptions=decodingOptions;
            return this;
        }


        public ImageScaleType getImageScaleType() {
            return imageScaleType;
        }

        public Builder setImageScaleType(ImageScaleType imageScaleType) {
            this.imageScaleType = imageScaleType;
            return this;
        }

        public boolean isConsiderExifParams() {
            return considerExifParams;
        }

        public Builder setConsiderExifParams(boolean considerExifParams) {
            this.considerExifParams = considerExifParams;
            return this;
        }

        public BitmapDisplayer getDisplayer() {
            return displayer;
        }

        public Builder setDisplayer(BitmapDisplayer displayer) {
            this.displayer = displayer;
            return this;
        }

        public Handler getHandler() {
            return handler;
        }

        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        public DisplayImageOptions build()
        {
            return new DisplayImageOptions(this);
        }

        public Builder cloneFrom(DisplayImageOptions options)
        {
            imageResOnLoading=options.imageResOnLoading;
            imageResForEmptyUri=options.imageResForEmptyUri;
            imageResOnFail=options.imageResOnFail;


            shouldCacheInMemory=options.shouldCacheInMemory;
            shouldCacheOnDisk=options.shouldCacheOnDisk;

            imageScaleType=options.imageScaleType;
            decodingOptions=options.decodingOptions;
            delayBeforeLoading=options.delayBeforeLoading;
            considerExifParams=options.considerExifParams;

            displayer=options.displayer;
            handler=options.handler;

            return this;
        }

    }

}
