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
    /**
     * Generate a hash of a file using a selectable algorithm and returns its
     * results as a hexadecimal string representation.
     * @param file The File that will have its hash calculated.
     * @param algorithm The algorithm to be used (can be MD5 and SHA-1).
     * @return The calculated hash in hexadecimal representation.
     * @throws java.security.NoSuchAlgorithmException If the algorithm isn't supported by java.security.MessageDigest.
     * @throws java.io.FileNotFoundException If the file can't be found.
     * @throws java.io.IOException If there are some error accessing the file.
     * @see java.security.MessageDigest
     */
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
