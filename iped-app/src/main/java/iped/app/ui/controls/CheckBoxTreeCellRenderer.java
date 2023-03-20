package iped.app.ui.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.tika.mime.MediaType;

import iped.app.ui.IconManager;
import iped.engine.data.Category;

public class CheckBoxTreeCellRenderer extends DefaultTreeCellRenderer{
    protected JCheckBox checkbox=new JCheckBox();
    protected JLabel label = new JLabel();
    private JTree tree;
    Predicate<Object> checkedPredicate;
    Predicate<Object> visiblePredicate;
    private JPanel ckPanel = new JPanel(new BorderLayout());

    public CheckBoxTreeCellRenderer(JTree tree, Predicate<Object> checkedPredicate) {
        this.tree = tree;
        this.checkedPredicate = checkedPredicate;
        
        TreeSelectionModel selModel = tree.getSelectionModel();
        
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        selModel.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if(e.isAddedPath()) {
                    Object value = e.getNewLeadSelectionPath().getLastPathComponent();
                    if(visiblePredicate==null || visiblePredicate.test(value)) {
                        checkbox.setSelected(!checkbox.isSelected());
                        tree.getSelectionModel().clearSelection();
                    }
                }
            }
        });        

        ckPanel.setBackground(Color.white);
        ckPanel.add(checkbox, BorderLayout.WEST);
    }

    public CheckBoxTreeCellRenderer(JTree tree, Predicate<Object> checkedPredicate, Predicate<Object> visiblePredicate) {
        this(tree, checkedPredicate);
        this.visiblePredicate = visiblePredicate;
    }
    
    public String getValueString(Object value) {
        return value.toString();        
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        if(row==-1) {
            label.setText("");
            return label;
        }
        
        JComponent result = null;

        Icon icon = null;
        if(value instanceof MediaType) {
            icon = IconManager.getFileIcon(value.toString().split("/")[0], "");
        }
        if(value instanceof Category) {
            icon = IconManager.getCategoryIcon(((Category)value).getName().toLowerCase());
        }

        TreePath tp = tree.getPathForRow(row);

        label.setText(getValueString(value));
        label.setIcon(icon);

        if(visiblePredicate==null || visiblePredicate.test(value)) {
            ckPanel.remove(label);
            ckPanel.add(label, BorderLayout.CENTER);
            checkbox.setSelected(checkedPredicate.test(value));
            result = ckPanel;
        }else{
            result = label;
        }

        return result;
    }

    public JCheckBox getCheckbox() {
        return checkbox;
    }
}
