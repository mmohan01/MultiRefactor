package net.sourceforge.ganttproject.gui.projectwizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sourceforge.ganttproject.gui.DialogAligner;
import net.sourceforge.ganttproject.gui.options.TopPanel;
import net.sourceforge.ganttproject.language.GanttLanguage;

abstract class WizardImpl extends JDialog {
    public class OkAction extends AbstractAction{
        OkAction() {
        	super(GanttLanguage.getInstance().getText("ok"));
        }
        public void actionPerformed(ActionEvent e) {        	
            WizardImpl.this.setVisible(false);
            WizardImpl.this.dispose();
            getCurrentPage().setActive(false);
        }

    }
    public class NextAction extends AbstractAction {
    	NextAction() {
    		super(GanttLanguage.getInstance().getText("next"));
    	}
		public void actionPerformed(ActionEvent e) {
			WizardImpl.this.nextPage();
		}
    	
    }
    public class BackAction extends AbstractAction {
    	BackAction() {
    		super(GanttLanguage.getInstance().getText("back"));
    	}
		public void actionPerformed(ActionEvent e) {
			WizardImpl.this.backPage();
		}
    	
    }
    private final ArrayList myPages = new ArrayList();
    private int myCurrentPage;
    private JPanel myPagesContainer;
	private CardLayout myCardLayout;
    private NextAction myNextAction;
    private BackAction myBackAction;
    
    public WizardImpl(JFrame frame, String title) {
        super(frame, title, true);
        myCardLayout = new CardLayout();
        myPagesContainer= new JPanel(myCardLayout);
        myNextAction = new NextAction();
        myBackAction = new BackAction();
    }
   
	public void nextPage() {
		if (myCurrentPage<myPages.size()-1) {
			getCurrentPage().setActive(false);
			myCurrentPage++;
			getCurrentPage().setActive(true);
			myCardLayout.next(myPagesContainer);
		}
		adjustButtonState();
	}

	public void backPage() {
		if (myCurrentPage>0) {
			getCurrentPage().setActive(false);
			myCurrentPage--;
			getCurrentPage().setActive(true);
			myCardLayout.previous(myPagesContainer);
		}
		adjustButtonState();
	}
	
	public void show() {
    	for (int i=0; i<myPages.size(); i++) {
    		WizardPage nextPage = (WizardPage)myPages.get(i);
    		//
    		JPanel pagePanel = new JPanel(new BorderLayout());
    		TopPanel titlePanel = new TopPanel(nextPage.getTitle(), null);
            pagePanel.add(titlePanel, BorderLayout.NORTH);
            pagePanel.add(nextPage.getComponent());
            //
    		myPagesContainer.add(pagePanel, nextPage.getTitle());
    	}
    	myCardLayout.first(myPagesContainer);
        Box buttonBox = Box.createHorizontalBox();
        JButton backButton = createBackButton();
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalGlue());
        JButton nextButton = createNextButton();
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalGlue());
        JButton okButton = createOkButton();
        buttonBox.add(okButton);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(myPagesContainer, BorderLayout.CENTER);        
        //
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(buttonBox, BorderLayout.EAST);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();        
        //setSize(300, 300);        
        adjustButtonState();
        DialogAligner.center(this, getParent());
        super.show();
    }

	private void adjustButtonState() {
		myBackAction.setEnabled(true);
		myNextAction.setEnabled(true);
		if (myCurrentPage==0) {
			myBackAction.setEnabled(false);			
		}
		if (myCurrentPage==myPages.size()-1) {
			myNextAction.setEnabled(false);
		}
	}

	private JButton createNextButton() {
		return new JButton(myNextAction);
	}

	private JButton createBackButton() {
		return new JButton(myBackAction);
	}

	private JButton createOkButton() {
        JButton result = new JButton(new OkAction());
        getRootPane().setDefaultButton(result);
        return result;
    }

    protected void addPage(WizardPage page) {
        myPages.add(page);
    }

    private WizardPage getCurrentPage() {
        return (WizardPage) myPages.get(myCurrentPage);
    }
    
    

}
