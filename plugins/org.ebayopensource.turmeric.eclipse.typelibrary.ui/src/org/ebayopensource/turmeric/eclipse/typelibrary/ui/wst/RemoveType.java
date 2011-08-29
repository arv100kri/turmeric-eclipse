/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.typelibrary.ui.wst;

import java.util.ArrayList;
import java.util.Map;

import org.ebayopensource.turmeric.common.config.LibraryType;
import org.ebayopensource.turmeric.eclipse.buildsystem.SynchronizeWsdlAndDepXML;
import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.SOAGlobalRegistryAdapter;
import org.ebayopensource.turmeric.eclipse.typelibrary.TypeLibraryActivator;
import org.ebayopensource.turmeric.eclipse.typelibrary.ui.TypeLibraryUtil;
import org.ebayopensource.turmeric.eclipse.typelibrary.ui.actions.ActionUtil;
import org.ebayopensource.turmeric.eclipse.ui.views.registry.TypeSelector;
import org.ebayopensource.turmeric.eclipse.utils.plugin.ProgressUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.WorkspaceUtil;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.ebayopensource.turmeric.eclipse.validator.utils.ValidateUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaDirective;
import org.eclipse.xsd.XSDTypeDefinition;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoveType.
 *
 * @author smathew
 * 
 * Cleanly removes an XSD Types. Removes the type info entry and typ dep entry
 * if exists
 */
public class RemoveType extends AbastractTypeLibraryAtion {

	/**
	 * Instantiates a new removes the type.
	 */
	public RemoveType() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IAction action) {
		
		UIJob removeType = new UIJob("Remove Type...") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				monitor.beginTask("Inline Type...", ProgressUtil.PROGRESS_STEP * 5);
				try {
					if (WTPTypeLibUtil
							.validateEditorForContextMenus(editorPart)) {
						Object adaptedObject = TypeLibraryUtil
								.getAdapterClassFromWTPEditors(editorPart);
						if (doValidation() == false) {
							return Status.OK_STATUS;
						}
						ProgressUtil.progressOneStep(monitor);
						if (editorPart.getEditorInput() instanceof IFileEditorInput) {
							final IFile editorFile = ((IFileEditorInput) editorPart
									.getEditorInput()).getFile();
							final IProject project = editorFile.getProject();
							final IStatus status = ActionUtil
									.validateTypeDependencyAndProjectConfigFile(project);

							final String messages = ValidateUtil
									.getFormattedStatusMessagesForAction(status);
							if (messages != null) {
								UIUtil.showErrorDialog(UIUtil.getActiveShell(),
										"Error", messages, (Throwable) null);
								return Status.OK_STATUS;
							}
						}
						ProgressUtil.progressOneStep(monitor);

						if (adaptedObject instanceof XSDSchema) {
							XSDSchema parentXSDSchema = (XSDSchema) adaptedObject;
							if (parentXSDSchema != null) {
								Map<LibraryType, XSDSchemaDirective> importedTypesMap = TypeLibraryActivator
										.getAllTypeLibImports(parentXSDSchema);
								TypeSelector typeSelector = new TypeSelector(
										UIUtil.getActiveShell(), "Select Type",
										importedTypesMap.keySet().toArray(
												new LibraryType[0]),
										getSelectedProject().getName());
								ProgressUtil.progressOneStep(monitor);
								typeSelector.setMultipleSelection(true);
								if (typeSelector.open() == Window.OK) {
									IStatus status = modifyXSD(typeSelector
											.getSelectedTypes(),
											parentXSDSchema, importedTypesMap,
											getSelectedProject(),
											getSelectedFile());
									if (status.isOK()) {
										MessageDialog
												.openInformation(
														UIUtil.getActiveShell(),
														"The operation was performed successfully.",
														"Successfully removed the selected type");
									} else {
										UIUtil.showErrorDialog(null, "Error",
												status);
									}
								}
								ProgressUtil.progressOneStep(monitor);
							}
						} else if (adaptedObject instanceof Definition) {
							ProgressUtil.progressOneStep(monitor);
							Definition definition = (Definition) adaptedObject;
							Map<LibraryType, XSDTypeDefinition> importedTypesMap = TypeLibraryActivator
									.getTypeLibraryTypes(definition);
							TypeSelector typeSelector = new TypeSelector(UIUtil
									.getActiveShell(), "Select Type",
									importedTypesMap.keySet().toArray(
											new LibraryType[0]),
									getSelectedProject().getName());
							ProgressUtil.progressOneStep(monitor);
							if (typeSelector.open() == Window.OK) {
								modifyWSDL(typeSelector.getSelectedTypes(),
										definition, importedTypesMap,
										getSelectedProject());
								MessageDialog
										.openInformation(
												UIUtil.getActiveShell(),
												"The operation was performed successfully.",
												"Successfully removed the selected type");
							}
							ProgressUtil.progressOneStep(monitor);
						} else {
							ImportTypeFromTypeLibrary
									.showCommonErrorDialog(null);
							ProgressUtil.progressOneStep(monitor);
						}
					}

				} catch (Exception e) {
					SOALogger.getLogger().error(e);
					ImportTypeFromTypeLibrary.showCommonErrorDialog(e);
				} finally{
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		removeType.schedule();
	}

	/**
	 * Modify wsdl.
	 *
	 * @param selectedTypes the selected types
	 * @param definition the definition
	 * @param importedTypesMap the imported types map
	 * @param project the project
	 * @throws Exception the exception
	 */
	public void modifyWSDL(ArrayList<LibraryType> selectedTypes,
			Definition definition,
			Map<LibraryType, XSDTypeDefinition> importedTypesMap,
			IProject project) throws Exception {

		for (LibraryType selectedType : selectedTypes) {
			WTPTypeLibUtil.removeInlineTypeFromWSDLDefinition(definition, selectedType,
					importedTypesMap);
		}
		
		SynchronizeWsdlAndDepXML synch = new SynchronizeWsdlAndDepXML(project);
		synch.syncronizeWSDLandDepXml(definition);
		synch.synchronizeTypeDepandProjectDep(ProgressUtil.getDefaultMonitor(null));
		WorkspaceUtil.refresh(project);
	}

	/**
	 * Modify xsd.
	 *
	 * @param selectedTypes the selected types
	 * @param parentXSDSchema the parent xsd schema
	 * @param importedTypesMap the imported types map
	 * @param project the project
	 * @param parentXSDfFile the parent xs df file
	 * @return the i status
	 * @throws Exception the exception
	 */
	public IStatus modifyXSD(ArrayList<LibraryType> selectedTypes,
			XSDSchema parentXSDSchema,
			Map<LibraryType, XSDSchemaDirective> importedTypesMap,
			IProject project, IFile parentXSDfFile) throws Exception {
		
		for (LibraryType selectedType : selectedTypes) {
			performDeleteTasksForXSDEditor(parentXSDSchema, selectedType,
					importedTypesMap);
		}
		SynchronizeWsdlAndDepXML synch = new SynchronizeWsdlAndDepXML(project);
		synch.syncronizeXSDandDepXml(parentXSDSchema, TypeLibraryUtil.toQName(parentXSDfFile));
		synch.synchronizeTypeDepandProjectDep(ProgressUtil.getDefaultMonitor(null));
		refreshGlobalRegistry(project.getName());
		WorkspaceUtil.refresh(project);
		return Status.OK_STATUS;
	}

	/**
	 * Perform delete tasks for xsd editor.
	 *
	 * @param parentXSDSchema the parent xsd schema
	 * @param selectedType the selected type
	 * @param importedTypesMap the imported types map
	 */
	private void performDeleteTasksForXSDEditor(XSDSchema parentXSDSchema,
			LibraryType selectedType,
			Map<LibraryType, XSDSchemaDirective> importedTypesMap) {
		// remove it from XSD
		parentXSDSchema.getContents()
				.remove(importedTypesMap.get(selectedType));

	}

	/**
	 * Refresh global registry.
	 *
	 * @param libraryName the library name
	 * @throws Exception the exception
	 */
	private void refreshGlobalRegistry(String libraryName) throws Exception {
		SOAGlobalRegistryAdapter.getInstance().populateRegistry(libraryName);
	}

}
