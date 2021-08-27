package itamar.dirsynch;

import static itamar.dirsynch.DirSynchProperties.getLogFile;
import static itamar.dirsynch.DirSynchProperties.getLogLevel;
import static itamar.dirsynch.DirSynchProperties.getMainDir;
import static itamar.dirsynch.DirSynchProperties.getSecDir;
import static itamar.dirsynch.DirSynchProperties.init;
import static itamar.dirsynch.DirSynchProperties.isHashEnabled;
import static itamar.dirsynch.DirSynchProperties.isHideEquals;
import static itamar.dirsynch.DirSynchProperties.isSubDirsInclude;
import junit.framework.TestCase;
import static itamar.util.Logger.LEVEL_DEBUG;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

/**
 *
 * @author Itamar Carvalho
 */
public class DirSynchPropertiesTest extends TestCase {
    String dirSynchDir = Paths.get(".").toAbsolutePath().normalize().toString();
    public DirSynchPropertiesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("init");
        String propertiesFile = dirSynchDir + "\\test\\DirSynchTest.properties";
        init(propertiesFile);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Test of method init, of class itamar.dirsynch.DirSynchProperties.
     * @throws java.lang.Exception
     */
    public void testInitFileNotFound() throws Exception {
        System.out.println("init");
        String propertiesFile = dirSynchDir + "\\test\\DirSynchDOESNOTEXISTS.properties";
        try {
            init(propertiesFile);
            fail("Should have thrown FileNotFoundException.");
        } catch (FileNotFoundException e) {
            assertTrue("Test executed with success", true);
        }
    }

    /**
     * Test of method isHashEnabled, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testIsHashEnabled() {
        System.out.println("isHashEnabled");
        
        boolean expResult = false;
        boolean result = isHashEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of method isHashOnlySmall, of class itamar.dirsynch.DirSynchProperties.
     */
    /*public void testIsHashOnlySmall() {
        System.System.out.println("isHashOnlySmall");
        
        boolean expResult = true;
        boolean result = itamar.dirsynch.DirSynchProperties.isHashOnlySmall();
        assertEquals(expResult, result);
    }*/

//    /**
//     * Test of method getHashMaxSize, of class itamar.dirsynch.DirSynchProperties.
//     */
//    public void testGetHashMaxSize() {
//        System.System.out.println("getHashMaxSize");
//        
//        int expResult = 0;
//        int result = itamar.dirsynch.DirSynchProperties.getHashMaxSize();
//        assertEquals(expResult, result);
//    }

    /**
     * Test of method getLogLevel, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testGetLogLevel() {
        System.out.println("getLogLevel");
        
        short expResult = LEVEL_DEBUG;
        short result = getLogLevel();
        assertEquals(expResult, result);
    }

    /**
     * Test of method getLogFile, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testGetLogFile() {
        System.out.println("getLogFile");
        
        String expResult = "DirSynchTest.log";
        String result = getLogFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of method getMainDir, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testGetMainDir() {
        System.out.println("getMainDir");
        
        String expResult = "[teste main dir]";
        String result = getMainDir();
        assertEquals(expResult, result);
    }

    /**
     * Test of method getSecDir, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testGetSecDir() {
        System.out.println("getSecDir");
        
        String expResult = "[teste sec dir]";
        String result = getSecDir();
        assertEquals(expResult, result);
    }

    /**
     * Test of method isSubDirsInclude, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testIsSubDirsInclude() {
        System.out.println("isSubDirsInclude");
        
        boolean expResult = true;
        boolean result = isSubDirsInclude();
        assertEquals(expResult, result);
    }

    /**
     * Test of method isHideEquals, of class itamar.dirsynch.DirSynchProperties.
     */
    public void testIsHideEquals() {
        System.out.println("isHideEquals");
        
        boolean expResult = true;
        boolean result = isHideEquals();
        assertEquals(expResult, result);
    }

//    /**
//     * Test of method getPropertiesAsString, of class itamar.dirsynch.DirSynchProperties.
//     */
//    public void testGetPropertiesAsString() {
//        System.System.out.println("getPropertiesAsString");
//        
//        String expResult = "";
//        String result = itamar.dirsynch.DirSynchProperties.getPropertiesAsString();
//        assertEquals(expResult, result);
//    }
    
}
