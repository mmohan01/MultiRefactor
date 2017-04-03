package net.sourceforge.ganttproject.gui.taskproperties;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 */
public class CommonPanel {
    protected void addUsingGBL(Container container, Component component,

                           GridBagConstraints gbc, int x,

                           int y, int w, int h) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    gbc.weighty = 0;
    container.add(component, gbc);
    container.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
  }

    /**set the first row in all the tabbed pane. thus give them a common look*/

  protected void setFirstRow(Container container, GridBagConstraints gbc,
                           JLabel nameLabel, JTextField nameField,
                           JLabel durationLabel, JTextField durationField) {
    container.setLayout(new GridBagLayout());
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets.right = 15;
    gbc.insets.left = 10;
    gbc.insets.top = 10;
    addUsingGBL(container, nameLabel, gbc, 0, 0, 1, 1);
    addUsingGBL(container, nameField, gbc, 1, 0, 1, 1);
    addUsingGBL(container, durationLabel, gbc, 2, 0, 1, 1);
    gbc.weightx = 1;
    addUsingGBL(container, durationField, gbc, 3, 0, 1, 1);
  }
    
}
