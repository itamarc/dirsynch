/*
 * FilePair.java
 *
 * Created on 4 de Agosto de 2006, 00:54
 */
package itamar.dirsynch;

import com.oktiva.util.FileUtil;
import java.io.File;
import java.io.IOException;

/**
 *
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
    
    public static final short ONLY_MAIN = 2;
    public static final short MAIN_NEWER = 1;
    public static final short EQUALS = 0;
    public static final short SEC_NEWER = -1;
    public static final short ONLY_SEC = -2;
    
    private static final String NEWER_SYMBOL = "New";
    private static final String EQUAL_SYMBOL = "==";
    private static final String ABSENT_SYMBOL = "no";
    private static final String OLDER_SYMBOL = "Old";
    private static final String ONLY_SYMBOL = "YES";
    
    /**
     * Creates a new instance of FilePair
     * @param path The relative path from the selected dir to the file.
     */
    public FilePair(String path, File mainDir, File secDir) {
	this.path = path;
	this.mainDir = mainDir;
	this.secDir = secDir;
    }
    
    public String getPath() {
	return path;
    }
    
    public boolean isInMainDir() {
	return inMainDir;
    }
    
    public boolean isInSecDir() {
	return inSecDir;
    }
    
    public boolean isInDir(File dir) {
	return new File(dir.getAbsolutePath() + getPath()).isFile();
    }
    
    public short getNewer() {
	return newer;
    }
    
    public String getMainSymbol() {
	if (isInMainDir()) {
	    if (getNewer() == EQUALS) {
		return EQUAL_SYMBOL;
	    } else if (getNewer() == MAIN_NEWER) {
		return NEWER_SYMBOL;
	    } else if (getNewer() == ONLY_MAIN) {
		return ONLY_SYMBOL;
	    } else {
		return OLDER_SYMBOL;
	    }
	} else {
	    return ABSENT_SYMBOL;
	}
    }
    
    public String getSecSymbol() {
	if (isInSecDir()) {
	    if (getNewer() == EQUALS) {
		return EQUAL_SYMBOL;
	    } else if (getNewer() == SEC_NEWER) {
		return NEWER_SYMBOL;
	    } else if (getNewer() == ONLY_SEC) {
		return ONLY_SYMBOL;
	    } else {
		return OLDER_SYMBOL;
	    }
	} else {
	    return ABSENT_SYMBOL;
	}
    }
    
    public File getMainFile() {
	return mainFile;
    }
    
    public void setMainFile(File mainFile) {
	inMainDir = (mainFile != null);
	this.mainFile = mainFile;
	if (mainFile != null) {
	    verifyNewer();
	}
    }
    
    public File getSecFile() {
	return secFile;
    }
    
    public void setSecFile(File secFile) {
	inSecDir = (secFile != null);
	this.secFile = secFile;
	if (secFile != null) {
	    verifyNewer();
	}
    }
    
    public String toString() {
	return getPath() + (isInMainDir() ? " (+," : " (-,") + (isInSecDir() ? "+)" : "-)");
    }
    
    public boolean equals(Object obj) {
	if (obj instanceof FilePair) {
	    return getPath().equals(((FilePair)obj).getPath());
	} else {
	    return false;
	}
    }
    
    public int hashCode() {
	return getPath().hashCode();
    }
    
    public int compareTo(Object o) {
	FilePair other = (FilePair)o;
	return this.getPath().compareTo(other.getPath());
    }
    
    private void verifyNewer() {
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
		long secModif = secFile.lastModified();
		long mainModif = mainFile.lastModified();
		if (mainModif == secModif) {
		    this.newer = EQUALS;
		} else if (mainModif > secModif) {
		    this.newer = MAIN_NEWER;
		} else {
		    this.newer = SEC_NEWER;
		}
	    }
	}
    }
    
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
    }
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
    public static String getLegend() {
	return "Equal: '" + EQUAL_SYMBOL
		+ "' Newer: '" + NEWER_SYMBOL
		+ "' Older: '" + OLDER_SYMBOL
		+ "' Only: '" + ONLY_SYMBOL
		+ "' Absent: '" + ABSENT_SYMBOL + "'";
    }
    public boolean isEquals() {
	return (newer == FilePair.EQUALS);
    }
}
