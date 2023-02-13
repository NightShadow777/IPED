package iped.app.ui.columns;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import iped.app.ui.App;
import iped.app.ui.Messages;
import iped.app.ui.ResultTableModel;
import iped.app.ui.columns.ColumnsManager.CheckBoxState;
import iped.localization.LocalizedProperties;
import iped.utils.StringUtil;

public class ColumnsSelectUI extends ColumnsManagerUI {
    private ColumnsManagerUI columnsManagerUI;
    private static ColumnsSelectUI instance;

    protected JCheckBox toggleSelectUnselectAllCheckBox = new JCheckBox(Messages.getString("ColumnsManager.ToggleSelectAll"));
    protected JButton selectVisibleButton = new JButton(Messages.getString("ColumnsManager.SelectVisible"));

    protected JButton okButton = new JButton("OK");

    ArrayList<String> loadedSelectedProperties;

    private static boolean okButtonClicked;
    protected String saveFileName;

    public static ColumnsSelectUI getInstance() {
        if (instance == null)
            instance = new ColumnsSelectUI();
        return instance;
    }

    @Override
    public void dispose() {
        super.dispose();
        dialog.setVisible(false);
        instance = null;
    }

    public static boolean getOkButtonClicked() {
        return okButtonClicked;
    }

    protected ColumnsSelectUI() {
        super();
        saveFileName = ColumnsManager.SELECTED_PROPERTIES_FILENAME;
        okButtonClicked = false;
        columnsManagerUI = ColumnsManagerUI.getInstance();

        dialog.setTitle(Messages.getString("ReportDialog.PropertiesDialogTitle"));

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        dialog.getContentPane().remove(panel);
        autoManage.removeActionListener(columnsManagerUI);

        toggleSelectUnselectAllCheckBox.setAlignmentX(0);
        toggleSelectUnselectAllCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        toggleSelectUnselectAllCheckBox.addActionListener(this);

        Box topPanel = Box.createVerticalBox();
        topPanel.add(toggleSelectUnselectAllCheckBox);
        topPanel.add(showColsLabel);
        topPanel.add(combo);
        topPanel.add(textFieldNameFilter);
        selectVisibleButton.addActionListener(this);
        okButton.addActionListener(this);

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollList, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(okButton, BorderLayout.EAST);
        bottomPanel.add(selectVisibleButton, BorderLayout.WEST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.setLocationRelativeTo(App.get());

        loadedSelectedProperties = ColumnsManager.loadSelectedFields(saveFileName);
        if (loadedSelectedProperties != null) {
            columnsManager.checkOnlySelectedProperties(loadedSelectedProperties);
        } else {
            columnsManager.checkOnlySelectedProperties(new ArrayList<String>(Arrays.asList(ResultTableModel.fields)));
        }
        updatePanelList();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(selectVisibleButton)) {
            columnsManager.checkOnlySelectedProperties(columnsManager.colState.visibleFields);
            updatePanelList();
        } else if (e.getSource().equals(toggleSelectUnselectAllCheckBox)) {
            if (toggleSelectUnselectAllCheckBox.isSelected()) {
                columnsManager.checkAllProperties();
            } else {
                columnsManager.uncheckAllProperties();
            }
            updatePanelList();
        } else if (e.getSource().equals(combo)) {
            updatePanelList();
        } else if (e.getSource().equals(okButton)) {
            columnsManager.saveSelectedProps(saveFileName);
            okButtonClicked = true;
            dispose();
        } else if (e.getSource() instanceof JCheckBox) {
            JCheckBox source = (JCheckBox) e.getSource();
            String nonLocalizedText = LocalizedProperties.getNonLocalizedField(source.getText());
            boolean isSelected = source.isSelected();
            columnsManager.allCheckBoxesState.put(nonLocalizedText, new CheckBoxState(isSelected));
        }

        if (columnsManager.getSelectedProperties().size() == 0) {
            okButton.setEnabled(false);
        } else {
            okButton.setEnabled(true);
        }
    }

    // Updates according to the allCheckBoxesStates list
    @Override
    protected void updatePanelList() {
        listPanel.removeAll();
        List<String> fieldNames = Arrays.asList(columnsManager.fieldGroups[combo.getSelectedIndex()]);
        fieldNames = fieldNames.stream().map(f -> LocalizedProperties.getLocalizedField(f)).collect(Collectors.toList());
        Collections.sort(fieldNames, StringUtil.getIgnoreCaseComparator());
        String filter = textFieldNameFilter.getText().trim().toLowerCase();
        for (String fieldName : fieldNames) {
            if (filter.isEmpty() || fieldName.toLowerCase().indexOf(filter) >= 0) {
                JCheckBox check = new JCheckBox();
                check.setText(fieldName);
                CheckBoxState checkBoxState = columnsManager.allCheckBoxesState.get(LocalizedProperties.getNonLocalizedField(fieldName));
                check.setSelected(checkBoxState.isSelected);
                check.setEnabled(checkBoxState.isEnabled);
                check.addActionListener(this);
                listPanel.add(check);
            }
        }
        dialog.revalidate();
        dialog.repaint();
    }
}
