/*
 */
package itamar.dirsynch;

import itamar.util.Logger;
import static itamar.util.Logger.LEVEL_ERROR;
import static itamar.util.Logger.log;
import static itamar.util.Logger.log;

/**
 *
 * @author Itamar Carvalho
 */
public class DirSynchExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
	log(LEVEL_ERROR, "Error on thread: "+t.getName());
	log(LEVEL_ERROR, e);
    }
    public void handle(Throwable t) {
	log(LEVEL_ERROR, "Error on Event Dispatch Thread.");
	log(LEVEL_ERROR, t);
    }
}
