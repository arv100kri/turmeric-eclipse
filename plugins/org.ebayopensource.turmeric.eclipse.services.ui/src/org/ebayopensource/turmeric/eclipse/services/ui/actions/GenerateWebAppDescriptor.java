/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.services.ui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ebayopensource.turmeric.eclipse.buildsystem.utils.ActionUtil;
import org.ebayopensource.turmeric.eclipse.buildsystem.utils.BuildSystemCodeGen;
import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.exception.resources.SOAActionExecutionFailedException;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.TrackingEvent;
import org.ebayopensource.turmeric.eclipse.repositorysystem.preferences.core.PreferenceConstants;
import org.ebayopensource.turmeric.eclipse.repositorysystem.utils.GlobalProjectHealthChecker;
import org.ebayopensource.turmeric.eclipse.utils.plugin.ProgressUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.WorkspaceUtil;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.ebayopensource.turmeric.eclipse.validator.utils.ValidateUtil;
import org.ebayopensource.turmeric.eclipse.validator.utils.common.AbstractBaseAccessValidator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


// TODO: Auto-generated Javadoc
/**
 * The Class GenerateWebAppDescriptor.
 *
 * @author smathew
 */
public class GenerateWebAppDescriptor implements IObjectActionDelegate {
	
	/** The selection. */
	private IStructuredSelection selection;
	
	/** The Constant logger. */
	private static final SOALogger logger = SOALogger.getLogger();

	/**
	 * {@inheritDoc}
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(final IAction action,
			final IWorkbenchPart targetPart) {
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(final IAction action) {
		try {
			if (SOALogger.DEBUG)
				logger.entering(action, selection);

			if (selection == null)
				return;
			
			final IProject project = 
				ActionUtil.preValidateAction(selection.getFirstElement(), logger);
			if (project == null)
				return;
			
			final IStatus status = new AbstractBaseAccessValidator() {

				@Override
				public List<IResource> getReadableFiles() {
					//should check the following files
					//service_iml_project.properties
					//ServiecConfig.xml
					try {
						return GlobalProjectHealthChecker.getSOAProjectReadableResources(project);
					} catch (Exception e) {
						logger.warning(e);
					}
					return new ArrayList<IResource>(1);
				}

				@Override
				public List<IResource> getWritableFiles() {
					//we should ensure that the typeMappings's folder is writable
					final List<IResource> result = new ArrayList<IResource>();
					final IPath path = new Path(SOAProjectConstants.FOLDER_WEB_CONTENT)
					.append(SOAProjectConstants.FOLDER_WEB_INF)
					.append(SOAProjectConstants.FILE_WEB_XML);
					result.add(project.getFile(path));
					return result;
				}
				
			}.validate(project.getName());
			
			final String messages = ValidateUtil.getFormattedStatusMessagesForAction(status);
			if (messages != null) {
				UIUtil.showErrorDialog(UIUtil.getActiveShell(), "Error", 
						messages, (Throwable)null);
				return;
			}
			
			final Class<?> templateLoadingClass;
			final Map<String, String> templates = new ConcurrentHashMap<String, String>(1);
			if (PreferenceConstants._PREF_DEFAULT_REPOSITORY_SYSTEM.equals(
					GlobalRepositorySystem.instanceOf().getActiveRepositorySystem().getId())) {
				//we only generate the Geronimo specific deployment file in V3 mode
				templates.put("WEB-INF/geronimo-web.xml", "geronimo-web.xml.ftl");
				templateLoadingClass = BuildSystemCodeGen.class;
			} else {
				templateLoadingClass = null;
			}
			
			final Job buildJob = new WorkspaceJob("Generating Web App Descriptor for "
					+ project.getName()) {
				@Override
				public boolean belongsTo(Object family) {
					return false;
				}

				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					try {
						monitor.beginTask(getName(), ProgressUtil.PROGRESS_STEP * 10);
						ActionUtil.generateWebXml(project, templates, 
								templateLoadingClass, monitor);
					} catch (Exception e) {
						logger.error(e);
						throw new SOAActionExecutionFailedException(e);
					} finally {
						monitor.done();
						WorkspaceUtil.refresh(monitor, project);
					}
					return Status.OK_STATUS;
				}
			};
			buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
					.deleteRule(project));
			GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
			.trackingUsage(new TrackingEvent(
					getClass().getName(), 
					TrackingEvent.TRACKING_ACTION));
			UIUtil.runJobInUIDialog(buildJob).schedule();
		} catch (Exception e) {
			logger.error(e);
			UIUtil.showErrorDialog(e);
		} finally {
			if (SOALogger.DEBUG)
				logger.exiting();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IAction action,
			final ISelection selection) {
		this.selection = (IStructuredSelection) selection;
	}
}
