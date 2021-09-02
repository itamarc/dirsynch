/*
 * Copyright (C) 2021 Itamar Carvalho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package itamar.dirsynch.ui;

import itamar.dirsynch.DirSynchProperties;
import itamar.util.Logger;
import java.io.File;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Itamar Carvalho
 */
public class JPanelLogOpt extends javax.swing.JPanel {

    /**
     * Creates new form JPanelLogOpt
     */
    public JPanelLogOpt() {
        initComponents();
        jTxtFldLogFile.setText(DirSynchProperties.getLogFile());
        jCmbBoxLogLevel.setSelectedIndex(DirSynchProperties.getLogLevel());
        jChkBoxLogAppend.setSelected(DirSynchProperties.isLogFileAppend());
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

    public void saveLogOptions() {
        DirSynchProperties.setLogFile(jTxtFldLogFile.getText());
        DirSynchProperties.setLogLevel((short) jCmbBoxLogLevel.getSelectedIndex());
        DirSynchProperties.setLogFileAppend(jChkBoxLogAppend.isSelected());
        Logger.init((short) jCmbBoxLogLevel.getSelectedIndex(),
                jTxtFldLogFile.getText(), jChkBoxLogAppend.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelLogOpt = new javax.swing.JPanel();
        jLblLogFile = new javax.swing.JLabel();
        jTxtFldLogFile = new javax.swing.JTextField();
        jBtnLogFile = new javax.swing.JButton();
        jLblLogLevel = new javax.swing.JLabel();
        jCmbBoxLogLevel = new javax.swing.JComboBox();
        jChkBoxLogAppend = new javax.swing.JCheckBox();

        javax.swing.GroupLayout jPanelLogOptLayout = new javax.swing.GroupLayout(jPanelLogOpt);
        jPanelLogOpt.setLayout(jPanelLogOptLayout);
        jPanelLogOptLayout.setHorizontalGroup(
            jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 366, Short.MAX_VALUE)
        );
        jPanelLogOptLayout.setVerticalGroup(
            jPanelLogOptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 98, Short.MAX_VALUE)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLblLogFile)
                    .addComponent(jLblLogLevel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTxtFldLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jBtnLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCmbBoxLogLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jChkBoxLogAppend))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLblLogFile)
                    .addComponent(jTxtFldLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnLogFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCmbBoxLogLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblLogLevel))
                .addGap(6, 6, 6)
                .addComponent(jChkBoxLogAppend))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnLogFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLogFileActionPerformed
        selectLogFile();
    }//GEN-LAST:event_jBtnLogFileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnLogFile;
    private javax.swing.JCheckBox jChkBoxLogAppend;
    private javax.swing.JComboBox jCmbBoxLogLevel;
    private javax.swing.JLabel jLblLogFile;
    private javax.swing.JLabel jLblLogLevel;
    private javax.swing.JPanel jPanelLogOpt;
    private javax.swing.JTextField jTxtFldLogFile;
    // End of variables declaration//GEN-END:variables
}