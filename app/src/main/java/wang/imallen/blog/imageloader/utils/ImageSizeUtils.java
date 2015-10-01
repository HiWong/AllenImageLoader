package wang.imallen.blog.imageloader.utils;

import android.opengl.GLES10;

import javax.microedition.khronos.opengles.GL10;

import wang.imallen.blog.imageloader.assist.ImageSize;
import wang.imallen.blog.imageloader.constant.ViewScaleType;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Created by allen on 15-9-13.
 */
public final class ImageSizeUtils
{
    private static final String TAG=ImageSizeUtils.class.getSimpleName();

    private static final int DEFAULT_MAX_BITMAP_DIMENSION=2048;


    private static ImageSize maxBitmapSize;

    static
    {
        int[]maxTextureSize=new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE,maxTextureSize,0);
        int maxBitmapDimension=Math.max(maxTextureSize[0],DEFAULT_MAX_BITMAP_DIMENSION);
        maxBitmapSize=new ImageSize(maxBitmapDimension,maxBitmapDimension);
    }

    private ImageSizeUtils()
    {

    }

    public static ImageSize defineTargetSizeForView(ViewWrapper viewWrapper,ImageSize maxImageSize)
    {
       int width=viewWrapper.getWidth();
        if(width<=0)
        {
            width=maxImageSize.getWidth();
        }
        int height=viewWrapper.getHeight();
        if(height<=0)
        {
            height=maxImageSize.getHeight();
        }
        return new ImageSize(width,height);

    }

    public static int computeMinImageSampleSize(ImageSize srcSize)
    {
        final int srcWidth=srcSize.getWidth();
        final int srcHeight=srcSize.getHeight();
        final int targetWidth=maxBitmapSize.getWidth();
        final int targetHeight=maxBitmapSize.getHeight();

        final int widthScale=(int)Math.ceil((float)srcWidth/targetWidth);
        final int heightScale=(int)Math.ceil((float)srcHeight/targetHeight);

        return Math.max(widthScale,heightScale);

    }

    public static int computeImageSampleSize(ImageSize srcSize,ImageSize targetSize,
                                             ViewScaleType viewScaleType,boolean powerOf2Scale)
    {
        final int srcWidth=srcSize.getWidth();
        final int srcHeight=srcSize.getHeight();
        final int targetWidth=targetSize.getWidth();
        final int targetHeight=targetSize.getHeight();

        int scale=1;

        switch(viewScaleType)
        {
            case FIT_INSIDE:
                if (powerOf2Scale)
                {
                    final int halfWidth = srcWidth / 2;
                    final int halfHeight = srcHeight / 2;
                    while ((halfWidth / scale) > targetWidth || (halfHeight / scale) > targetHeight)
                    { // ||
                        scale *= 2;
                    }
                }
                else
                {
                    scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight); // max
                }
                break;
            case CROP:
                if (powerOf2Scale)
                {
                    final int halfWidth = srcWidth / 2;
                    final int halfHeight = srcHeight / 2;
                    while ((halfWidth / scale) > targetWidth && (halfHeight / scale) > targetHeight)
                    { // &&
                        scale *= 2;
                    }
                } else
                {
                    scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight); // min
                }
                break;
        }

        if(scale<1)
        {
            scale=1;
        }
        scale=considerMaxTextureSize(srcWidth,srcHeight,scale,powerOf2Scale);
        return scale;
    }

    private static int considerMaxTextureSize(int srcWidth,int srcHeight,
                                              int scale,boolean powerOf2)
    {
        final int maxWidth=maxBitmapSize.getWidth();
        final int maxHeight=maxBitmapSize.getHeight();
        while ((srcWidth / scale) > maxWidth || (srcHeight / scale) > maxHeight)
        {
            if (powerOf2)
            {
                scale *= 2;
            }
            else
            {
                scale++;
            }
        }
        return scale;
    }

    public static float computeImageScale(ImageSize srcSize,ImageSize targetSize,ViewScaleType viewScaleType,
                                          boolean stretch)
    {
        final int srcWidth=srcSize.getWidth();
        final int srcHeight=srcSize.getHeight();
        final int targetWidth=targetSize.getWidth();
        final int targetHeight=targetSize.getHeight();

        final float widthScale=(float)srcWidth/targetWidth;
        final float heightScale=(float)srcHeight/targetHeight;

        final int destWidth;
        final int destHeight;
        if((viewScaleType==ViewScaleType.FIT_INSIDE&&widthScale>=heightScale)
                ||(viewScaleType==ViewScaleType.CROP&&widthScale<heightScale))
        {
            destWidth=targetWidth;
            destHeight=(int)(srcHeight/widthScale);
        }
        else
        {
            destWidth=(int)(srcWidth/heightScale);
            destHeight=targetHeight;
        }

        float scale=1;
        if((!stretch&&destWidth<srcWidth&&destHeight<srcHeight)
                ||(stretch&&destWidth!=srcWidth&&destHeight!=srcHeight))
        {
            scale=(float)destWidth/srcWidth;
        }

        return scale;

    }

}
