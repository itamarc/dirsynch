/*
 */
package itamar.dirsynch;

import itamar.util.Logger;

/**
 *
 * @author Itamar Carvalho
 */
public class DirSynchExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
	Logger.log(Logger.LEVEL_ERROR, "Error on thread: "+t.getName());
	Logger.log(Logger.LEVEL_ERROR, e);
    }
    public void handle(Throwable t) {
	Logger.log(Logger.LEVEL_ERROR, "Error on Event Dispatch Thread.");
	Logger.log(Logger.LEVEL_ERROR, t);
    }
}
