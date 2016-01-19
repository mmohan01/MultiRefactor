package edu.atilim.acma.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Package;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.ui.design.LoadedDesignsPanelBase;
import edu.atilim.acma.util.Log;
import edu.atilim.acma.util.Pair;

public class LoadedDesigns extends LoadedDesignsPanelBase {
    private static final long serialVersionUID = 1L;

    private Design activeDesign;
    private ArrayList<Pair<Design, DesignPanel>> designs;

    public LoadedDesigns() {
        designs = new ArrayList<Pair<Design, DesignPanel>>();
        activeDesign = null;

        designTree.addTreeSelectionListener(new TreeSelectionListener(){
            @Override
             public void valueChanged(TreeSelectionEvent arg0) {
                TreePath path = arg0.getNewLeadSelectionPath();
                if (path == null) {
                    btnUnload.setEnabled(false);
                    designSelected(null);
                    return;
                }

                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                if (node.getUserObject() instanceof Design) {
                    designSelected((Design)node.getUserObject());
                    btnUnload.setEnabled(true);
                } else {
                    btnUnload.setEnabled(false);
                }
            }
        });

        btnUnload.addActionListener(new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent arg0) {
                int resp = JOptionPane.showConfirmDialog(LoadedDesigns.this, "Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    removeDesign(activeDesign);
                }
            }
        });

        btnRefresh.addActionListener(new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent arg0) {
                DefaultTreeModel model = (DefaultTreeModel)designTree.getModel();
                model.reload();
            }
        });
    }

    private void designSelected(Design design) {
        activeDesign = design;
    }

    void addDesign(Design design) {
        DesignPanel panel = new DesignPanel(design);
        MainWindow.getInstance().getTabs().addTab(design.toString().substring(design.toString().lastIndexOf("\\") + 1),
                new ImageIcon(LoadedDesigns.class.getResource("/resources/icons/design_16.png")),
                panel,
                null);
        designs.add(new Pair<Design, DesignPanel>(design, panel));

        MainWindow.getInstance().getTabs().setSelectedComponent(panel);

        populateDesignTree(design);
    }

    void removeDesign(Design design) {
        Log.info("Unloading design %s", design.toString());

        for (Pair<Design, DesignPanel> pair: designs) {
            if (pair.getFirst().equals(design)) {
                designs.remove(pair);
                MainWindow.getInstance().getTabs().remove(pair.getSecond());
                break;
            }
        }

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(i);

            if (node.getUserObject().equals(design)) {
                DefaultTreeModel model = (DefaultTreeModel)designTree.getModel();
                model.removeNodeFromParent(node);
                return;
            }
        }
    }

    private void populateDesignTree(Design d) {
        DefaultMutableTreeNode dNode = new DefaultMutableTreeNode(d);

        for (Package pack: d.getPackages())
            dNode.add(populatePackageTree(pack));

        DefaultTreeModel model = (DefaultTreeModel)designTree.getModel();
        model.insertNodeInto(dNode, rootNode, rootNode.getChildCount());

        designTree.scrollPathToVisible(new TreePath(dNode.getPath()));

        if (dNode.getFirstChild() != null && dNode.getFirstChild() instanceof DefaultMutableTreeNode) {
            designTree.collapsePath(new TreePath(((DefaultMutableTreeNode)dNode.getFirstChild()).getPath()));
        }
    }

    private DefaultMutableTreeNode populatePackageTree(Package pack) {
        DefaultMutableTreeNode pNode = new DefaultMutableTreeNode(pack);

        for (Type t: pack.getTypes()) {
            if (t.getParentType() == null && t.isUserVisible())
                pNode.add(populateTypeTree(t));
        }

        return pNode;
    }

    private DefaultMutableTreeNode populateTypeTree(Type type) {
        DefaultMutableTreeNode tNode = new DefaultMutableTreeNode(type);
        for (Type cType: type.getNestedTypes()) {
            if (cType.isUserVisible())
                tNode.add(populateTypeTree(cType));
        }
        return tNode;
    }
}
