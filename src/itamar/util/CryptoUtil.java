package itamar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CryptoUtil {
    // Generates OutOfMemory for large files
    /*public static String generateHashHexTosco(File file, String algorithm)
    throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        MessageDigest md = MessageDigest.getInstance(algorithm); // SHA or MD5
        String hash = "";
        
        byte[] data = new byte[(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        
        md.update(data); // Reads it all at one go. Might be better to chunk it.
        
        byte[] digest = md.digest();
        
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(digest[i]);
            if (hex.length() == 1) hex = "0" + hex;
            hex = hex.substring(hex.length() - 2);
            hash += hex;
        }
        
        return hash;
    }*/
    
    public static String generateHashHex(File file, String algorithm)
    throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        InputStream is = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int read = 0;
        try {
            while( (read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            return bigInt.toString(16);
        } catch(IOException e) {
            throw new RuntimeException("Unable to process file for "+algorithm, e);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                throw new RuntimeException("Unable to close input stream for "+algorithm+" calculation", e);
            }
        }
    }
}
