package itamar.dirsynch;
import junit.framework.*;
import itamar.util.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
/*
 * DirSynchPropertiesTest.java
 * JUnit based test
 *
 * Created on 26 de Janeiro de 2008, 13:39
 */

/**
 *
 * @author Administrator
 */
public class DirSynchPropertiesTest extends TestCase {
    String dirSynchDir = "P:\\Pessoal\\DirSynch";
    public DirSynchPropertiesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        System.out.println("init");
        String propertiesFile = dirSynchDir + "\\test\\DirSynchTest.properties";
        itamar.dirsynch.DirSynchProperties.init(propertiesFile);
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Teste do método init, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testInitFileNotFound() throws Exception {
        System.out.println("init");
        String propertiesFile = dirSynchDir + "\\test\\DirSynchDOESNOTEXISTS.properties";
        try {
            itamar.dirsynch.DirSynchProperties.init(propertiesFile);
            fail("Should have thrown FileNotFoundException.");
        } catch (FileNotFoundException e) {
            assertTrue("Test executed with success", true);
        }
    }

    /**
     * Teste do método isHashEnabled, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testIsHashEnabled() {
        System.out.println("isHashEnabled");
        
        boolean expResult = false;
        boolean result = itamar.dirsynch.DirSynchProperties.isHashEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Teste do método isHashOnlySmall, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testIsHashOnlySmall() {
        System.out.println("isHashOnlySmall");
        
        boolean expResult = true;
        boolean result = itamar.dirsynch.DirSynchProperties.isHashOnlySmall();
        assertEquals(expResult, result);
    }

//    /**
//     * Teste do método getHashMaxSize, da classe itamar.dirsynch.DirSynchProperties.
//     */
//    public void testGetHashMaxSize() {
//        System.out.println("getHashMaxSize");
//        
//        int expResult = 0;
//        int result = itamar.dirsynch.DirSynchProperties.getHashMaxSize();
//        assertEquals(expResult, result);
//        
//        // TODO rever o código de teste gerado e remover a chamada de falha padrão.
//        fail("O caso de teste é um protótipo.");
//    }

    /**
     * Teste do método getLogLevel, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testGetLogLevel() {
        System.out.println("getLogLevel");
        
        short expResult = Logger.LEVEL_DEBUG;
        short result = itamar.dirsynch.DirSynchProperties.getLogLevel();
        assertEquals(expResult, result);
    }

    /**
     * Teste do método getLogFile, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testGetLogFile() {
        System.out.println("getLogFile");
        
        String expResult = "DirSynchTest.log";
        String result = itamar.dirsynch.DirSynchProperties.getLogFile();
        assertEquals(expResult, result);
    }

    /**
     * Teste do método getMainDir, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testGetMainDir() {
        System.out.println("getMainDir");
        
        String expResult = "[teste main dir]";
        String result = itamar.dirsynch.DirSynchProperties.getMainDir();
        assertEquals(expResult, result);
    }

    /**
     * Teste do método getSecDir, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testGetSecDir() {
        System.out.println("getSecDir");
        
        String expResult = "[teste sec dir]";
        String result = itamar.dirsynch.DirSynchProperties.getSecDir();
        assertEquals(expResult, result);
    }

    /**
     * Teste do método isSubDirsInclude, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testIsSubDirsInclude() {
        System.out.println("isSubDirsInclude");
        
        boolean expResult = true;
        boolean result = itamar.dirsynch.DirSynchProperties.isSubDirsInclude();
        assertEquals(expResult, result);
    }

    /**
     * Teste do método isHideEquals, da classe itamar.dirsynch.DirSynchProperties.
     */
    public void testIsHideEquals() {
        System.out.println("isHideEquals");
        
        boolean expResult = true;
        boolean result = itamar.dirsynch.DirSynchProperties.isHideEquals();
        assertEquals(expResult, result);
    }

//    /**
//     * Teste do método getPropertiesAsString, da classe itamar.dirsynch.DirSynchProperties.
//     */
//    public void testGetPropertiesAsString() {
//        System.out.println("getPropertiesAsString");
//        
//        String expResult = "";
//        String result = itamar.dirsynch.DirSynchProperties.getPropertiesAsString();
//        assertEquals(expResult, result);
//        
//        // TODO rever o código de teste gerado e remover a chamada de falha padrão.
//        fail("O caso de teste é um protótipo.");
//    }
    
}
