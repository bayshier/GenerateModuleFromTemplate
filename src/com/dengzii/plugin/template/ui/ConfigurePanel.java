package com.dengzii.plugin.template.ui;

import com.dengzii.plugin.template.Config;
import com.dengzii.plugin.template.model.Module;
import com.dengzii.plugin.template.template.AucTemplate;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;

/**
 * <pre>
 * author : dengzi
 * e-mail : dengzii@foxmail.com
 * github : https://github.com/dengzii
 * time   : 2020/1/8
 * desc   :
 * </pre>
 */
public class ConfigurePanel extends JPanel implements SearchableConfigurable {

    private JPanel contentPane;

    private JButton btAdd;
    private JButton btRemove;
    private JButton btCopy;

    private JTextArea taPlaceholder;
    private JTextField tfName;
    private JBTable JB;
    private JBList listTemplate;
    private JPanel panelStructure;

    private List<Module> configs;
    private DefaultListModel<String> model;

    private Module currentConfig;

    private ConfigurePanel panel;
    private PreviewPanel previewPanel;

    private ConfigurePanel() {
        setLayout(new BorderLayout());
        add(contentPane);
        initPanel();
    }

    @Override
    public void apply() {
        if (panel != null) {
            panel.apply();
            return;
        }
        Config.INSTANCE.saveModuleTemplates(configs);
    }

    @NotNull
    @Override
    public String getId() {
        return "preferences.ModuleTemplateConfig";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        panel = new ConfigurePanel();
        return panel;
    }

    @Override
    public boolean isModified() {
        return true;//currentConfig != null && !tfName.getText().equals(currentConfig.getTemplateName());
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Module Template Generator";
    }

    private void onAdd() {
        currentConfig = Module.Companion.create(AucTemplate.INSTANCE.getPKG(), "ModuleName", "com.example", "Java", "TemplateName");
        configs.add(currentConfig);
        model.addElement(currentConfig.getTemplateName());
        listTemplate.doLayout();
        listTemplate.setSelectedIndex(configs.indexOf(currentConfig));
    }

    private void onRemove() {
        if (getSelectedIndex() == -1) {
            return;
        }
        int selectedIndex = getSelectedIndex();
        configs.remove(selectedIndex);
        model.remove(selectedIndex);
        listTemplate.doLayout();
    }

    private void onCopy() {
        if (getSelectedIndex() == -1) {
            return;
        }
        listTemplate.doLayout();
    }

    private int getSelectedIndex() {
        return listTemplate.getSelectedIndex();
    }

    private void loadConfig() {
        configs = Config.INSTANCE.loadModuleTemplates();
        model = new DefaultListModel<>();
        configs.forEach(module -> {
            model.addElement(module.getTemplateName());
        });
        listTemplate.setModel(model);
    }

    private void onTemplateSelect(int index) {
        if (currentConfig == configs.get(index)) {
            return;
        }
        currentConfig = configs.get(index);
        tfName.setText(currentConfig.getTemplateName());
        previewPanel.setModuleConfig(currentConfig);
    }

    private void initPanel() {

        setIconButton(btAdd, AllIcons.General.Add);
        setIconButton(btRemove, AllIcons.General.Remove);
        setIconButton(btCopy, AllIcons.General.CopyHovered);
        btCopy.addActionListener(e -> onCopy());
        btAdd.addActionListener(e -> onAdd());
        btRemove.addActionListener(e -> onRemove());

        listTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTemplate.addListSelectionListener(e -> {
            if (getSelectedIndex() == -1) return;
            onTemplateSelect(getSelectedIndex());

        });
        tfName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                if (currentConfig != null)
                    currentConfig.setTemplateName(tfName.getText());
            }
        });
        loadConfig();
        previewPanel = new PreviewPanel();
        panelStructure.add(previewPanel);
        if (model.size() > 1) {
            listTemplate.setSelectedIndex(0);
        }
    }

    private void setIconButton(JButton button, Icon icon) {
        button.setToolTipText(button.getText());
        button.setIcon(icon);
        button.setText("");
        button.setPreferredSize(new Dimension(25, 25));
    }
}
