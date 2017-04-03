package net.sourceforge.ganttproject.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class NewArtefactAction extends AbstractAction implements RolloverAction {

	private ActiveActionProvider myProvider;
	private Icon myIconOnMouseOver;

	public NewArtefactAction(ActiveActionProvider provider, String iconSize) {
		myProvider = provider;
        Icon icon = new ImageIcon(getClass().getResource("/icons/insert_"+iconSize+".gif"));
        if (icon!=null) {
            putValue(Action.SMALL_ICON, icon);            
        }
	}
	public void actionPerformed(ActionEvent e) {
		AbstractAction activeAction = myProvider.getActiveAction();
		activeAction.actionPerformed(e);
	}
	
	public static interface ActiveActionProvider {
		public AbstractAction getActiveAction();
	}

	public Icon getIconOnMouseOver() {
        return myIconOnMouseOver;
	}

}
