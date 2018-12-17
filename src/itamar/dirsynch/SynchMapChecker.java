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

/**
 *
 * @author Itamar Carvalho
 */
public class SynchMapChecker {
    private Map<String, String> synchMap = null;
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
                    if (lines[i].startsWith("|")) {
                        getWildCards().add(Pattern.compile(lines[i].substring(1), Pattern.CASE_INSENSITIVE));
                    } else {
                        map.put(lines[i], "1");
                    }
                }
            }
            Logger.log(Logger.LEVEL_INFO, "{No,Only}Synch file loaded: "+synchFile.getCanonicalPath());
            Logger.log(Logger.LEVEL_DEBUG, "Map: "+map+"  WildCards: "+getWildCards());
        } catch (IOException e) {
            Logger.log(Logger.LEVEL_ERROR, "Failed to load file '"+synchFile+"'.");
            Logger.log(Logger.LEVEL_ERROR, e);
        }
    }
    
    public void init(File[] synchFiles) {
        for (int i = 0; i < synchFiles.length; i++) {
            if (synchFiles[i].isFile() && synchFiles[i].canRead()) {
                loadSynchFile(synchFiles[i]);
            }
        }
    }
    
    public boolean match(String fileName) {
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
