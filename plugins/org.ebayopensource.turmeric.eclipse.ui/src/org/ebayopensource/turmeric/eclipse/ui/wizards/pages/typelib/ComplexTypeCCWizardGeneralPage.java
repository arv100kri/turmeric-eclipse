/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.ui.wizards.pages.typelib;

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.common.config.LibraryType;
import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAXSDTemplateSubType;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.SOAGlobalRegistryAdapter;
import org.ebayopensource.turmeric.eclipse.ui.views.registry.TypeSelector;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * General Page for complex type with complex content wizard.
 *
 * @author ramurthy
 */
public class ComplexTypeCCWizardGeneralPage extends
		ComplexTypeWizardGeneralPage {

	private Text extensionType;

	private Button extensionTypeButton;

	/**
	 * Instantiates a new complex type cc wizard general page.
	 *
	 * @param typeLibName the type lib name
	 */
	public ComplexTypeCCWizardGeneralPage(String typeLibName) {
		super("complexTypeCCWizardGeneralPage",
				"Create Complex Type (Complex Content)",
				"Create a new complex type with complex content", typeLibName);
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.ui.wizards.pages.typelib.ComplexTypeWizardGeneralPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		try {
			super.createControl(parent, false);
			createExtensionType(container);
			createDocumentationText(container);
			dialogChanged();
		} catch (Exception e) {
			SOALogger.getLogger().error(e);
			UIUtil.showErrorDialog(e);
		}
	}

	/**
	 * Creates the extension type.
	 *
	 * @param parent the parent
	 */
	protected void createExtensionType(final Composite parent) {
		extensionType = super.createLabelTextField(container, getBaseType(), "", 
				modifyListener, false, false, "base extension type");
		
		// Browse Button
		extensionTypeButton = new Button(container, SWT.PUSH);
		extensionTypeButton.setAlignment(SWT.LEFT);
		extensionTypeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false, 1, 1));
		extensionTypeButton.setText("&Select...");
		extensionTypeButton.setSelection(false);
		extensionTypeButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				TypeSelector typeSelector = null;
				try {
					typeSelector = new TypeSelector(UIUtil.getActiveShell(),
							"Select Type", SOAGlobalRegistryAdapter.getInstance()
									.getGlobalRegistry().getAllTypes().toArray(
											new LibraryType[0]), "", "");
				} catch (Exception e1) {
					SOALogger.getLogger().error(e1);
				}
				if (typeSelector.open() == Window.OK) {
					final LibraryType libType = typeSelector.getSelectedTypes()
					.get(0);
					extensionType.setData(libType);
					extensionType.setText(libType.getName());
					dialogChanged();
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.ui.wizards.pages.AbstractNewTypeWizardPage#dialogChanged()
	 */
	@Override
	protected boolean dialogChanged() {
		if (super.dialogChanged() == false)
			return false;
		if (extensionType != null) {
			if (getRawBaseType() == null || 
					StringUtils.isBlank(getRawBaseType().toString())) {
				updateStatus(extensionType, "Please select the extension type");
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.ui.wizards.pages.AbstractNewTypeWizardPage#getRawBaseType()
	 */
	@Override
	public Object getRawBaseType() {
		if (extensionType.getData() != null) {
			return extensionType.getData();
		} else {
			return getTextValue(extensionType);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, URL> getTemplateTypes() {
		return getTemplateTypeNames(SOAXSDTemplateSubType.COMPLEX_COMPLEXCONTENT);
	}
}
