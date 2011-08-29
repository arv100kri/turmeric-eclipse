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
package org.ebayopensource.turmeric.eclipse.services.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.registry.ExtensionPointFactory;
import org.ebayopensource.turmeric.eclipse.registry.intf.IClientRegistryProvider;
import org.ebayopensource.turmeric.eclipse.repositorysystem.ui.utils.ActionUtil;
import org.ebayopensource.turmeric.eclipse.repositorysystem.utils.GlobalProjectHealthChecker;
import org.ebayopensource.turmeric.eclipse.repositorysystem.utils.TurmericServiceUtils;
import org.ebayopensource.turmeric.eclipse.services.ui.SOAMessages;
import org.ebayopensource.turmeric.eclipse.utils.plugin.EclipseMessageUtils;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.ebayopensource.turmeric.eclipse.validator.utils.ValidateUtil;
import org.ebayopensource.turmeric.eclipse.validator.utils.common.AbstractBaseAccessValidator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * The Class SubmitNewConsumer.
 *
 * @author yayu
 * @since 1.0.0
 */
public class SubmitNewConsumer implements IObjectActionDelegate {
	private static final SOALogger logger = SOALogger.getLogger();
	
	private IStructuredSelection selection;
	
	/**
	 * Instantiates a new submit new consumer.
	 */
	public SubmitNewConsumer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	/**
	 * {@inheritDoc}
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		try {
			if (SOALogger.DEBUG)
				logger.entering(action, selection);
			
			final IStatus status = preValidate(selection);
			
			final String messages = ValidateUtil.getFormattedStatusMessagesForAction(status);
			if (messages != null) {
				UIUtil.showErrorDialog(UIUtil.getActiveShell(), "Error", 
						messages, (Throwable)null);
				return;
			}
			final IProject project = (IProject) ((IAdaptable) selection.getFirstElement())
			.getAdapter(IProject.class);
			
			final IStatus result = ActionUtil.submitNewClientToSOARegistry(project);
			if (result.isOK()) {
					final String message = MessageFormat.format(
							SOAMessages.CLIENT_SUMISSION_SUCCEEDED_MESSAGE, 
							new Object[]{project.getName(), result.getMessage()});
					logger.info(message);
			} else {
				final String message = ValidateUtil.getFormattedStatusMessagesForAction(result);
				UIUtil.showErrorDialog(UIUtil.getActiveShell(), 
						SOAMessages.ASSET_SUMISSION_FAILED_TITLE, 
						message, (Throwable)null);
				logger.warning(message);
			}
		} catch (Exception e) {
			logger.error(e);
			UIUtil.showErrorDialog(e);
		} finally {
			if (SOALogger.DEBUG)
				logger.exiting();
		}
	}
	
	/**
	 * Pre validate.
	 *
	 * @param selection the selection
	 * @return the i status
	 * @throws CoreException the core exception
	 */
	protected IStatus preValidate(IStructuredSelection selection) throws CoreException {
		if (selection == null)
			return EclipseMessageUtils.createErrorStatus(SOAMessages.ERR_EMPTY_SELECTION);

		final IProject project = org.ebayopensource.turmeric.eclipse.buildsystem.utils.ActionUtil
				.preValidateAction(selection.getFirstElement(), logger);
		if (project == null) {
			return EclipseMessageUtils
					.createErrorStatus(SOAMessages.ERR_INVALID_PROJECT);
		}

		if (TurmericServiceUtils.isSOAConsumerProject(project) == false) {
			return EclipseMessageUtils.createErrorStatus("The selected project is not a SOA consumer project->" + project);
		}

		final IStatus status = new AbstractBaseAccessValidator() {

			@Override
			public List<IResource> getReadableFiles() {
				try {
					return GlobalProjectHealthChecker.getSOAProjectReadableResources(project);
				} catch (Exception e) {
					logger.warning(e);
				}
				return new ArrayList<IResource>(1);
			}

			@Override
			public List<IResource> getWritableFiles() {
				//we do not need to modify anything
				final List<IResource> result = new ArrayList<IResource>();
				return result;
			}

		}.validate(project.getName());

		return status;
	}

	/**
	 * {@inheritDoc}
	 *  @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;
		IClientRegistryProvider regProvider = null;
		try {
			regProvider = ExtensionPointFactory.getSOAClientRegistryProvider();
		} catch (Exception e) {
		}

		action.setEnabled(regProvider != null);
	}

}
