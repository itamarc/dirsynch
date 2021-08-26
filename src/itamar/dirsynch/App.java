/*
 * Copyright (C) 2021 itamar.iac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package itamar.dirsynch;

import static itamar.dirsynch.DirSynchProperties.getPropertiesAsString;
import itamar.util.Logger;
import static itamar.util.Logger.LEVEL_DEBUG;
import static itamar.util.Logger.LEVEL_INFO;
import static itamar.util.Logger.log;
import java.awt.EventQueue;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.getDefaultUncaughtExceptionHandler;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

/**
 *
 * @author Itamar Carvalho
 */
public class App {

    public static final String VERSION = "1.7-SNAPSHOT";
    private static String defaultMainDirPath = null;
    private static String defaultSecDirPath = null;
    private static boolean defaultKeep = false;
    private static String propertiesFilePath = "DirSynch.properties";
    private static MainJFrame frame = null;
    private static DirSynchExceptionHandler handler = null;
    private static boolean firstInit = true;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("sun.awt.exception.handler",
                DirSynchExceptionHandler.class.getName());
        handler = new DirSynchExceptionHandler();
        setDefaultUncaughtExceptionHandler(handler);
        currentThread().setUncaughtExceptionHandler(handler);
        if (processParams(args)) {
            EventQueue.invokeLater(() -> {
                currentThread().setUncaughtExceptionHandler(handler);
                if (currentThread().getUncaughtExceptionHandler() != getDefaultUncaughtExceptionHandler()) {
                    System.err.println("UEH=" + currentThread().getUncaughtExceptionHandler().getClass().getName() + " DefaultUEH=" + getDefaultUncaughtExceptionHandler().getClass().getName());
                }
                initDirSynchProperties();
                frame = new MainJFrame(defaultMainDirPath, defaultSecDirPath, defaultKeep);
                frame.setVisible(true);
            });
        }
    }

    private static boolean processParams(String[] args) {
        boolean continueAfterThis = true;
        try {
            for (int i = 0; i < args.length; i++) {
                if (null == args[i]) {
                    System.out.println("Incorrect parameters!\n");
                    showUsage();
                    continueAfterThis = false;
                } else {
                    switch (args[i]) {
                        case "-main":
                            defaultMainDirPath = args[++i];
                            break;
                        case "-sec":
                            defaultSecDirPath = args[++i];
                            break;
                        case "-prop":
                            propertiesFilePath = args[++i];
                            break;
                        case "-keep":
                            defaultKeep = true;
                            break;
                        case "-help":
                        case "-h":
                        case "/?":
                        case "--help":
                            showUsage();
                            continueAfterThis = false;
                            break;
                        default:
                            System.out.println("Incorrect parameters!\n");
                            showUsage();
                            continueAfterThis = false;
                            break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Incorrect parameters!\n");
            showUsage();
            continueAfterThis = false;
        }
        return continueAfterThis;
    }

    private static void showUsage() {
        System.out.println("DirSynch " + VERSION + "\n" + (char) 184 + " 2006-2021 Itamar Carvalho <itamarc at gmail.com>\n");
        System.out.println("java[w] -jar DirSynch.jar <Params>");
        System.out.println("Params:");
        System.out.println("  -main <main dir path>           Set the main dir.");
        System.out.println("  -sec <sec dir path>             Set the secondary dir.");
        System.out.println("  -keep                           Keep backups.");
        System.out.println("  -prop <properties file path>    DirSynch.properties file path.");
        System.out.println("  --help | -help | -h | /?        Show this usage message.");
    }

    protected static void initDirSynchProperties(String propsFilePath) {
        propertiesFilePath = propsFilePath;
        initDirSynchProperties();
    }

    private static void initDirSynchProperties() {
        try {
            DirSynchProperties.init(propertiesFilePath);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(); // Logger is not initialized at this point
            frame.showWarning("File '" + propertiesFilePath + "' not found!");
        } catch (IOException ex) {
            ex.printStackTrace(); // Logger is not initialized at this point
            frame.showWarning("Error reading file '" + propertiesFilePath + "':\n" + ex.getMessage());
        }
        if (!DirSynchProperties.getLogFile().equals(Logger.getLogFile())) {
            firstInit = true;
        }
        Logger.init(DirSynchProperties.getLogLevel(), DirSynchProperties.getLogFile(),
                DirSynchProperties.isLogFileAppend());
        if (firstInit) {
            log(LEVEL_INFO, "==========  DirSynch v" + VERSION + " started.  ==========");
        }
        log(LEVEL_INFO, "Properties initialized with file '" + propertiesFilePath + "'");
        log(LEVEL_DEBUG, "Properties read: " + getPropertiesAsString());
        firstInit = false;
    }
}
