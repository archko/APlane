package com.me.microblog.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: archko Date: 12-12-25 Time: 下午2:31
 */
public class Md5Digest {

    public MessageDigest mDigest;
    private static Md5Digest sInstance;

    public Md5Digest() {
    }

    public static Md5Digest getInstance() {
        if (null == sInstance) {
            sInstance = new Md5Digest();
        }
        return sInstance;
    }

    public String getMd5(String content) {
        if (null == mDigest) {
            try {
                mDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                // This shouldn't happen.
                throw new RuntimeException("No MD5 algorithm.");
            }
        }
        mDigest.update(content.getBytes());

        return getHashString(mDigest);
    }

    public String getHashString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }

        return builder.toString();
    }
}
