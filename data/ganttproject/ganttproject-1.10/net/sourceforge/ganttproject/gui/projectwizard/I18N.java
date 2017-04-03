package net.sourceforge.ganttproject.gui.projectwizard;

import java.text.MessageFormat;

import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.roles.Role;

public class I18N {

    public String getNewProjectWizardWindowTitle() {
        return GanttLanguage.getInstance().getText("createNewProject");
    }

    public String getProjectDomainPageTitle() {
    	return GanttLanguage.getInstance().getText("selectProjectDomain");
    }

    public String getRolesetTooltipHeader(String roleSetName) {
        return MessageFormat.format("<html><body><h3>{0}</h3><ul>", new String[] {roleSetName});
    }

    public String getRolesetTooltipFooter() {
        return "</ul></body></html>";
    }
    
    public String formatRoleForTooltip(Role role) {
        return MessageFormat.format("<li>{0}</li>", new String[] {role.getName()});
    }
    
}
