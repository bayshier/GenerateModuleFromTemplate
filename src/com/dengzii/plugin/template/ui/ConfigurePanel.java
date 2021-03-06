package com.dengzii.plugin.template.ui;

import com.dengzii.plugin.template.Config;
import com.dengzii.plugin.template.CreateModuleAction;
import com.dengzii.plugin.template.model.Module;
import com.dengzii.plugin.template.utils.PopMenuUtils;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * author : dengzi
 * e-mail : dengzii@foxmail.com
 * github : https://github.com/dengzii
 * time   : 2020/1/8
 * desc   :
 * </pre>
 */
public class ConfigurePanel extends JPanel {

    private JPanel contentPane;

    private JTextField tfName;
    private JBList listTemplate;
    private JPanel panelStructure;
    private JPanel panelPlaceholder;
    private JPanel panelFileTemp;
    private EditToolbar actionbar;
    private JCheckBox cbPlaceholder;
    private JBTabbedPane tabbedPane;

    private List<Module> configs;
    private DefaultListModel<String> templateListModel;

    private Module currentConfig;

    private ConfigurePanel panelConfig;
    private PreviewPanel panelPreview;

    private EditableTable tablePlaceholder;
    private EditableTable tableFileTemp;

    public ConfigurePanel() {

        initComponent();
        loadConfig();
        initData();
    }

    public void cacheConfig() {
        if (currentConfig == null) return;
        currentConfig.getTemplate().setFileTemplates(tableFileTemp.getPairResult());
        currentConfig.getTemplate().setPlaceholders(tablePlaceholder.getPairResult());
    }

    public void saveConfig() {
        Config.INSTANCE.saveModuleTemplates(configs);
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        add(contentPane);

        panelPreview = new PreviewPanel();
        tablePlaceholder = new EditableTable(new String[]{"Placeholder", "Default Value"}, new Boolean[]{true, true});
        tableFileTemp = new EditableTable(new String[]{"FileName", "Template"}, new Boolean[]{true, true});
        panelStructure.add(panelPreview);
        panelPlaceholder.add(tablePlaceholder, BorderLayout.CENTER);
        panelFileTemp.add(tableFileTemp, BorderLayout.CENTER);
    }


    private void initData() {

        actionbar.onAdd(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onAddConfig(e);
            }
        });
        actionbar.onRemove(() -> {
            onRemoveConfig();
            return null;
        });
        actionbar.onCopy(() -> {
            onCopyConfig();
            return null;
        });
        actionbar.onExport(() -> {
            onExportTemplate();
            return null;
        });

        cbPlaceholder.addChangeListener(e -> {
            panelPreview.setReplacePlaceholder(cbPlaceholder.isSelected());
        });
        tabbedPane.addChangeListener(e -> onChangeTab());
        listTemplate.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTemplate.addListSelectionListener(e -> {
            if (noSelectedConfig()) return;
            onConfigSelect(getSelectedConfigIndex());
        });
        listTemplate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() != MouseEvent.BUTTON3) {
                    return;
                }
            }
        });
        //noinspection unchecked
        listTemplate.setModel(templateListModel);
        tfName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                if (currentConfig != null)
                    currentConfig.setTemplateName(tfName.getText());
            }
        });
        if (templateListModel.size() > 1) {
            listTemplate.setSelectedIndex(0);
        }
    }

    private void onChangeTab() {
        if (0 == tabbedPane.getSelectedIndex()) {
            currentConfig.getTemplate().setPlaceholders(tablePlaceholder.getPairResult());
        }
        if (2 == tabbedPane.getSelectedIndex()) {
            Map<String, String> mergedPlaceholder = currentConfig.getTemplate().getAllPlaceholdersMap();
            List<String> allPlaceholders = currentConfig.getTemplate().getAllPlaceholderInTree();
            allPlaceholders.forEach(s -> {
                if (!mergedPlaceholder.containsKey(s)) {
                    mergedPlaceholder.put(s, "");
                }
            });
            tablePlaceholder.setPairData(mergedPlaceholder);
        }
        panelPreview.setModuleConfig(currentConfig);
    }

    private void onAddConfig(MouseEvent e) {

        Map<String, PopMenuUtils.PopMenuListener> items = new HashMap<>();
        items.put("Empty Template", () -> addModuleTemplate(Module.Companion.getEmpty()));
        items.put("Android Application", () -> addModuleTemplate(Module.Companion.getAndroidApplication()));
        items.put("Auc Module", () -> addModuleTemplate(Module.Companion.getAucModule()));
        items.put("Auc app", () -> addModuleTemplate(Module.Companion.getAucApp()));
        items.put("Auc Pkg", () -> addModuleTemplate(Module.Companion.getAucPkg()));
        items.put("Auc Export", () -> addModuleTemplate(Module.Companion.getAucExport()));
        items.put("Android Mvp", () -> addModuleTemplate(Module.Companion.getAndroidMvp()));
        items.put("=> Import From File", this::onImportTemplate);
        PopMenuUtils.INSTANCE.create(items).show(actionbar, e.getX(), e.getY());
    }

    private void addModuleTemplate(Module module) {
        configs.add(module);
        currentConfig = module;
        templateListModel.addElement(module.getTemplateName());
        listTemplate.doLayout();
        listTemplate.setSelectedIndex(configs.indexOf(module));
    }

    private void onRemoveConfig() {
        if (noSelectedConfig()) {
            return;
        }
        int selectedIndex = getSelectedConfigIndex();
        configs.remove(selectedIndex);
        templateListModel.remove(selectedIndex);
        listTemplate.doLayout();
    }

    private void onCopyConfig() {
        if (noSelectedConfig()) {
            return;
        }
        Module newConfig = currentConfig.clone();
        configs.add(newConfig);
        templateListModel.addElement(newConfig.getTemplateName());
        listTemplate.doLayout();
        listTemplate.setSelectedIndex(configs.indexOf(newConfig));
    }

    private void loadConfig() {
        configs = Config.INSTANCE.loadModuleTemplates();
        templateListModel = new DefaultListModel<>();
        configs.forEach(module -> templateListModel.addElement(module.getTemplateName()));
        if (templateListModel.size() > 0) {
            listTemplate.setSelectedIndex(0);
        }
    }

    private void onExportTemplate() {

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle("Save Template to File");
        VirtualFile vf = FileChooser.chooseFile(descriptor,
                CreateModuleAction.Companion.getProject(),
                null);
        if (vf != null && vf.isWritable()) {
            String config = Config.INSTANCE.getGSON().toJson(currentConfig);
            File file = new File(vf.getPath(), currentConfig.getTemplateName() + ".json");
            OutputStreamWriter outputStream = null;
            try {
                outputStream = new OutputStreamWriter(new FileOutputStream(file));
                outputStream.write(config);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void onImportTemplate() {

        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json");
        descriptor.setTitle("Import Template From File");
        VirtualFile vf = FileChooser.chooseFile(descriptor,
                CreateModuleAction.Companion.getProject(),
                null);
        if (vf != null && vf.exists()) {
            File file = new File(vf.getPath());
            BufferedInputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = new byte[1024];
                int len = 0;
                StringBuilder stringBuilder = new StringBuilder();
                while ((len = inputStream.read(bytes)) > 0) {
                    stringBuilder.append(new String(bytes, 0, len));
                }
                Module template = Config.INSTANCE.getGSON().fromJson(stringBuilder.toString(), Module.class);
                template.initTemplate(template.getTemplate());
                addModuleTemplate(template);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void onConfigSelect(int index) {
        if (currentConfig == configs.get(index)) {
            return;
        }
        if (currentConfig != null) {
            cacheConfig();
        }
        currentConfig = configs.get(index);
        tfName.setText(currentConfig.getTemplateName());

        // update tree, file template and placeholder table
        panelPreview.setModuleConfig(currentConfig);
        tableFileTemp.setPairData(currentConfig.getTemplate().getFileTemplates());
        tablePlaceholder.setPairData(currentConfig.getTemplate().getPlaceholders());
    }

    private int getSelectedConfigIndex() {
        return listTemplate.getSelectedIndex();
    }

    private boolean noSelectedConfig() {
        return getSelectedConfigIndex() == -1;
    }
}
