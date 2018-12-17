/*
 * DirSynchProperties.java
 *
 * Created on 24 de Janeiro de 2008, 13:52
 */

package itamar.dirsynch;

import itamar.util.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    
    // Hash only small files?
    // Value: [true, false] (default: true)
    //hash.onlysmall=true
    private static boolean hashOnlySmall = true;
    
    // If hashing only small files, what's the max size?
    // Value: integer, size in Kb (default: 256)
    //hash.onlysmall.maxsize=256
    private static int hashMaxSize = 256;
    
    // Level of the log: [NONE, ERROR, WARNING, INFO, DEBUG] (default: WARNING)
    //log.level=WARNING
    private static short logLevel = Logger.LEVEL_WARNING;
    
    // The file to receive log messages.
    // Value: string, path to file (default: ".\DirSynch.log")
    //log.file=DirSynch.log
    private static String logFile = "DirSynch.log";
    
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
    private static Properties properties;
    
    /* Static class. */
    private DirSynchProperties() {}
    
    public static void init(String propertiesFile)
    throws FileNotFoundException, IOException {
        initied = true;
        properties = new Properties();
        properties.load(new FileInputStream(propertiesFile));
        
        //hash.enabled=false
        hashEnabled = getBoolean(properties, "hash.enabled", false);
        
        //hash.onlysmall=true
        hashOnlySmall = getBoolean(properties, "hash.onlysmall", true);
        
        //hash.onlysmall.maxsize=256
        hashMaxSize = getInteger(properties, "hash.onlysmall.maxsize", 256);
        
        //log.level=WARNING
        logLevel = getLogLevel(properties, "log.level", "WARNING");
        
        //log.file=DirSynch.log
        logFile = getString(properties, "log.file", "DirSynch.log");
        
        //maindir=[path to main dir]
        mainDir = getString(properties, "maindir", "");
        
        //secdir=[path to sec dir]
        secDir = getString(properties, "secdir", "");
        
        //subdirs.include=true
        subDirsInclude = getBoolean(properties, "subdirs.include", true);
        
        //hide.equals=true
        hideEquals = getBoolean(properties, "hide.equals", true);
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
                Logger.log(Logger.LEVEL_WARNING, "Error reading property '"+key+"': invalid number '"+prop+"'");
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
        if ("NONE".equals(prop)) {
            return Logger.LEVEL_NONE;
        } else if ("DEBUG".equals(prop)) {
            return Logger.LEVEL_DEBUG;
        } else if ("INFO".equals(prop)) {
            return Logger.LEVEL_INFO;
        } else if ("WARNING".equals(prop)) {
            return Logger.LEVEL_WARNING;
        } else if ("ERROR".equals(prop)) {
            return Logger.LEVEL_ERROR;
        } else {
            return Logger.LEVEL_WARNING;
        }
    }
    
    public static boolean isHashEnabled() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hashEnabled;
    }

    public static boolean isHashOnlySmall() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hashOnlySmall;
    }

    public static int getHashMaxSize() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hashMaxSize;
    }

    public static short getLogLevel() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return logLevel;
    }

    public static String getLogFile() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return logFile;
    }

    public static String getMainDir() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return mainDir;
    }

    public static String getSecDir() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return secDir;
    }

    public static boolean isSubDirsInclude() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return subDirsInclude;
    }

    public static boolean isHideEquals() {
        if (!initied) {
            Logger.log(Logger.LEVEL_WARNING, "DirSynchProperties not initialized!");
        }
        return hideEquals;
    }

    public static String getPropertiesAsString() {
        return properties.toString();
    }
}
