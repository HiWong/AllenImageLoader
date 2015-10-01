package wang.imallen.blog.imageloader.decode.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.constant.ImageScaleType;
import wang.imallen.blog.imageloader.constant.Schema;
import wang.imallen.blog.imageloader.decode.ImageDecoder;
import wang.imallen.blog.imageloader.decode.ImageDecodingInfo;
import wang.imallen.blog.imageloader.info.ImageLoadingInfo;
import wang.imallen.blog.imageloader.log.Logger;
import wang.imallen.blog.imageloader.utils.IOUtils;
import wang.imallen.blog.imageloader.utils.ImageSizeUtils;

/**
 * Created by allen on 15-9-12.
 */
public final class BaseImageDecoder implements ImageDecoder {

    private static final String TAG=BaseImageDecoder.class.getSimpleName();

    protected static final String LOG_SUBSAMPLE_IMAGE = "Subsample original image (%1$s) to %2$s (scale = %3$d) [%4$s]";
    protected static final String LOG_SCALE_IMAGE = "Scale subsampled image (%1$s) to %2$s (scale = %3$.5f) [%4$s]";
    protected static final String LOG_ROTATE_IMAGE = "Rotate image on %1$d\u00B0 [%2$s]";
    protected static final String LOG_FLIP_IMAGE = "Flip image horizontally [%s]";
    protected static final String ERROR_NO_IMAGE_STREAM = "No stream for image [%s]";
    protected static final String ERROR_CANT_DECODE_IMAGE = "Image can't be decoded [%s]";

    protected final boolean loggingEnabled;

    private static BaseImageDecoder instance;

    public static BaseImageDecoder getInstance(boolean logEnabledFlag)
    {
        if(null==instance)
        {
            synchronized (BaseImageDecoder.class)
            {
                if(null==instance)
                {
                    instance=new BaseImageDecoder(logEnabledFlag);
                }
            }
        }
        return instance;
    }


    //public BaseImageDecoder(boolean loggingEnabled)
    private BaseImageDecoder(boolean loggingEnabled)
    {
        this.loggingEnabled=loggingEnabled;
    }

    @Override
    public Bitmap decode(ImageDecodingInfo info) throws IOException
    {
        Bitmap decodedBitmap;
        ImageFileInfo imageFileInfo;

        InputStream imageStream=getImageStream(info);
        if(null==imageStream)
        {
            Logger.e(ERROR_NO_IMAGE_STREAM,info.getImageKey());
            return null;
        }
        try
        {
            imageFileInfo=defineImageSizeAndRotation(imageStream,info);
            imageStream=resetStream(imageStream,info);

            BitmapFactory.Options decodingOptions=prepareDecodingOptons(imageFileInfo.imageSize,info);

            decodedBitmap=BitmapFactory.decodeStream(imageStream,null,decodingOptions);
        }
        finally
        {
            IOUtils.closeSilently(imageStream);
        }

        if(null==decodedBitmap)
        {
            Logger.e(ERROR_CANT_DECODE_IMAGE,info.getImageKey());
        }
        else
        {
            decodedBitmap=considerExactScaleAndOrientation(decodedBitmap,
                    info,imageFileInfo.exif.rotation,imageFileInfo.exif.flipHorizontal);
        }
        return decodedBitmap;

    }

    protected BitmapFactory.Options prepareDecodingOptons(ImageSize imageSize,
                                                          ImageDecodingInfo decodingInfo)
    {
        ImageScaleType scaleType=decodingInfo.getImageScaleType();
        int scale;
        if(scaleType==ImageScaleType.NONE)
        {
            scale=1;
        }
        else if(scaleType==ImageScaleType.NONE_SAFE)
        {
            scale= ImageSizeUtils.computeMinImageSampleSize(imageSize);
        }
        else
        {
            ImageSize targetSize=decodingInfo.getTargetSize();
            boolean powerOf2=scaleType==ImageScaleType.IN_SAMPLE_POWER_OF_2;
            scale=ImageSizeUtils.computeImageSampleSize(imageSize,
                    targetSize,decodingInfo.getViewScaleType(),powerOf2);

        }

        Logger.d(TAG,"scale="+scale);
        BitmapFactory.Options decodingOptions=decodingInfo.getDecodingOptions();
        decodingOptions.inSampleSize=scale;
        return decodingOptions;
    }

    protected Bitmap considerExactScaleAndOrientation(Bitmap subSampledBitmap,ImageDecodingInfo decodingInfo,
                                                      int rotation,boolean flipHorizontal)
    {
        Matrix m=new Matrix();
        //Scale to exact size if need
        ImageScaleType scaleType=decodingInfo.getImageScaleType();
        if(scaleType==ImageScaleType.EXACTLY||
                scaleType==ImageScaleType.EXACTLY_STRETCHED)
        {
            ImageSize srcSize=new ImageSize(subSampledBitmap.getWidth(),
                    subSampledBitmap.getHeight());
            float scale=ImageSizeUtils.computeImageScale(srcSize,decodingInfo.getTargetSize(),
                    decodingInfo.getViewScaleType(),scaleType==ImageScaleType.EXACTLY_STRETCHED);
            if(Float.compare(scale,1f)!=0)
            {
                m.setScale(scale,scale);
            }

        }

        //Flip bitmap if need
        if(flipHorizontal)
        {
            m.postScale(-1,1);
        }

        //rotate bitmap if need
        if(rotation!=0)
        {
            m.postRotate(rotation);
        }

        Bitmap finalBitmap=Bitmap.createBitmap(subSampledBitmap,0,0,subSampledBitmap.getWidth(),
                subSampledBitmap.getHeight(),m,true);
        if(finalBitmap!=subSampledBitmap)
        {
            subSampledBitmap.recycle();
        }
        return finalBitmap;

    }


    protected ImageFileInfo defineImageSizeAndRotation(InputStream imageStream,
                                                       ImageDecodingInfo decodingInfo) throws IOException
    {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(imageStream,null,options);

        ExifInfo exif;
        String imageUri=decodingInfo.getImageUri();
        if(decodingInfo.shouldConsiderExifParams()&&
                canDefinedExifParams(imageUri,options.outMimeType))
        {
            exif=defineExifOrientation(imageUri);
        }
        else
        {
            exif=new ExifInfo();
        }
        return new ImageFileInfo(new ImageSize(options.outWidth,options.outHeight,
                exif.rotation),exif);
    }

    protected InputStream resetStream(InputStream imageStream,
                                      ImageDecodingInfo decodingInfo) throws IOException
    {
        try
        {
            imageStream.reset();
        }
        catch(IOException ex)
        {
            IOUtils.closeSilently(imageStream);
            imageStream=getImageStream(decodingInfo);
        }
        return imageStream;
    }

    private boolean canDefinedExifParams(String imageUri,String mimeType)
    {
        return "image/jpeg".equalsIgnoreCase(mimeType)
                &&(Schema.ofUri(imageUri)==Schema.FILE);
    }

    protected ExifInfo defineExifOrientation(String imageUri)
    {
        int rotation=0;
        boolean flip=false;
        try
        {
            ExifInterface exif=new ExifInterface(Schema.FILE.crop(imageUri));
            int exifOrientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch(exifOrientation)
            {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    flip=true;
                case ExifInterface.ORIENTATION_NORMAL:
                    rotation=0;
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    flip=true;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation=90;
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    flip=true;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation=180;
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    flip=true;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation=270;
                    break;

            }
        }
        catch(IOException ex)
        {
            Logger.d(TAG,"Can't read EXIF tags from file [%s]"+imageUri);
        }
        return new ExifInfo(rotation,flip);
    }

    /**
     * usually, getDownloader.getStream() is not download image from network but from local file
     * but if image not download before, then uri starts with Schema.HTTP or Schema.HTTPS,
     * then here is download from network directly. And as a result, image file will not cached on disk.
     * @param decodingInfo
     * @return
     * @throws IOException
     */
    protected InputStream getImageStream(ImageDecodingInfo decodingInfo) throws IOException
    {
        //Logger.d(TAG,"getImageStream,imageUri="+decodingInfo.getImageUri());
        //Attention! getDownloader() is ImageDownloaderManager,it may use urlDownloader,fileDownloader,contentDownloader,assetsDownloader,
        //drawableDownloader or otherSourceDownloader
        return decodingInfo.getDownloader().getStream(decodingInfo.getImageUri());
    }


    protected static class ExifInfo
    {
        public final int rotation;
        public final boolean flipHorizontal;

        protected ExifInfo()
        {
            this.rotation=0;
            this.flipHorizontal=false;
        }

        protected ExifInfo(int rotation,boolean flipHorizontal)
        {
            this.rotation=rotation;
            this.flipHorizontal=flipHorizontal;
        }
    }

    protected static class ImageFileInfo
    {
        public final ImageSize imageSize;
        private final ExifInfo exif;

        protected ImageFileInfo(ImageSize imageSize,ExifInfo info)
        {
            this.imageSize=imageSize;
            this.exif=info;
        }

    }


}
