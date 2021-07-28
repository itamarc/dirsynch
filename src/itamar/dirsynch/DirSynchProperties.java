/*
 * DirSynchProperties.java
 *
 * Created on 24 de Janeiro de 2008, 13:52
 */

package itamar.dirsynch;

import itamar.util.Logger;
import static itamar.util.Logger.LEVEL_DEBUG;
import static itamar.util.Logger.LEVEL_ERROR;
import static itamar.util.Logger.LEVEL_INFO;
import static itamar.util.Logger.LEVEL_NONE;
import static itamar.util.Logger.LEVEL_WARNING;
import static itamar.util.Logger.log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * DirSynchProperties.init(pathToPropertiesFile);
 * ...
 * DirSynchProperties.get
 *
 * <p> * DirSynch.properties sample:<pre>
 * # Use hash to compare files?
 * # true = Size -> Hash -> Time
 * # false = Time -> Size
 * # Value: [true, false] (default: false)
 * hash.enabled=false
 *
 * # Only synchronize times if files have the same hash.
 * # Value: [true, false] (default: true)
 * synch.times.same.hash=true
 *
 * # Hash only small files?
 * # Value: [true, false] (default: true)
 * hash.onlysmall=true
 *
 * # If hashing only small files, what's the max size?
 * # Value: integer, size in Kb (default: 256)
 * hash.onlysmall.maxsize=256
 *
 * # Level of the log: [NONE, ERROR, WARNING, INFO, DEBUG] (default: WARNING)
 * log.level=WARNING
 *
 * # The file to receive log messages.
 * # Value: string, path to file (default: ".\DirSynch.log")
 * log.file=DirSynch.log
 *
 * # Append to the log file or nor (overwrite the old log)
 * # Value: [true, false] (default: true)
 * log.file.append=true
 *
 * # Initial main dir
 * # Value: string, path to main dir
 * maindir=[path to main dir]
 *
 * # Initial sec dir
 * # Value: string, path to sec dir
 * secdir=[path to sec dir]
 *
 * # Include all subdirs in the comparison?
 * # Value: [true, false] (default: true)
 * subdirs.include=true
 *
 * # Hide the equals files from the list?
 * # Value: [true, false] (default: true)
 * hide.equals=true
 *</pre>
 * @author Itamar Carvalho
 */
public class DirSynchProperties {
    private static boolean initied = false;
    
    // Use hash to compare files?
    // true = Size -> Hash -> Time
    // false = Time -> Size
    // Value: [true, false] (default: false)
    // hash.enabled=false
    private static boolean hashEnabled = false;

    // Only synchronize times if files have the same hash.
    // Value: [true, false] (default: true)
    //synch.times.same.hash=true 
    private static boolean synchTimesSameHash = true;
        
    // Hash only small files?
    // Value: [true, false] (default: true)
    //hash.onlysmall=true
    //private static boolean hashOnlySmall = true;
    
    // If hashing only small files, what's the max size?
    // Value: integer, size in Kb (default: 256)
    //hash.onlysmall.maxsize=256
    //private static int hashMaxSize = 256;
    
    // Level of the log: [NONE, ERROR, WARNING, INFO, DEBUG] (default: WARNING)
    //log.level=WARNING
    private static short logLevel = LEVEL_WARNING;
    
    // The file to receive log messages.
    // Value: string, path to file (default: ".\DirSynch.log")
    //log.file=DirSynch.log
    private static String logFile = "DirSynch.log";
    
    // Append to the log file or nor (overwrite the old log)
    // Value: [true, false] (default: true)
    //log.file.append=true
    private static boolean logFileAppend = true;
    
    // Initial main dir
    // Value: string, path to main dir
    //maindir=[path to main dir]
    private static String mainDir = "";
    
    // Initial sec dir
    // Value: string, path to sec dir
    //secdir=[path to sec dir]
    private static String secDir = "";
    
    // Include all subdirs in the comparison?
    // Value: [true, false] (default: true)
    //subdirs.include=true
    private static boolean subDirsInclude = true;
    
    // Hide the equals files from the list?
    // Value: [true, false] (default: true)
    //hide.equals=true
    private static boolean hideEquals = true;
    
    // NOT IMPLEMENTED YET
    // Verify if the data were correctly copied?
    // Value: [true, false] (default: false)
    //verify.enabled=false
    
    // Use the files hash to verify the copy?
    // Value: [true, false] (default: false)
    //verify.usehash=false

    // The properties object that will hold the loaded values
    private static Properties properties;

    private static String KEY_HASH_ENABLED = "hash.enabled";

    private static String KEY_SYNCH_TIMES_SAME_HASH = "synch.times.same.hash";

    private static String KEY_LOG_LEVEL = "log.level";

    private static String KEY_LOG_FILE = "log.file";

    private static String KEY_LOG_FILE_APPEND = "log.file.append";

    private static String KEY_MAINDIR = "maindir";

    private static String KEY_SECDIR = "secdir";

    private static String KEY_SUBDIRS_INCLUDE = "subdirs.include";

    private static String KEY_HIDE_EQUALS = "hide.equals";
    
    /* Static class. */
    private DirSynchProperties() {}
    
    public static void init(String propertiesFile)
    throws FileNotFoundException, IOException {
        initied = true;
        properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        
        //hash.enabled=false
        setHashEnabled(getBoolean(properties, KEY_HASH_ENABLED, false));
        
        //synch.times.same.hash=true
        setSynchTimesSameHash(getBoolean(properties, KEY_SYNCH_TIMES_SAME_HASH, true));
        
        //hash.onlysmall=true
//        hashOnlySmall = getBoolean(properties, "hash.onlysmall", true);
        
        //hash.onlysmall.maxsize=256
//        hashMaxSize = getInteger(properties, "hash.onlysmall.maxsize", 256);
        
        //log.level=WARNING
        setLogLevel(getLogLevel(properties, KEY_LOG_LEVEL, "WARNING"));
        
        //log.file=DirSynch.log
        setLogFile(getString(properties, KEY_LOG_FILE, "DirSynch.log"));
        
        //log.file.append=true
        setLogFileAppend(getBoolean(properties, KEY_LOG_FILE_APPEND, true));
        
        //maindir=[path to main dir]
        setMainDir(getString(properties, KEY_MAINDIR, ""));
        
        //secdir=[path to sec dir]
        setSecDir(getString(properties, KEY_SECDIR, ""));
        
        //subdirs.include=true
        setSubDirsInclude(getBoolean(properties, KEY_SUBDIRS_INCLUDE, true));
        
        //hide.equals=true
        setHideEquals(getBoolean(properties, KEY_HIDE_EQUALS, true));
    }
    
    private static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
        String prop = properties.getProperty(key);
        if (prop != null) {
            if (defaultValue) {
                if ("false".equals(prop)) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return Boolean.parseBoolean(prop);
            }
        } else {
            return defaultValue;
        }
    }
    
    private static String getString(Properties properties, String key, String defaultValue) {
        String prop = properties.getProperty(key);
        if (prop != null) {
            return prop;
        } else {
            return defaultValue;
        }
    }

    private static int getInteger(Properties properties, String key, int defaultValue) {
        int i = defaultValue;
        String prop = properties.getProperty(key);
        if (prop != null) {
            try {
                i = Integer.parseInt(prop);
            } catch (NumberFormatException e) {
                i = defaultValue;
                log(LEVEL_WARNING, "Error reading property '"+key+"': invalid number '"+prop+"'");
            }
        }
        return i;
    }
    
    private static short getLogLevel(Properties properties, String key, String defaultValue) {
        //log.level=WARNING
        //[NONE, ERROR, WARNING, INFO, DEBUG]
        String prop = properties.getProperty(key);
        if (prop == null) {
            prop = defaultValue;
        }
        if (null == prop) {
            return LEVEL_WARNING;
        } else switch (prop) {
            case "NONE":
                return LEVEL_NONE;
            case "DEBUG":
                return LEVEL_DEBUG;
            case "INFO":
                return LEVEL_INFO;
            case "WARNING":
                return LEVEL_WARNING;
            case "ERROR":
                return LEVEL_ERROR;
            default:
                return LEVEL_WARNING;
        }
    }
    
    public static boolean isHashEnabled() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hashEnabled;
    }

/*    public static boolean isHashOnlySmall() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hashOnlySmall;
    }*/

    /*public static int getHashMaxSize() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hashMaxSize;
    }*/

    public static short getLogLevel() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return logLevel;
    }

    public static String getLogFile() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return logFile;
    }

    public static String getMainDir() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return mainDir;
    }

    public static String getSecDir() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return secDir;
    }

    public static boolean isSubDirsInclude() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return subDirsInclude;
    }

    public static boolean isHideEquals() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hideEquals;
    }

    public static String getPropertiesAsString() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return properties.toString();
    }

    public static boolean isSynchTimesSameHash() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return synchTimesSameHash;
    }

    public static boolean isLogFileAppend() {
        if (!initied) {
            log(LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return logFileAppend;
    }
    
    public static void save(String propertiesFile)
    throws FileNotFoundException, IOException {
        properties.store(new FileOutputStream(propertiesFile), "AUTOMATICALLY GENERATED BY DirSynch");
    }
    
    public static void save(File propertiesFile)
    throws FileNotFoundException, IOException {
        properties.store(new FileOutputStream(propertiesFile, false), "AUTOMATICALLY GENERATED BY DirSynch");
    }

    public static void setHashEnabled(boolean aHashEnabled) {
        hashEnabled = aHashEnabled;
        properties.setProperty(KEY_HASH_ENABLED, Boolean.toString(aHashEnabled));
    }

    public static void setSynchTimesSameHash(boolean aSynchTimesSameHash) {
        synchTimesSameHash = aSynchTimesSameHash;
        properties.setProperty(KEY_SYNCH_TIMES_SAME_HASH, Boolean.toString(aSynchTimesSameHash));
    }

    public static void setLogLevel(short aLogLevel) {
        logLevel = aLogLevel;
        properties.setProperty(KEY_LOG_LEVEL, Logger.getLevelName(aLogLevel));
    }

    public static void setLogFile(String aLogFile) {
        logFile = aLogFile;
        properties.setProperty(KEY_LOG_FILE, aLogFile);
    }

    public static void setLogFileAppend(boolean aLogFileAppend) {
        logFileAppend = aLogFileAppend;
        properties.setProperty(KEY_LOG_FILE_APPEND, Boolean.toString(aLogFileAppend));
    }

    public static void setMainDir(String aMainDir) {
        mainDir = aMainDir;
        properties.setProperty(KEY_MAINDIR, aMainDir);
    }

    public static void setSecDir(String aSecDir) {
        secDir = aSecDir;
        properties.setProperty(KEY_SECDIR, aSecDir);
    }

    public static void setSubDirsInclude(boolean aSubDirsInclude) {
        subDirsInclude = aSubDirsInclude;
        properties.setProperty(KEY_SUBDIRS_INCLUDE, Boolean.toString(aSubDirsInclude));
    }

    public static void setHideEquals(boolean aHideEquals) {
        hideEquals = aHideEquals;
        properties.setProperty(KEY_HIDE_EQUALS, Boolean.toString(aHideEquals));
    }
}
