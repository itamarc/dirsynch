/*
 * Class created in 01 June 2008
 */
package itamar.dirsynch;

import itamar.util.Logger;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author Itamar Carvalho
 */
public class Synchronizer implements PropertyChangeListener {

    private MainJFrame mainFrame = null;
    private ProgressMonitor progressMonitor = null;
    private Vector<FilePair> selFiles = null;
    private boolean synchTimesSameHash;
    private boolean keepBackup;

    public Synchronizer(MainJFrame mainFrame) {
	this.mainFrame = mainFrame;
    }

    void execute() {
	mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	synchTimesSameHash = mainFrame.isSynchTimesSameHash();
	keepBackup = mainFrame.isKeepBackup();
	Logger.log(Logger.LEVEL_INFO, "Starting synchronization process...");
	mainFrame.setButtonsEnabled(false, false);
	selFiles = mainFrame.getSelectedFiles();
	Logger.log(Logger.LEVEL_INFO, "Synchronizing "+selFiles.size()+" files.");
	progressMonitor = new ProgressMonitor(mainFrame,
		"Synchronizing . . .",
		"", 0, selFiles.size());
	progressMonitor.setMillisToDecideToPopup(0);
	progressMonitor.setMillisToPopup(0);
	SynchronizerWorker worker = new SynchronizerWorker();
	worker.addPropertyChangeListener(this);
	worker.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
	if ("progress".equals(evt.getPropertyName()))  {
	    int progress = (Integer) evt.getNewValue();
	    progressMonitor.setProgress(progress);
	}
    }

    class SynchronizerWorker extends SwingWorker<Void, String> {

	private String finalMsg = "Synchronization completed!";
	private String finalTitle = "Success";
	private int finalMsgType = JOptionPane.INFORMATION_MESSAGE;
	/*
	 * Main task. Executed in background thread.
	 */

	@Override
	protected Void doInBackground() {
	    Thread.currentThread().setUncaughtExceptionHandler(
		    new DirSynchExceptionHandler());
	    FilePair filePair = null;
	    if (filePair == null) {// START OF DAMN TESTING CODE
		Logger.log(Logger.LEVEL_ERROR, "Throwing damn RuntimeException!");
		Logger.log(Logger.LEVEL_ERROR, "Thread="+Thread.currentThread().getName());
		Logger.log(Logger.LEVEL_ERROR, "UEH="+Thread.currentThread().getUncaughtExceptionHandler().getClass().getName());
		Logger.log(Logger.LEVEL_ERROR, "sun.awt.exception.handler="+System.getProperty("sun.awt.exception.handler"));
		throw new RuntimeException("Testando uncaught exception handler.");
	    }// END OF DAMN TESTING CODE
	    try {
		for (int i = 0; i < selFiles.size(); i++) {
		    if (progressMonitor.isCanceled()) {
			cancel(false);
			break;
		    }
		    filePair = selFiles.get(i);
		    publish(filePair.getPath());
		    filePair.synchronize(synchTimesSameHash, keepBackup);
		    setProgress((i+1)*(100/selFiles.size()));
		}
		if (isCancelled()) {
		    Logger.log(Logger.LEVEL_INFO, "Synchronization process cancelled by user.");
		    finalMsg = "Synchronization CANCELED!";
		    finalTitle = "Cancelled";
		    finalMsgType = JOptionPane.WARNING_MESSAGE;
		} else {
		    Logger.log(Logger.LEVEL_INFO, "Synchronization process finished successfully.");
		    finalMsg = "Synchronization completed!";
		    finalTitle = "Success";
		    finalMsgType = JOptionPane.INFORMATION_MESSAGE;
		}
	    } catch (IOException ex) {
		finalMsg = "Failure in synchronization process!";
		finalTitle = "Error";
		finalMsgType = JOptionPane.ERROR_MESSAGE;
		Logger.log(Logger.LEVEL_ERROR, "Error processing file: " + filePair);
		Logger.log(Logger.LEVEL_ERROR, ex);
	    } catch (NoSuchAlgorithmException ex) {
		finalMsg = "Failure in synchronization process!";
		finalTitle = "Weird Error";
		finalMsgType = JOptionPane.ERROR_MESSAGE;
		Logger.log(Logger.LEVEL_ERROR, "Weird Error processing file: " + filePair);
		Logger.log(Logger.LEVEL_ERROR, ex);
	    }
	    return null;
	}
	/*
	 * Executed in event dispatching thread
	 */

	@Override
	public void done() {
	    progressMonitor.close();
	    mainFrame.setCursor(null);
	    JOptionPane.showMessageDialog(mainFrame, finalMsg, finalTitle,
		    finalMsgType);
	    mainFrame.load();
	    mainFrame.setButtonsEnabled(true, true);
		throw new RuntimeException("Testando uncaught exception handler 2.");
	}

	/*
	 * Process the objects from method "publish" in the event dispatching thread
	 */
	@Override
	protected void process(List<String> notes) {
	    for (String note : notes) {
		progressMonitor.setNote(note);
	    }
	}
    }
}
