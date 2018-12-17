/*
 */
package itamar.dirsynch;

import itamar.util.Logger;

/**
 *
 * @author Itamar
 */
public class DirSynchExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
	System.err.println("SHIT 1 - uncaughtException");
	Logger.log(Logger.LEVEL_ERROR, "Error on thread: "+t.getName());
	Logger.log(Logger.LEVEL_ERROR, e);
    }
    public void handle(Throwable t) {
	System.err.println("SHIT 2 - handle hack");
	Logger.log(Logger.LEVEL_ERROR, "Error on Event Dispatch Thread.");
	Logger.log(Logger.LEVEL_ERROR, t);
    }
}
