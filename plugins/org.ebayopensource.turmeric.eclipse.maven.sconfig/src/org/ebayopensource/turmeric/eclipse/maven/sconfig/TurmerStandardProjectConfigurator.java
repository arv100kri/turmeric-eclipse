/*******************************************************************************
 * Copyright (c) 2011 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.maven.sconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants.SupportedProjectType;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.utils.plugin.JDTUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

/**
 * The Class TurmerStandardProjectConfigurator.
 */
public class TurmerStandardProjectConfigurator extends
		AbstractJavaProjectConfigurator {

	private static final String GENERATED_RESOURCES_CODEGEN = "/target/generated-resources/codegen";
	private static final String GENERATED_SOURCES_CODEGEN = "/target/generated-sources/codegen";
	private static final String GENERATED_RESOURCES_JAXB_EPISODE = "/target/generated-resources/jaxb-episode";
	private static final String GENERATED_SOURCES_JAXB_EPISODE = "/target/generated-sources/jaxb-episode";
	private static final String GEN_TYPELIBRARY = "gen-typelibrary";
	private static final String GEN_ERRORLIBRARY = "gen-errorlibrary";
	private static final String GEN_IMPLEMENTATION = "gen-implementation";
	private static final String GEN_INTERFACE_WSDL = "gen-interface-wsdl";
	private static final String TURMERIC_MAVEN_PLUGIN = "turmeric-maven-plugin";

	/**
	 * Instantiates a new turmeric standard project configurator.
	 */
	public TurmerStandardProjectConfigurator() {

	}

	@Override
	public void configureRawClasspath(ProjectConfigurationRequest request,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {

		IProject project = request.getProject();
		IMavenProjectFacade facade = request.getMavenProjectFacade();
		List<IPath> additionalSrcDirs = new ArrayList<IPath>();

		if (isErrorLibProject(request) || isInterfaceProject(request)
				|| isTypeLibProject(request)
				|| isImplementationProject(request)) {
			additionalSrcDirs.add(new Path(project.getFullPath().toString() + GENERATED_SOURCES_CODEGEN));
			additionalSrcDirs
					.add(new Path(project.getFullPath().toString() + GENERATED_RESOURCES_CODEGEN));
		} else {
			additionalSrcDirs.add(new Path(project.getFullPath().toString() +
					GENERATED_SOURCES_JAXB_EPISODE));
			additionalSrcDirs.add(new Path(project.getFullPath().toString() +
					GENERATED_RESOURCES_JAXB_EPISODE));
		}

		for (IPath path : additionalSrcDirs) {
				if (!classpath.containsPath(path)) {
					classpath.addSourceEntry(path,
							facade.getOutputLocation(), true);
			}
		}
	}

	@Override
	public void configure(ProjectConfigurationRequest projRequest,
			IProgressMonitor monitor) throws CoreException {

		SupportedProjectType projectType = null;
		IProject project = projRequest.getProject();
		if (isInterfaceProject(projRequest)) {
			projectType = SupportedProjectType.INTERFACE;
		} else if (isImplementationProject(projRequest)) {
			projectType = SupportedProjectType.IMPL;
		} else if (isErrorLibProject(projRequest)) {
			projectType = SupportedProjectType.ERROR_LIBRARY;
		} else if (isTypeLibProject(projRequest)) {
			projectType = SupportedProjectType.TYPE_LIBRARY;
		} else if (isConsumerLibProject(projRequest)) {
			projectType = SupportedProjectType.CONSUMER;
		} else {
			return;
		}

		String natureId = GlobalRepositorySystem.instanceOf()
				.getActiveRepositorySystem().getProjectNatureId(projectType);

		JDTUtil.addNatures(project, monitor, natureId);
	}

	/**
	 * Checks if is interface project.
	 * 
	 * @param projRequest
	 *            the project request
	 * @return true, if is an interface project
	 */
	public boolean isInterfaceProject(ProjectConfigurationRequest projRequest) {

		return isProjectType(GEN_INTERFACE_WSDL, projRequest);
	}

	private boolean isProjectType(String goalType,
			ProjectConfigurationRequest projRequest) {
		MavenProject mproj = projRequest.getMavenProject();
		List<Plugin> buildPlugins = mproj.getBuildPlugins();
		for (Plugin mplug : buildPlugins) {
			if (TURMERIC_MAVEN_PLUGIN.equals(mplug.getArtifactId())) {
				List<PluginExecution> exList = mplug.getExecutions();

				for (PluginExecution pexec : exList) {
					List<String> goals = pexec.getGoals();
					for (String goal : goals) {
						if (goalType.equals(goal)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if is an implementation project.
	 * 
	 * @param projRequest
	 *            the proj request
	 * @return true, if is implementation project
	 */
	public boolean isImplementationProject(
			ProjectConfigurationRequest projRequest) {
		return isProjectType(GEN_IMPLEMENTATION, projRequest);
	}

	/**
	 * Checks if is an errorlib project.
	 * 
	 * @param projRequest
	 *            the project request
	 * @return true, if is error lib project
	 */
	public boolean isErrorLibProject(ProjectConfigurationRequest projRequest) {
		return isProjectType(GEN_ERRORLIBRARY, projRequest);
	}

	/**
	 * Checks if is a typelib project.
	 * 
	 * @param projRequest
	 *            the proj request
	 * @return true, if is type lib project
	 */
	public boolean isTypeLibProject(ProjectConfigurationRequest projRequest) {
		return isProjectType(GEN_TYPELIBRARY, projRequest);
	}

	/**
	 * Checks if is consumer lib project.
	 * 
	 * @param projRequest
	 *            the proj request
	 * @return true, if is consumer lib project
	 */
	public boolean isConsumerLibProject(ProjectConfigurationRequest projRequest) {
		return isFileAccessible(projRequest.getProject(),
				SOAProjectConstants.PROPS_FILE_SERVICE_CONSUMER);
	}

	private static boolean isFileAccessible(IProject project,
			String fileRelativePath) {
		if (project.isAccessible()) {
			return project.getFile(fileRelativePath).isAccessible();
		}
		return false;
	}

	@Override
	public AbstractBuildParticipant getBuildParticipant(
			IMavenProjectFacade projectFacade, MojoExecution execution,
			IPluginExecutionMetadata executionMetadata) {
		return new TurmericStandardBuildParticipant(execution);
	}

}
