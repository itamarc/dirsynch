/*
 * Logger.java
 *
 * Created on 22 de Janeiro de 2008, 17:58
 */
package itamar.util;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

/**
 * Very simple logger class.<p>
 * Log levels: NONE, ERROR, WARNING, INFO and DEBUG.<p>
 * Example:
 * <code><pre>
 * Logger.init(Logger.LEVEL_INFO, "c:\temp\MyLogFile.txt", true);
 * Logger.log(Logger.LEVEL_WARNING, exception);
 * Logger.log(Logger.LEVEL_INFO, "Something has happened.");
 * </pre></code>
 * @author Itamar Carvalho
 */
public class Logger {
    public static final short LEVEL_NONE = 0;
    public static final short LEVEL_ERROR = 1;
    public static final short LEVEL_WARNING = 2;
    public static final short LEVEL_INFO = 3;
    public static final short LEVEL_DEBUG = 4;
    private static short level = -1;
    private static String logFile = null;

    /** Logger isn't supposed to instantiate. */
    private Logger() {}
    
    /**
     * Initiates the Logger, pointing the <tt>System.err</tt> to the log file.
     * @param level Level to operate. Only messages with level smaller or equals to this will be printed.
     * @param logFile The log file to be used.
     * @param append If true, the log file will be appended, if false, overwrited.
     */
    public static void init(short level, String logFile, boolean append) {
        Logger.level = level;
        Logger.logFile = logFile;
        try {
            System.setErr(
                    new PrintStream(
                    new BufferedOutputStream(
                    new FileOutputStream(logFile, append))));
        } catch (FileNotFoundException ex) {
            System.err.println(new Date().toString());
            ex.printStackTrace(System.err);
            System.err.flush();
        }
        log(LEVEL_INFO, "Logger initialized level '"+getLevelName(level)+"' with file '"+logFile+"'");
    }
    
    public static void log(short msgLevel, String message) {
        if (msgLevel <= level) {
            System.err.println(new Date().toString() + " [" + getLevelName(msgLevel) + "] " + message);
            System.err.flush();
        } else if (level < 0) {
            System.err.println("Logger not initialized!");
            new Exception().printStackTrace(System.err);
            System.err.flush();
        }
    }
    
    public static void log(short msgLevel, Throwable ex) {
        if (msgLevel <= level) {
            System.err.println(new Date().toString() + " [" + getLevelName(msgLevel) + "] ");
            ex.printStackTrace(System.err);
            System.err.flush();
        } else if (level < 0) {
            System.err.println("Logger not initialized!");
            new Exception().printStackTrace(System.err);
            System.err.flush();
        }
    }
    
    public static String getLevelName(short level) {
        switch (level) {
            case LEVEL_DEBUG:
                return "DEBUG";
            case LEVEL_INFO:
                return "INFO";
            case LEVEL_WARNING:
                return "WARNING";
            case LEVEL_ERROR:
                return "ERROR";
            case LEVEL_NONE:
                return "NONE";
            default:
                return "Unknown";
        }
    }

    /**
     * The log file name (with or without path).
     * @return The log file.
     */
    public static String getLogFile() {
	return logFile;
    }
}
