package net.sourceforge.ganttproject.gui.projectwizard;

import java.awt.Frame;

import javax.swing.JFrame;

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.roles.RoleSet;

public class NewProjectWizardWindow extends WizardImpl {
    private I18N myI18n;

    public NewProjectWizardWindow(JFrame frame, I18N i18n) {
        super(frame, i18n.getNewProjectWizardWindowTitle());
        myI18n = i18n;
    }

    public void addRoleSetPage(RoleSet[] roleSets) {
        WizardPage roleSetPage = new RoleSetPage(roleSets, myI18n);
        addPage(roleSetPage);
    }

    public void addProjectNamePage(IGanttProject project) {
        WizardPage projectNamePage = new ProjectNamePage((Frame)getOwner(), project,myI18n);
        addPage(projectNamePage);
    }
    
    
}
