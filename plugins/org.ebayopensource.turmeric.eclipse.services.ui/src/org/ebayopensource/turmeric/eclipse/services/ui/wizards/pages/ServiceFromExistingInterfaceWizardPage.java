/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.turmeric.eclipse.services.ui.wizards.pages;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.ISOAHelpProvider;
import org.ebayopensource.turmeric.eclipse.ui.wizards.pages.AbstractNewServiceWizardPage;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


// TODO: Auto-generated Javadoc
/**
 * The Class ServiceFromExistingInterfaceWizardPage.
 *
 * @author yayu
 * @deprecated Creating service from existing Java interface is no longer supported.
 */
@Deprecated
public class ServiceFromExistingInterfaceWizardPage extends
		AbstractNewServiceWizardPage {
	
	/** The service interface text. */
	private Text serviceInterfaceText;

	/**
	 * Instantiates a new service from existing interface wizard page.
	 *
	 */
	public ServiceFromExistingInterfaceWizardPage() {
		super("newServiceFromExistingInterfaecWizardPage", "New Service From Existing Interface", 
		"Create a new service from a pre-existing interface.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite container = super.createParentControl(parent,1);
        addWorkspaceRootChooser(container);
        addAdminName(container);
        addServiceInterfaceFile(container);
        addServiceImpl(container);
        addServiceVersion(container);
//        addOptions(container);
        addServiceLayer(container);
		dialogChanged();
	}
	
	/**
	 * Adds the service interface file.
	 *
	 * @param parent the parent
	 * @return the composite
	 */
	private Composite addServiceInterfaceFile(final Composite parent) {
		final Composite composite = new Composite( parent, SWT.None );
		composite.setLayout( new GridLayout( 3, false ) );
		composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        final Label serviceInterfaceLabel = new Label( composite, SWT.NULL );
        serviceInterfaceLabel.setText( "Interface &File:" );
        serviceInterfaceText = new Text( composite, SWT.BORDER | SWT.SINGLE );
        serviceInterfaceText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        serviceInterfaceText.addModifyListener( modifyListener );
        serviceInterfaceText.setText( "" );
        serviceInterfaceText.setEditable( true );
        
        final Button browseButton = new Button( composite, SWT.PUSH );
        browseButton.setAlignment( SWT.LEFT );
        browseButton.setText( "Browse" );
        browseButton.setSelection( false );    
        final SelectionListener selectionListener = new SelectionListener()
        {
            @Override
			public void widgetDefaultSelected( final SelectionEvent e )
            {
                widgetSelected( e );
            }
            @Override
			public void widgetSelected( final SelectionEvent e )
            {
                final String fileName = UIUtil.fileDialog( "Select Interface File", "*.java" );
                if( StringUtils.isBlank( fileName ) )
                    return;
                
                serviceInterfaceText.setText( fileName );
                dialogChanged();
            }
        };
        browseButton.addSelectionListener( selectionListener );
        return composite;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean dialogChanged() {
		final boolean result = super.dialogChanged();
		if (result == false)
			return result;
		if( StringUtils.isBlank( getInterfaceFile() ) )
        {
            updateStatus(this.serviceInterfaceText, 
            		"Service Interface File must be specified" );
            return false;
        }
		updateStatus( null );
		return true;
	}
	
	/**
	 * Gets the interface file.
	 *
	 * @return the interface file
	 */
	public String getInterfaceFile() {
		if (serviceInterfaceText != null)
			return serviceInterfaceText.getText();
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHelpContextID() {
		return GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
		.getHelpProvider().getHelpContextID(
				ISOAHelpProvider.SOA_TUTORIAL);
	}

}
