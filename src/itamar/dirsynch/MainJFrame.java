/*
 * MainJFrame.java
 *
 * Created on 3 de Agosto de 2006, 20:32
 */

package itamar.dirsynch;

import com.oktiva.util.FileUtil;
import itamar.util.Logger;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author  Itamar Carvalho
 */
public class MainJFrame extends javax.swing.JFrame {
    private File mainDir;
    private File secDir;
    private Map<String, File> mainDirMap;
    private Map<String, File> secDirMap;
    private static String defaultMainDirPath = null;
    private static String defaultSecDirPath = null;
    
    private static String version = "1.5.1";
    private static boolean defaultKeep = false;
    private SynchMapChecker noSynchMap = null;
    private boolean firstLoad = true;
    private final String helpFile = "DirSynch-help.html";
    private static String propertiesFilePath = "DirSynch.properties";
    
    /** Creates new form MainJFrame */
    public MainJFrame() {
        initDirSynchProperties();
        initComponents();
        setTitle("DirSynch " + version);
        jTableFiles.getColumn("Sel").setMaxWidth(30);
        jTableFiles.getColumn("Main").setMaxWidth(30);
        jTableFiles.getColumn("Sec").setMaxWidth(30);
        setStatusBarText(FilePair.getLegend());
        
        ListSelectionModel rowSM = jTableFiles.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;
                
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    //System.out.println("No rows are selected.");
                    setStatusBarText(FilePair.getLegend());
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
//                    System.out.println("Row " + selectedRow
//                            + " is now selected.");
                    updateStatus();
                }
            }
        });

        initOptions();
    }

    private void initDirSynchProperties() {
        try {
            DirSynchProperties.init(propertiesFilePath);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "File '"+propertiesFilePath+"' not found!",
                    "Warning!",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error reading file '"+propertiesFilePath+"':\n"+ex.getMessage(),
                    "Warning!",
                    JOptionPane.WARNING_MESSAGE);
        }
        Logger.init(DirSynchProperties.getLogLevel(), DirSynchProperties.getLogFile(), DirSynchProperties.isLogFileAppend());
        Logger.log(Logger.LEVEL_INFO, "Properties initialized with file '"+propertiesFilePath+"'");
        Logger.log(Logger.LEVEL_DEBUG, "Properties read: "+DirSynchProperties.getPropertiesAsString());
    }
    
    private void initOptions() {
        // defaultXXXDirPath comes from command-line parameters and has priority over .properties file
        setMainDirPath((defaultMainDirPath == null ? DirSynchProperties.getMainDir() : defaultMainDirPath));
        setSecDirPath((defaultSecDirPath == null ? DirSynchProperties.getSecDir() : defaultSecDirPath));
        
        Logger.log(Logger.LEVEL_DEBUG, "SubDirsInclude="+DirSynchProperties.isSubDirsInclude());
        jChkBxMenuItemUseHash.setSelected(DirSynchProperties.isHashEnabled());
        jChkBxMenuItemIncSubdirs.setSelected(DirSynchProperties.isSubDirsInclude());
        jChkBxMenuItemHideEquals.setSelected(DirSynchProperties.isHideEquals());
        jChkBxMenuItemSynchTimesHash.setSelected(DirSynchProperties.isSynchTimesSameHash());
        
        jChkBxMenuItemKeepBackup.setSelected(defaultKeep);
    }

    private void setOptionsInProps() {
        DirSynchProperties.setMainDir(getMainDirPath());
        DirSynchProperties.setSecDir(getSecDirPath());

        DirSynchProperties.setHashEnabled(isHashEnabled());
        DirSynchProperties.setSubDirsInclude(isIncludeSubdirs());
        DirSynchProperties.setHideEquals(isHideEquals());
        DirSynchProperties.setSynchTimesSameHash(isSynchTimesSameHash());
//        jChkBxMenuItemKeepBackup.setSelected(defaultKeep);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jDialogHelp = new javax.swing.JDialog();
        jScrollPaneHelp = new javax.swing.JScrollPane();
        jEdtPaneHelp = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTxtFldSecDir = new javax.swing.JTextField();
        jTxtFldMainDir = new javax.swing.JTextField();
        statusBar = new javax.swing.JPanel();
        jLblStatusBar = new javax.swing.JLabel();
        jBtnMainDir = new javax.swing.JButton();
        jBtnSecDir = new javax.swing.JButton();
        jBtnLoad = new javax.swing.JButton();
        jBtnSynch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFiles = new javax.swing.JTable();
        jChkBoxHideEquals = new javax.swing.JCheckBox();
        jChkBoxUseHash = new javax.swing.JCheckBox();
        jMenuBarDirSynch = new javax.swing.JMenuBar();
        jMenuTools = new javax.swing.JMenu();
        jMenuSelect = new javax.swing.JMenu();
        jMenuItemAll = new javax.swing.JMenuItem();
        jMenuItemNone = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemYesNo = new javax.swing.JMenuItem();
        jMenuItemOnlyInMain = new javax.swing.JMenuItem();
        jMenuItemOnlyInSec = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemNewerInMain = new javax.swing.JMenuItem();
        jMenuItemNewerInSec = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemSelRegexp = new javax.swing.JMenuItem();
        jMenuItemUnselRegexp = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jChkBxMenuItemIncSubdirs = new javax.swing.JCheckBoxMenuItem();
        jChkBxMenuItemUseHash = new javax.swing.JCheckBoxMenuItem();
        jChkBxMenuItemHideEquals = new javax.swing.JCheckBoxMenuItem();
        jChkBxMenuItemKeepBackup = new javax.swing.JCheckBoxMenuItem();
        jChkBxMenuItemSynchTimesHash = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItemLoadOpt = new javax.swing.JMenuItem();
        jMenuItemSaveOpt = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemDirSynchHelp = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        jDialogHelp.setTitle("DirSynch Help");
        jDialogHelp.setAlwaysOnTop(true);
        jDialogHelp.setModal(true);
        jScrollPaneHelp.setMinimumSize(new java.awt.Dimension(600, 400));
        jScrollPaneHelp.setPreferredSize(new java.awt.Dimension(600, 400));
        jEdtPaneHelp.setEditable(false);
        jEdtPaneHelp.setContentType("text/html");
        jScrollPaneHelp.setViewportView(jEdtPaneHelp);

        org.jdesktop.layout.GroupLayout jDialogHelpLayout = new org.jdesktop.layout.GroupLayout(jDialogHelp.getContentPane());
        jDialogHelp.getContentPane().setLayout(jDialogHelpLayout);
        jDialogHelpLayout.setHorizontalGroup(
            jDialogHelpLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDialogHelpLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPaneHelp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialogHelpLayout.setVerticalGroup(
            jDialogHelpLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jDialogHelpLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPaneHelp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DirSync");
        jLabel1.setText("Main directory:");

        jLabel2.setText("Second directory:");

        jTxtFldSecDir.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 128, 0)));
        jTxtFldSecDir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTxtFldSecDirKeyTyped(evt);
            }
        });

        jTxtFldMainDir.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255)));
        jTxtFldMainDir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTxtFldMainDirKeyTyped(evt);
            }
        });

        statusBar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                statusBarMouseClicked(evt);
            }
        });

        jLblStatusBar.setText(" ");

        org.jdesktop.layout.GroupLayout statusBarLayout = new org.jdesktop.layout.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusBarLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLblStatusBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLblStatusBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        jBtnMainDir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dir.png")));
        jBtnMainDir.setToolTipText("Select main dir...");
        jBtnMainDir.setIconTextGap(0);
        jBtnMainDir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBtnMainDir.setMinimumSize(new java.awt.Dimension(20, 20));
        jBtnMainDir.setName("jBtnMainDir");
        jBtnMainDir.setPreferredSize(new java.awt.Dimension(20, 20));
        jBtnMainDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnMainDirActionPerformed(evt);
            }
        });

        jBtnSecDir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dir.png")));
        jBtnSecDir.setToolTipText("Select second dir...");
        jBtnSecDir.setIconTextGap(0);
        jBtnSecDir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBtnSecDir.setMinimumSize(new java.awt.Dimension(20, 20));
        jBtnSecDir.setName("jBtnSecDir");
        jBtnSecDir.setPreferredSize(new java.awt.Dimension(20, 20));
        jBtnSecDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSecDirActionPerformed(evt);
            }
        });

        jBtnLoad.setText("Load");
        jBtnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLoadActionPerformed(evt);
            }
        });

        jBtnSynch.setText("Synchronize");
        jBtnSynch.setEnabled(false);
        jBtnSynch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSynchActionPerformed(evt);
            }
        });

        jTableFiles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableFiles.setModel(new FileVOTableModel(
            new Object [][] {
                {new Boolean(false), null, null, null}
            },
            new String [] {
                "Sel", "Main", "Sec", "File"
            }
        ));
        jTableFiles.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(jTableFiles);

        jChkBoxHideEquals.setText("Hide equals");
        jChkBoxHideEquals.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jChkBoxHideEquals.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkBoxHideEquals.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkBoxHideEqualsItemStateChanged(evt);
            }
        });

        jChkBoxUseHash.setText("Use hash");
        jChkBoxUseHash.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jChkBoxUseHash.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkBoxUseHash.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkBoxUseHashItemStateChanged(evt);
            }
        });

        jMenuTools.setMnemonic('t');
        jMenuTools.setText("Tools");
        jMenuSelect.setMnemonic('s');
        jMenuSelect.setText("Select");
        jMenuItemAll.setMnemonic('a');
        jMenuItemAll.setText("All");
        jMenuItemAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAllActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemAll);

        jMenuItemNone.setMnemonic('n');
        jMenuItemNone.setText("None");
        jMenuItemNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNoneActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemNone);

        jMenuSelect.add(jSeparator1);

        jMenuItemYesNo.setMnemonic('y');
        jMenuItemYesNo.setText("Only YES/no");
        jMenuItemYesNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemYesNoActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemYesNo);

        jMenuItemOnlyInMain.setMnemonic('m');
        jMenuItemOnlyInMain.setText("Only in main dir");
        jMenuItemOnlyInMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOnlyInMainActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemOnlyInMain);

        jMenuItemOnlyInSec.setMnemonic('s');
        jMenuItemOnlyInSec.setText("Only in sec dir");
        jMenuItemOnlyInSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOnlyInSecActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemOnlyInSec);

        jMenuSelect.add(jSeparator2);

        jMenuItemNewerInMain.setMnemonic('n');
        jMenuItemNewerInMain.setText("Newer in main");
        jMenuItemNewerInMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewerInMainActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemNewerInMain);

        jMenuItemNewerInSec.setMnemonic('e');
        jMenuItemNewerInSec.setText("Newer in sec");
        jMenuItemNewerInSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewerInSecActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemNewerInSec);

        jMenuSelect.add(jSeparator3);

        jMenuItemSelRegexp.setText("Sel with regexp...");
        jMenuItemSelRegexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSelRegexpActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemSelRegexp);

        jMenuItemUnselRegexp.setText("Unsel with regexp...");
        jMenuItemUnselRegexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUnselRegexpActionPerformed(evt);
            }
        });

        jMenuSelect.add(jMenuItemUnselRegexp);

        jMenuTools.add(jMenuSelect);

        jMenuBarDirSynch.add(jMenuTools);

        jMenuOptions.setMnemonic('o');
        jMenuOptions.setText("Options");
        jChkBxMenuItemIncSubdirs.setMnemonic('I');
        jChkBxMenuItemIncSubdirs.setSelected(true);
        jChkBxMenuItemIncSubdirs.setText("Include subdirs");
        jChkBxMenuItemIncSubdirs.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkBxMenuItemIncSubdirsItemStateChanged(evt);
            }
        });

        jMenuOptions.add(jChkBxMenuItemIncSubdirs);

        jChkBxMenuItemUseHash.setMnemonic('h');
        jChkBxMenuItemUseHash.setText("Use hash");
        jChkBxMenuItemUseHash.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkBxMenuItemUseHashItemStateChanged(evt);
            }
        });

        jMenuOptions.add(jChkBxMenuItemUseHash);

        jChkBxMenuItemHideEquals.setMnemonic('e');
        jChkBxMenuItemHideEquals.setText("Hide equals");
        jChkBxMenuItemHideEquals.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jChkBxMenuItemHideEqualsStateChanged(evt);
            }
        });

        jMenuOptions.add(jChkBxMenuItemHideEquals);

        jChkBxMenuItemKeepBackup.setMnemonic('b');
        jChkBxMenuItemKeepBackup.setSelected(true);
        jChkBxMenuItemKeepBackup.setText("Keep backup");
        jMenuOptions.add(jChkBxMenuItemKeepBackup);

        jChkBxMenuItemSynchTimesHash.setMnemonic('t');
        jChkBxMenuItemSynchTimesHash.setSelected(true);
        jChkBxMenuItemSynchTimesHash.setText("Synch times for same hashes");
        jMenuOptions.add(jChkBxMenuItemSynchTimesHash);

        jMenuOptions.add(jSeparator4);

        jMenuItemLoadOpt.setMnemonic('L');
        jMenuItemLoadOpt.setText("Load options...");
        jMenuItemLoadOpt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadOptActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemLoadOpt);

        jMenuItemSaveOpt.setMnemonic('S');
        jMenuItemSaveOpt.setText("Save options...");
        jMenuItemSaveOpt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveOptActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSaveOpt);

        jMenuBarDirSynch.add(jMenuOptions);

        jMenuHelp.setMnemonic('h');
        jMenuHelp.setText("Help");
        jMenuItemDirSynchHelp.setMnemonic('h');
        jMenuItemDirSynchHelp.setText("DirSynch Help");
        jMenuItemDirSynchHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDirSynchHelpActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemDirSynchHelp);

        jMenuItemAbout.setMnemonic('a');
        jMenuItemAbout.setText("About...");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemAbout);

        jMenuBarDirSynch.add(jMenuHelp);

        setJMenuBar(jMenuBarDirSynch);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jTxtFldMainDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jBtnMainDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(jTxtFldSecDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jBtnSecDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(statusBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jBtnLoad, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jBtnSynch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 213, Short.MAX_VALUE)
                        .add(jChkBoxUseHash)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jChkBoxHideEquals)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel1)
                        .add(jTxtFldMainDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jBtnMainDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTxtFldSecDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jBtnSecDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jBtnLoad)
                        .add(jBtnSynch))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jChkBoxHideEquals)
                        .add(jChkBoxUseHash)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pack();
    }//GEN-END:initComponents

    private void jMenuItemSaveOptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveOptActionPerformed
        saveOptions();
    }//GEN-LAST:event_jMenuItemSaveOptActionPerformed

    private void jMenuItemLoadOptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadOptActionPerformed
        loadOptions();
    }//GEN-LAST:event_jMenuItemLoadOptActionPerformed
    
    private void jMenuItemUnselRegexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUnselRegexpActionPerformed
        selectWithRegexp(false);
    }//GEN-LAST:event_jMenuItemUnselRegexpActionPerformed
    
    private void jMenuItemSelRegexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelRegexpActionPerformed
        selectWithRegexp(true);
    }//GEN-LAST:event_jMenuItemSelRegexpActionPerformed
    
    private void jChkBxMenuItemIncSubdirsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkBxMenuItemIncSubdirsItemStateChanged
        if (!firstLoad) {
            load();
        }
    }//GEN-LAST:event_jChkBxMenuItemIncSubdirsItemStateChanged
    
    private void jChkBxMenuItemHideEqualsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jChkBxMenuItemHideEqualsStateChanged
        jChkBoxHideEquals.setSelected(jChkBxMenuItemHideEquals.isSelected());
        jChkBxMenuItemSynchTimesHash.setEnabled(!jChkBoxUseHash.isSelected() || !jChkBoxHideEquals.isSelected());
    }//GEN-LAST:event_jChkBxMenuItemHideEqualsStateChanged
    
    private void jChkBxMenuItemUseHashItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkBxMenuItemUseHashItemStateChanged
        jChkBoxUseHash.setSelected(jChkBxMenuItemUseHash.isSelected());
        jChkBxMenuItemSynchTimesHash.setEnabled(!jChkBoxUseHash.isSelected() || !jChkBoxHideEquals.isSelected());
    }//GEN-LAST:event_jChkBxMenuItemUseHashItemStateChanged
    
    private void jMenuItemNewerInSecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewerInSecActionPerformed
        selectFilesByStatus(FilePair.SEC_NEWER);
    }//GEN-LAST:event_jMenuItemNewerInSecActionPerformed
    
    private void jMenuItemNewerInMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewerInMainActionPerformed
        selectFilesByStatus(FilePair.MAIN_NEWER);
    }//GEN-LAST:event_jMenuItemNewerInMainActionPerformed
    
    private void jMenuItemOnlyInSecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOnlyInSecActionPerformed
        selectFilesByStatus(FilePair.ONLY_SEC);
    }//GEN-LAST:event_jMenuItemOnlyInSecActionPerformed
    
    private void jMenuItemOnlyInMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOnlyInMainActionPerformed
        selectFilesByStatus(FilePair.ONLY_MAIN);
    }//GEN-LAST:event_jMenuItemOnlyInMainActionPerformed
    
    private void jMenuItemYesNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemYesNoActionPerformed
        selectFilesByStatus(new short[] {FilePair.ONLY_MAIN, FilePair.ONLY_SEC});
    }//GEN-LAST:event_jMenuItemYesNoActionPerformed
    
    private void jMenuItemNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNoneActionPerformed
        selectAllFiles(false);
    }//GEN-LAST:event_jMenuItemNoneActionPerformed
    
    private void jMenuItemAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAllActionPerformed
        selectAllFiles(true);
    }//GEN-LAST:event_jMenuItemAllActionPerformed
    
    private void jMenuItemDirSynchHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDirSynchHelpActionPerformed
        String helpText;
        try {
//            helpText = FileUtil.readFile(helpFile);
//            jEdtPaneHelp.setText(helpText);
            jEdtPaneHelp.setPage("file:///"+System.getProperty("user.dir")+File.separator+helpFile);
            jEdtPaneHelp.setCaretPosition(0);
            jDialogHelp.pack();
            jDialogHelp.setVisible(true);
        } catch (IOException ex) {
            Logger.log(Logger.LEVEL_ERROR, ex);
        }
    }//GEN-LAST:event_jMenuItemDirSynchHelpActionPerformed
    
    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        showAbout();
    }//GEN-LAST:event_jMenuItemAboutActionPerformed
    
    private void jChkBoxUseHashItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkBoxUseHashItemStateChanged
        jChkBxMenuItemUseHash.setSelected(jChkBoxUseHash.isSelected());
        jChkBxMenuItemSynchTimesHash.setEnabled(!jChkBoxUseHash.isSelected() || !jChkBoxHideEquals.isSelected());
        if (!firstLoad) {
            load();
        }
    }//GEN-LAST:event_jChkBoxUseHashItemStateChanged
    
    private void jChkBoxHideEqualsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkBoxHideEqualsItemStateChanged
        jChkBxMenuItemHideEquals.setSelected(jChkBoxHideEquals.isSelected());
        jChkBxMenuItemSynchTimesHash.setEnabled(!jChkBoxUseHash.isSelected() || !jChkBoxHideEquals.isSelected());
        if (!firstLoad) {
            load();
        }
    }//GEN-LAST:event_jChkBoxHideEqualsItemStateChanged
    
    private void jTxtFldSecDirKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtFldSecDirKeyTyped
        jBtnSynch.setEnabled(false);
        firstLoad = true;
    }//GEN-LAST:event_jTxtFldSecDirKeyTyped
    
    private void jTxtFldMainDirKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtFldMainDirKeyTyped
        jBtnSynch.setEnabled(false);
        firstLoad = true;
    }//GEN-LAST:event_jTxtFldMainDirKeyTyped
    
    private void jBtnSynchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSynchActionPerformed
        synchronize();
    }//GEN-LAST:event_jBtnSynchActionPerformed
    
    private void jBtnSecDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSecDirActionPerformed
        selectDir(evt);
    }//GEN-LAST:event_jBtnSecDirActionPerformed
    
    private void jBtnMainDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnMainDirActionPerformed
        selectDir(evt);
    }//GEN-LAST:event_jBtnMainDirActionPerformed
    
    private void statusBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_statusBarMouseClicked
        showAbout();
    }//GEN-LAST:event_statusBarMouseClicked
    
    private void jBtnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLoadActionPerformed
        load();
    }//GEN-LAST:event_jBtnLoadActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if (processParams(args)) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new MainJFrame().setVisible(true);
                }
            });
        }
    }
    
    private static boolean processParams(String[] args) {
        boolean continueAfterThis = true;
        try {
            for (int i = 0; i < args.length; i++) {
                if ("-main".equals(args[i])) {
                    defaultMainDirPath = args[++i];
                } else if ("-sec".equals(args[i])) {
                    defaultSecDirPath = args[++i];
                } else if ("-prop".equals(args[i])) {
                    propertiesFilePath = args[++i];
                } else if ("-keep".equals(args[i])) {
                    defaultKeep = true;
                } else if ("-help".equals(args[i]) || "-h".equals(args[i]) || "/?".equals(args[i]) || "--help".equals(args[i])) {
                    showUsage();
                    continueAfterThis = false;
                } else {
                    System.out.println("Incorrect parameters!\n");
                    showUsage();
                    continueAfterThis = false;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Incorrect parameters!\n");
            showUsage();
            continueAfterThis = false;
        }
        return continueAfterThis;
    }
    
    private static void showUsage() {
        System.out.println("DirSynch "+version+"\n"+(char)184+" 2006-2008 Itamar Carvalho <itamarc at gmail.com>\n");
        System.out.println("java[w] -jar DirSynch.jar <Params>");
        System.out.println("Params:");
        System.out.println("  -main <main dir path>           Set the main dir.");
        System.out.println("  -sec <sec dir path>             Set the secondary dir.");
        System.out.println("  -keep                           Keep backups.");
        System.out.println("  -prop <properties file path>    DirSynch.properties file path.");
        System.out.println("  --help | -help | -h | /?        Show this usage message.");
    }
    
    private void selectDir(ActionEvent evt) {
        JButton button = (JButton)evt.getSource();
        String buttonName = button.getName();
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if ("jBtnMainDir".equals(buttonName)) {
            chooser.setCurrentDirectory(new File(getMainDirPath()));
        } else {
            chooser.setCurrentDirectory(new File(getSecDirPath()));
        }
        //DirFileFilter filter = new DirFileFilter();
        //chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            if ("jBtnMainDir".equals(buttonName)) {
                setMainDirPath(chooser.getSelectedFile().getAbsolutePath());
            } else { // jBtnSecDir
                setSecDirPath(chooser.getSelectedFile().getAbsolutePath());
            }
        }
        jBtnSynch.setEnabled(false);
        firstLoad = true;
    }
    
    private boolean dirsOk() {
        mainDir = new File(getMainDirPath());
        Logger.log(Logger.LEVEL_DEBUG, "Main dir: "+getMainDirPath());
        if (!mainDir.isDirectory() || !mainDir.canRead()) {
            JOptionPane.showMessageDialog(this,
                    "Directory '" + mainDir + "' does not exist or can't be read.\nCheck the paths!",
                    "Warning!",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        } else {
            secDir = new File(getSecDirPath());
            Logger.log(Logger.LEVEL_DEBUG, "Sec dir: "+getSecDirPath());
            if (!secDir.isDirectory() || !secDir.canRead()) {
                JOptionPane.showMessageDialog(this,
                        "Directory '" + secDir + "' does not exist or can't be read.\nCheck the paths!",
                        "Warning!",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            } else {
                return true;
            }
        }
    }
    
    private String getMainDirPath() {
        return jTxtFldMainDir.getText();
    }
    private void setMainDirPath(String path) {
        jTxtFldMainDir.setText(path);
    }
    
    private String getSecDirPath() {
        return jTxtFldSecDir.getText();
    }
    private void setSecDirPath(String path) {
        jTxtFldSecDir.setText(path);
    }
    
    private void compareDirs()
    throws IOException, NoSuchAlgorithmException {
        final MainJFrame mainFrame = this;
        Runnable syncRun = new Runnable() {
            public void run() {
                int step = 0;
                ProgressMonitor progressMonitor = null;
                try {
                    Logger.log(Logger.LEVEL_INFO, "Starting load process...");
                    setButtonsEnabled(false, false);
                    progressMonitor = new ProgressMonitor(mainFrame,
                            "Loading . . .",
                            "", 0, 6);
                    progressMonitor.setMillisToDecideToPopup(0);
                    progressMonitor.setMillisToPopup(0);
                    progressMonitor.setNote("Loading no synch files");
                    // Step 1 - Loading no synch files
                    loadNoSynchFiles();
                    if (progressMonitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    progressMonitor.setNote("Loading main dir data");
                    progressMonitor.setProgress(++step);
                    // Step 2 - Loading main dir data
                    mainDirMap = new HashMap<String, File>();
                    // We need to remove the trailing "\" in the case one of the dirs is the root of a drive.
                    int rootPathSize = (mainDir.getPath().endsWith("\\") ? mainDir.getPath().length()-1 : mainDir.getPath().length());
                    Logger.log(Logger.LEVEL_DEBUG, "Main dir path: "+mainDir.getPath()+" ("+rootPathSize+")");
                    buildMap(mainDir, mainDirMap, rootPathSize);
                    if (progressMonitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    progressMonitor.setNote("Loading sec dir data");
                    progressMonitor.setProgress(++step);
                    // Step 3 - Loading sec dir data
                    secDirMap = new HashMap<String, File>();
                    // We need to remove the trailing "\" in the case one of the dirs is the root of a drive.
                    rootPathSize = (secDir.getPath().endsWith("\\") ? secDir.getPath().length()-1 : secDir.getPath().length());
                    Logger.log(Logger.LEVEL_DEBUG, "Sec dir path: "+secDir.getPath()+" ("+rootPathSize+")");
                    buildMap(secDir, secDirMap, rootPathSize);
                    if (progressMonitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    progressMonitor.setNote("Comparing data");
                    progressMonitor.setProgress(++step);
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
                        file = new FilePair((String)iter.next(), mainDir, secDir);
                        file.setUseHash(jChkBoxUseHash.isSelected());
                        file.setMainFile((File)mainDirMap.get(file.getPath()));
                        Logger.log(Logger.LEVEL_DEBUG, "File added main: '"+file.getPath()+"'");
                        if (secDirMap.containsKey(file.getPath())) {
                            file.setSecFile((File)secDirMap.get(file.getPath()));
                        }
                        files.add(file);
                    }
                    // Sec
                    iter = secDirMap.keySet().iterator();
                    while (iter.hasNext()) {
                        if (progressMonitor.isCanceled()) {
                            throw new InterruptedException();
                        }
                        String path = (String)iter.next();
                        if (!mainDirMap.containsKey(path)) {
                            file = new FilePair(path, mainDir, secDir);
                            file.setUseHash(jChkBoxUseHash.isSelected());
                            file.setSecFile((File)secDirMap.get(file.getPath()));
                            files.add(file);
                            Logger.log(Logger.LEVEL_DEBUG, "File added sec: '"+file.getPath()+"'");
                        }
                    }
                    // Show differences
                    if (progressMonitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    progressMonitor.setNote("Sorting data");
                    progressMonitor.setProgress(++step);
                    // Step 5 - Sorting data
                    Collections.<FilePair>sort(files);
                    if (progressMonitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    progressMonitor.setNote("Preparing to show data");
                    progressMonitor.setProgress(++step);
                    // Step 6 - Preparing to show data
                    ((FileVOTableModel)jTableFiles.getModel()).setFiles(files, getHideEquals());
                    progressMonitor.setProgress(++step);
                    progressMonitor.close();
                    setButtonsEnabled(true, true);
                    Logger.log(Logger.LEVEL_INFO, "Load process finished successfully.");
                } catch (InterruptedException ex) {
                    Logger.log(Logger.LEVEL_INFO, "Load process cancelled by user.");
                    JOptionPane.showMessageDialog(mainFrame,
                            "Loading operation CANCELED!", "Cancelled",
                            JOptionPane.WARNING_MESSAGE);
                    setButtonsEnabled(true, false);
                    if (progressMonitor != null) {
                        progressMonitor.close();
                    }
                } catch (IOException ex) {
                    Logger.log(Logger.LEVEL_ERROR, "Load process failed: "+ex.getMessage());
                    JOptionPane.showMessageDialog(mainFrame,
                            ex.getClass().getName() + ": " + ex.getMessage(),
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    Logger.log(Logger.LEVEL_ERROR, ex);
                    setButtonsEnabled(true, false);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.log(Logger.LEVEL_ERROR, "Load process crashed: "+ex.getMessage());
                    JOptionPane.showMessageDialog(mainFrame,
                            ex.getClass().getName() + ": " + ex.getMessage(),
                            "Weird Error!",
                            JOptionPane.ERROR_MESSAGE);
                    Logger.log(Logger.LEVEL_ERROR, ex);
                    setButtonsEnabled(true, false);
                }
            }
        };
        new Thread(syncRun).start();
    }
    
    private boolean getHideEquals() {
        return jChkBoxHideEquals.isSelected();
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
    
    private void buildMap(File dir, Map<String, File> dirMap, int rootPathSize)
    throws IOException {
        Logger.log(Logger.LEVEL_DEBUG, "buildMap: "+dir.getPath()+" - "+rootPathSize);
        File[] dirFiles = dir.listFiles();
        Logger.log(Logger.LEVEL_DEBUG, "buildMap: "+dirFiles.length);
        for (int i = 0; i < dirFiles.length; i++) {
            File file = dirFiles[i];
            Logger.log(Logger.LEVEL_DEBUG, "buildMap dirFiles["+i+"]: "+file.getPath());
            if (!noSynchMap.match(file.getName())) {
                if (file.isDirectory()) {
                    if (isIncludeSubdirs()) {
                        buildMap(file, dirMap, rootPathSize);
                    }
                } else {
                    dirMap.put(file.getPath().substring(rootPathSize), file);
                }
            }
        }
    }
    
    
    
    private void synchronize() {
        final MainJFrame mainFrame = this;
        Runnable syncRun = new Runnable() {
            public void run() {
                FilePair filePair = null;
                try {
                    Logger.log(Logger.LEVEL_INFO, "Starting synchronization process...");
                    setButtonsEnabled(false, false);
                    Vector files = ((FileVOTableModel)jTableFiles.getModel()).getFiles();
                    ProgressMonitor progressMonitor = new ProgressMonitor(mainFrame,
                            "Synchronizing . . .",
                            "", 0, files.size());
                    progressMonitor.setMillisToDecideToPopup(0);
                    progressMonitor.setMillisToPopup(0);
                    boolean cancel = false;
                    for (int i = 0; i < files.size(); i++) {
                        if (progressMonitor.isCanceled()) {
                            cancel = true;
                            break;
                        }
                        jTableFiles.setRowSelectionInterval(i, i);
                        Boolean selected = (Boolean)jTableFiles.getModel().getValueAt(i, 0);
                        if (selected) {
                            filePair = (FilePair)files.get(i);
                            progressMonitor.setNote(filePair.getPath());
                            filePair.synchronize((jChkBxMenuItemSynchTimesHash.isEnabled()
                            && jChkBxMenuItemSynchTimesHash.isSelected()),
                                    jChkBxMenuItemKeepBackup.isSelected());
                        }
                        progressMonitor.setProgress(i);
                    }
                    progressMonitor.close();
                    if (cancel) {
                        Logger.log(Logger.LEVEL_INFO, "Synchronization process cancelled by user.");
                        JOptionPane.showMessageDialog(mainFrame,
                                "Synchronization CANCELED!", "Cancelled",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        Logger.log(Logger.LEVEL_INFO, "Synchronization process finished successfully.");
                        JOptionPane.showMessageDialog(mainFrame,
                                "Synchronization completed!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    load();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Failure in synchronization process!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    Logger.log(Logger.LEVEL_ERROR, "Error processing file: " + filePair);
                    Logger.log(Logger.LEVEL_ERROR, ex);
                } catch (NoSuchAlgorithmException ex) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Failure in synchronization process!",
                            "Weird Error",
                            JOptionPane.ERROR_MESSAGE);
                    Logger.log(Logger.LEVEL_ERROR, "Weird Error processing file: " + filePair);
                    Logger.log(Logger.LEVEL_ERROR, ex);
                } finally {
                    setButtonsEnabled(true, true);
                }
            }
        };
        new Thread(syncRun).start();
    }
    void setButtonsEnabled(boolean loadStatus, boolean synchStatus) {
        jBtnLoad.setEnabled(loadStatus);
        jChkBoxHideEquals.setEnabled(loadStatus);
//        jChkBoxKeepBackup.setEnabled(loadStatus);
        jChkBoxUseHash.setEnabled(loadStatus);
        jTxtFldMainDir.setEnabled(loadStatus);
        jTxtFldSecDir.setEnabled(loadStatus);
        jBtnMainDir.setEnabled(loadStatus);
        jBtnSecDir.setEnabled(loadStatus);
        jBtnSynch.setEnabled(synchStatus);
    }
    
    private void load() {
        if (dirsOk()) {
            try {
                compareDirs();
                firstLoad = false;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getClass().getName() + ": " + ex.getMessage(),
                        "I/O Error!",
                        JOptionPane.ERROR_MESSAGE);
                Logger.log(Logger.LEVEL_ERROR, ex);
            } catch (NoSuchAlgorithmException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getClass().getName() + ": " + ex.getMessage(),
                        "Weird Error!",
                        JOptionPane.ERROR_MESSAGE);
                Logger.log(Logger.LEVEL_ERROR, ex);
            }
        }
    }
    
    private void setStatusBarText(String text) {
        jLblStatusBar.setText(text);
    }
    
    private void updateStatus() {
        setStatusBarText(getSelectedFilePair().getStatus());
    }
    
    private FilePair getSelectedFilePair() {
        FileVOTableModel model = ((FileVOTableModel)jTableFiles.getModel());
        int row = jTableFiles.getSelectedRow();
        return ( row >= 0 ? (FilePair) model.getFiles().get(row) : null );
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "DirSynch "+version+"\n© 2007 Itamar Carvalho <itamarc AT gmail\u00B7com>",
                "About DirSynch",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void selectAllFiles(boolean checked) {
        // If the action is to uncheck all or if the "hide equals" option is on, do all
        if (!checked || getHideEquals()) {
            for (int i = 0; i < ((FileVOTableModel)jTableFiles.getModel()).getRowCount(); i++) {
                jTableFiles.getModel().setValueAt(new Boolean(checked), i, 0);
            }
        } else {  // Select only the different files
            Vector files = ((FileVOTableModel)jTableFiles.getModel()).getFiles();
            for (int i = 0; i < files.size(); i++) {
                if (!((FilePair)files.get(i)).isEquals()) {
                    jTableFiles.getModel().setValueAt(new Boolean(checked), i, 0);
                }
            }
        }
    }
    
    private void selectFilesByStatus(short status) {
        selectFilesByStatus(new short[] {status});
    }
    
    private void selectFilesByStatus(short[] statusList) {
        // If the action is to uncheck all or if the "hide equals" option is on, do all
        Vector files = ((FileVOTableModel)jTableFiles.getModel()).getFiles();
        for (int i = 0; i < files.size(); i++) {
            // If the status of the pair is in the list received
            if (statusInList(((FilePair)files.get(i)).getNewer(), statusList)) {
                jTableFiles.getModel().setValueAt(true, i, 0);
            } else {
                jTableFiles.getModel().setValueAt(false, i, 0);
            }
        }
    }
    
    private boolean statusInList(short status, short[] statusList) {
        boolean found = false;
        for (int i = 0; i < statusList.length; i++) {
            if (status == statusList[i]) {
                found = true;
            }
        }
        return found;
    }
    
    private boolean isIncludeSubdirs() {
        return jChkBxMenuItemIncSubdirs.isSelected();
    }
    
    private boolean isHashEnabled() {
        return jChkBxMenuItemUseHash.isSelected();
    }
    
    private boolean isHideEquals() {
        return jChkBxMenuItemHideEquals.isSelected();
    }
    
    private boolean isSynchTimesSameHash() {
        return jChkBxMenuItemSynchTimesHash.isSelected();
    }
    
    private void selectWithRegexp(boolean select) {
        String regexp = JOptionPane.showInputDialog(this, "Regular expression:", "Regexp", JOptionPane.QUESTION_MESSAGE);
        Pattern regPat = null;
        while (regPat == null) {
            try {
                regPat = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                Logger.log(Logger.LEVEL_WARNING, "Pattern invalid: "+e.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Pattern invalid: "+e.getMessage(),
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                regexp = JOptionPane.showInputDialog(this, "Regular expression:", regexp);
            }
        }
        
        Vector files = ((FileVOTableModel)jTableFiles.getModel()).getFiles();
        for (int i = 0; i < files.size(); i++) {
            // If the path of the pair matches the regexp
            if (regPat.matcher(((FilePair)files.get(i)).getPath()).matches()) {
                jTableFiles.getModel().setValueAt(select, i, 0);
            }
        }
    }

    private void loadOptions() {
        JFileChooser fileDiag = new JFileChooser(System.getProperty("user.dir"));
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                if (f.isFile()) {
                    return f.getName().endsWith(".properties");
                } else {
                    return true;
                }
            }
            public String getDescription() {
                return "Properties Files (*.properties)";
            }
        };
        fileDiag.setFileFilter(ff);
        fileDiag.setDialogTitle("Load options from file...");
        int ret = fileDiag.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File propsFile = fileDiag.getSelectedFile();
            Logger.log(Logger.LEVEL_DEBUG, "File selected to open: "+propsFile.getAbsolutePath());
            propertiesFilePath = propsFile.getAbsolutePath();
            initDirSynchProperties();
            defaultMainDirPath = null;
            defaultSecDirPath = null;
            initOptions();
        }
    }

    private void saveOptions() {
        setOptionsInProps();
        JFileChooser fileDiag = new JFileChooser(System.getProperty("user.dir"));
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".properties");
            }
            public String getDescription() {
                return "Properties Files (*.properties)";
            }
        };
        fileDiag.setFileFilter(ff);
        fileDiag.setDialogTitle("Save options to file...");
        int ret = fileDiag.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File propsFile = fileDiag.getSelectedFile();
            Logger.log(Logger.LEVEL_DEBUG, "File selected to save: "+propsFile.getAbsolutePath());
            if (!propsFile.getName().endsWith(".properties")) {
                String newFile = propsFile.getAbsoluteFile()+".properties";
                propsFile = new File(newFile);
            }
            try {
                DirSynchProperties.save(propsFile);
            } catch (FileNotFoundException ex) {
                Logger.log(Logger.LEVEL_ERROR, ex);
                JOptionPane.showMessageDialog(this,
                        "Error saving file '"+propsFile.getAbsolutePath()+"':\n"+ex.getMessage(),
                        "Warning!",
                        JOptionPane.WARNING_MESSAGE);
            } catch (IOException ex) {
                Logger.log(Logger.LEVEL_ERROR, ex);
                JOptionPane.showMessageDialog(this,
                        "Error saving file '"+propsFile.getAbsolutePath()+"':\n"+ex.getMessage(),
                        "Warning!",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    // Declaração de variáveis - não modifique//GEN-BEGIN:variables
    private javax.swing.JButton jBtnLoad;
    private javax.swing.JButton jBtnMainDir;
    private javax.swing.JButton jBtnSecDir;
    private javax.swing.JButton jBtnSynch;
    private javax.swing.JCheckBox jChkBoxHideEquals;
    private javax.swing.JCheckBox jChkBoxUseHash;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemHideEquals;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemIncSubdirs;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemKeepBackup;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemSynchTimesHash;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemUseHash;
    private javax.swing.JDialog jDialogHelp;
    private javax.swing.JEditorPane jEdtPaneHelp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLblStatusBar;
    private javax.swing.JMenuBar jMenuBarDirSynch;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemAll;
    private javax.swing.JMenuItem jMenuItemDirSynchHelp;
    private javax.swing.JMenuItem jMenuItemLoadOpt;
    private javax.swing.JMenuItem jMenuItemNewerInMain;
    private javax.swing.JMenuItem jMenuItemNewerInSec;
    private javax.swing.JMenuItem jMenuItemNone;
    private javax.swing.JMenuItem jMenuItemOnlyInMain;
    private javax.swing.JMenuItem jMenuItemOnlyInSec;
    private javax.swing.JMenuItem jMenuItemSaveOpt;
    private javax.swing.JMenuItem jMenuItemSelRegexp;
    private javax.swing.JMenuItem jMenuItemUnselRegexp;
    private javax.swing.JMenuItem jMenuItemYesNo;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuSelect;
    private javax.swing.JMenu jMenuTools;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneHelp;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTable jTableFiles;
    private javax.swing.JTextField jTxtFldMainDir;
    private javax.swing.JTextField jTxtFldSecDir;
    private javax.swing.JPanel statusBar;
    // Fim da declaração de variáveis//GEN-END:variables
    
}
