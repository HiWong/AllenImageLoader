package wang.imallen.blog.imageloader.cache.disk.naming.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import wang.imallen.blog.imageloader.cache.disk.naming.FileNameGenerator;

/**
 * Created by allen on 15-9-6.
 */
public class Md5FileNameGenerator implements FileNameGenerator {

    private static final String HASH_ALGORITHM="MD5";
    //10 digits+26 letters
    private static final int RADIX=10+26;

    @Override
    public String generate(String imageUri) {
        byte[]md5=getMD5(imageUri.getBytes());
        BigInteger bi=new BigInteger(md5).abs();
        return bi.toString(RADIX);
    }

    private byte[]getMD5(byte[]data)
    {
        byte[]hash=null;
        try
        {
            MessageDigest digest=MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            hash=digest.digest();
        }
        catch(NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
        }
        return hash;
    }

}
