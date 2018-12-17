/*
 * SynchMapChecker.java
 *
 * Created on 19 de Fevereiro de 2008, 22:33
 */
package itamar.dirsynch;

import com.oktiva.util.FileUtil;
import itamar.util.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
/* CURRENT:
 * synchMap = {
 *		".nosynch" => 1,
 *		"Thumbs.db" => 1
 * }
 * wildCards = [
 *		/.*~/,
 *		/.*\.bak/
 * ]
 */
/* NEW:
 * synchMapFileName = {
 *		".nosynch" => "CS",
 *		"Thumbs.db" => "CS"
 * }
 * synchMapDirName = {
 *		"temp" => "CS",
 *		"Tmp" => "CS"
 * }
 * synchMapFilePath = {
 *		"temp/build/img/logo.jpg" => "CI",
 *		"img/Thumbs.db" => "CS"
 * }
 * synchMapDirPath = {
 *		"temp/build/" => "CS",
 *		"img/" => "CS"
 * }
 * wildCardsFileName = {
 *		/ .*~ / => "CS",
 *		/ .*\.bak / => "CS"
 * }
 * wildCardsDirName = {
 *		/ New folder.* / => "CS",
 *		/ .*temp.* / => "CI"
 * }
 * wildCardsFilePath = {
 *		/ src\/.*~ / => "CS",
 *		/ build\/.*\.class / => "CS"
 * }
 * wildCardsDirPath = {
 *		/ .*\/tmp\/.* / => "CS"
 * }
 */
/*
 * Optional tags...

...for type of object:
<F> Apply to files only
<D> Apply to dirs only
<FD> Apply to files and dirs (default)

...for what to match:
<N> Match only the Name (default)
<P> Match the name including the relative Path

...for how to match:
<CI> Case insensitive
<CS> Case sensitive (default)

</> To be substitued by the "System.fileSeparator"
 */
/**
 * <p>A class to proccess .nosynch and .onlysynch files.
 * <p>This utility class reads the files, parses it and for a filename given, it
 * checks if it maps with the loaded data.
 * @author Itamar Carvalho
 */
public class SynchMapChecker {
	/**
	 * A map with the file names loaded from the config files.
	 */
    private Map<String, String> synchMap = null;
	/**
	 * A list of patterns that must be checked against the given filenames when
	 * checking if they match, loaded from the config files.
	 */
    private Vector<Pattern> wildCards = null;
    
    /** Creates a new instance of SynchMapChecker */
    public SynchMapChecker() {
    }
    
    private Map<String, String> getSynchMap() {
        if (synchMap == null) {
            synchMap = new HashMap<String, String>();
        }
        return synchMap;
    }
    
    private Vector<Pattern> getWildCards() {
        if (wildCards == null) {
            wildCards = new Vector<Pattern>();
        }
        return wildCards;
    }

	/**
	 * Loads a "synchFile" (.nosynch or .onlysynch) and put the data in the map
	 * and the list of patterns.
	 * @param synchFile The file to be read.
	 */
    private void loadSynchFile(File synchFile) {
        Map<String, String> map = getSynchMap();
        wildCards = getWildCards();
        try {
            String[] lines = FileUtil.readFileAsArray(synchFile);
            for (int i = 0; i < lines.length; i++) {
                while (lines[i].endsWith("\r") || lines[i].endsWith("\n")) {
                    lines[i] = lines[i].substring(0, lines[i].length()-1);
                }
                if (lines[i] != null && !"".equals(lines[i])) {
					// TODO Implement optional tags (Issue #2)
                    if (lines[i].startsWith("|")) {
                        wildCards.add(Pattern.compile(lines[i].substring(1), Pattern.CASE_INSENSITIVE));
                    } else {
                        map.put(lines[i], "1");
                    }
                }
            }
            Logger.log(Logger.LEVEL_INFO, "{No,Only}Synch file loaded: "+synchFile.getCanonicalPath());
            Logger.log(Logger.LEVEL_DEBUG, "Map: "+map+"  WildCards: "+wildCards);
        } catch (IOException e) {
            Logger.log(Logger.LEVEL_ERROR, "Failed to load file '"+synchFile+"'.");
            Logger.log(Logger.LEVEL_ERROR, e);
        }
    }

	/**
	 * <p>Read a set of "synchFiles" (.nosynch or .onlysynch) and keep its data in
	 * memory for future use.
	 * <p>If one of the File objects point to a dir or if the file can't be read,
	 * this method just ignore this silently, to allow that all possible paths
	 * can be passed to it.
	 * @param synchFiles An array of File objects.
	 */
    public void init(File[] synchFiles) {
        for (int i = 0; i < synchFiles.length; i++) {
            if (synchFiles[i].isFile() && synchFiles[i].canRead()) {
                loadSynchFile(synchFiles[i]);
            }
        }
    }

	/**
	 * Check if a given file matches with the parameters loaded from the
	 * "synchFiles" (.nosynch or .onlysynch).
	 * @param file A file to check.
	 * @return True if the file matches with some rule.
	 */
    public boolean match(File file) {
		String fileName = file.getName();
        Logger.log(Logger.LEVEL_DEBUG, "SynchMapChecker.match: "+fileName);
        boolean match = getSynchMap().containsKey(fileName);
        if (!match) {
            for (int i = 0; i < getWildCards().size(); i++) {
                if (((Pattern)getWildCards().get(i)).matcher(fileName).matches()){
                    match = true;
                    Logger.log(Logger.LEVEL_DEBUG, "Match ["+getWildCards().get(i).pattern()+"]: "+fileName);
                    break;
                }
            }
        }
        Logger.log(Logger.LEVEL_DEBUG, "Exiting SynchMapChecker.match: "+match);
        return match;
    }
}
