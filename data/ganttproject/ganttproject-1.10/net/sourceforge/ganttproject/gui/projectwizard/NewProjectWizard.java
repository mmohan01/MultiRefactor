package net.sourceforge.ganttproject.gui.projectwizard;

import javax.swing.JFrame;

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.roles.RoleSet;

public class NewProjectWizard {
    private final JFrame myMainFrame;
    
    public NewProjectWizard(JFrame mainFrame) {
        myMainFrame = mainFrame;
    }
    
    public PrjInfos createNewProject(IGanttProject project) {
        RoleSet[] roleSets = project.getRoleManager().getRoleSets();        
        NewProjectWizardWindow newProjectWizard = new NewProjectWizardWindow(myMainFrame, new I18N());
        newProjectWizard.addProjectNamePage(project);
        newProjectWizard.addRoleSetPage(roleSets);     
        newProjectWizard.show();
        return new PrjInfos();
    }
    
    
}
