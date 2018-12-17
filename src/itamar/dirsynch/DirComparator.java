/*
 * Class created in 01 June 2008
 */
package itamar.dirsynch;

import itamar.util.Logger;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author Itamar Carvalho
 */
public class DirComparator implements PropertyChangeListener {

    private MainJFrame mainFrame = null;
    private Map<String, File> mainDirMap;
    private Map<String, File> secDirMap;
    private File mainDir = null;
    private File secDir = null;
    private SynchMapChecker noSynchMap = null;
    private ProgressMonitor progressMonitor = null;

    public DirComparator(MainJFrame mainFrame) {
	this.mainFrame = mainFrame;
	this.mainDir = new File(mainFrame.getMainDirPath());
	this.secDir = new File(mainFrame.getSecDirPath());
    }

    void execute() {
	mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	progressMonitor = new ProgressMonitor(mainFrame, "Loading . . .", "", 0, 100);
	progressMonitor.setMillisToDecideToPopup(0);
	progressMonitor.setMillisToPopup(0);
	DirComparatorWorker worker = new DirComparatorWorker();
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

    class DirComparatorWorker extends SwingWorker<Vector<FilePair>, String> {
	private boolean enableSynchBtn = false;
	/*
	 * Main task. Executed in background thread.
	 */
	@Override
	protected Vector<FilePair> doInBackground() {
	    int step = 0;
	    final int STEPS = 6;
	    try {
		Logger.log(Logger.LEVEL_INFO, "Starting load process...");
		mainFrame.setButtonsEnabled(false, false);
		publish("Loading no synch files");
		// Step 1 - Loading no synch files
		loadNoSynchFiles();
		if (progressMonitor.isCanceled()) {
		    throw new InterruptedException();
		}
		publish("Loading main dir data");
		setProgress(++step*(100/STEPS));
		// Step 2 - Loading main dir data
		mainDirMap = new HashMap<String, File>();
		// We need to remove the trailing "\" in the case one of the dirs is the root of a drive.
		int rootPathSize = mainDir.getPath().endsWith("\\") ? mainDir.getPath().length() - 1 : mainDir.getPath().length();
		Logger.log(Logger.LEVEL_DEBUG, "Main dir path: " + mainDir.getPath() + " (" + rootPathSize + ")");
		buildMap(mainDir, mainDirMap, rootPathSize);
		if (progressMonitor.isCanceled()) {
		    throw new InterruptedException();
		}
		publish("Loading sec dir data");
		setProgress(++step*(100/STEPS));
		// Step 3 - Loading sec dir data
		secDirMap = new HashMap<String, File>();
		// We need to remove the trailing "\" in the case one of the dirs is the root of a drive.
		rootPathSize = (secDir.getPath().endsWith("\\") ? secDir.getPath().length() - 1 : secDir.getPath().length());
		Logger.log(Logger.LEVEL_DEBUG, "Sec dir path: " + secDir.getPath() + " (" + rootPathSize + ")");
		buildMap(secDir, secDirMap, rootPathSize);
		if (progressMonitor.isCanceled()) {
		    throw new InterruptedException();
		}
		publish("Comparing data");
		setProgress(++step*(100/STEPS));
		// Step 4 - Comparing data
		// Compare maps
		Vector<FilePair> files = new Vector<FilePair>(mainDirMap.size());
		// Main
		Iterator iter = mainDirMap.keySet().iterator();
		FilePair file;
		while (iter.hasNext()) {
		    if (progressMonitor.isCanceled()) {
			throw new InterruptedException();
		    }
		    file = new FilePair((String) iter.next(), mainDir, secDir);
		    file.setUseHash(mainFrame.isHashEnabled());
		    file.setMainFile((File) mainDirMap.get(file.getPath()));
		    Logger.log(Logger.LEVEL_DEBUG, "File added main: '" + file.getPath() + "'");
		    if (secDirMap.containsKey(file.getPath())) {
			file.setSecFile((File) secDirMap.get(file.getPath()));
		    }
		    files.add(file);
		}
		// Sec
		iter = secDirMap.keySet().iterator();
		while (iter.hasNext()) {
		    if (progressMonitor.isCanceled()) {
			throw new InterruptedException();
		    }
		    String path = (String) iter.next();
		    if (!mainDirMap.containsKey(path)) {
			file = new FilePair(path, mainDir, secDir);
			file.setUseHash(mainFrame.isHashEnabled());
			file.setSecFile((File) secDirMap.get(file.getPath()));
			files.add(file);
			Logger.log(Logger.LEVEL_DEBUG, "File added sec: '" + file.getPath() + "'");
		    }
		}
		// Show differences
		if (progressMonitor.isCanceled()) {
		    throw new InterruptedException();
		}
		publish("Sorting data");
		setProgress(++step*(100/STEPS));
		// Step 5 - Sorting data
		Collections.<FilePair>sort(files);
		if (progressMonitor.isCanceled()) {
		    throw new InterruptedException();
		}
		publish("Preparing to show data");
		setProgress(++step*(100/STEPS));
		// Step 6 - Preparing to show data
		mainFrame.setFilesInTable(files);
		setProgress(100); // ++step*(100/6));
		enableSynchBtn = true;
		Logger.log(Logger.LEVEL_INFO, "Load process finished successfully, "+
			files.size()+" files loaded.");
	    } catch (InterruptedException ex) {
		Logger.log(Logger.LEVEL_INFO, "Load process cancelled by user.");
		JOptionPane.showMessageDialog(mainFrame, "Loading operation CANCELED!", "Cancelled", JOptionPane.WARNING_MESSAGE);
		mainFrame.setButtonsEnabled(true, false);
	    } catch (IOException ex) {
		Logger.log(Logger.LEVEL_ERROR, "Load process failed: " + ex.getMessage());
		JOptionPane.showMessageDialog(mainFrame, ex.getClass().getName() + ": " + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		Logger.log(Logger.LEVEL_ERROR, ex);
		mainFrame.setButtonsEnabled(true, false);
	    } catch (NoSuchAlgorithmException ex) {
		Logger.log(Logger.LEVEL_ERROR, "Load process crashed: " + ex.getMessage());
		JOptionPane.showMessageDialog(mainFrame, ex.getClass().getName() + ": " + ex.getMessage(), "Weird Error!", JOptionPane.ERROR_MESSAGE);
		Logger.log(Logger.LEVEL_ERROR, ex);
		mainFrame.setButtonsEnabled(true, false);
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
	    mainFrame.setButtonsEnabled(true, enableSynchBtn);
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

	private void loadNoSynchFiles() {
	    File[] noSynchFiles = {
		new File(mainDir + File.separator + ".nosynch"),
		new File(mainDir + File.separator + "_nosynch"),
		new File(secDir + File.separator + ".nosynch"),
		new File(secDir + File.separator + "_nosynch"),
		new File(System.getProperty("user.dir") + File.separator + ".nosynch"),
		new File(System.getProperty("user.dir") + File.separator + "_nosynch")
	    };
	    noSynchMap = new SynchMapChecker();
	    noSynchMap.init(noSynchFiles);
	}

	/**
	 * Recursive method to build the map of files from some dir.
	 * @param dir The directory to be read.
	 * @param dirMap The map to be feed with the files.
	 * @param rootPathSize The number of characters of the root path string.
	 * @throws java.io.IOException If there is some problem accessing the files.
	 */
	private void buildMap(File dir, Map<String, File> dirMap, int rootPathSize)
		throws IOException {
	    Logger.log(Logger.LEVEL_DEBUG, "buildMap: " + dir.getPath() + " - " + rootPathSize);
	    File[] dirFiles = dir.listFiles();
	    Logger.log(Logger.LEVEL_DEBUG, "buildMap: " + dirFiles.length);
	    for (int i = 0; i < dirFiles.length; i++) {
		File file = dirFiles[i];
		Logger.log(Logger.LEVEL_DEBUG, "buildMap dirFiles[" + i + "]: " + file.getPath());
		if (!noSynchMap.match(file.getName())) {
		    Logger.log(Logger.LEVEL_DEBUG, "No match, verifying if it's a dir or a file...");
		    if (file.isDirectory()) {
			Logger.log(Logger.LEVEL_DEBUG, "It's a dir, checking if I'll follow it");
			if (mainFrame.isIncludeSubdirs()) {
			    Logger.log(Logger.LEVEL_DEBUG, "Going to next level with file: " + file.getPath());
			    buildMap(file, dirMap, rootPathSize);
			}
		    } else {
			Logger.log(Logger.LEVEL_DEBUG, "It's a file, putting in the map.");
			dirMap.put(file.getPath().substring(rootPathSize), file);
		    }
		}
	    }
	}
    }
}
