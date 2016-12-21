/***************************************************************************
               FTPLSettingsPanel.java 
------------------------------------------
begin                : 16 Februar 2005
copyright            : (C) 2005 by Matthias Wronka
email                : matthias@wronka.info
***************************************************************************/

/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package net.sourceforge.ganttproject.gui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * This class extends the settings dialog with a panel to set ftp-Settings. This is needed to
 * provide a comfortable 1-click-method of publishing the ganttChart to a webserver.
 */
public class FTPSettingsPanel extends GeneralOptionPanel implements ActionListener {

    JButton buttonTest;
    JTextField tfURL, tfDirectory, tfUser, tfPwd;
    JLabel labelURL, labelDirectory, labelUser, labelPwd;

    private GanttProject appli;

    /** Constructor. */
    public FTPSettingsPanel(GanttProject parent) {
        super(
            GanttProject.correctLabel(GanttLanguage.getInstance().getText("ftpexport")),
            GanttLanguage.getInstance().getText("settingsFTPExport"),
            parent);

        appli = parent;

        JPanel ftpTheme = new JPanel(new SpringLayout());

        buttonTest = new JButton(GanttLanguage.getInstance().getText("testFTPConnection"));

        labelURL = new JLabel(GanttLanguage.getInstance().getText("ftpserver"), JLabel.TRAILING);
        tfURL = new JTextField();
        labelURL.setLabelFor(tfURL);

        labelDirectory = new JLabel(GanttLanguage.getInstance().getText("ftpdirectory"), JLabel.TRAILING);
        tfDirectory = new JTextField();
        labelDirectory.setLabelFor(tfDirectory);

        labelUser = new JLabel(GanttLanguage.getInstance().getText("ftpuser"), JLabel.TRAILING);
        tfUser = new JTextField();
        labelUser.setLabelFor(tfUser);

        labelPwd = new JLabel(GanttLanguage.getInstance().getText("ftppwd"), JLabel.TRAILING);
        tfPwd = new JPasswordField();
        labelPwd.setLabelFor(tfPwd);

        // add components to panel
        ftpTheme.add(labelURL);
        ftpTheme.add(tfURL);
        ftpTheme.add(labelDirectory);
        ftpTheme.add(tfDirectory);
        ftpTheme.add(labelUser);
        ftpTheme.add(tfUser);
        ftpTheme.add(labelPwd);
        ftpTheme.add(tfPwd);
        ftpTheme.add(buttonTest);

        SpringUtilities.makeCompactGrid(ftpTheme, 4, 2, //rows, cols
        6, 6, //initX, initY
        6, 6); //xPad, yPad

        vb.add(ftpTheme);
        vb.add(buttonTest);
        vb.add(new JPanel());

        buttonTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuffer urlString = new StringBuffer();
                urlString.append("ftp://");
                urlString.append(tfUser.getText());
                urlString.append(":");
                urlString.append(tfPwd.getText());
                urlString.append("@");
                urlString.append(tfURL.getText());
                urlString.append("/");
                urlString.append(tfDirectory.getText());
                urlString.append("/");
                URL url = null;
                try {
                    url = new URL(urlString.toString()+"test.txt");
                    URLConnection urlc = url.openConnection();
                    OutputStream os = urlc.getOutputStream();
                    os.write(("This is GanttProject +++ I was here!").getBytes());
                    os.close();
                    JOptionPane.showMessageDialog(
                        vb,
                        GanttLanguage.getInstance().getText("successFTPConnection"),
                        GanttLanguage.getInstance().getText("success"),
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e2) {
                    JOptionPane.showMessageDialog(
                        vb,
                        GanttLanguage.getInstance().getText("errorFTPConnection"),
                        GanttLanguage.getInstance().getText("error"),
                        JOptionPane.ERROR_MESSAGE);
                } finally {

                }
            }
        });

        vb.add(new JPanel());

        applyComponentOrientation(language.getComponentOrientation());
    }

    /** This method check if the value has changed, and assk for commit changes. */
    public boolean applyChanges(boolean askForApply) {
        if (appli.getOptions().getFTPUrl().equals(tfURL.getText())
            && appli.getOptions().getFTPDirectory().equals(tfDirectory.getText())
            && appli.getOptions().getFTPUser().equals(tfUser.getText())
            && appli.getOptions().getFTPPwd().equals(tfPwd.getText())) {
            bHasChange = false;
        } else {
            bHasChange = true;
            if (!askForApply || (askForApply && askForApplyChanges())) {
                appli.getOptions().setFTPUrl(tfURL.getText());
                appli.getOptions().setFTPDirectory(tfDirectory.getText());
                appli.getOptions().setFTPUser(tfUser.getText());
                appli.getOptions().setFTPPwd(tfPwd.getText());
            }
        }
        return bHasChange;

    }

    /** Initialize the component. */
    public void initialize() {
        try {
            //tfXsl.setText(GanttProject.xslDir!=null?GanttProject.xslDir:System.getProperty("user.home"));
            tfURL.setText(appli.getOptions().getFTPUrl());
            tfDirectory.setText(appli.getOptions().getFTPDirectory());
            tfUser.setText(appli.getOptions().getFTPUser());
            tfPwd.setText(appli.getOptions().getFTPPwd());
        } catch (AccessControlException e) {
            // This can happen when running in a sandbox (Java WebStart)
            System.err.println(e + ": " + e.getMessage());
        }
    }

    /** Return the xsl directory */
    public String getXslDirectory(boolean useDefault) {
        return "HalloString";
    }

    /** Action callback. */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonTest) {
            bHasChange = true;
        }
    }

}
