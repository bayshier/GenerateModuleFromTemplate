package com.dengzii.plugin.template.ui;

import com.dengzii.plugin.template.Config;
import com.dengzii.plugin.template.TemplateConfigurable;
import com.dengzii.plugin.template.model.Module;
import com.dengzii.plugin.template.utils.Logger;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.bouncycastle.math.raw.Mod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CreateModuleDialog extends JDialog {

    private static final String TAG = CreateModuleDialog.class.getSimpleName();

    private JPanel rootPanel;
    private JLabel labelTitle;
    private JComboBox cbModuleTemplate;
    private JPanel mainPanel;
    private JPanel contentPanel;

    private JButton btConfigure;
    private JButton btCancel;
    private JButton btPrevious;
    private JButton btFinish;
    private JPanel panelPlaceholder;

    private EditableTable tablePlaceholder;
    private OnFinishListener onFinishListener;

    private java.util.List<Module> moduleTemplates = Collections.emptyList();
    private Module selectedModule;

    private HashMap<String, JPanel> panels = new HashMap<>();
    private List<String> titles = new ArrayList<>();

    private PreviewPanel previewPanel;

    private int currentPanelIndex;
    private Project project;

    private CreateModuleDialog(Project project, OnFinishListener onFinishListener) {
        setContentPane(rootPanel);
        setModal(true);
        getRootPane().setDefaultButton(btFinish);
        this.project = project;
        this.onFinishListener = onFinishListener;
    }

    public static void createAndShow(Project project, OnFinishListener onFinishListener) {

        CreateModuleDialog dialog = new CreateModuleDialog(project, onFinishListener);
        dialog.initDialog();
        dialog.initData();
        dialog.pack();
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        createAndShow(null, config -> {

        });
    }

    private void onNextClick(ActionEvent e) {
        selectedModule.getTemplate().setPlaceholders(tablePlaceholder.getPairResult());
        previewPanel.setModuleConfigPreview(selectedModule);

        if (currentPanelIndex == panels.size() - 1) {
            onFinishListener.onFinish(selectedModule);
            dispose();
            return;
        }
        currentPanelIndex++;
        setPanel();
        setButton();
    }

    private void onPreviousClick(ActionEvent e) {
        if (currentPanelIndex == 0) {
            return;
        }
        currentPanelIndex--;
        setPanel();
        setButton();
    }

    private void onConfClick(ActionEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(project, new TemplateConfigurable(this::initData));

    }

    private void setButton() {
        btPrevious.setEnabled(true);
        btFinish.setText("Next");
        if (currentPanelIndex == panels.size() - 1) {
            btFinish.setText("Finish");
        } else if (currentPanelIndex == 0) {
            btPrevious.setEnabled(false);
        }
    }

    private void setPanel() {
        String title = titles.get(currentPanelIndex);
        labelTitle.setText(title);
        contentPanel.removeAll();
        contentPanel.add(panels.get(title));
        contentPanel.doLayout();
        contentPanel.updateUI();
    }

    private void onModuleConfigChange() {
        Logger.INSTANCE.i(TAG, "onModuleConfigChange");
        selectedModule = moduleTemplates.get(cbModuleTemplate.getSelectedIndex());
        previewPanel.setModuleConfigPreview(selectedModule);
        tablePlaceholder.setPairData(selectedModule.getTemplate().getPlaceholderInherit());
    }

    private void initDialog() {

        Dimension screen = getToolkit().getScreenSize();
        int w = screen.width / 3 + 160;
        int h = 600;
        int x = screen.width / 2 - w / 2;
        int y = screen.height / 2 - h / 2;
        setLocation(x, y);
        rootPanel.setPreferredSize(new Dimension(w, h));

        setTitle("Create Module");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        rootPanel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        tablePlaceholder = new EditableTable(new String[]{"Key", "Value"}, new Boolean[]{false, true});
        tablePlaceholder.setToolBarVisible(false);
        panelPlaceholder.add(tablePlaceholder, BorderLayout.CENTER);

        btConfigure.setIcon(AllIcons.General.GearPlain);
        previewPanel = new PreviewPanel();
        titles.add("Create Module From Template");
        titles.add("Preview Module");

        panels.put(titles.get(0), mainPanel);
        panels.put(titles.get(1), previewPanel);

        btFinish.addActionListener(this::onNextClick);
        btPrevious.addActionListener(this::onPreviousClick);
        btCancel.addActionListener(e -> dispose());
        btConfigure.addActionListener(this::onConfClick);

        cbModuleTemplate.addItemListener(e -> {
            onModuleConfigChange();
        });
    }

    private void initData() {
        Logger.INSTANCE.i(TAG, "initData");
        moduleTemplates = Config.INSTANCE.loadModuleTemplates();
        List<String> temp = new ArrayList<>();
        moduleTemplates.forEach(i -> temp.add(i.getTemplateName()));
        cbModuleTemplate.setModel(new DefaultComboBoxModel<>(temp.toArray()));
        if (cbModuleTemplate.getModel().getSize() > 0) {
            cbModuleTemplate.setSelectedIndex(0);
            onModuleConfigChange();
        }
    }

    public interface OnFinishListener {
        void onFinish(Module config);
    }
}
