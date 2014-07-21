// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.oozie.scheduler.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.command.CommandStackForComposite;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.tableviewer.IModifiedBeanListener;
import org.talend.commons.ui.swt.tableviewer.ModifiedBeanEvent;
import org.talend.commons.utils.data.list.IListenableListListener;
import org.talend.commons.utils.data.list.ListenableListEvent;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.hadoop.IOozieService;
import org.talend.core.hadoop.version.EAuthenticationMode;
import org.talend.core.hadoop.version.EHadoopDistributions;
import org.talend.core.hadoop.version.EHadoopVersion4Drivers;
import org.talend.core.hadoop.version.custom.ECustomVersionGroup;
import org.talend.core.hadoop.version.custom.ECustomVersionType;
import org.talend.core.hadoop.version.custom.HadoopCustomVersionDefineDialog;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.model.components.EOozieParameterName;
import org.talend.oozie.scheduler.constants.TOozieUIConstants;
import org.talend.oozie.scheduler.ui.model.HadoopPropertiesFieldModel;
import org.talend.oozie.scheduler.ui.view.HadoopPropertiesTableView;
import org.talend.oozie.scheduler.utils.TOozieParamUtils;
import org.talend.oozie.scheduler.views.OozieJobTrackerListener;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * created by ycbai on 2013-2-26 Detailled comment
 * 
 */
public class OozieSettingComposite extends ScrolledComposite {

    private LabelledCombo hadoopDistributionCombo;

    private LabelledCombo hadoopVersionCombo;

    private LabelledText nameNodeEndPointTxt;

    private LabelledText jobTrackerEndPointTxt;

    private LabelledText oozieEndPointTxt;

    private LabelledText userNameTxt;

    private LabelledText groupText;

    private Button customButton;

    private Button useYarnButton;

    private Group customGroup;

    private LabelledCombo authenticationCombo;

    private String nameNodeEndPointValue;

    private String jobTrackerEndPointValue;

    private String oozieEndPointValue;

    private String userNameValue;

    private String group;

    private String customJars;

    private LabelledCombo ooziePropertyTypeCombo;

    private Text oozieRepositoryText;

    private Button oozieSelectBtn;

    private String repositoryId;

    private String repositoryName;

    private Group propertyTypeGroup;

    private Button kerbBtn;

    private LabelledText nnPrincipalText;

    private Button keytabBtn;

    private LabelledText ktPrincipalText;

    private LabelledFileField ktText;

    private Button ooKerbBtn;

    private boolean enableKerberos;

    private boolean enableOoKerberos;

    private String nnPrincipalValue;

    private boolean useKeytab;

    private String ktPrincipal;

    private String keytab;

    private boolean useYarn;

    private String authMode;

    private HadoopPropertiesTableView propertiesTableView;

    private List<HashMap<String, Object>> properties;

    private HadoopPropertiesFieldModel model;

    /**
     * DOC ycbai OozieSettingComposite constructor comment.
     * 
     * @param parent
     * @param style
     */

    public OozieSettingComposite(Composite parent, int style, boolean forPrefPage) {
        super(parent, style);
        setLayout(new GridLayout());
        setLayoutData(new GridData(GridData.FILL_BOTH));
        setExpandHorizontal(true);
        setExpandVertical(true);
        createContents(this, forPrefPage);
    }

    protected void createContents(Composite parent, boolean forPrefPage) {
        preInitialization();

        Composite comp = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(comp);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        comp.setLayout(layout);
        setContent(comp);

        IProcess2 process = OozieJobTrackerListener.getProcess();
        if (!forPrefPage) {
            if (!GlobalServiceRegister.getDefault().isServiceRegistered(IOozieService.class)) {
                if (process != null) {
                    process.getElementParameter(EOozieParameterName.REPOSITORY_CONNECTION_ID.getName()).setValue("");
                }
            } else {
                addPropertyType(comp);
            }
        }
        addVersionFields(comp);
        addCustomFields(comp);
        addAuthenticationFields(comp);
        addConnectionFields(comp);
        if (!forPrefPage && process != null) {
            addHadoopPropertiesFields(comp);
        }
        updateVersionPart(getHadoopDistribution());
        updateProperty();
        addListeners();
    }

    private void addAuthenticationFields(Composite comp) {
        Group authGroup = Form.createGroup(comp, 1, "Authentication");
        authGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addFSAuthenticationFields(authGroup);
        addOozieAuthenticationFields(authGroup);
    }

    private void addHadoopPropertiesFields(Composite comp) {
        // table view
        Composite compositeTable = Form.startNewDimensionnedGridLayout(comp, 1, comp.getBorderWidth(), 150);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 4;
        compositeTable.setLayoutData(gridData);
        CommandStackForComposite commandStack = new CommandStackForComposite(compositeTable);
        model = new HadoopPropertiesFieldModel(properties, "Hadoop Properties");
        propertiesTableView = new HadoopPropertiesTableView(model, compositeTable);
        propertiesTableView.getExtendedTableViewer().setCommandStack(commandStack);
        final Composite fieldTableEditorComposite = propertiesTableView.getMainComposite();
        fieldTableEditorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        fieldTableEditorComposite.setBackground(null);
    }

    private void addPropertyType(Composite comp) {
        propertyTypeGroup = Form.createGroup(comp, 5, "Property");
        propertyTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String[] types = new String[] { "from preference(Deprecated)", "from repository" };
        ooziePropertyTypeCombo = new LabelledCombo(propertyTypeGroup, "Property Type", "", types, 1, true);
        GridDataFactory.fillDefaults().span(1, 1).align(SWT.FILL, SWT.CENTER).applyTo(ooziePropertyTypeCombo.getCombo());
        oozieRepositoryText = new Text(propertyTypeGroup, SWT.BORDER);
        oozieRepositoryText.setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, ooziePropertyTypeCombo.getCombo().getItemHeight())
                .span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(oozieRepositoryText);
        oozieSelectBtn = new Button(propertyTypeGroup, SWT.PUSH);
        oozieSelectBtn.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        GridDataFactory.fillDefaults().grab(false, false).hint(SWT.DEFAULT, ooziePropertyTypeCombo.getCombo().getItemHeight())
                .align(SWT.BEGINNING, SWT.FILL).span(1, 1).applyTo(oozieSelectBtn);
        if (OozieJobTrackerListener.getProcess() == null) {
            ooziePropertyTypeCombo.setReadOnly(true);
            ooziePropertyTypeCombo.select(0);
            oozieSelectBtn.setEnabled(false);
        }
        initPropertyCombo();
    }

    /**
     * DOC PLV Comment method "initPropertyCombo".
     */
    protected void initPropertyCombo() {
        IProcess2 process = OozieJobTrackerListener.getProcess();
        if (process != null) {
            String connId = (String) process.getElementParameter(EOozieParameterName.REPOSITORY_CONNECTION_ID.getName())
                    .getValue();
            if (StringUtils.isNotEmpty(connId)) {
                ooziePropertyTypeCombo.select(0);
                this.repositoryId = connId;
                Connection connection = TOozieParamUtils.getOozieConnectionById(connId);
                if (connection != null) {
                    oozieRepositoryText.setText(connection.getLabel());
                }
            }
        }
        ooziePropertyTypeCombo.select(1);
        oozieSelectBtn.setVisible(true);
        oozieRepositoryText.setVisible(true);
    }

    protected void preInitialization() {
    }

    private void addVersionFields(Composite parent) {
        Group versionGroup = Form.createGroup(parent, 3, TOozieUIConstants.OOZIE_LBL_VERSION_GROUP);
        versionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        List<String> filterDistributionDisplayNames = EHadoopDistributions.getAllDistributionDisplayNames();
        filterDistributionDisplayNames.remove(EHadoopDistributions.APACHE.getDisplayName());
        filterDistributionDisplayNames.remove(EHadoopDistributions.AMAZON_EMR.getDisplayName());
        filterDistributionDisplayNames.remove(EHadoopDistributions.PIVOTAL_HD.getDisplayName());

        hadoopDistributionCombo = new LabelledCombo(versionGroup, TOozieUIConstants.OOZIE_LBL_HADOOP_DISTRIBUTION,
                "", filterDistributionDisplayNames //$NON-NLS-1$
                        .toArray(new String[0]), 2, true);
        hadoopVersionCombo = new LabelledCombo(versionGroup, TOozieUIConstants.OOZIE_LBL_HADOOP_VERSION,
                "", new String[0], 2, true); //$NON-NLS-1$
        customButton = new Button(versionGroup, SWT.NULL);
        customButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        customButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 1));
        useYarnButton = new Button(versionGroup, SWT.CHECK);
        useYarnButton.setText("Use Yarn");
        useYarnButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
    }

    private void addCustomFields(Composite parent) {
        customGroup = Form.createGroup(parent, 4, "Custom");
        customGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        authenticationCombo = new LabelledCombo(customGroup, "Authentication", "Set the authentication mode", EAuthenticationMode
                .getAllAuthenticationDisplayNames().toArray(new String[0]), 1, false);
    }

    private void addConnectionFields(Composite parent) {
        Group connectionGroup = Form.createGroup(parent, 4, TOozieUIConstants.OOZIE_LBL_CONNECTION_GROUP);
        connectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Name node end point
        nameNodeEndPointTxt = new LabelledText(connectionGroup, TOozieUIConstants.OOZIE_LBL_NAME_NODE_EP, 3);
        // Job tracker end point
        jobTrackerEndPointTxt = new LabelledText(connectionGroup, TOozieUIConstants.OOZIE_LBL_JOB_TRACKER_EP, 3);
        // Oozie end point
        oozieEndPointTxt = new LabelledText(connectionGroup, TOozieUIConstants.OOZIE_LBL_OOZIE_EP, 3);
    }

    private void addFSAuthenticationFields(Composite parent) {
        Group authGroup = Form.createGroup(parent, 4, "File System");
        authGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        kerbBtn = new Button(authGroup, SWT.CHECK);
        kerbBtn.setText("Enable kerberos security");
        kerbBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        nnPrincipalText = new LabelledText(authGroup, "Namenode Principal", 3, true);
        if (TOozieParamUtils.isFromRepository() && repositoryId != null) {
            enableKerberos = enableKerberosFromRepository(repositoryId);
            useKeytab = isUseKeytabFromRepository(repositoryId);
        } else {
            enableKerberos = TOozieParamUtils.enableKerberos();
            useKeytab = TOozieParamUtils.isUseKeytab();
        }

        userNameTxt = new LabelledText(authGroup, TOozieUIConstants.OOZIE_LBL_USERNAME, 3);
        groupText = new LabelledText(authGroup, "Group", 3);

        Composite authKeytabComposite = new Composite(authGroup, SWT.NULL);
        GridLayout authKeytabCompLayout = new GridLayout(5, false);
        authKeytabCompLayout.marginWidth = 0;
        authKeytabCompLayout.marginHeight = 0;
        authKeytabComposite.setLayout(authKeytabCompLayout);
        GridData authKeytabData = new GridData(GridData.FILL_HORIZONTAL);
        authKeytabData.horizontalSpan = 4;
        authKeytabComposite.setLayoutData(authKeytabData);

        keytabBtn = new Button(authKeytabComposite, SWT.CHECK);
        keytabBtn.setText("Use a keytab to authenticate");
        keytabBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 5, 1));
        ktPrincipalText = new LabelledText(authKeytabComposite, "Principal", 1);
        String[] extensions = { "*.*" }; //$NON-NLS-1$
        ktText = new LabelledFileField(authKeytabComposite, "Keytab", extensions);
    }

    private void addOozieAuthenticationFields(Composite parent) {
        Group authGroup = Form.createGroup(parent, 1, "Oozie");
        authGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        ooKerbBtn = new Button(authGroup, SWT.CHECK);
        ooKerbBtn.setText("Enable kerberos security");
        ooKerbBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        if (TOozieParamUtils.isFromRepository() && repositoryId != null) {
            enableOoKerberos = enableOoKerberosFromRepository(repositoryId);
        } else {
            enableOoKerberos = TOozieParamUtils.enableKerberos();
        }
    }

    protected void initUI() {
        EHadoopVersion4Drivers version4Drivers;
        if (EHadoopDistributions.CUSTOM.equals(getHadoopDistribution())) {
            version4Drivers = EHadoopVersion4Drivers.CUSTOM;
        } else {
            version4Drivers = EHadoopVersion4Drivers.indexOfByVersion(this.getHadoopVersionValue());
        }
        updateSetting(version4Drivers);
    }

    /**
     * DOC plv Comment method "updateSetting".
     * 
     * @param isSupportSecurity
     */
    private void updateSetting(EHadoopVersion4Drivers version4Drivers) {
        if (ooziePropertyTypeCombo != null && ooziePropertyTypeCombo.getSelectionIndex() == 1) {
            // from repository
            updateSettingFromRep(version4Drivers);
        } else {
            // from perf
            updateSettingFromPref(version4Drivers);
        }
        nameNodeEndPointTxt.setText(nameNodeEndPointValue == null ? "" : nameNodeEndPointValue); //$NON-NLS-1$
        jobTrackerEndPointTxt.setText(jobTrackerEndPointValue == null ? "" : jobTrackerEndPointValue); //$NON-NLS-1$
        oozieEndPointTxt.setText(oozieEndPointValue == null ? "" : oozieEndPointValue); //$NON-NLS-1$
        updateValues();
    }

    private void updateSettingFromRep(EHadoopVersion4Drivers version4Drivers) {
        if (EHadoopDistributions.CUSTOM.equals(version4Drivers.getDistribution())) {
            updateCustomSetting();
        }
        hadoopDistributionCombo.setReadOnly(true);
        hadoopVersionCombo.setReadOnly(true);
        authenticationCombo.setReadOnly(true);
        useYarnButton.setEnabled(false);
        kerbBtn.setEnabled(false);
        ooKerbBtn.setEnabled(false);
        keytabBtn.setEnabled(false);
        nnPrincipalText.setEditable(false);
        ktPrincipalText.setEditable(false);
        ktText.setEditable(false);
        nameNodeEndPointTxt.setEditable(false);
        jobTrackerEndPointTxt.setEditable(false);
        oozieEndPointTxt.setEditable(false);
        userNameTxt.setEditable(false);
        groupText.setEditable(false);
        nnPrincipalText.setEnabled(true);
        if (propertiesTableView != null) {
            propertiesTableView.setReadOnly(true);
        }
        ktPrincipalText.setText(ktPrincipal != null ? ktPrincipal : "");//$NON-NLS-1$
        ktText.setText(keytab != null ? keytab : "");//$NON-NLS-1$
        groupText.setText(group != null ? group : ""); //$NON-NLS-1$
        nnPrincipalText.setText(nnPrincipalValue != null ? nnPrincipalValue : "");//$NON-NLS-1$
        userNameTxt.setText(userNameValue != null ? userNameValue : ""); //$NON-NLS-1$
        kerbBtn.setSelection(enableKerberos);
        ooKerbBtn.setSelection(enableOoKerberos);
        keytabBtn.setSelection(useKeytab);
    }

    private void updateSettingFromPref(EHadoopVersion4Drivers version4Drivers) {
        boolean isSupportSecurity = version4Drivers.isSupportSecurity();
        boolean isSupportGroup = version4Drivers.isSupportGroup();
        hadoopDistributionCombo.setReadOnly(false);
        hadoopVersionCombo.setReadOnly(false);
        authenticationCombo.setReadOnly(false);
        nameNodeEndPointTxt.setEditable(true);
        jobTrackerEndPointTxt.setEditable(true);
        oozieEndPointTxt.setEditable(true);
        kerbBtn.setEnabled(isSupportSecurity || EHadoopDistributions.CUSTOM.equals(version4Drivers.getDistribution()));
        kerbBtn.setSelection(enableKerberos && kerbBtn.isEnabled());
        ooKerbBtn.setEnabled(isSupportSecurity);
        ooKerbBtn.setSelection(enableOoKerberos && ooKerbBtn.isEnabled());
        useYarnButton.setEnabled(true);
        keytabBtn.setEnabled(kerbBtn.isEnabled() && kerbBtn.getSelection());
        keytabBtn.setSelection(useKeytab && keytabBtn.isEnabled());
        nnPrincipalText.setEditable(kerbBtn.getEnabled() && kerbBtn.getSelection());
        ktPrincipalText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
        ktText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
        userNameTxt.setEditable(!nnPrincipalText.getEditable());
        groupText.setEditable(isSupportGroup);
        if (propertiesTableView != null) {
            propertiesTableView.setReadOnly(false);
        }
        if (EHadoopDistributions.CUSTOM.equals(version4Drivers.getDistribution())) {
            updateCustomSetting();
        }
        ktPrincipalText.setText(ktPrincipalText.getEditable() ? ktPrincipal : "");//$NON-NLS-1$
        ktText.setText(ktText.getEditable() ? keytab : "");//$NON-NLS-1$
        groupText.setText(groupText.getEditable() ? group : ""); //$NON-NLS-1$
        nnPrincipalText.setText(nnPrincipalText.getEditable() ? nnPrincipalValue : "");//$NON-NLS-1$
        userNameTxt.setText(userNameTxt.getEditable() && userNameValue != null ? userNameValue : ""); //$NON-NLS-1$
    }

    private void updateCustomSetting() {
        EAuthenticationMode mode = EAuthenticationMode.getAuthenticationByName(authMode, false);
        if (mode == null) {
            mode = EAuthenticationMode.USERNAME;
        }
        switch (mode) {
        case KRB:
            kerbBtn.setEnabled(true);
            ooKerbBtn.setEnabled(true);
            nnPrincipalText.setEditable(kerbBtn.isEnabled() && kerbBtn.getSelection());
            keytabBtn.setEnabled(kerbBtn.isEnabled() && kerbBtn.getSelection());
            ktPrincipalText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
            ktText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
            userNameTxt.setEditable(false);
            groupText.setEditable(false);
            break;
        case UGI:
            kerbBtn.setEnabled(false);
            ooKerbBtn.setEnabled(false);
            nnPrincipalText.setEditable(false);
            keytabBtn.setEnabled(false);
            ktPrincipalText.setEditable(false);
            ktText.setEditable(false);
            userNameTxt.setEditable(true);
            groupText.setEditable(true);
            break;
        default:
            kerbBtn.setEnabled(false);
            ooKerbBtn.setEnabled(false);
            nnPrincipalText.setEditable(false);
            keytabBtn.setEnabled(false);
            ktPrincipalText.setEditable(false);
            ktText.setEditable(false);
            userNameTxt.setEditable(true);
            groupText.setEditable(false);
            break;
        }
        useYarnButton.setSelection(isUseYarn());
        authenticationCombo.setText(mode.getDisplayName());
        updateJobtrackerContent();
    }

    private void updateModel() {
        setProperties(propertiesTableView.getExtendedTableModel().getBeansList());
    }

    private void updateValues() {
        setEnableKerberos(kerbBtn.getSelection());
        setEnableOoKerberos(ooKerbBtn.getSelection());
        setUseKeytab(keytabBtn.getSelection());
        setUseYarn(useYarnButton.getSelection());
    }

    protected void addListeners() {
        if (propertiesTableView != null) {
            propertiesTableView.getExtendedTableModel().addAfterOperationListListener(new IListenableListListener() {

                @Override
                public void handleEvent(ListenableListEvent event) {
                    // checkFieldsValue();
                    updateModel();
                }
            });
            propertiesTableView.getExtendedTableModel().addModifiedBeanListener(
                    new IModifiedBeanListener<HashMap<String, Object>>() {

                        @Override
                        public void handleEvent(ModifiedBeanEvent<HashMap<String, Object>> event) {
                            // checkFieldsValue();
                            updateModel();
                        }
                    });
        }
        kerbBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setEnableKerberos(kerbBtn.getSelection());
                initUI();
            }
        });
        ooKerbBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setEnableOoKerberos(ooKerbBtn.getSelection());
            }
        });

        nnPrincipalText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                setPrincipal(nnPrincipalText.getText());
            }
        });
        keytabBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setUseKeytab(keytabBtn.getSelection());
                initUI();
            }
        });
        ktPrincipalText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                setKtPrincipal(ktPrincipalText.getText());
            }
        });
        ktText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                setKeytab(ktText.getText());
            }
        });
        if (ooziePropertyTypeCombo != null) {
            ooziePropertyTypeCombo.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    oozieSelectBtn.setVisible(ooziePropertyTypeCombo.getSelectionIndex() == 1);
                    oozieRepositoryText.setVisible(ooziePropertyTypeCombo.getSelectionIndex() == 1);
                    if (ooziePropertyTypeCombo.getSelectionIndex() == 0) {
                        IProcess2 process = OozieJobTrackerListener.getProcess();
                        process.getElementParameter(EOozieParameterName.REPOSITORY_CONNECTION_ID.getName()).setValue("");
                        oozieRepositoryText.setText("");
                        setRepositoryId("");
                        model.setProperties(new ArrayList<HashMap<String, Object>>());
                        propertiesTableView.getTable().redraw();
                    }
                    updateProperty();
                }
            });
        }
        if (oozieSelectBtn != null) {
            oozieSelectBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    RepositoryReviewDialog dialog = new RepositoryReviewDialog(new Shell(), ERepositoryObjectType.METADATA,
                            "OOZIE");
                    if (dialog.open() == RepositoryReviewDialog.OK) {
                        String id = dialog.getResult().getObject().getId();
                        repositoryId = id;
                        oozieRepositoryText.setText(dialog.getResult().getObject().getLabel());
                        IProcess2 process = OozieJobTrackerListener.getProcess();
                        process.getElementParameter(EOozieParameterName.REPOSITORY_CONNECTION_ID.getName())
                                .setValue(repositoryId);
                        enableKerberos = enableKerberosFromRepository(repositoryId);
                        kerbBtn.setSelection(enableKerberos);
                        enableOoKerberos = enableOoKerberosFromRepository(repositoryId);
                        ooKerbBtn.setSelection(enableOoKerberos);
                        useKeytab = isUseKeytabFromRepository(repositoryId);
                        keytabBtn.setSelection(useKeytab);
                        model.setProperties(getHadoopProperties(repositoryId));
                        propertiesTableView.getTable().redraw();
                    }
                    updateProperty();
                }
            });
        }
        hadoopDistributionCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                String newDistributionDisplayName = hadoopDistributionCombo.getText();
                EHadoopDistributions distribution = EHadoopDistributions.getDistributionByDisplayName(newDistributionDisplayName);
                if (distribution != null) {
                    updateVersionPart(distribution);
                    initUI();
                }
            }
        });
        hadoopVersionCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                // String newVersionDisplayName = hadoopVersionCombo.getText();
                // EHadoopVersion4Drivers newVersion4Drivers =
                // EHadoopVersion4Drivers.indexOfByVersionDisplay(newVersionDisplayName);
                // if (newVersion4Drivers != null) {
                // updateSetting(newVersion4Drivers);
                // }

                initUI();
            }
        });
        customButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                HadoopCustomVersionDefineDialog customVersionDialog = new HadoopCustomVersionDefineDialog(getShell(),
                        getCustomVersionMap()) {

                    @Override
                    protected ECustomVersionType[] getDisplayTypes() {
                        return new ECustomVersionType[] { ECustomVersionType.OOZIE };
                    }
                };
                if (customVersionDialog.open() == Window.OK) {
                    customJars = customVersionDialog.getLibListStr(ECustomVersionGroup.COMMON);
                }
            }
        });
        useYarnButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setUseYarn(useYarnButton.getSelection());
                updateJobtrackerContent();
            }
        });
        authenticationCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                String newAuthDisplayName = authenticationCombo.getText();
                EAuthenticationMode newAuthMode = EAuthenticationMode.getAuthenticationByDisplayName(newAuthDisplayName);
                EAuthenticationMode originalAuthMode = EAuthenticationMode.getAuthenticationByName(authMode, false);
                if (newAuthMode != null && newAuthMode != originalAuthMode) {
                    setAuthMode(newAuthMode.getName());
                    initUI();
                }
            }
        });
        nameNodeEndPointTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setNameNodeEndPointValue(nameNodeEndPointTxt.getText());
            }
        });
        jobTrackerEndPointTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setJobTrackerEndPointValue(jobTrackerEndPointTxt.getText());
            }
        });
        oozieEndPointTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setOozieEndPointValue(oozieEndPointTxt.getText());
            }
        });
        userNameTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setUserNameValue(userNameTxt.getText());
            }
        });
        groupText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setGroup(groupText.getText());
            }
        });
    }

    protected void updateProperty() {
        if (ooziePropertyTypeCombo != null && ooziePropertyTypeCombo.getSelectionIndex() == 1 && repositoryId != null
                && repositoryId.length() != 0) {
            setHadoopDistributionValue((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SHCEDULER_HADOOP_DISTRIBUTION, repositoryId));
            setHadoopVersionValue((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SHCEDULER_HADOOP_VERSION, repositoryId));
            setUseYarn(isUseYarnFromRepository(repositoryId));
            setAuthMode((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SCHEDULER_AUTH_MODE, repositoryId));
            setNameNodeEndPointValue((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SHCEDULER_NAME_NODE_ENDPOINT, repositoryId));
            setJobTrackerEndPointValue((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SHCEDULER_JOB_TRACKER_ENDPOINT, repositoryId));
            setOozieEndPointValue((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SHCEDULER_OOZIE_ENDPOINT, repositoryId));
            setUserNameValue((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SCHEDULER_USER_NAME, repositoryId));
            setGroup((String) TOozieParamUtils.getParamValueFromRepositoryById(ITalendCorePrefConstants.OOZIE_SCHEDULER_GROUP,
                    repositoryId));
            setCustomJars((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_CUSTOM_JARS, repositoryId));
            setEnableKerberos(enableKerberosFromRepository(repositoryId));
            setEnableOoKerberos(enableOoKerberosFromRepository(repositoryId));
            setPrincipal((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_PRINCIPAL, repositoryId));
            setUseKeytab(isUseKeytabFromRepository(repositoryId));
            setKtPrincipal((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_KEYTAB_PRINCIPAL, repositoryId));
            setKeytab((String) TOozieParamUtils.getParamValueFromRepositoryById(
                    ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_KEYTAB_PATH, repositoryId));
        } else {
            setHadoopDistributionValue((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SHCEDULER_HADOOP_DISTRIBUTION));
            setHadoopVersionValue((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SHCEDULER_HADOOP_VERSION));
            setUseYarn((Boolean) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_USE_YARN));
            setAuthMode((String) TOozieParamUtils.getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_AUTH_MODE));
            setNameNodeEndPointValue((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SHCEDULER_NAME_NODE_ENDPOINT));
            setJobTrackerEndPointValue((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SHCEDULER_JOB_TRACKER_ENDPOINT));
            setOozieEndPointValue((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SHCEDULER_OOZIE_ENDPOINT));
            setUserNameValue((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_USER_NAME));
            setGroup((String) TOozieParamUtils.getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_GROUP));
            setCustomJars((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_CUSTOM_JARS));
            setEnableKerberos((Boolean) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_KERBEROS));
            setEnableOoKerberos((Boolean) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_OOZIE_KERBEROS));
            setPrincipal((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_PRINCIPAL));
            setUseKeytab((Boolean) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_USE_KEYTAB));
            setKtPrincipal((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_KEYTAB_PRINCIPAL));
            setKeytab((String) TOozieParamUtils
                    .getParamValueFromPreference(ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_KEYTAB_PATH));
        }
        initUI();
    }

    private void updateVersionPart(EHadoopDistributions distribution) {
        GridData distriData = (GridData) hadoopDistributionCombo.getCombo().getLayoutData();
        if (distribution == EHadoopDistributions.CUSTOM) {
            hadoopVersionCombo.setHideWidgets(true);
            distriData.horizontalSpan = 1;
            hideControl(customButton, false);
            hideControl(useYarnButton, false);
            hideControl(customGroup, false);
        } else {
            hadoopVersionCombo.setHideWidgets(false);
            distriData.horizontalSpan = 2;
            hideControl(customButton, true);
            hideControl(useYarnButton, true);
            hideControl(customGroup, true);
            List<String> items = getDistributionVersions(distribution);
            if (distribution == EHadoopDistributions.MAPR) {
                items.remove(EHadoopVersion4Drivers.MAPR1.getVersionDisplay());
            }
            String[] versions = new String[items.size()];
            items.toArray(versions);
            hadoopVersionCombo.getCombo().setItems(versions);
            if (versions.length > 0) {
                hadoopVersionCombo.getCombo().select(0);
            }
        }
    }

    private void updateJobtrackerContent() {
        jobTrackerEndPointTxt.setLabelText(useYarn ? "Resource Manager end point" : TOozieUIConstants.OOZIE_LBL_JOB_TRACKER_EP); //$NON-NLS-1$
        jobTrackerEndPointTxt.getTextControl().getParent().layout();
    }

    private Map<String, Set<String>> getCustomVersionMap() {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();

        if (customJars == null) {
            customJars = TOozieParamUtils.getHadoopCustomJars();
        }
        if (StringUtils.isNotEmpty(customJars)) {
            Set<String> jarSet = new HashSet<String>();
            String[] jarArray = customJars.split(";"); //$NON-NLS-1$
            for (String jar : jarArray) {
                jarSet.add(jar);
            }
            map.put(ECustomVersionGroup.COMMON.getName(), jarSet);
        }

        return map;
    }

    private List<String> getDistributionVersions(EHadoopDistributions distribution) {
        List<String> result = new ArrayList<String>();
        List<EHadoopVersion4Drivers> v4dList = EHadoopVersion4Drivers.indexOfByDistribution(distribution);
        for (EHadoopVersion4Drivers v4d : v4dList) {
            result.add(v4d.getVersionDisplay());
        }
        return result;
    }

    protected void hideControl(Control control, boolean hide) {
        GridData dataBtn = (GridData) control.getLayoutData();
        dataBtn.exclude = hide;
        control.setLayoutData(dataBtn);
        control.setVisible(!hide);
        Composite parent = control.getParent();
        if (parent != null) {
            parent.layout();
            Composite pParent = parent.getParent();
            if (pParent != null) {
                pParent.layout();
            }
        }
    }

    public EHadoopDistributions getHadoopDistribution() {
        String newDistributionDisplayName = hadoopDistributionCombo.getText();
        EHadoopDistributions distribution = EHadoopDistributions.getDistributionByDisplayName(newDistributionDisplayName);
        return distribution;
    }

    public String getHadoopDistributionValue() {
        EHadoopDistributions distribution = getHadoopDistribution();
        if (distribution != null) {
            return distribution.getName();
        }
        return null;
    }

    public void setHadoopDistributionValue(String hadoopDistributionValue) {
        EHadoopDistributions distribution = EHadoopDistributions.getDistributionByName(hadoopDistributionValue, false);
        EHadoopDistributions originalDistribution = EHadoopDistributions.getDistributionByDisplayName(hadoopDistributionCombo
                .getText());
        if (distribution != null && !distribution.equals(originalDistribution)) {
            String distributionDisplayName = distribution.getDisplayName();
            hadoopDistributionCombo.setText(distributionDisplayName);
            if (getHadoopVersionValue() == null) {
                updateVersionPart(distribution);
            }
        }

    }

    public String getHadoopVersionValue() {
        String newVersionDisplayName = hadoopVersionCombo.getText();
        EHadoopVersion4Drivers newVersion4Drivers = EHadoopVersion4Drivers.indexOfByVersionDisplay(newVersionDisplayName);
        if (newVersion4Drivers != null) {
            if (newVersion4Drivers.getVersionValue().length() > 0) {
                return newVersion4Drivers.getVersionValue();
            }
        }
        return null;
    }

    public void setHadoopVersionValue(String hadoopVersionValue) {
        EHadoopVersion4Drivers version4Drivers = EHadoopVersion4Drivers.indexOfByVersion(hadoopVersionValue);
        EHadoopVersion4Drivers originalVersion4Drivers = EHadoopVersion4Drivers.indexOfByVersionDisplay(hadoopVersionCombo
                .getText());
        if (version4Drivers != null && !version4Drivers.equals(originalVersion4Drivers)) {
            hadoopVersionCombo.setText(version4Drivers.getVersionDisplay());
        }
    }

    public String getNameNodeEndPointValue() {
        return this.nameNodeEndPointValue;
    }

    public void setNameNodeEndPointValue(String nameNodeEndPointValue) {
        this.nameNodeEndPointValue = nameNodeEndPointValue;
    }

    public String getJobTrackerEndPointValue() {
        return this.jobTrackerEndPointValue;
    }

    public void setJobTrackerEndPointValue(String jobTrackerEndPointValue) {
        this.jobTrackerEndPointValue = jobTrackerEndPointValue;
    }

    public String getOozieEndPointValue() {
        return this.oozieEndPointValue;
    }

    public void setOozieEndPointValue(String oozieEndPointValue) {
        this.oozieEndPointValue = oozieEndPointValue;
    }

    public String getUserNameValue() {
        return this.userNameValue;
    }

    public void setUserNameValue(String userNameValue) {
        this.userNameValue = userNameValue;
    }

    /**
     * Getter for group.
     * 
     * @return the group
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Sets the group.
     * 
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    public String getCustomJars() {
        return this.customJars;
    }

    public void setCustomJars(String customJars) {
        this.customJars = customJars;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Group getPropertyTypeGroup() {
        return propertyTypeGroup;
    }

    public boolean isEnableKerberos() {
        return enableKerberos;
    }

    public void setEnableKerberos(boolean enableKerberos) {
        this.enableKerberos = enableKerberos;
    }

    public String getPrincipal() {
        return nnPrincipalValue;
    }

    public void setPrincipal(String principal) {
        this.nnPrincipalValue = principal;
    }

    /**
     * Getter for useKeytab.
     * 
     * @return the useKeytab
     */
    public boolean isUseKeytab() {
        return this.useKeytab;
    }

    /**
     * Sets the useKeytab.
     * 
     * @param useKeytab the useKeytab to set
     */
    public void setUseKeytab(boolean useKeytab) {
        this.useKeytab = useKeytab;
    }

    /**
     * Getter for ktPrincipal.
     * 
     * @return the ktPrincipal
     */
    public String getKtPrincipal() {
        return this.ktPrincipal;
    }

    /**
     * Sets the ktPrincipal.
     * 
     * @param ktPrincipal the ktPrincipal to set
     */
    public void setKtPrincipal(String ktPrincipal) {
        this.ktPrincipal = ktPrincipal;
    }

    /**
     * Getter for keytab.
     * 
     * @return the keytab
     */
    public String getKeytab() {
        return this.keytab;
    }

    /**
     * Sets the keytab.
     * 
     * @param keytab the keytab to set
     */
    public void setKeytab(String keytab) {
        this.keytab = keytab;
    }

    /**
     * Getter for useYarn.
     * 
     * @return the useYarn
     */
    public boolean isUseYarn() {
        return this.useYarn;
    }

    /**
     * Sets the useYarn.
     * 
     * @param useYarn the useYarn to set
     */
    public void setUseYarn(boolean useYarn) {
        this.useYarn = useYarn;
    }

    /**
     * Getter for authMode.
     * 
     * @return the authMode
     */
    public String getAuthMode() {
        return this.authMode;
    }

    /**
     * Sets the authMode.
     * 
     * @param authMode the authMode to set
     */
    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    /**
     * Getter for enableOoKerberos.
     * 
     * @return the enableOoKerberos
     */
    public boolean isEnableOoKerberos() {
        return this.enableOoKerberos;
    }

    /**
     * Sets the enableOoKerberos.
     * 
     * @param enableOoKerberos the enableOoKerberos to set
     */
    public void setEnableOoKerberos(boolean enableOoKerberos) {
        this.enableOoKerberos = enableOoKerberos;
    }

    public boolean enableKerberosFromRepository(String id) {
        return (Boolean) TOozieParamUtils.getRepositoryOozieParam(id).get(
                ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_KERBEROS);
    }

    public boolean enableOoKerberosFromRepository(String id) {
        return (Boolean) TOozieParamUtils.getRepositoryOozieParam(id)
                .get(ITalendCorePrefConstants.OOZIE_SCHEDULER_OOZIE_KERBEROS);
    }

    public boolean isUseKeytabFromRepository(String id) {
        return (Boolean) TOozieParamUtils.getRepositoryOozieParam(id).get(
                ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_USE_KEYTAB);
    }

    public boolean isUseYarnFromRepository(String id) {
        return (Boolean) TOozieParamUtils.getRepositoryOozieParam(id).get(
                ITalendCorePrefConstants.OOZIE_SCHEDULER_HADOOP_USE_YARN);
    }

    public List<HashMap<String, Object>> getHadoopProperties(String id) {
        return TOozieParamUtils.getHadoopProperties(id);
    }

    public void setProperties(List<HashMap<String, Object>> properties) {
        this.properties = properties;
    }

    public List<HashMap<String, Object>> getProperties() {
        return properties;
    }
}
