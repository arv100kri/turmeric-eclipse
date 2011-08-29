/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.typelibrary.ui.wst;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.common.config.LibraryType;
import org.ebayopensource.turmeric.common.config.TypeDependencyType;
import org.ebayopensource.turmeric.common.config.TypeLibraryDependencyType;
import org.ebayopensource.turmeric.eclipse.buildsystem.SynchronizeWsdlAndDepXML;
import org.ebayopensource.turmeric.eclipse.buildsystem.TypeDepMarshaller;
import org.ebayopensource.turmeric.eclipse.buildsystem.TypeLibSynhcronizer;
import org.ebayopensource.turmeric.eclipse.core.TurmericCoreActivator;
import org.ebayopensource.turmeric.eclipse.core.compare.LibraryTypeComparator;
import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.SOAGlobalRegistryAdapter;
import org.ebayopensource.turmeric.eclipse.resources.model.ISOAProject;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAIntfProject;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAServiceUtil;
import org.ebayopensource.turmeric.eclipse.typelibrary.resources.SOAMessages;
import org.ebayopensource.turmeric.eclipse.typelibrary.ui.TypeLibraryUtil;
import org.ebayopensource.turmeric.eclipse.typelibrary.ui.actions.ActionUtil;
import org.ebayopensource.turmeric.eclipse.typelibrary.utils.XSDSchemaValidationUtil;
import org.ebayopensource.turmeric.eclipse.ui.views.registry.TypeSelector;
import org.ebayopensource.turmeric.eclipse.utils.lang.StringUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.EclipseMessageUtils;
import org.ebayopensource.turmeric.eclipse.utils.plugin.ProgressUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.WorkspaceUtil;
import org.ebayopensource.turmeric.eclipse.utils.ui.UIUtil;
import org.ebayopensource.turmeric.tools.library.SOATypeRegistry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.xsd.XSDSchema;

/**
 * Represents the import functionality of SOA type library. Contributes the
 * context menu to WTP editor context and import types to both WSDL and XSD.
 * Validates the type for duplicates and equality before importing into XSD or
 * WSDL. In XSDs it uses the custom SOA type library protocol and in WSDLs it
 * in-lines the XSD fully. Also validates the editor before importing the types.
 * This class is also responsible for formatting the final document, but due to
 * some WTP issues this might not be a successful operation always and in case
 * of failures this will show up a warning and tells the user how to format the
 * document.
 * 
 * @author smathew
 * 
 */
public class ImportTypeFromTypeLibrary extends AbastractTypeLibraryAtion {
	private static final SOALogger logger = SOALogger.getLogger();

	/**
	 * Instantiates a new import type from type library.
	 */
	public ImportTypeFromTypeLibrary() {
	}

	/**
	 * The import action on the WTP Editor. This is for both XSD and WSDL
	 * editors. Call back from eclipse.
	 *
	 * @param action the action
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction
	 * )
	 */
	@Override
	public void run(IAction action) {

		UIJob inlineType = new UIJob("Inline Type...") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				
				monitor.beginTask("Inline Type...", ProgressUtil.PROGRESS_STEP * 10);

				if (WTPTypeLibUtil.validateEditorForContextMenus(editorPart) == false) {
					return Status.OK_STATUS;
				}
				
				ProgressUtil.progressOneStep(monitor);

				try {
					if (doValidation() == false) {
						return Status.OK_STATUS;
					}
					SOAGlobalRegistryAdapter registryAdapter = SOAGlobalRegistryAdapter
							.getInstance();
					SOATypeRegistry typeRegistry = registryAdapter
							.getGlobalRegistry();

					TypeSelector typeSelector = new TypeSelector(
							UIUtil.getActiveShell(),
							SOAMessages.SEL_TYPE,
							typeRegistry.getAllTypes().toArray(
									new LibraryType[0]),
							getSelectedFile().getProject().getName(),
							TypeLibraryUtil
									.getXsdTypeNameFromFileName(getSelectedFile()
											.getName()));
					typeSelector.setMultipleSelection(true);
					if (typeSelector.open() != Window.OK) {
						return Status.OK_STATUS;
					}
					ProgressUtil.progressOneStep(monitor);
					
					ArrayList<LibraryType> selectedTypes = typeSelector
							.getSelectedTypes();
					// // selectedTypes.addAll(c)
					// for (LibraryType selectedObject : typeSelector
					// .getSelectedTypes()) {
					// selectedTypes.add((LibraryType) selectedObject);
					// }
					if (selectedTypes.isEmpty() == true) {
						return Status.OK_STATUS;
					}
					Object adaptedObject = TypeLibraryUtil
							.getAdapterClassFromWTPEditors(editorPart);

					LibraryType[] selectedtypeArr = selectedTypes
							.toArray(new LibraryType[0]);
					if (adaptedObject == null
							|| validateSelectedTypeForImport(selectedtypeArr,
									getSelectedFile()) == false
							|| validateDuplicateImports(selectedtypeArr,
									getSelectedFile(), adaptedObject) == false) {
						return Status.OK_STATUS;
					}
					
					ProgressUtil.progressOneStep(monitor);
					
					if (adaptedObject instanceof XSDSchema) {
						if (ActionUtil
								.validateEditor(editorPart, adaptedObject)) {
							performImportTasksForXSDEditor(
									(XSDSchema) adaptedObject, selectedtypeArr,
									getSelectedFile());
							ITextOperationTarget formatter = TypeLibraryUtil
									.getFormatter(editorPart);
							ProgressUtil.progressOneStep(monitor);
							if (formatter != null) {
								// should not use
								// StructuredTextViewer.FORMAT_DOCUMENT,
								// it will set Definition.getTypes() to null
								formatter
										.doOperation(ISourceViewer.FORMAT);
								ProgressUtil.progressOneStep(monitor);
							}
						}
					} else if (adaptedObject instanceof Definition) {
						if (ActionUtil
								.validateEditor(editorPart, adaptedObject)) {
							ProgressUtil.progressOneStep(monitor);
							// This could be done on the WSDL Editor
							performInlineOperationsForWSDLEditor(
									(Definition) adaptedObject,
									selectedtypeArr, getSelectedFile());

							ProgressUtil.progressOneStep(monitor);
							
							ITextOperationTarget formatter = TypeLibraryUtil
									.getFormatter(editorPart);
							if (formatter != null) {
								// should not use
								// StructuredTextViewer.FORMAT_DOCUMENT,
								// it will set Definition.getTypes() to null
								formatter
										.doOperation(ISourceViewer.FORMAT);
							}
							ProgressUtil.progressOneStep(monitor);
						}
					} else {
						showCommonErrorDialog(null);
					}

				} catch (Exception e) {
					logger.error(e);
					UIUtil.showErrorDialog(e);
				} finally{
					monitor.done();
				}

				return Status.OK_STATUS;
			}

		};
		inlineType.schedule();
	}

	/**
	 * Shows the common Error dialog in case of import failures. Most of these
	 * failures are due to stale registry or invalid XSD opened up outside the
	 * work space etc. Thats why we show this common error in most cases.
	 *
	 * @param t the t
	 */
	public static void showCommonErrorDialog(Throwable t) {
		String msg = SOAMessages.OP_ERR_DETAILS;
		if (t != null) {
			msg += "\r\rRoot Cause:" + t.toString();
		}
		UIUtil.openChoiceDialog(SOAMessages.OP_ERR, msg,
				MessageDialog.INFORMATION);

	}

	/**
	 * Perform import tasks for xsd editor.
	 *
	 * @param parentXSDSchema the parent xsd schema
	 * @param selectedTypes the selected types
	 * @param selectedFile the selected file
	 * @throws Exception the exception
	 */
	public static void performImportTasksForXSDEditor(
			XSDSchema parentXSDSchema, LibraryType selectedTypes[],
			IFile selectedFile) throws Exception {
		WorkspaceUtil.refresh(selectedFile);
		IProject project = selectedFile.getProject();
		// Add Import to XSD
		for (LibraryType selectedType : selectedTypes) {
			runImportCommandInXSDEditor(parentXSDSchema, selectedType);
		}
		// Modify TypeDep.xml
		SynchronizeWsdlAndDepXML synch = new SynchronizeWsdlAndDepXML(project);
		synch.syncronizeXSDandDepXml(parentXSDSchema, TypeLibraryUtil.toQName(selectedFile));
		// Modify project xml/pom
		synch.synchronizeTypeDepandProjectDep(ProgressUtil.getDefaultMonitor(null));
		TypeLibSynhcronizer.updateVersionEntryTypeDep(TypeLibraryUtil
				.getXsdTypeNameFromFileName(selectedFile.getName()), Arrays
				.asList(selectedTypes), project);
		
		SOAGlobalRegistryAdapter registryAdapter = SOAGlobalRegistryAdapter.getInstance();
		registryAdapter.populateRegistry(project.getName());
		WorkspaceUtil.refresh(project);
		
		IStatusLineManager lineManager = UIUtil.getStatusLineManager();
		if (lineManager != null) {
			lineManager.setMessage(SOAMessages.OP_SUCCESS);
		}
	}

	/**
	 * In-line the selected types to the target definition object. Updates the
	 * type dependency XML file, then modifies the project dependency. If it is
	 * single name space WSDL then the information about the type library and
	 * name space are added to the WSDL as annotation. In addition to this we
	 * add it to the type dependency file also. If it is a multiple name space
	 * we only add it to the type dependency file. When in-lining types in the
	 * WSDL the dependent types are also found out and in-lined to form a stand
	 * alone WSDL.
	 *
	 * @param definition the definition
	 * @param selectedTypes the selected types
	 * @param selectedFile the selected file
	 * @throws Exception the exception
	 */
	public static void performInlineOperationsForWSDLEditor(
			Definition definition, LibraryType selectedTypes[],
			IFile selectedFile) throws Exception {
		WorkspaceUtil.refresh(selectedFile);
		IProject project = selectedFile.getProject();
		if (definition == null || (definition instanceof Definition) == false) {
			showCommonErrorDialog(null);
			return;
		}
		TreeSet<LibraryType> librarySet = new TreeSet<LibraryType>(
				new LibraryTypeComparator());
		// TreeSet<LibraryType> selectedTypesSet = new TreeSet<LibraryType>(
		// new LibraryTypeComparator());
		// finding out all the dependent types for in-lining.
		SOATypeRegistry typeRegistry = SOAGlobalRegistryAdapter.getInstance().getGlobalRegistry();
		for (LibraryType selectedType : selectedTypes) {
			librarySet.add(selectedType);
			// selectedTypesSet.add(selectedType);
			for (LibraryType libraryType : typeRegistry.getDependentParentTypeFiles(
							selectedType)) {
				librarySet.add(libraryType);
			}
		}

		XSDSchemaValidationUtil.validateType(project,
				librarySet.toArray(new LibraryType[0]));
		boolean typeFolding = SOAServiceUtil.getSOAIntfMetadata(
				SOAServiceUtil.getSOAEclipseMetadata(project)).getTypeFolding();

		final List<IStatus> statuses = new ArrayList<IStatus>();
		for (LibraryType selectedType : librarySet) {
			IStatus result = WTPTypeLibUtil.inlineType(selectedType,
					definition, true, typeFolding);
			if (result.getSeverity() == IStatus.ERROR) {
				statuses.add(result);
			}else if(result.isOK() == false){
				SOALogger.getLogger().debug(result.getMessage());
			}
		}

		// Modify TypeDep.xml
		SynchronizeWsdlAndDepXML synch = new SynchronizeWsdlAndDepXML(project);
		synch.syncronizeWSDLandDepXml(definition);
		synch.synchronizeTypeDepandProjectDep(ProgressUtil.getDefaultMonitor(null));
		TypeLibSynhcronizer.updateVersionEntryTypeDep(
				GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
				.getTypeRegistryBridge().getTypeDependencyWsdlTypeName(),
				librarySet, project);
		WorkspaceUtil.refresh(project);

		if (statuses.size() > 0) {
			IStatus results = EclipseMessageUtils
					.createErrorMultiStatusBasedOnChildrenSeverity(statuses,
							"Inline Type operation results");
			UIUtil.showErrorDialog(UIUtil.getActiveShell(),
					"Inline Type Results", results);
		} else {
			IStatusLineManager lineManager = UIUtil.getStatusLineManager();
			if (lineManager != null) {
				lineManager.setMessage(SOAMessages.OP_SUCCESS);
			}
		}
	}

	/**
	 * Validate selected type for import.
	 *
	 * @param selectedTypes the selected types
	 * @param parentFile the parent file
	 * @return true, if successful
	 */
	public static boolean validateSelectedTypeForImport(
			LibraryType[] selectedTypes, IFile parentFile) {

		for (LibraryType selectedType : selectedTypes) {
			try {
				if (StringUtils.equals(selectedType.getName(), TypeLibraryUtil
						.getXsdTypeNameFromFileName(parentFile.getName()))) {
					UIUtil
							.showErrorDialog(null, SOAMessages.IMPORT_FAILED,
									SOAMessages.IMPORT_XSD_ERR,
									StringUtil.formatString(
											SOAMessages.XSD_ERR_DETAILS,
											selectedType, parentFile.getName()));
					return false;
				}

			} catch (Exception exception) {
				SOALogger.getLogger().error(exception);
				UIUtil
						.showErrorDialog(null, SOAMessages.IMPORT_ERR,
								SOAMessages.IMPORT_FAILED, StringUtil
										.formatString(SOAMessages.BUILD_PROJ,
												exception.getMessage()));
				return false;
			}
		}

		return true;
	}

	/**
	 * Validate duplicate imports.
	 *
	 * @param selectedTypes the selected types
	 * @param selectedFile the selected file
	 * @param schema the schema
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean validateDuplicateImports(LibraryType[] selectedTypes,
			IFile selectedFile, Object schema) throws Exception {
		IProject intfProject = selectedFile.getProject();
		boolean isNamespaceFoldingEnabled = false;

		ISOAProject soaProject = GlobalRepositorySystem.instanceOf()
				.getActiveRepositorySystem().getAssetRegistry().getSOAProject(
						intfProject);
		if (soaProject instanceof SOAIntfProject) {
			SOAIntfProject soaIntfProject = (SOAIntfProject) soaProject;
			isNamespaceFoldingEnabled = soaIntfProject.getMetadata()
					.getTypeFolding();
		}

		Set<QName> duplicatedType = new HashSet<QName>();

		Set<QName> duplicatedName = new HashSet<QName>();

		fillDuplicateImports(selectedTypes, selectedFile, schema,
				duplicatedType, duplicatedName, isNamespaceFoldingEnabled);

		StringBuffer errMsg = new StringBuffer();

		if (duplicatedType.isEmpty() == false) {
			errMsg.append(SOAMessages.TYPEDEP_ERR
					+ StringUtil.join(duplicatedType, ","));
		}

		if (duplicatedName.isEmpty() == false) {
			errMsg.append("\r\n" + SOAMessages.TYPEDEP_ERR_ENF
					+ StringUtil.join(duplicatedName, ","));
		}

		if (errMsg.length() == 0) {
			return true;
		} else {
			MessageDialog.openWarning(UIUtil.getActiveShell(),
					SOAMessages.DUP_TYPES, errMsg + ".\r\n\r\n"
							+ SOAMessages.TYPE_LINE_FAIL);
			return false;
		}

	}

	private void showFormatErrorDialog() {
		MessageDialog.openInformation(UIUtil.getActiveShell(),
				SOAMessages.FORMAT_ERR, SOAMessages.FORMAT_ERR_DETAILS);
	}

	private static void runImportCommandInXSDEditor(XSDSchema parentXSDSchema,
			LibraryType selectedType) throws MalformedURLException,
			IOException, Exception {
		XSDSchema xsdSchema = TypeLibraryUtil.parseSchema(TypeLibraryUtil
				.getXSD(selectedType));

		AddImportCommand addImportCommand = new AddImportCommand(
				parentXSDSchema, TypeLibraryUtil
						.getProtocolString(selectedType), xsdSchema);
		addImportCommand.run();

	}

	private static void fillDuplicateImports(LibraryType[] selectedTypes,
			IFile selectedFile, Object schema, Set<QName> duplicatedType,
			Set<QName> duplicatedName, boolean isNamespaceFoldingEnabled)
			throws Exception {
		// Is it already imported ie duplicate?.
		String typeName = GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
		.getTypeRegistryBridge().getTypeDependencyWsdlTypeName();
		if (schema instanceof XSDSchema) {
			typeName = TypeLibraryUtil
					.getTypeNameFromXSDSchemaLocation(((XSDSchema) schema)
							.getSchemaLocation());
		}
		IFile typeDepFile = TurmericCoreActivator.getDependencyFile(selectedFile
				.getProject());
		if (typeDepFile.exists() == false) {
			return;
		}
		TypeLibraryDependencyType typeLibraryDependencyType = TypeDepMarshaller
				.unmarshallIt(typeDepFile);
		TypeDependencyType typeDependencyType = TypeDepMarshaller.getTypeEntry(
				typeLibraryDependencyType, typeName);
		if (typeDependencyType == null) {
			return;
		}

		// if isNamespaceFoldingEnabled == true, then type name duplication is
		// not allowed. Otherwise, type duplication is not allowed.
		Set<QName> importedTypes = TypeDepMarshaller
				.getAllReferredtypes(typeDependencyType);

		Set<String> importedTypeNamesStr = new HashSet<String>();
		if (isNamespaceFoldingEnabled == true) {
			for (QName importedType : importedTypes) {
				String name = importedType.getLocalPart();
				importedTypeNamesStr.add(name);
			}
		}

		for (LibraryType selectedLibraryType : selectedTypes) {
			QName name = TypeLibraryUtil.toQName(selectedLibraryType);
			if (importedTypes.contains(name)) {
				duplicatedType.add(name);
				continue;
			}

			if (isNamespaceFoldingEnabled == true) {
				if (importedTypeNamesStr.contains(name.getLocalPart())) {
					duplicatedName.add(name);
				}

			}
		}

	}

}
