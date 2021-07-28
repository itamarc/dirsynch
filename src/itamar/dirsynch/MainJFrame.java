/*
 * MainJFrame.java
 *
 * Created on 3 de Agosto de 2006, 20:32
 */
package itamar.dirsynch;

import static itamar.dirsynch.DirSynchProperties.getMainDir;
import static itamar.dirsynch.DirSynchProperties.getPropertiesAsString;
import static itamar.dirsynch.DirSynchProperties.getSecDir;
import static itamar.dirsynch.DirSynchProperties.isSubDirsInclude;
import static itamar.dirsynch.DirSynchProperties.setHashEnabled;
import static itamar.dirsynch.DirSynchProperties.setHideEquals;
import static itamar.dirsynch.DirSynchProperties.setMainDir;
import static itamar.dirsynch.DirSynchProperties.setSecDir;
import static itamar.dirsynch.DirSynchProperties.setSubDirsInclude;
import static itamar.dirsynch.DirSynchProperties.setSynchTimesSameHash;
import static itamar.dirsynch.FilePair.MAIN_NEWER;
import static itamar.dirsynch.FilePair.ONLY_MAIN;
import static itamar.dirsynch.FilePair.ONLY_SEC;
import static itamar.dirsynch.FilePair.SEC_NEWER;
import itamar.util.Logger;
import static itamar.util.Logger.LEVEL_DEBUG;
import static itamar.util.Logger.LEVEL_ERROR;
import static itamar.util.Logger.LEVEL_INFO;
import static itamar.util.Logger.LEVEL_WARNING;
import static itamar.util.Logger.log;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.getDefaultUncaughtExceptionHandler;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import java.util.regex.PatternSyntaxException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author  Itamar Carvalho
 */
public class MainJFrame extends javax.swing.JFrame {

    private File mainDir;
    private File secDir;
    private static String defaultMainDirPath = null;
    private static String defaultSecDirPath = null;
    private static final String VERSION = "1.7alpha2";
    private static boolean defaultKeep = false;
    private boolean firstLoad = true;
    private boolean firstInit = true;
    private final String helpFile = "DirSynch-help.html";
    private static String propertiesFilePath = "DirSynch.properties";
    static DirSynchExceptionHandler handler = null;

    /** Creates new form MainJFrame */
    public MainJFrame() {
        initDirSynchProperties();
        initComponents();
        setTitle("DirSynch " + VERSION);
        jTableFiles.getColumn("Sel").setMaxWidth(30);
        jTableFiles.getColumn("Main").setMaxWidth(30);
        jTableFiles.getColumn("Sec").setMaxWidth(30);
        setStatusBarText(FilePair.getLegend());

        ListSelectionModel rowSM = jTableFiles.getSelectionModel();
        rowSM.addListSelectionListener((ListSelectionEvent e) -> {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    //System.out.println("No rows are selected.");
                    setStatusBarText(FilePair.getLegend());
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
    //                    System.out.println("Row " + selectedRow
    //                            + " is now selected.");
                    updateStatus();
                }
            });

        initOptions();
        firstInit = false;
    }

    private void initDirSynchProperties() {
        try {
            DirSynchProperties.init(propertiesFilePath);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(); // Logger is not initialized at this point
            showMessageDialog(this, "File '" + propertiesFilePath + "' not found!", "Warning!", WARNING_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace(); // Logger is not initialized at this point
            showMessageDialog(this, "Error reading file '" + propertiesFilePath + "':\n" + ex.getMessage(), "Warning!",
                    WARNING_MESSAGE);
        }
        if (!DirSynchProperties.getLogFile().equals(Logger.getLogFile())) {
            firstInit = true;
        }
        Logger.init(DirSynchProperties.getLogLevel(), DirSynchProperties.getLogFile(),
                DirSynchProperties.isLogFileAppend());
        if (firstInit) {
            log(LEVEL_INFO, "==========  DirSynch v" + VERSION + " started.  ==========");
        }
        log(LEVEL_INFO, "Properties initialized with file '" + propertiesFilePath + "'");
        log(LEVEL_DEBUG, "Properties read: " + getPropertiesAsString());
    }

    private void initOptions() {
        // defaultXXXDirPath comes from command-line parameters and has priority over .properties file
        setMainDirPath((defaultMainDirPath == null ? getMainDir() : defaultMainDirPath));
        setSecDirPath((defaultSecDirPath == null ? getSecDir() : defaultSecDirPath));

        log(LEVEL_DEBUG, "SubDirsInclude=" + isSubDirsInclude());
        jChkBxMenuItemUseHash.setSelected(DirSynchProperties.isHashEnabled());
        jChkBxMenuItemIncSubdirs.setSelected(isSubDirsInclude());
        jChkBxMenuItemHideEquals.setSelected(DirSynchProperties.isHideEquals());
        jChkBxMenuItemSynchTimesHash.setSelected(DirSynchProperties.isSynchTimesSameHash());

        jChkBxMenuItemKeepBackup.setSelected(defaultKeep);
    }

    private void selectLogFile() {
        String userDir = System.getProperty("user.dir");
        JFileChooser fileDiag = new JFileChooser(userDir);
        FileFilter ff = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    return f.getName().endsWith(".log");
                } else {
                    return true;
                }
            }

            @Override
            public String getDescription() {
                return "Log Files (*.log)";
            }
        };
        fileDiag.setFileFilter(ff);
        fileDiag.setDialogTitle("Select log file...");
        int ret = fileDiag.showOpenDialog(this);
        if (ret == APPROVE_OPTION) {
            File logFile = fileDiag.getSelectedFile();
            if (logFile.getParent().equals(userDir)) {
                jTxtFldLogFile.setText(logFile.getName());
            } else {
                jTxtFldLogFile.setText(logFile.getAbsolutePath());
            }
        }
    }

    private void setOptionsInProps() {
        setMainDir(getMainDirPath());
        setSecDir(getSecDirPath());
        setHashEnabled(isHashEnabled());
        setSubDirsInclude(isIncludeSubdirs());
        setHideEquals(isHideEquals());
        setSynchTimesSameHash(isSynchTimesSameHash());
//        jChkBxMenuItemKeepBackup.setSelected(defaultKeep);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialogHelp = new javax.swing.JDialog();
        jScrollPaneHelp = new javax.swing.JScrollPane();
        jEdtPaneHelp = new javax.swing.JEditorPane();
        jPanelLogOpt = new javax.swing.JPanel();
        jLblLogFile = new javax.swing.JLabel();
        jTxtFldLogFile = new javax.swing.JTextField();
        jBtnLogFile = new javax.swing.JButton();
        jLblLogLevel = new javax.swing.JLabel();
        jCmbBoxLogLevel = new javax.swing.JComboBox();
        jChkBoxLogAppend = new javax.swing.JCheckBox();
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
        jToolBar1 = new javax.swing.JToolBar();
        jBtnSelAll = new javax.swing.JButton();
        jBtnUnselAll = new javax.swing.JButton();
        jBtnSelOnlyMain = new javax.swing.JButton();
        jBtnSelOnlySec = new javax.swing.JButton();
        jBtnSelNewerMain = new javax.swing.JButton();
        jBtnSelNewerSec = new javax.swing.JButton();
        jBtnSelRegexp = new javax.swing.JButton();
        jBtnUnselRegexp = new javax.swing.JButton();
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
        jMenuItemLogOpt = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
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

        jEdtPaneHelp.setContentType("text/html");
        jEdtPaneHelp.setEditable(false);
        jScrollPaneHelp.setViewportView(jEdtPaneHelp);

        javax.swing.GroupLayout jDialogHelpLayout = new javax.swing.GroupLayout(jDialogHelp.getContentPane());
        jDialogHelp.getContentPane().setLayout(jDialogHelpLayout);
        jDialogHelpLayout.setHorizontalGroup(
            jDialogHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jDialogHelpLayout.setVerticalGroup(
            jDialogHelpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialogHelpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLblLogFile.setText("Log file:");

        jBtnLogFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dir.png"))); // NOI18N
        jBtnLogFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLogFileActionPerformed(evt);
            }
        });

        jLblLogLevel.setText("Log level:");

        jCmbBoxLogLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Error", "Warning", "Info", "Debug" }));
        jCmbBoxLogLevel.setSelectedIndex(2);

        jChkBoxLogAppend.setText("Append to file if exists (don't overwrite).");

        javax.swing.GroupLayout jPanelLogOptLayout = new javax.swing.GroupLayout(jPanelLogOpt);
        jPanelLogOpt.setLayout(jPanelLogOptLayout);
        jPanelLogOptLayout.setHorizontalGroup(
            jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLogOptLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLblLogFile)
                    .addComponent(jLblLogLevel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelLogOptLayout.createSequentialGroup()
                        .addComponent(jTxtFldLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCmbBoxLogLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChkBoxLogAppend))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelLogOptLayout.setVerticalGroup(
            jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLogOptLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblLogFile)
                    .addComponent(jTxtFldLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnLogFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCmbBoxLogLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblLogLevel))
                .addGap(6, 6, 6)
                .addComponent(jChkBoxLogAppend)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLblStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
                .addContainerGap())
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLblStatusBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
        );

        jBtnMainDir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dir.png"))); // NOI18N
        jBtnMainDir.setToolTipText("Select main dir...");
        jBtnMainDir.setIconTextGap(0);
        jBtnMainDir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBtnMainDir.setMinimumSize(new java.awt.Dimension(20, 20));
        jBtnMainDir.setName("jBtnMainDir"); // NOI18N
        jBtnMainDir.setPreferredSize(new java.awt.Dimension(20, 20));
        jBtnMainDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnMainDirActionPerformed(evt);
            }
        });

        jBtnSecDir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/dir.png"))); // NOI18N
        jBtnSecDir.setToolTipText("Select second dir...");
        jBtnSecDir.setIconTextGap(0);
        jBtnSecDir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBtnSecDir.setMinimumSize(new java.awt.Dimension(20, 20));
        jBtnSecDir.setName("jBtnSecDir"); // NOI18N
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
                {false, null, null, null}
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

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jBtnSelAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-sel-all.png"))); // NOI18N
        jBtnSelAll.setToolTipText("Select all");
        jBtnSelAll.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnSelAll.setFocusable(false);
        jBtnSelAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnSelAll.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnSelAll.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnSelAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSelAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelAllActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnSelAll);

        jBtnUnselAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-unsel-all.png"))); // NOI18N
        jBtnUnselAll.setToolTipText("Unselect all");
        jBtnUnselAll.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnUnselAll.setFocusable(false);
        jBtnUnselAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnUnselAll.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnUnselAll.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnUnselAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnUnselAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnUnselAllActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnUnselAll);

        jBtnSelOnlyMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-sel-onlymain.png"))); // NOI18N
        jBtnSelOnlyMain.setToolTipText("Select only in main dir");
        jBtnSelOnlyMain.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnSelOnlyMain.setFocusable(false);
        jBtnSelOnlyMain.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnSelOnlyMain.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnSelOnlyMain.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnSelOnlyMain.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSelOnlyMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelOnlyMainActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnSelOnlyMain);

        jBtnSelOnlySec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-sel-onlysec.png"))); // NOI18N
        jBtnSelOnlySec.setToolTipText("Select only in sec dir");
        jBtnSelOnlySec.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnSelOnlySec.setFocusable(false);
        jBtnSelOnlySec.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnSelOnlySec.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnSelOnlySec.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnSelOnlySec.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSelOnlySec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelOnlySecActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnSelOnlySec);

        jBtnSelNewerMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-sel-newermain.png"))); // NOI18N
        jBtnSelNewerMain.setToolTipText("Select newer in main dir");
        jBtnSelNewerMain.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnSelNewerMain.setFocusable(false);
        jBtnSelNewerMain.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnSelNewerMain.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnSelNewerMain.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnSelNewerMain.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSelNewerMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelNewerMainActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnSelNewerMain);

        jBtnSelNewerSec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-sel-newersec.png"))); // NOI18N
        jBtnSelNewerSec.setToolTipText("Select newer in sec dir");
        jBtnSelNewerSec.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnSelNewerSec.setFocusable(false);
        jBtnSelNewerSec.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnSelNewerSec.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnSelNewerSec.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnSelNewerSec.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSelNewerSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelNewerSecActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnSelNewerSec);

        jBtnSelRegexp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-sel-regexp.png"))); // NOI18N
        jBtnSelRegexp.setToolTipText("Select with regexp...");
        jBtnSelRegexp.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnSelRegexp.setFocusable(false);
        jBtnSelRegexp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnSelRegexp.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnSelRegexp.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnSelRegexp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnSelRegexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSelRegexpActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnSelRegexp);

        jBtnUnselRegexp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/toolbar-unsel-regexp.png"))); // NOI18N
        jBtnUnselRegexp.setToolTipText("Unselect with regexp...");
        jBtnUnselRegexp.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jBtnUnselRegexp.setFocusable(false);
        jBtnUnselRegexp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBtnUnselRegexp.setMaximumSize(new java.awt.Dimension(33, 31));
        jBtnUnselRegexp.setMinimumSize(new java.awt.Dimension(33, 31));
        jBtnUnselRegexp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBtnUnselRegexp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnUnselRegexpActionPerformed(evt);
            }
        });
        jToolBar1.add(jBtnUnselRegexp);

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

        jMenuItemLogOpt.setText("Log options...");
        jMenuItemLogOpt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLogOptActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemLogOpt);
        jMenuOptions.add(jSeparator5);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTxtFldMainDir, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnMainDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTxtFldSecDir, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnSecDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jBtnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnSynch, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 213, Short.MAX_VALUE)
                        .addComponent(jChkBoxUseHash)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jChkBoxHideEquals)))
                .addContainerGap())
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jTxtFldMainDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jBtnMainDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTxtFldSecDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSecDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBtnLoad)
                        .addComponent(jBtnSynch))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jChkBoxHideEquals)
                        .addComponent(jChkBoxUseHash)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
	selectFilesByStatus(SEC_NEWER);
    }//GEN-LAST:event_jMenuItemNewerInSecActionPerformed

    private void jMenuItemNewerInMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewerInMainActionPerformed
	selectFilesByStatus(MAIN_NEWER);
    }//GEN-LAST:event_jMenuItemNewerInMainActionPerformed

    private void jMenuItemOnlyInSecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOnlyInSecActionPerformed
	selectFilesByStatus(ONLY_SEC);
    }//GEN-LAST:event_jMenuItemOnlyInSecActionPerformed

    private void jMenuItemOnlyInMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOnlyInMainActionPerformed
	selectFilesByStatus(ONLY_MAIN);
    }//GEN-LAST:event_jMenuItemOnlyInMainActionPerformed

    private void jMenuItemYesNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemYesNoActionPerformed
	selectFilesByStatus(new short[]{ONLY_MAIN, ONLY_SEC});
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
	    jEdtPaneHelp.setPage("file:///" + System.getProperty("user.dir") + File.separator + helpFile);
	    jEdtPaneHelp.setCaretPosition(0);
	    jDialogHelp.pack();
	    jDialogHelp.setVisible(true);
	} catch (IOException ex) {
	    log(LEVEL_ERROR, ex);
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

private void jMenuItemLogOptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLogOptActionPerformed
    jTxtFldLogFile.setText(DirSynchProperties.getLogFile());
    jCmbBoxLogLevel.setSelectedIndex(DirSynchProperties.getLogLevel());
    jChkBoxLogAppend.setSelected(DirSynchProperties.isLogFileAppend());
    if (showConfirmDialog(this, jPanelLogOpt, "Log options", OK_CANCEL_OPTION, PLAIN_MESSAGE)
	    == OK_OPTION) {
            DirSynchProperties.setLogFile(jTxtFldLogFile.getText());
            DirSynchProperties.setLogLevel((short)jCmbBoxLogLevel.getSelectedIndex());
            DirSynchProperties.setLogFileAppend(jChkBoxLogAppend.isSelected());
            Logger.init((short)jCmbBoxLogLevel.getSelectedIndex(),
		jTxtFldLogFile.getText(), jChkBoxLogAppend.isSelected());
    }
}//GEN-LAST:event_jMenuItemLogOptActionPerformed

private void jBtnLogFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLogFileActionPerformed
    selectLogFile();
}//GEN-LAST:event_jBtnLogFileActionPerformed

private void jBtnSelAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelAllActionPerformed
	selectAllFiles(true);
}//GEN-LAST:event_jBtnSelAllActionPerformed

private void jBtnUnselAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnUnselAllActionPerformed
	selectAllFiles(false);
}//GEN-LAST:event_jBtnUnselAllActionPerformed

private void jBtnSelOnlyMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelOnlyMainActionPerformed
	selectFilesByStatus(ONLY_MAIN);
}//GEN-LAST:event_jBtnSelOnlyMainActionPerformed

private void jBtnSelOnlySecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelOnlySecActionPerformed
	selectFilesByStatus(ONLY_SEC);
}//GEN-LAST:event_jBtnSelOnlySecActionPerformed

private void jBtnSelNewerMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelNewerMainActionPerformed
	selectFilesByStatus(MAIN_NEWER);
}//GEN-LAST:event_jBtnSelNewerMainActionPerformed

private void jBtnSelNewerSecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelNewerSecActionPerformed
	selectFilesByStatus(SEC_NEWER);
}//GEN-LAST:event_jBtnSelNewerSecActionPerformed

private void jBtnSelRegexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSelRegexpActionPerformed
	selectWithRegexp(true);
}//GEN-LAST:event_jBtnSelRegexpActionPerformed

private void jBtnUnselRegexpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnUnselRegexpActionPerformed
	selectWithRegexp(false);
}//GEN-LAST:event_jBtnUnselRegexpActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	System.setProperty("sun.awt.exception.handler",
	    DirSynchExceptionHandler.class.getName());
	handler = new DirSynchExceptionHandler();
	setDefaultUncaughtExceptionHandler(handler);
	currentThread().setUncaughtExceptionHandler(handler);
        if (processParams(args)) {
            EventQueue.invokeLater(() -> {
                currentThread().setUncaughtExceptionHandler(handler);
                if (currentThread().getUncaughtExceptionHandler() != getDefaultUncaughtExceptionHandler()) {
                    System.err.println("UEH=" + currentThread().getUncaughtExceptionHandler().getClass().getName() + " DefaultUEH=" + getDefaultUncaughtExceptionHandler().getClass().getName());
                }
                new MainJFrame().setVisible(true);
            });
        }
    }
    
    private static boolean processParams(String[] args) {
        boolean continueAfterThis = true;
        try {
            for (int i = 0; i < args.length; i++) {
                if (null == args[i]) {
                    System.out.println("Incorrect parameters!\n");
                    showUsage();
                    continueAfterThis = false;
                } else switch (args[i]) {
                    case "-main":
                        defaultMainDirPath = args[++i];
                        break;
                    case "-sec":
                        defaultSecDirPath = args[++i];
                        break;
                    case "-prop":
                        propertiesFilePath = args[++i];
                        break;
                    case "-keep":
                        defaultKeep = true;
                        break;
                    case "-help":
                    case "-h":
                    case "/?":
                    case "--help":
                        showUsage();
                        continueAfterThis = false;
                        break;
                    default:
                        System.out.println("Incorrect parameters!\n");
                        showUsage();
                        continueAfterThis = false;
                        break;
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
        System.out.println("DirSynch "+VERSION+"\n"+(char)184+" 2006-2021 Itamar Carvalho <itamarc at gmail.com>\n");
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
        chooser.setFileSelectionMode(DIRECTORIES_ONLY);
        if ("jBtnMainDir".equals(buttonName)) {
            chooser.setCurrentDirectory(new File(getMainDirPath()));
        } else {
            chooser.setCurrentDirectory(new File(getSecDirPath()));
        }
        //DirFileFilter filter = new DirFileFilter();
        //chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == APPROVE_OPTION) {
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
        log(LEVEL_DEBUG, "Main dir: "+getMainDirPath());
        if (!mainDir.isDirectory() || !mainDir.canRead()) {
            showMessageDialog(this,
                    "Directory '" + mainDir + "' does not exist or can't be read.\nCheck the paths!",
                    "Warning!", WARNING_MESSAGE);
	    log(LEVEL_INFO, "Main dir '" + mainDir + "' does not exist or can't be read.");
            return false;
        } else {
            secDir = new File(getSecDirPath());
            log(LEVEL_DEBUG, "Sec dir: "+getSecDirPath());
            if (!secDir.isDirectory() || !secDir.canRead()) {
                showMessageDialog(this,
                        "Directory '" + secDir + "' does not exist or can't be read.\nCheck the paths!",
                        "Warning!", WARNING_MESSAGE);
		log(LEVEL_INFO, "Second dir '" + secDir + "' does not exist or can't be read.");
                return false;
            } else {
                return true;
            }
        }
    }
    
    protected String getMainDirPath() {
        return jTxtFldMainDir.getText();
    }
    private void setMainDirPath(String path) {
        jTxtFldMainDir.setText(path);
    }
    
    protected String getSecDirPath() {
        return jTxtFldSecDir.getText();
    }
    private void setSecDirPath(String path) {
        jTxtFldSecDir.setText(path);
    }
    
    private void compareDirs()
    throws IOException, NoSuchAlgorithmException {
        final MainJFrame mainFrame = this;
        new DirComparator(mainFrame).execute();
    }
    
    private boolean getHideEquals() {
        return jChkBoxHideEquals.isSelected();
    }
    
    private void synchronize() {
        final MainJFrame mainFrame = this;
	new Synchronizer(mainFrame).execute();
    }
    /**
     * Get the files selected by the user to synchronize.
     * @return A Vector of FilePair with the selected files in the table.
     */
    protected Vector<FilePair> getSelectedFiles() {
	Vector<FilePair> selectedFiles = new Vector<>();
	Vector<FilePair> files = ((FileVOTableModel) jTableFiles.getModel()).getFiles();
	jTableFiles.setEnabled(false);
	for (int i = 0; i < files.size(); i++) {
	    jTableFiles.setRowSelectionInterval(i, i);
	    Boolean selected = (Boolean) jTableFiles.getModel().getValueAt(i, 0);
	    if (selected) {
		selectedFiles.add(files.get(i));
	    }
	}
	jTableFiles.setEnabled(true);
	return selectedFiles;
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
    
    protected void setFilesInTable(Vector<FilePair> files) {
	((FileVOTableModel) jTableFiles.getModel()).setFiles(files, getHideEquals());
    }

    protected void load() {
        if (dirsOk()) {
            try {
                compareDirs();
                firstLoad = false;
            } catch (IOException ex) {
                showMessageDialog(this,
                        ex.getClass().getName() + ": " + ex.getMessage(),
                        "I/O Error!", ERROR_MESSAGE);
                log(LEVEL_ERROR, ex);
            } catch (NoSuchAlgorithmException ex) {
                showMessageDialog(this,
                        ex.getClass().getName() + ": " + ex.getMessage(),
                        "Weird Error!", ERROR_MESSAGE);
                log(LEVEL_ERROR, ex);
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
        showMessageDialog(this,
                "DirSynch "+VERSION+"\nhttps://itamarc.github.io/dirsynch/\n"+
                        "\u00A9 2007-2021 Itamar Carvalho <itamarc+dirsynch AT gmail\u00B7com>\n\n"+
                        "This software is released under the GNU General Public License version 3.\n"+
                        "https://www.gnu.org/licenses/gpl-3.0.txt",
                "About DirSynch",
                INFORMATION_MESSAGE);
    }
    
    private void selectAllFiles(boolean checked) {
        // If the action is to uncheck all or if the "hide equals" option is on, do all
        if (!checked || getHideEquals()) {
            for (int i = 0; i < ((FileVOTableModel)jTableFiles.getModel()).getRowCount(); i++) {
                jTableFiles.getModel().setValueAt(checked, i, 0);
            }
        } else {  // Select only the different files
            Vector files = ((FileVOTableModel)jTableFiles.getModel()).getFiles();
            for (int i = 0; i < files.size(); i++) {
                if (!((FilePair)files.get(i)).isEquals()) {
                    jTableFiles.getModel().setValueAt(checked, i, 0);
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
    
    protected boolean isIncludeSubdirs() {
        return jChkBxMenuItemIncSubdirs.isSelected();
    }
    
    protected boolean isHashEnabled() {
        return jChkBxMenuItemUseHash.isSelected();
    }
    
    protected boolean isHideEquals() {
        return jChkBxMenuItemHideEquals.isSelected();
    }
    
    protected boolean isSynchTimesSameHash() {
        return jChkBxMenuItemSynchTimesHash.isEnabled() &&
		jChkBxMenuItemSynchTimesHash.isSelected();
    }
    
    protected boolean isKeepBackup() {
	return jChkBxMenuItemKeepBackup.isSelected();
    }

    private void selectWithRegexp(boolean select) {
        String regexp = showInputDialog(this, "Regular expression:", "Regexp", QUESTION_MESSAGE);
        Pattern regPat = null;
        while (regPat == null) {
            try {
                regPat = Pattern.compile(regexp, CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                log(LEVEL_WARNING, "Pattern invalid: "+e.getMessage());
                showMessageDialog(this,
                        "Pattern invalid: "+e.getMessage(),
                        "Warning", WARNING_MESSAGE);
                regexp = showInputDialog(this, "Regular expression:", regexp);
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
			@Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    return f.getName().endsWith(".properties");
                } else {
                    return true;
                }
            }
			@Override
            public String getDescription() {
                return "Properties Files (*.properties)";
            }
        };
        fileDiag.setFileFilter(ff);
        fileDiag.setDialogTitle("Load options from file...");
        int ret = fileDiag.showOpenDialog(this);
        if (ret == APPROVE_OPTION) {
            File propsFile = fileDiag.getSelectedFile();
            log(LEVEL_DEBUG, "File selected to open: "+propsFile.getAbsolutePath());
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
			@Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    return f.getName().endsWith(".properties");
                } else {
                    return true;
                }
            }
			@Override
            public String getDescription() {
                return "Properties Files (*.properties)";
            }
        };
        fileDiag.setFileFilter(ff);
        fileDiag.setDialogTitle("Save options to file...");
        int ret = fileDiag.showSaveDialog(this);
        if (ret == APPROVE_OPTION) {
            File propsFile = fileDiag.getSelectedFile();
            log(LEVEL_DEBUG, "File selected to save: "+propsFile.getAbsolutePath());
            if (!propsFile.getName().endsWith(".properties")) {
                String newFile = propsFile.getAbsoluteFile()+".properties";
                propsFile = new File(newFile);
            }
            try {
                DirSynchProperties.save(propsFile);
            } catch (FileNotFoundException ex) {
                log(LEVEL_ERROR, ex);
                showMessageDialog(this,
                        "Error saving file '"+propsFile.getAbsolutePath()+"':\n"+ex.getMessage(),
                        "Warning!", WARNING_MESSAGE);
            } catch (IOException ex) {
                log(LEVEL_ERROR, ex);
                showMessageDialog(this,
                        "Error saving file '"+propsFile.getAbsolutePath()+"':\n"+ex.getMessage(),
                        "Warning!", WARNING_MESSAGE);
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnLoad;
    private javax.swing.JButton jBtnLogFile;
    private javax.swing.JButton jBtnMainDir;
    private javax.swing.JButton jBtnSecDir;
    private javax.swing.JButton jBtnSelAll;
    private javax.swing.JButton jBtnSelNewerMain;
    private javax.swing.JButton jBtnSelNewerSec;
    private javax.swing.JButton jBtnSelOnlyMain;
    private javax.swing.JButton jBtnSelOnlySec;
    private javax.swing.JButton jBtnSelRegexp;
    private javax.swing.JButton jBtnSynch;
    private javax.swing.JButton jBtnUnselAll;
    private javax.swing.JButton jBtnUnselRegexp;
    private javax.swing.JCheckBox jChkBoxHideEquals;
    private javax.swing.JCheckBox jChkBoxLogAppend;
    private javax.swing.JCheckBox jChkBoxUseHash;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemHideEquals;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemIncSubdirs;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemKeepBackup;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemSynchTimesHash;
    private javax.swing.JCheckBoxMenuItem jChkBxMenuItemUseHash;
    private javax.swing.JComboBox jCmbBoxLogLevel;
    private javax.swing.JDialog jDialogHelp;
    private javax.swing.JEditorPane jEdtPaneHelp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLblLogFile;
    private javax.swing.JLabel jLblLogLevel;
    private javax.swing.JLabel jLblStatusBar;
    private javax.swing.JMenuBar jMenuBarDirSynch;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemAll;
    private javax.swing.JMenuItem jMenuItemDirSynchHelp;
    private javax.swing.JMenuItem jMenuItemLoadOpt;
    private javax.swing.JMenuItem jMenuItemLogOpt;
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
    private javax.swing.JPanel jPanelLogOpt;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneHelp;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTable jTableFiles;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextField jTxtFldLogFile;
    private javax.swing.JTextField jTxtFldMainDir;
    private javax.swing.JTextField jTxtFldSecDir;
    private javax.swing.JPanel statusBar;
    // End of variables declaration//GEN-END:variables
   
}
