/*
 * FilePair.java
 *
 * Created on 4 de Agosto de 2006, 00:54
 */
package itamar.dirsynch;

import com.oktiva.util.FileUtil;
import itamar.util.CryptoUtil;
import itamar.util.Logger;
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
public class FilePair implements Comparable<FilePair> {
    private String path;
    private boolean inMainDir = false;
    private boolean inSecDir = false;
    private short newer = EQUALS;
    private File mainDir;
    private File secDir;
    private File mainFile;
    private File secFile;
    private boolean useHash = false;
    private String mainFileHash = null;
    private String secFileHash = null;
    
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
	@Override
    public String toString() {
        return getPath() + (isInMainDir() ? " (+," : " (-,") + (isInSecDir() ? "+)" : "-)");
    }
    
    /**
     *
     * @param obj
     * @return
     */
	@Override
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
	@Override
    public int hashCode() {
        return getPath().hashCode();
    }
    
    /**
     *
     * @param other
     * @return
     */
	@Override
    public int compareTo(FilePair other) {
        return this.getPath().compareTo(other.getPath());
    }
    
    private void verifyNewer()
    throws IOException, NoSuchAlgorithmException {
        if (mainFile == null) {
            if (secFile == null) {  // None
                // Impossible!!!
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
                        if (getMainFileHash().equals(getSecFileHash())) {
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
                    } else {
                        if (mainModif > secModif) {
                            this.newer = MAIN_NEWER;
                        } else if (mainModif < secModif) {
                            this.newer = SEC_NEWER;
                        } else { // mainModif == secModif
                            if (mainSize > secSize) {
                                this.newer = MAIN_BIGGER;
                            } else { // mainSize < secSize
                                this.newer = SEC_BIGGER;
                            }
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
     * Synchronize the files, based on their status and some options.
     * @param synchTimeHash Only synchronize the times if files are equals (have same hash).
     * @param keepBackup Keep a backup of the files to be overwritten.
     * @throws java.io.IOException
     * @throws java.security.NoSuchAlgorithmException
     */
    public void synchronize(boolean synchTimeHash, boolean keepBackup)
    throws IOException, NoSuchAlgorithmException {
        if (newer == FilePair.EQUALS) {
            // This will happen only if useHash is on: the files are equals
            // (same hash), but have different timestamps
            if (synchTimeHash && mainFile.lastModified() != secFile.lastModified()) {
                Logger.log(Logger.LEVEL_INFO, "Synching times [HashOn]: "+getPath());
		if (mainFile.lastModified() > secFile.lastModified()) {
		    if (!secFile.setLastModified(mainFile.lastModified())) {
			throw new IOException("Can't synch time: " + getPath());
		    }
		} else { // if (mainFile.lastModified() < secFile.lastModified()) {
                    if (!mainFile.setLastModified(secFile.lastModified())) {
			throw new IOException("Can't synch time: " + getPath());
		    }
                }
            } else {
                return; // Do nothing
            }
        } else if (newer == FilePair.MAIN_NEWER) {
            // If the option "synch times if same hashes" is on
            // and the files are the same (have same hash)
            if (synchTimeHash
                    && (mainFile.length() == secFile.length())
                    && (getMainFileHash().equals(getSecFileHash()))) {
                Logger.log(Logger.LEVEL_INFO, "Synching times [MN]: "+getPath());
                if (!secFile.setLastModified(mainFile.lastModified())) {
		    throw new IOException("Can't synch time: "+getPath());
		}
            } else {
                // Copy main to sec
                Logger.log(Logger.LEVEL_INFO, "Copying file [MN]: "+getPath());
                copy(mainFile, secFile, keepBackup);
            }
        } else if (newer == FilePair.ONLY_MAIN) {
            // Copy main to sec dir
            Logger.log(Logger.LEVEL_INFO, "Copying file [OM]: "+getPath());
            copy(mainFile, new File(secDir + File.separator + path), keepBackup);
        } else if (newer == FilePair.SEC_NEWER) {
            // If the option "synch times if same hashes" is on
            // and the files are the same (have same hash)
            if (synchTimeHash
                    && (mainFile.length() == secFile.length())
                    && (getMainFileHash().equals(getSecFileHash()))) {
                Logger.log(Logger.LEVEL_INFO, "Synching times [SN]: "+getPath());
                if (!mainFile.setLastModified(secFile.lastModified())) {
		    throw new IOException("Can't synch time: "+getPath());
		}
            } else {
                // Copy sec to main
                Logger.log(Logger.LEVEL_INFO, "Copying file [SN]: "+getPath());
                copy(secFile, mainFile, keepBackup);
            }
        } else if (newer == FilePair.ONLY_SEC) {
            // Copy sec to main dir
            Logger.log(Logger.LEVEL_INFO, "Copying file [OS]: "+getPath());
            copy(secFile, new File(mainDir + File.separator + path), keepBackup);
        }
        // TODO Let the user define action if newer == MAIN_BIGGER or SEC_BIGGER (Issue #14)
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
    
    public String getMainFileHash()
    throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        if (mainFileHash == null && mainFile != null) {
            mainFileHash = CryptoUtil.generateHashHex(mainFile, "MD5");
        }
//                        System.out.println(mainFile.getName()+" Main MD5: "+mainFileHash);
        return mainFileHash;
    }
    
    public String getSecFileHash()
    throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        if (secFileHash == null && secFile != null) {
            secFileHash = CryptoUtil.generateHashHex(secFile, "MD5");
//                        System.out.println(secFile.getName()+" Sec  MD5: "+secFileHash);
        }
        return secFileHash;
    }
}
