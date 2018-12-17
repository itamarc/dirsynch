/*
 * FilePair.java
 *
 * Created on 4 de Agosto de 2006, 00:54
 */
package itamar.dirsynch;

import com.oktiva.util.FileUtil;
import itamar.util.CryptoUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;

/**
 * This file represents a pair of files that are supposed to be compared.
 * @author Itamar
 */
public class FilePair implements Comparable {
    private String path;
    private boolean inMainDir = false;
    private boolean inSecDir = false;
    private short newer = EQUALS;
    private File mainDir;
    private File secDir;
    private File mainFile;
    private File secFile;
    private boolean useHash = false;
    private String mainFileHash;
    private String secFileHash;
    
    public static final short MAIN_BIGGER = 3;
    public static final short ONLY_MAIN = 2;
    public static final short MAIN_NEWER = 1;
    public static final short EQUALS = 0;
    public static final short SEC_NEWER = -1;
    public static final short ONLY_SEC = -2;
    public static final short SEC_BIGGER = -3;
    public static final short DIFF_HASH = 100;
    
    private static final String NEWER_SYMBOL = "New";
    private static final String EQUAL_SYMBOL = "==";
    private static final String ABSENT_SYMBOL = "no";
    private static final String OLDER_SYMBOL = "Old";
    private static final String ONLY_SYMBOL = "YES";
    private static final String BIGGER_SYMBOL = "BIG";
    private static final String SMALLER_SYMBOL = "sml";
    private static final String DIFF_HASH_SYMBOL = "<!>";
    private static final String UNKNOWN_SYMBOL = "???";
    
    /**
     * Creates a new instance of FilePair
     * @param path The relative path from the selected dir to the file.
     */
    public FilePair(String path, File mainDir, File secDir) {
        this.path = path;
        this.mainDir = mainDir;
        this.secDir = secDir;
    }
    
    /**
     *
     * @return
     */
    public String getPath() {
        return path;
    }
    
    /**
     *
     * @return
     */
    public boolean isInMainDir() {
        return inMainDir;
    }
    
    /**
     *
     * @return
     */
    public boolean isInSecDir() {
        return inSecDir;
    }
    
    /**
     *
     * @param dir
     * @return
     */
    public boolean isInDir(File dir) {
        return new File(dir.getAbsolutePath() + getPath()).isFile();
    }
    
    /**
     *
     * @return
     */
    public short getNewer() {
        return newer;
    }
    
    /**
     *
     * @return
     */
    public String getMainSymbol() {
        if (isInMainDir()) {
            if (getNewer() == EQUALS) {
                return EQUAL_SYMBOL;
            } else if (getNewer() == MAIN_NEWER) {
                return NEWER_SYMBOL;
            } else if (getNewer() == SEC_NEWER) {
                return OLDER_SYMBOL;
            } else if (getNewer() == ONLY_MAIN) {
                return ONLY_SYMBOL;
            } else if (getNewer() == MAIN_BIGGER) {
                return BIGGER_SYMBOL;
            } else if (getNewer() == SEC_BIGGER) {
                return SMALLER_SYMBOL;
            } else if (getNewer() == DIFF_HASH) {
                return DIFF_HASH_SYMBOL;
            } else {
                return UNKNOWN_SYMBOL;
            }
        } else {
            return ABSENT_SYMBOL;
        }
    }
    
    /**
     *
     * @return
     */
    public String getSecSymbol() {
        if (isInSecDir()) {
            if (getNewer() == EQUALS) {
                return EQUAL_SYMBOL;
            } else if (getNewer() == SEC_NEWER) {
                return NEWER_SYMBOL;
            } else if (getNewer() == MAIN_NEWER) {
                return OLDER_SYMBOL;
            } else if (getNewer() == ONLY_SEC) {
                return ONLY_SYMBOL;
            } else if (getNewer() == SEC_BIGGER) {
                return BIGGER_SYMBOL;
            } else if (getNewer() == MAIN_BIGGER) {
                return SMALLER_SYMBOL;
            } else if (getNewer() == DIFF_HASH) {
                return DIFF_HASH_SYMBOL;
            } else {
                return UNKNOWN_SYMBOL;
            }
        } else {
            return ABSENT_SYMBOL;
        }
    }
    
    /**
     *
     * @return
     */
    public File getMainFile() {
        return mainFile;
    }
    
    /**
     *
     * @param mainFile
     */
    public void setMainFile(File mainFile)
    throws IOException, NoSuchAlgorithmException {
        inMainDir = (mainFile != null);
        this.mainFile = mainFile;
        if (mainFile != null) {
            verifyNewer();
        }
    }
    
    /**
     *
     * @return
     */
    public File getSecFile() {
        return secFile;
    }
    
    /**
     *
     * @param secFile
     */
    public void setSecFile(File secFile)
    throws IOException, NoSuchAlgorithmException {
        inSecDir = (secFile != null);
        this.secFile = secFile;
        if (secFile != null) {
            verifyNewer();
        }
    }
    
    /**
     * Generates a string with the path and the situation about if it's in main
     * and sec dirs.
     * @return A string representation of this file.
     */
    public String toString() {
        return getPath() + (isInMainDir() ? " (+," : " (-,") + (isInSecDir() ? "+)" : "-)");
    }
    
    /**
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj instanceof FilePair) {
            return getPath().equals(((FilePair)obj).getPath());
        } else {
            return false;
        }
    }
    
    /**
     *
     * @return
     */
    public int hashCode() {
        return getPath().hashCode();
    }
    
    /**
     *
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        FilePair other = (FilePair)o;
        return this.getPath().compareTo(other.getPath());
    }
    
    private void verifyNewer()
    throws IOException, NoSuchAlgorithmException {
        if (mainFile == null) {
            if (secFile == null) {  // None
                // Impossile!!!
                throw new RuntimeException("AN IMPOSSIBLE THING HAPPENED!?!");
            } else { // Sec only
                this.newer = ONLY_SEC;
            }
        } else { // Main exists
            if (secFile == null) {  // Main only
                this.newer = ONLY_MAIN;
            } else { // Both
                long mainModif = mainFile.lastModified();
                long secModif = secFile.lastModified();
                long mainSize = mainFile.length();
                long secSize = secFile.length();
                if (useHash) { // Use hash true = Size -> Hash -> Time
                    if (mainSize == secSize) {
                        mainFileHash = CryptoUtil.generateHashHex(mainFile, "MD5");
                        secFileHash = CryptoUtil.generateHashHex(secFile, "MD5");
//                        System.out.println(mainFile.getName()+" Main MD5: "+mainFileHash);
//                        System.out.println(mainFile.getName()+" Sec  MD5: "+secFileHash);
                        if (mainFileHash.equals(secFileHash)) {
                            this.newer = EQUALS;
                        } else {
                            if (mainModif > secModif) {
                                this.newer = MAIN_NEWER;
                            } else if (mainModif < secModif) {
                                this.newer = SEC_NEWER;
                            } else { // mainModif == secModif
                                this.newer = DIFF_HASH;
                            }
                        }
                    } else if (mainSize > secSize) {
                        if (mainModif > secModif) {
                            this.newer = MAIN_NEWER;
                        } else if (mainModif < secModif) {
                            this.newer = SEC_NEWER;
                        } else { // mainModif == secModif
                            this.newer = MAIN_BIGGER;
                        }
                    } else { // mainSize < secSize
                        if (mainModif > secModif) {
                            this.newer = MAIN_NEWER;
                        } else if (mainModif < secModif) {
                            this.newer = SEC_NEWER;
                        } else { // mainModif == secModif
                            this.newer = SEC_BIGGER;
                        }
                    }
                } else { // Use hash false = Time -> Size
                    if (mainModif == secModif) {
                        if (mainSize == secSize) {
                            this.newer = EQUALS;
                        } else if (mainSize > secSize) {
                            this.newer = MAIN_BIGGER;
                        } else {
                            this.newer = SEC_BIGGER;
                        }
                    } else if (mainModif > secModif) {
                        this.newer = MAIN_NEWER;
                    } else {
                        this.newer = SEC_NEWER;
                    }
                }
            }
        }
    }
    
    /**
     *
     * @param keepBackup
     * @throws java.io.IOException
     */
    public void synchronize(boolean keepBackup)
    throws IOException {
        if (newer == FilePair.EQUALS) {
            return; // Do nothing
        } else if (newer == FilePair.MAIN_NEWER) {
            // Copy main to sec
            copy(mainFile, secFile, keepBackup);
        } else if (newer == FilePair.ONLY_MAIN) {
            // Copy main to sec dir
            copy(mainFile, new File(secDir + File.separator + path), keepBackup);
        } else if (newer == FilePair.SEC_NEWER) {
            // Copy sec to main
            copy(secFile, mainFile, keepBackup);
        } else if (newer == FilePair.ONLY_SEC) {
            // Copy sec to main dir
            copy(secFile, new File(mainDir + File.separator + path), keepBackup);
        }
        // TODO Let the user define action if newer == MAIN_BIGGER or SEC_BIGGER
        // For now, do nothing.
    }
    /**
     *
     * @param src
     * @param dest
     * @param keepBackup
     * @throws java.io.IOException
     */
    private void copy(File src, File dest, boolean keepBackup)
    throws IOException {
        if (keepBackup && dest.isFile() && dest.exists()) {
            //System.out.println("Creating backup for "+dest.getAbsolutePath());
            File backup = new File(dest + ".bak");
            //System.out.println("   Copy "+dest.getAbsolutePath()+" to "+backup.getAbsolutePath());
            FileUtil.copy(dest, backup);
        }
        //System.out.println("Copy "+src.getAbsolutePath()+" to "+dest.getAbsolutePath());
        FileUtil.copy(src, dest);
    }
    /**
     * Get a legend text explaining the symbols.
     * @return The legend text.
     */
    public static String getLegend() {
        return "Equal: '" + EQUAL_SYMBOL
                + "' Newer: '" + NEWER_SYMBOL
                + "' Older: '" + OLDER_SYMBOL
                + "' Only: '" + ONLY_SYMBOL
                + "' Absent: '" + ABSENT_SYMBOL + "'";
    }
    /**
     *
     * @return
     */
    public boolean isEquals() {
        return (newer == FilePair.EQUALS);
    }
    
    public String getStatus() {
        if (isEquals()) {
            return mainFile.getName() + " - " + getFileStatus(mainFile, false);
        } else {
            return (mainFile == null ? (secFile == null ? "[ERROR#300]" : secFile.getName()) : mainFile.getName()) + " - "
                    + (isInMainDir() ? "Main:" + getFileStatus(mainFile, true) : "")
                    + (isInMainDir() && isInSecDir() ? " | " : "")
                    + (isInSecDir() ? "Sec:" + getFileStatus(secFile, true) : "");
        }
    }
    
    private String getFileStatus(File file, boolean includeRWInfo) {
        if (file == null || !file.exists()) {
            return "[ERROR]";
        } else {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
            return (includeRWInfo ? (file.canWrite() ? " RW " : " RO " ) : " ")
            + df.format(new java.util.Date(file.lastModified()))
            + " - " + humanReadableSize(file.length());
        }
    }
    
    private String humanReadableSize(long size) {
        String hrSize = null;
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
        formatter.setMaximumFractionDigits(3);
        if (size > 1073741824) {
            hrSize = formatter.format(((double)size)/1073741824.0d) +" Gb";
        } else if (size > 1048576) {
            hrSize = formatter.format(((double)size)/1048576.0d) +" Mb";
        } else if (size > 1024) {
            hrSize = formatter.format(((double)size)/1024.0d) +" Kb";
        } else {
            hrSize = size +" bytes";
        }
        return hrSize;
    }
    
    public boolean getUseHash() {
        return useHash;
    }
    
    public void setUseHash(boolean useHash) {
        this.useHash = useHash;
    }
}
