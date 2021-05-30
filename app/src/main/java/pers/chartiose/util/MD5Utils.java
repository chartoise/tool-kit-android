package pers.chartiose.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    public static String md5(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null");
        }

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[4096];
        while (true) {
            int length = inputStream.read(buffer);
            if (length <= 0) {
                break;
            }
            messageDigest.update(buffer, 0, length);
        }

        byte[] bytes = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
        for (byte data : bytes) {
            stringBuilder.append(String.format("%02X", data & 0xFF));
        }

        return stringBuilder.toString().toUpperCase();
    }
}
