/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.eclipse.buildsystem.utils.BuildSystemUtil;
import org.ebayopensource.turmeric.eclipse.core.model.services.ServiceFromTemplateWsdlParamModel;
import org.ebayopensource.turmeric.eclipse.core.model.services.ServiceFromTemplateWsdlParamModel.Binding;
import org.ebayopensource.turmeric.eclipse.core.model.services.ServiceFromTemplateWsdlParamModel.Operation;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.resources.model.IAssetInfo;
import org.ebayopensource.turmeric.eclipse.resources.model.ISOAConsumerProject.SOAClientConfig;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAConsumerMetadata;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAConsumerProject;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAImplMetadata;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAImplProject;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAIntfMetadata;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAIntfProject;
import org.ebayopensource.turmeric.eclipse.resources.model.SOAProjectEclipseMetadata;
import org.ebayopensource.turmeric.eclipse.resources.ui.model.ConsumerFromJavaParamModel;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAClientConfigUtil;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAConsumerUtil;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAServiceUtil;
import org.ebayopensource.turmeric.eclipse.services.buildsystem.ServiceCreator;
import org.ebayopensource.turmeric.eclipse.soatools.configtool.ConfigTool;
import org.ebayopensource.turmeric.eclipse.utils.plugin.ProgressUtil;
import org.ebayopensource.turmeric.eclipse.utils.plugin.WorkspaceUtil;
import org.ebayopensource.turmeric.eclipse.utils.wsdl.WSDLUtil;
import org.ebayopensource.turmeric.repositorysystem.imp.utils.TurmericConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/*
 * Models for Service Creation, Consumer Creation
 *
 */
/**
 * The Class ServicesUtil.
 */
public class ServicesUtil {

	/** The Constant MAJOR_VERSION_PREFIX. */
	public static final String MAJOR_VERSION_PREFIX = "V";
	
	/** The Constant SERVICE_MAJOR_VERSION. */
	public static final String SERVICE_MAJOR_VERSION = "1";
	
	/** The Constant DEFAULT_DOMAIN_CLASSIFIER. */
	public static final String DEFAULT_DOMAIN_CLASSIFIER = "Blogs";

	/*
	 * Service Creation from New Wsdl
	 */
	/**
	 * Creates the new svc frm wsdl.
	 *
	 * @param name the name
	 * @param location the location
	 * @param domainClassifier the domain classifier
	 * @return true, if successful
	 */
	public static boolean createNewSvcFrmWsdl(String name, String location,
			String domainClassifier) {
		try {
			final ServiceFromTemplateWsdlParamModel model = new ServiceFromTemplateWsdlParamModel();
			final String v3ViewRoot = JavaCore.getClasspathVariable(
					"V3_VIEW_ROOT").toString();
			final URL templateFile = new URL(
					v3ViewRoot
							+ "\\nexustools\\com.ebay.tools\\com.ebay.tools.soa\\plugins\\com.ebay.tools.soa.config\\templates\\wsdl\\marketplace\\Marketplace_NoOperationTemplate.wsdl");
			String publicServiceName = getPublicServiceName(name,
					domainClassifier);
			String nsPart = StringUtils.lowerCase(domainClassifier);
			String targetNamespace = getTargetNamespace(domainClassifier);
			String interfacePackage = getInterfacePackage(name, targetNamespace);
			String implClass = SOAServiceUtil.generateServiceImplClassName(
					publicServiceName, name, targetNamespace);
			List<Operation> operations = new ArrayList<Operation>();
			final Operation op = ServiceFromTemplateWsdlParamModel
					.createOperation("getVersion");
			op.getOutputParameter().getElements().get(0).setName("version");
			operations.add(op);
			final Set<Binding> bindings = new LinkedHashSet<Binding>();
			final Binding binding0 = new Binding(
					SOAProjectConstants.TemplateBinding.values()[0]);
			final Binding binding1 = new Binding(
					SOAProjectConstants.TemplateBinding.values()[1]);
			bindings.add(binding0);
			bindings.add(binding1);
			model.setTemplateFile(templateFile);
			model.setTargetNamespace(targetNamespace);
			model.setServiceName(name);
			model.setServiceInterface(interfacePackage);
			model.setWorkspaceRootDirectory(location);
			model.setServiceImpl(implClass);
			model.setServiceVersion("1.0.0");
			model.setImplName(name + "Impl");
			model.setWSDLSourceType(SOAProjectConstants.InterfaceWsdlSourceType.NEW);
			model.setPublicServiceName(publicServiceName);
			model.setServiceLayer("COMMON");
			model.setServiceDomain(domainClassifier);
			model.setNamespacePart(nsPart);
			model.setOperations(operations);
			model.setBindings(bindings);
			model.setTypeFolding(true);
			model.setTypeNamespace(targetNamespace);
			SimpleTestUtil.setAutoBuilding(false);
			ServiceCreator.createServiceFromBlankWSDL(model,
					ProgressUtil.getDefaultMonitor(null));

			WorkspaceUtil.getProject(model.getServiceName()).build(
					IncrementalProjectBuilder.FULL_BUILD,
					ProgressUtil.getDefaultMonitor(null));

			WorkspaceUtil.getProject(model.getImplName() + "Impl").build(
					IncrementalProjectBuilder.FULL_BUILD,
					ProgressUtil.getDefaultMonitor(null));
			SimpleTestUtil.setAutoBuilding(true);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/*
	 * Consumer Creation from Java
	 */
	/**
	 * Creates the consumer frm java.
	 *
	 * @param name the name
	 * @param location the location
	 * @param environment the environment
	 * @return true, if successful
	 */
	public static boolean createConsumerFrmJava(String name, String location,
			List<String> environment) {

		environment.add("production");

		try {

			String consumerId = "cons_id";
			ConsumerFromJavaParamModel model = new ConsumerFromJavaParamModel();
			model.setBaseConsumerSrcDir("src");
			model.setClientName(name + "Consumer");
			ArrayList<String> list = new ArrayList<String>();
			list.add(name);
			model.setServiceNames(list);
			model.setParentDirectory(location);
			model.setConsumerId(consumerId);
			model.setEnvironments(environment);
			ServiceCreator.createConsumerFromJava(model,
					ProgressUtil.getDefaultMonitor(null));
			IProject consProject = WorkspaceUtil.getProject(model
					.getClientName());
			consProject.build(IncrementalProjectBuilder.FULL_BUILD,
					ProgressUtil.getDefaultMonitor(null));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Invoke consumer.
	 *
	 * @param consProject the cons project
	 * @return the string[]
	 * @throws Exception the exception
	 */
	public static String[] invokeConsumer(IProject consProject)
			throws Exception {

		IJavaProject clientJProj = JavaCore.create(consProject);

		String consumerClass = getConsumerFQN(consProject);

		Assert.assertNotNull(consumerClass);

		IVMInstall vm = JavaRuntime.getVMInstall(clientJProj);
		if (vm == null)
			vm = JavaRuntime.getDefaultVMInstall();
		IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
		String[] cp = JavaRuntime.computeDefaultRuntimeClassPath(clientJProj);
		VMRunnerConfiguration config = new VMRunnerConfiguration(consumerClass,
				cp);
		ILaunch launch = new Launch(null, ILaunchManager.RUN_MODE, null);
		vmr.run(config, launch, null);

		// This is the only one process we just fired off..so there will be only
		// process
		IProcess launchedProcess = launch.getProcesses()[0];
		IStreamsProxy streamProxy = launchedProcess.getStreamsProxy();

		final StringBuffer outSb = new StringBuffer(1024);
		final StringBuffer errSb = new StringBuffer(1024);
		streamProxy.getOutputStreamMonitor().addListener(new IStreamListener() {

			@Override
			public void streamAppended(String text, IStreamMonitor monitor) {
				outSb.append(text);

			}
		});
		streamProxy.getErrorStreamMonitor().addListener(new IStreamListener() {

			@Override
			public void streamAppended(String text, IStreamMonitor monitor) {
				errSb.append(text);

			}
		});

		int i = 0;
		while (i < 60 && !launchedProcess.isTerminated()) {
			i++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (launchedProcess.isTerminated()) {
			System.out.println("The Stdout is -- " + outSb.toString());
			System.out.println("The Stderr is -- " + errSb.toString());
		} else {
			System.out.println("The launched process did not terminate");
		}
		String[] out = new String[2];
		out[0] = outSb.toString();
		out[1] = errSb.toString();
		return out;

	}

	/**
	 * Gets the consumer fqn.
	 *
	 * @param prj the prj
	 * @return the consumer fqn
	 */
	@SuppressWarnings("unchecked")
	public static String getConsumerFQN(IProject prj) {

		String className = null;
		NameFileFilter fileFilter = new NameFileFilter("TestConsumer.java");

		Collection<File> files = FileUtils
				.listFiles(prj.getLocation().toFile(), fileFilter,
						TrueFileFilter.INSTANCE);

		Assert.assertNotNull(files);
		Assert.assertTrue(files.size() > 0);

		File consFile = files.iterator().next();

		InputStream input = null;
		try {
			input = new FileInputStream(consFile);

			LineIterator iter = IOUtils.lineIterator(input, null);
			while (iter.hasNext()) {
				String line = iter.nextLine();
				if (line.startsWith("package")) {
					className = StringUtils.substringBetween(line, "package",
							";").trim();
					className = className + ".TestConsumer";
					break;
				}
			}
			iter.close();
		} catch (Exception e) {
			e.printStackTrace();
			IOUtils.closeQuietly(input);
		}

		return className;

	}

	/*
	 * For now it sets Binding to LOCAL
	 */

	/**
	 * Modify client prj transport.
	 *
	 * @param consProject the cons project
	 * @param serviceName the service name
	 * @param binding the binding
	 */
	public static void modifyClientPrjTransport(IProject consProject,
			String serviceName, SOAProjectConstants.Binding binding) {

		try {
			final SOAClientConfig config = SOAConsumerUtil.loadClientConfig(
					consProject, "production", serviceName);

			config.setServiceBinding("LOCAL");
			String protocalProcessorClassName = GlobalRepositorySystem
					.instanceOf().getActiveRepositorySystem()
					.getActiveOrganizationProvider()
					.getSOAPProtocolProcessorClassName();
			SOAClientConfigUtil.save(config, protocalProcessorClassName);

			// When adding the local binding u might need to add the project as
			// dependency also..For that use the below code,

			GlobalRepositorySystem
					.instanceOf()
					.getActiveRepositorySystem()
					.getProjectConfigurer()
					.addDependency(consProject.getName(), serviceName + "Impl",
							IAssetInfo.TYPE_PROJECT, true,
							ProgressUtil.getDefaultMonitor(null));

			GlobalRepositorySystem
					.instanceOf()
					.getActiveRepositorySystem()
					.getProjectConfigurer()
					.addDependency(consProject.getName(), "SOAServer",
							IAssetInfo.TYPE_LIBRARY, true,
							ProgressUtil.getDefaultMonitor(null));

			WorkspaceUtil.refresh(consProject);

			BuildSystemUtil.updateSOAClasspathContainer(consProject);

			consProject.build(IncrementalProjectBuilder.FULL_BUILD,
					ProgressUtil.getDefaultMonitor(null));

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("changing transport to LOCAL failed");
		}

	}

	/**
	 * Gets the service name.
	 *
	 * @param wsdlFilePath the wsdl file path
	 * @return the service name
	 */
	public static String getServiceName(String wsdlFilePath) { // gets the
																// Service Name
																// and not the
																// Service Admin
																// Name (wsdl
																// doesnt have
																// admin name)
		String serviceName = "";
		try {
			final Definition definition = WSDLUtil.readWSDL(wsdlFilePath);
			final Collection<?> services = definition.getServices().values();
			if (services.size() > 0) { // we believe that the wsdl should
				// contain only one service
				final Service service = (Service) services.toArray()[0];
				// serviceName =
				// StringUtils.capitalize(service.getQName().getLocalPart());
				serviceName = service.getQName().getLocalPart();
			}

		} catch (final WSDLException wsdlE) {
			wsdlE.printStackTrace();
		}
		return serviceName;
	}

	/**
	 * Gets the target namespace from wsdl.
	 *
	 * @param wsdlFilePath the wsdl file path
	 * @return the target namespace from wsdl
	 */
	public static String getTargetNamespaceFromWsdl(String wsdlFilePath) { // gets
																			// the
																			// Service
																			// Name
																			// and
																			// not
																			// the
																			// Service
																			// Admin
																			// Name
																			// (wsdl
																			// doesnt
																			// have
																			// admin
																			// name)
		String targetNamespace = "";
		try {
			final Definition definition = WSDLUtil.readWSDL(wsdlFilePath);
			targetNamespace = definition.getTargetNamespace();
		} catch (final WSDLException wsdlE) {
			wsdlE.printStackTrace();
		}
		return targetNamespace;
	}

	/**
	 * Gets the namespace to package.
	 *
	 * @param wsdlFilePath the wsdl file path
	 * @return the namespace to package
	 */
	public static Map<String, String> getNamespaceToPackage(String wsdlFilePath) {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		try {
			final Definition definition = WSDLUtil.readWSDL(wsdlFilePath);
			for (final String namespace : WSDLUtil
					.getAllTargetNamespaces(definition)) {
				// we do not need the default namespace to be displayed
				if (TurmericConstants.DEFAULT_SERVICE_NAMESPACE
						.equals(namespace) == false) {
					final String defaultPkgName = ConfigTool
							.getDefaultPackageNameFromNamespace(namespace);

					result.put(namespace, defaultPkgName);
				}
			}
		} catch (final WSDLException wsdlE) {
			wsdlE.printStackTrace();
		}
		return result;
	}

	/**
	 * Gets the domain classifier from wsdl.
	 *
	 * @param wsdlFilePath the wsdl file path
	 * @return the domain classifier from wsdl
	 */
	public static String getDomainClassifierFromWsdl(String wsdlFilePath) { // gets
																			// the
																			// domain
																			// classifier
		String domainClassifier = "";
		try {
			final Definition definition = WSDLUtil.readWSDL(wsdlFilePath);
			final String targetNamespace = definition.getTargetNamespace();

			if (StringUtils.isNotEmpty(targetNamespace)
					&& targetNamespace
							.contains(TurmericConstants.DEFAULT_SERVICE_NAMESPACE_PREFIX)
					&& !targetNamespace
							.equals(TurmericConstants.DEFAULT_SERVICE_NAMESPACE)) {
				domainClassifier = StringUtils.substringBetween(
						targetNamespace,
						TurmericConstants.DEFAULT_SERVICE_NAMESPACE_PREFIX
								+ "/", "/").trim();
			} else {
				domainClassifier = DEFAULT_DOMAIN_CLASSIFIER;

			}
		} catch (final WSDLException wsdlE) {
			wsdlE.printStackTrace();
		}
		return StringUtils.capitalize(domainClassifier);
	}

	private static SOAIntfMetadata intfMetadata = null;
	private static SOAImplMetadata implMetadata = null;
	private static SOAConsumerMetadata consumerMetadata = null;
	private static SOAIntfProject intfProject = null;
	private static SOAImplProject implProject = null;
	private static SOAConsumerProject consumerProject = null;
	private static SOAProjectEclipseMetadata soaEclipseMetadata = null;

	/**
	 * Services intf set up.
	 *
	 * @param name the name
	 * @return the sOA intf project
	 * @throws Exception the exception
	 */
	public static SOAIntfProject servicesIntfSetUp(String name)
			throws Exception {
		soaEclipseMetadata = SOAServiceUtil.getSOAEclipseMetadata(WorkspaceUtil
				.getProject(name));
		intfMetadata = SOAServiceUtil.getSOAIntfMetadata(soaEclipseMetadata);
		intfProject = SOAIntfProject.create(intfMetadata, soaEclipseMetadata);
		return intfProject;
	}

	/**
	 * Services impl set up.
	 *
	 * @param name the name
	 * @return the sOA impl project
	 * @throws Exception the exception
	 */
	public static SOAImplProject servicesImplSetUp(String name)
			throws Exception {
		soaEclipseMetadata = SOAServiceUtil.getSOAEclipseMetadata(WorkspaceUtil
				.getProject(name));
		implMetadata = SOAServiceUtil.getSOAImplMetadata(soaEclipseMetadata);
		implProject = SOAImplProject.create(implMetadata, soaEclipseMetadata);
		return implProject;
	}

	/**
	 * Services consumer set up.
	 *
	 * @param name the name
	 * @return the sOA consumer project
	 * @throws Exception the exception
	 */
	public static SOAConsumerProject servicesConsumerSetUp(String name)
			throws Exception {
		soaEclipseMetadata = SOAServiceUtil.getSOAEclipseMetadata(WorkspaceUtil
				.getProject(name));
		consumerMetadata = SOAServiceUtil
				.getSOAImplMetadata(soaEclipseMetadata);
		consumerProject = SOAConsumerProject.create(consumerMetadata,
				soaEclipseMetadata);
		return consumerProject;
	}

	/**
	 * Execute pre code generation for consumer fromjava.
	 *
	 * @param paramModel the param model
	 * @return the string
	 */
	public static String executePreCodeGenerationForConsumerFromjava(
			ConsumerFromJavaParamModel paramModel) {
		return "";
	}

	/**
	 * Execute code generation for cosumer from java.
	 *
	 * @param projectName the project name
	 * @return the sOA consumer project
	 */
	public static SOAConsumerProject executeCodeGenerationForCosumerFromJava(
			String projectName) {

		return null;

	}

	/**
	 * Gets the admin name.
	 *
	 * @param serviceName the service name
	 * @return the admin name
	 */
	public static String getAdminName(String serviceName) {

		StringBuffer result = new StringBuffer();
		result.append(DEFAULT_DOMAIN_CLASSIFIER).append(serviceName)
				.append(MAJOR_VERSION_PREFIX).append(SERVICE_MAJOR_VERSION);
		serviceName = result.toString();
		return serviceName;
	}

	/**
	 * Gets the public service name.
	 *
	 * @param serviceName the service name
	 * @param domainClassifier the domain classifier
	 * @return the public service name
	 */
	public static String getPublicServiceName(String serviceName,
			String domainClassifier) {

		StringBuffer result = new StringBuffer();
		result.append(serviceName.substring(domainClassifier.length(),
				serviceName.indexOf('V')));
		return result.toString();
	}

	/**
	 * Gets the target namespace.
	 *
	 * @param domainClassifier the domain classifier
	 * @return the target namespace
	 */
	public static String getTargetNamespace(String domainClassifier) {
		String nsPart = StringUtils.lowerCase(domainClassifier);

		String targetNS = TurmericConstants.DEFAULT_SERVICE_NAMESPACE_PREFIX
				+ "/" + nsPart + "/" + MAJOR_VERSION_PREFIX.toLowerCase()
				+ SERVICE_MAJOR_VERSION
				+ TurmericConstants.DEFAULT_SERVICE_NAMESPACE_SUFFIX;
		return targetNS;
	}

	/**
	 * Gets the interface package.
	 *
	 * @param serviceName the service name
	 * @param targetNS the target ns
	 * @return the interface package
	 */
	public static String getInterfacePackage(String serviceName, String targetNS) {

		String adminName = ServicesUtil.getAdminName(serviceName);
		final String servicePackageName = SOAServiceUtil
				.generateServicePackageName(serviceName,
						SOAServiceUtil.generatePackageNamePrefix(targetNS));
		return StringUtils.isBlank(servicePackageName) ? adminName
				: servicePackageName + "." + adminName;
	}

}
