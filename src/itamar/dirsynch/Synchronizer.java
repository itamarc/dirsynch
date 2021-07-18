/*
 * Class created in 01 June 2008
 */
package itamar.dirsynch;

import static itamar.util.Logger.LEVEL_ERROR;
import static itamar.util.Logger.LEVEL_INFO;
import static itamar.util.Logger.log;
import static java.awt.Cursor.WAIT_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import static java.lang.Thread.currentThread;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
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
	mainFrame.setCursor(getPredefinedCursor(WAIT_CURSOR));
	synchTimesSameHash = mainFrame.isSynchTimesSameHash();
	keepBackup = mainFrame.isKeepBackup();
	log(LEVEL_INFO, "Starting synchronization process...");
	mainFrame.setButtonsEnabled(false, false);
	selFiles = mainFrame.getSelectedFiles();
	log(LEVEL_INFO, "Synchronizing "+selFiles.size()+" files.");
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
	private int finalMsgType = INFORMATION_MESSAGE;
	/*
	 * Main task. Executed in background thread.
	 */

	@Override
	protected Void doInBackground() {
	    currentThread().setUncaughtExceptionHandler(
		    new DirSynchExceptionHandler());
	    FilePair filePair = null;
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
		    log(LEVEL_INFO, "Synchronization process cancelled by user.");
		    finalMsg = "Synchronization CANCELED!";
		    finalTitle = "Cancelled";
		    finalMsgType = WARNING_MESSAGE;
		} else {
		    log(LEVEL_INFO, "Synchronization process finished successfully.");
		    finalMsg = "Synchronization completed!";
		    finalTitle = "Success";
		    finalMsgType = INFORMATION_MESSAGE;
		}
	    } catch (IOException ex) {
		finalMsg = "Failure in synchronization process!";
		finalTitle = "Error";
		finalMsgType = ERROR_MESSAGE;
		log(LEVEL_ERROR, "Error processing file: " + filePair);
		log(LEVEL_ERROR, ex);
	    } catch (NoSuchAlgorithmException ex) {
		finalMsg = "Failure in synchronization process!";
		finalTitle = "Weird Error";
		finalMsgType = ERROR_MESSAGE;
		log(LEVEL_ERROR, "Weird Error processing file: " + filePair);
		log(LEVEL_ERROR, ex);
	    } catch (RuntimeException ex) {
		finalMsg = "Failure in synchronization process!";
		finalTitle = "Unknown Error";
		finalMsgType = ERROR_MESSAGE;
		log(LEVEL_ERROR, "Error processing file: " + filePair);
		log(LEVEL_ERROR, ex);
	    } catch (Throwable t) {
		finalMsg = "Failure in synchronization process!";
		finalTitle = "Unknown Throwable";
		finalMsgType = ERROR_MESSAGE;
		log(LEVEL_ERROR, "Error processing file: " + filePair);
		log(LEVEL_ERROR, t);
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
	    showMessageDialog(mainFrame, finalMsg, finalTitle,
		    finalMsgType);
	    mainFrame.load();
	    mainFrame.setButtonsEnabled(true, true);
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
