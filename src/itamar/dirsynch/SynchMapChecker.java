/*
 * SynchMapChecker.java
 *
 * Created on 19 de Fevereiro de 2008, 22:33
 */
package itamar.dirsynch;

import com.oktiva.util.FileUtil;
import static itamar.util.Logger.LEVEL_DEBUG;
import static itamar.util.Logger.LEVEL_ERROR;
import static itamar.util.Logger.LEVEL_INFO;
import static itamar.util.Logger.log;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 * The value of the map is a bitmask of the options:
...for type of object:
 * Mask: 11 (2^0 + 2^1)
 * 01 <F> Apply to files only
 * 10 <D> Apply to dirs only
 * 00 <FD> Apply to files and dirs (default)
...for what to match:
 * Mask: 100 (2^2)
 * 0 <N> Match only the Name (default)
 * 1 <P> Match the name including the relative Path
...for how to match:
 * Mask: 1000 (2^3)
 * 0 <CI> Case insensitive (default)
 * 1 <CS> Case sensitive
 *
 * </> To be substitued by the "System.fileSeparator"
 */
/**
 * <p>A class to proccess .nosynch and .onlysynch files.
 * <p>This utility class reads the files, parses it and for a filename given, it
 * checks if it matches with the loaded data.
 * @author Itamar Carvalho
 */
public class SynchMapChecker {
	private static final int OBJECT_TYPE_MAP = 3;           // binary 11 (2^0 + 2^1)
	private static final int FILES_DIRS_OPTION = 0;
	private static final int FILES_ONLY_OPTION = 1;
	private static final int DIRS_ONLY_OPTION = 2;
	private static final int PARTIAL_MATCH_MAP = 4;         // binary 100 (2^2)
	private static final int FILE_NAME_OPTION = 0;
	private static final int RELATIVE_PATH_OPTION = 4;
	private static final int CASE_SENSITIVITY_MAP = 8;      // binary 1000 (2^3)
	private static final int CASE_INSENSITIVE_OPTION = 0;
	private static final int CASE_SENSITIVE_OPTION = 8;
	/**
	 * A map with the file names loaded from the nosynch files.
	 */
	private Map<String, Integer> noSynchMap = null;
	/**
	 * A map with the file names loaded from the onlysynch files.
	 */
	private Map<String, Integer> onlySynchMap = null;
	/**
	 * A list of patterns that must be checked against the given filenames when
	 * checking if they match, loaded from the nosynch files.
	 */
	private Map<Pattern, Integer> noSynchWildCards = null;
	/**
	 * A list of patterns that must be checked against the given filenames when
	 * checking if they match, loaded from the onlysynch files.
	 */
	private Map<Pattern, Integer> onlySynchWildCards = null;
    
	/** Creates a new instance of SynchMapChecker
	 * <p>Read a set of "synchFiles" (.nosynch or .onlysynch) and keep its data in
	 * memory for future use.
	 * <p>If one of the File objects point to a dir or if the file can't be read,
	 * this method just ignore this silently, to allow that all possible paths
	 * can be passed to it.
	 * @param noSynchFiles An array of File objects.
	 * @param onlySynchFiles An array of File objects.
	 */
    public SynchMapChecker(File[] noSynchFiles, File[] onlySynchFiles) {
	    init(noSynchFiles, onlySynchFiles);
    }
    
    private Map<String, Integer> getNoSynchMap() {
        if (noSynchMap == null) {
            noSynchMap = new HashMap<>();
        }
        return noSynchMap;
    }

    private Map<String, Integer> getOnlySynchMap() {
        if (onlySynchMap == null) {
            onlySynchMap = new HashMap<>();
        }
        return onlySynchMap;
    }
    
    private Map<Pattern, Integer> getWildCardsNoSynch() {
        if (noSynchWildCards == null) {
            noSynchWildCards = new HashMap<>();
        }
        return noSynchWildCards;
    }

    private Map<Pattern, Integer> getWildCardsOnlySynch() {
        if (onlySynchWildCards == null) {
            onlySynchWildCards = new HashMap<>();
        }
        return onlySynchWildCards;
    }

	/**
	 * Loads a "synchFile" (.nosynch or .onlysynch) and put the data in the maps.
	 * @param synchFile The file to be read.
	 */
    private void loadSynchFile(File synchFile, Map<String, Integer> map,
	    Map<Pattern, Integer> wildCards) {
        try {
		log(LEVEL_DEBUG, "Loading {No,Only}Synch file: "+synchFile.getCanonicalPath());
            String[] lines = FileUtil.readFileAsArray(synchFile);
            for (int i = 0; i < lines.length; i++) {
                while (lines[i].endsWith("\r") || lines[i].endsWith("\n")) {
                    lines[i] = lines[i].substring(0, lines[i].length()-1);
                }
                if (lines[i] != null && !"".equals(lines[i])) {
			lines[i] = lines[i].replaceAll("</>", Matcher.quoteReplacement("\\"+File.separator));
			int options = parseOptionalTags(lines, i);
			if (lines[i].startsWith("|")) {
				wildCards.put(((options & CASE_SENSITIVITY_MAP) == CASE_INSENSITIVE_OPTION ?
					Pattern.compile(lines[i].substring(1), Pattern.CASE_INSENSITIVE) :
					Pattern.compile(lines[i].substring(1))), options);
			} else {
				map.put(lines[i], options);
			}
			log(LEVEL_DEBUG,
				"options="+Integer.toBinaryString(options)+" & CS_MAP="+
				Integer.toBinaryString(options & CASE_SENSITIVITY_MAP)+
				" & OT_MAP="+Integer.toBinaryString(options & OBJECT_TYPE_MAP)+
				" & PM_MAP"+Integer.toBinaryString(options & PARTIAL_MATCH_MAP));
                }
            }
            log(LEVEL_INFO, "{No,Only}Synch file loaded: "+synchFile.getCanonicalPath());
            log(LEVEL_DEBUG, "Map: "+map+"  WildCards: "+wildCards);
        } catch (IOException e) {
            log(LEVEL_ERROR, "Failed to load file '"+synchFile+"'.");
            log(LEVEL_ERROR, e);
        }
    }

	/**
	 * <p>Read a set of "synchFiles" (.nosynch or .onlysynch) and keep its data in
	 * memory for future use.
	 * <p>If one of the File objects point to a dir or if the file can't be read,
	 * this method just ignore this silently, to allow that all possible paths
	 * can be passed to it.
	 * @param noSynchFiles An array of File objects.
	 * @param onlySynchFiles An array of File objects.
	 */
	private void init(File[] noSynchFiles, File[] onlySynchFiles) {
            for (File noSynchFile : noSynchFiles) {
                if (noSynchFile.isFile() && noSynchFile.canRead()) {
                    loadSynchFile(noSynchFile, getNoSynchMap(), getWildCardsNoSynch());
                }
            }
            for (File onlySynchFile : onlySynchFiles) {
                if (onlySynchFile.isFile() && onlySynchFile.canRead()) {
                    loadSynchFile(onlySynchFile, getOnlySynchMap(), getWildCardsOnlySynch());
                }
            }
	}

	/**
	 * Check if a given file is blocked according to the rules of .nosynch
	 * or .onlysynch files.
	 * @param file A file to check.
	 * @param rootPathSize The size of the root path.
	 * @return True if the file is blocked by some rule.
	 */
	public boolean isBlocked(File file, int rootPathSize) {
		boolean blocked;
		// By default, there is no onlysynch restriction
		boolean only = true;
		// If there are any onlysynch restriction
		if (!getOnlySynchMap().isEmpty() || !getWildCardsOnlySynch().isEmpty()) {
			// Check if the file is on the onlysynch list
			only = match(file, rootPathSize, getOnlySynchMap(), getWildCardsOnlySynch());
			log(LEVEL_DEBUG, "Checked onlysynch rules (" + file.getName() + "): " + only);
		}
		// If there is no onlysynch restriction, test for nosynch ones
		if (only) {
			blocked = match(file, rootPathSize, getNoSynchMap(), getWildCardsNoSynch());
		} else {
			blocked = true;
			log(LEVEL_DEBUG, "File blocked by onlysynch rule.");
		}
		log(LEVEL_DEBUG, "Exiting SynchMapChecker.isBlocked: " + blocked);
		return blocked;
	}

	private boolean match(File file, int rootPathSize,
		Map<String, Integer> map, Map<Pattern, Integer> wildCardsMap) {
		String fileName = file.getName();
		String relativePath = file.getPath().substring(rootPathSize);
		log(LEVEL_DEBUG, "SynchMapChecker.match: " + fileName);
		int options;
		boolean match = false;
		if (map.containsKey(fileName)) {
			options = map.get(fileName);
			if (((options & OBJECT_TYPE_MAP) == FILES_DIRS_OPTION) ||
				(((options & OBJECT_TYPE_MAP) == FILES_ONLY_OPTION) && file.isFile()) ||
				(((options & OBJECT_TYPE_MAP) == DIRS_ONLY_OPTION) && file.isDirectory())) {
				match = ((options & PARTIAL_MATCH_MAP) == FILE_NAME_OPTION);
			}
		}
		if (map.containsKey(relativePath)) {
			options = map.get(relativePath);
			if (((options & OBJECT_TYPE_MAP) == FILES_DIRS_OPTION) ||
				(((options & OBJECT_TYPE_MAP) == FILES_ONLY_OPTION) && file.isFile()) ||
				(((options & OBJECT_TYPE_MAP) == DIRS_ONLY_OPTION) && file.isDirectory())) {
				match = ((options & PARTIAL_MATCH_MAP) == RELATIVE_PATH_OPTION);
			}
		}
		if (!match) {
                    for (Pattern pattern : wildCardsMap.keySet()) {
                        options = wildCardsMap.get(pattern);
                        if (((options & OBJECT_TYPE_MAP) == FILES_DIRS_OPTION) ||
                                (((options & OBJECT_TYPE_MAP) == FILES_ONLY_OPTION) && file.isFile()) ||
                                (((options & OBJECT_TYPE_MAP) == DIRS_ONLY_OPTION) && file.isDirectory())) {
                            String nameOrPath = (((options & PARTIAL_MATCH_MAP) == RELATIVE_PATH_OPTION) ? relativePath : fileName);
                            if (pattern.matcher(nameOrPath).matches()) {
                                match = true;
                                log(LEVEL_DEBUG, "Match [" + pattern + "]: " + nameOrPath);
                                break;
                            }
                        }
                    }
		}
		return match;
	}

	/**
	 * Parse the optional tags...<br>
	 * <br>
	 * ...for type of object:<br>
	 * &lt;F&gt; Apply to files only<br>
	 * &lt;D&gt; Apply to dirs only<br>
	 * &lt;FD&gt; Apply to files and dirs (default)<br>
	 * <br>
	 * ...for what to match:<br>
	 * &lt;N&gt; Match only the Name (default)<br>
	 * &lt;P&gt; Match the name including the relative Path<br>
	 * <br>
	 * ...for how to match:<br>
	 * &lt;CI&gt; Case insensitive<br>
	 * &lt;CS&gt; Case sensitive (default)<br>
	 * <br>
	 * &lt;/&gt; To be substitued by the "System.fileSeparator"<br>
	 */
	private int parseOptionalTags(String[] lines, int i) {
		int options = 0;
		log(LEVEL_DEBUG, "Parsing options from line: "+lines[i]);
		while (lines[i].startsWith("<")) {
			if (lines[i].startsWith("<F>")) {
				options += FILES_ONLY_OPTION;
				lines[i] = lines[i].substring(3);
			} else if (lines[i].startsWith("<D>")) {
				options += DIRS_ONLY_OPTION;
				lines[i] = lines[i].substring(3);
			} else if (lines[i].startsWith("<FD>")) {
				options += FILES_DIRS_OPTION;
				lines[i] = lines[i].substring(4);
			} else if (lines[i].startsWith("<N>")) {
				options += FILE_NAME_OPTION;
				lines[i] = lines[i].substring(3);
			} else if (lines[i].startsWith("<P>")) {
				options += RELATIVE_PATH_OPTION;
				lines[i] = lines[i].substring(3);
			} else if (lines[i].startsWith("<CI>")) {
				options += CASE_INSENSITIVE_OPTION;
				lines[i] = lines[i].substring(4);
			} else if (lines[i].startsWith("<CS>")) {
				options += CASE_SENSITIVE_OPTION;
				lines[i] = lines[i].substring(4);
			}
		}
		log(LEVEL_DEBUG, "Parsed line: "+lines[i]);
		lines[i] = lines[i].replaceAll("</>", File.pathSeparator);
		return options;
	}
}
