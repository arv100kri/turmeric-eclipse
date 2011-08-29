/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.soatools.configtool;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.common.config.ClientConfig;
import org.ebayopensource.turmeric.common.config.ClientConfigList;
import org.ebayopensource.turmeric.common.config.ClientGroupConfig;
import org.ebayopensource.turmeric.common.config.ServiceConfig;
import org.ebayopensource.turmeric.tools.codegen.CodeGenInfoFinder;
import org.ebayopensource.turmeric.tools.codegen.ConfigHelper;
import org.ebayopensource.turmeric.tools.codegen.exception.BadInputValueException;
import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
import org.ebayopensource.turmeric.tools.codegen.external.WSDLUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * The Class ConfigTool.
 */
public class ConfigTool {
	
	/**
	 * Modify service config namespace.
	 *
	 * @param newNamespace the new namespace
	 * @param fileLocation the file location
	 * @throws Exception the exception
	 */
	public static void modifyServiceConfigNamespace(final String newNamespace, 
			final URL fileLocation) throws Exception {
		final ClassLoader loader = Thread.currentThread()
		.getContextClassLoader();
		InputStream input = null;
		OutputStream out = null;
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			if (fileLocation != null) {
				input = fileLocation.openStream();
				final ServiceConfig svcConfig = ConfigHelper.parseServiceConfig(input);
				IOUtils.closeQuietly(input);
				input = null;
				String fullServiceName = svcConfig.getServiceName();
				
				if (StringUtils.isNotBlank(fullServiceName)) {
					final String[] names = parseFullyQualifiedServiceName(fullServiceName);
					if (names != null) {
						fullServiceName = "{" + newNamespace + "}" + names[1];
					}
					svcConfig.setServiceName(fullServiceName);
					final String configXml = ConfigHelper.serviceConfigToXml(svcConfig);
					out = new FileOutputStream(fileLocation.getFile());
					IOUtils.write(configXml, out);
				}
			}
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(out);
			Thread.currentThread().setContextClassLoader(loader);
		}
	}
	
	/**
	 * Save server config.
	 *
	 * @param serviceConfig the service config
	 * @param fileLocation the file location
	 * @throws Exception the exception
	 * @deprecated plugin should never modify the ServiceConfig.xml
	 */
	public static void saveServerConfig(final ISOAServiceConfig serviceConfig, 
			final IFile fileLocation) throws Exception {
		final ClassLoader loader = Thread.currentThread()
		.getContextClassLoader();
		InputStream input = null;
		OutputStream out = null;
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			if (fileLocation != null) {
				input = fileLocation.getLocationURI().toURL().openStream();
				ServiceConfigXmlHelper svcConfigXMLHelper = new ServiceConfigXmlHelper();
				svcConfigXMLHelper.setServiceImplementationName(input,
						serviceConfig.getServiceImplClassName(), fileLocation);
				//we are no longer modifying the current version, and the version would be maintained 
				//in the service_metadata.properties
				//svcConfig.setCurrentVersion(serviceConfig.getCurrentVersion());
				// svcConfig.setServiceImplClassName(serviceConfig.getServiceImplClassName());
				// final String configXml =
				// ConfigHelper.serviceConfigToXml(svcConfig);
				// out = new FileOutputStream(fileLocation.getFile());
				// IOUtils.write(configXml, out);
			}
			
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(out);
			Thread.currentThread().setContextClassLoader(loader);
		}
	}
	
	/**
	 * Modify client config namespace.
	 *
	 * @param newNamespace the new namespace
	 * @param fileLocation the file location
	 * @throws Exception the exception
	 */
	public static void modifyClientConfigNamespace(final String newNamespace, 
			final URL fileLocation) throws Exception {
		final ClassLoader loader = Thread.currentThread()
		.getContextClassLoader();
		InputStream input = null;
		OutputStream out = null;
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			if (fileLocation != null) {
				input = fileLocation.openStream();
				final ClientConfigList clientConfigList = ConfigHelper.parseClientConfig(input);
				IOUtils.closeQuietly(input);
				input = null;
				if (clientConfigList.getClientConfig().size() > 0) {
					final ClientConfig clientConfig = clientConfigList.getClientConfig().get(0);
					String fullServiceName = clientConfig.getServiceName();
						
					if (StringUtils.isNotBlank(fullServiceName)) {
						final String[] names = parseFullyQualifiedServiceName(fullServiceName);
						if (names != null) {
							fullServiceName = "{" + newNamespace + "}" + names[1];
						}
						clientConfig.setServiceName(fullServiceName);
						final String configXml = ConfigHelper.clientConfigToXml(clientConfigList);
						out = new FileOutputStream(fileLocation.getFile());
						IOUtils.write(configXml, out);
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(out);
			Thread.currentThread().setContextClassLoader(loader);
		}
	}
	
	/**
	 * Parses the client config.
	 *
	 * @param input the input
	 * @param clientConfig the client config
	 * @return the iSOA client config
	 * @throws Exception the exception
	 */
	public static ISOAClientConfig parseClientConfig(final InputStream input, final ISOAClientConfig clientConfig)
			throws Exception {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			List<ISOAClientConfig> result = new ArrayList<ISOAClientConfig>();
			for (final ClientConfig config : ConfigHelper.parseClientConfig(input).getClientConfig()) {
				clientConfig.setGroup(config.getGroup());
				clientConfig.setServiceInterfaceClassName(config.getServiceInterfaceClassName());
				clientConfig.setServiceLocation(config.getServiceLocation());
				clientConfig.setFullyQualifiedServiceName(config.getServiceName());
				if (StringUtils.isNotBlank(config.getServiceName())) {
					final String[] names = parseFullyQualifiedServiceName(config.getServiceName());
					if (names != null) {
						clientConfig.setTargetNamespace(names[0]);
						clientConfig.setServiceName(names[1]);
					}
				}
				
				clientConfig.setWsdlLocation(config.getWsdlLocation());
				
				if (config.getClientInstanceConfig() != null) {
					ClientGroupConfig instanceConfig = config.getClientInstanceConfig();
					if (instanceConfig.getInvocationOptions() != null) {
						clientConfig.setServiceBinding(instanceConfig.getInvocationOptions().getPreferredTransport().getName());
						clientConfig.setInvocationUseCase(instanceConfig.getInvocationOptions().getInvocationUseCase());
						clientConfig.setRequestDataBinding(instanceConfig.getInvocationOptions().getRequestDataBinding());
						clientConfig.setResponseDataBinding(instanceConfig.getInvocationOptions().getResponseDataBinding());
					}
					if (instanceConfig.getProtocolProcessor() != null && instanceConfig.getProtocolProcessor().size() > 0) {
						clientConfig.setMessageProtocol(instanceConfig.getProtocolProcessor().get(0).getName());
					}
				}
				result.add(clientConfig);
			}
			return result.get(0);
		} finally {
			IOUtils.closeQuietly(input);
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * Parses the service config.
	 *
	 * @param input the input
	 * @param serviceConfig the service config
	 * @return the iSOA service config
	 * @throws Exception the exception
	 */
	public static ISOAServiceConfig parseServiceConfig(final InputStream input, final ISOAServiceConfig serviceConfig)
			throws Exception {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			ServiceConfig config = new ServiceConfigXmlHelper().parseServiceConfig(input);
			serviceConfig.setFullyQualifiedServiceName(config.getServiceName());
			if (StringUtils.isNotBlank(config.getServiceName())) {
				String[] names = parseFullyQualifiedServiceName(config.getServiceName());
				if (names != null) {
					serviceConfig.setTargetNamespace(names[0]);
					serviceConfig.setServiceName(names[1]);
				}
			}
			serviceConfig.setServiceInterfaceClassName(StringUtils.trim(config.getServiceInterfaceClassName()));
			serviceConfig.setServiceImplClassName(StringUtils.trim(config.getServiceImplClassName()));
			return serviceConfig;
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Client config to xml.
	 *
	 * @param clientCfgList the client cfg list
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String clientConfigToXml(final ClientConfigList clientCfgList)
			throws Exception {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			return ConfigHelper.clientConfigToXml(clientCfgList);
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * Service config to xml.
	 *
	 * @param config the config
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String serviceConfigToXml(final ServiceConfig config)
			throws Exception {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					ConfigTool.class.getClassLoader());
			return ConfigHelper.serviceConfigToXml(config);
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * Gets the service meta data path.
	 *
	 * @param serviceName the service name
	 * @return the service meta data path
	 * @throws BadInputValueException the bad input value exception
	 */
	public static IPath getServiceMetaDataPath(final String serviceName) throws BadInputValueException {
			return new Path(CodeGenInfoFinder.getPathforNonModifiableArtifact(
					serviceName, "SERVICE_METADATA"));
	}

	/**
	 * Gets the wSDL path.
	 *
	 * @param serviceName the service name
	 * @return the wSDL path
	 * @throws BadInputValueException the bad input value exception
	 */
	public static IPath getWSDLPath(final String serviceName) throws BadInputValueException {
		return new Path(CodeGenInfoFinder.getPathforNonModifiableArtifact(
				serviceName, "WSDL"));
	}

	/**
	 * Gets the default service layers from file.
	 *
	 * @return the default service layers from file
	 */
	public static List<String> getDefaultServiceLayersFromFile() {
		try {
			return CodeGenInfoFinder.getServiceLayersFromDefaultFile();
		} catch (CodeGenFailedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the default package name from namespace.
	 *
	 * @param namespace the namespace
	 * @return the default package name from namespace
	 */
	public static String getDefaultPackageNameFromNamespace(final String namespace) {
		return WSDLUtil.getPackageFromNamespace(namespace);
	}
	
	/**
	 * Gets the type package name from namespace.
	 *
	 * @param namespace the namespace
	 * @param serviceName the service name
	 * @return the type package name from namespace
	 */
	public static String getTypePackageNameFromNamespace(final String namespace, 
			final String serviceName) {
		final String defaultPkgName = getDefaultPackageNameFromNamespace(namespace);
		return new StringBuilder(defaultPkgName).append(".")
		.append(StringUtils.lowerCase(serviceName)).toString();
	}
	
	/**
	 * The Interface ISOAClientConfig.
	 */
	public static interface ISOAClientConfig {
		
		/**
		 * Sets the target namespace.
		 *
		 * @param targetNamespace the new target namespace
		 */
		public void setTargetNamespace(String targetNamespace);
		
		/**
		 * Sets the service name.
		 *
		 * @param serviceName the new service name
		 */
		public void setServiceName(String serviceName);
		
		/**
		 * Sets the fully qualified service name.
		 *
		 * @param fullyQualifiedServiceName the new fully qualified service name
		 */
		public void setFullyQualifiedServiceName(String fullyQualifiedServiceName);
		
		/**
		 * Sets the group.
		 *
		 * @param group the new group
		 */
		public void setGroup(String group);
		
		/**
		 * Sets the service interface class name.
		 *
		 * @param serviceInterfaceClassName the new service interface class name
		 */
		public void setServiceInterfaceClassName(String serviceInterfaceClassName);
		
		/**
		 * Sets the service location.
		 *
		 * @param serviceLocation the new service location
		 */
		public void setServiceLocation(String serviceLocation);
		
		/**
		 * Sets the wsdl location.
		 *
		 * @param wsdlLocation the new wsdl location
		 */
		public void setWsdlLocation(String wsdlLocation);
		
		/**
		 * Sets the service binding.
		 *
		 * @param serviceBinding the new service binding
		 */
		public void setServiceBinding(String serviceBinding);
		
		/**
		 * Sets the invocation use case.
		 *
		 * @param invocationUseCase the new invocation use case
		 */
		public void setInvocationUseCase(String invocationUseCase);
		
		/**
		 * Sets the request data binding.
		 *
		 * @param requestDataBinding the new request data binding
		 */
		public void setRequestDataBinding(String requestDataBinding);
		
		/**
		 * Sets the response data binding.
		 *
		 * @param responseDataBinding the new response data binding
		 */
		public void setResponseDataBinding(String responseDataBinding);
		
		/**
		 * Sets the consumer id.
		 *
		 * @param consumerId the new consumer id
		 */
		public void setConsumerId(String consumerId);
		
		/**
		 * Sets the message protocol.
		 *
		 * @param messageProtocol the new message protocol
		 */
		public void setMessageProtocol(String messageProtocol);
		
	}
	
	/**
	 * The Interface ISOAServiceConfig.
	 */
	public static interface ISOAServiceConfig {
		//service config file will no longer have version
		/*public String getCurrentVersion();
		public void setCurrentVersion(String currentVersion);*/

		/**
		 * Sets the supported version.
		 *
		 * @param supportedVersion the new supported version
		 */
		public void setSupportedVersion(String supportedVersion);

		/**
		 * Sets the group.
		 *
		 * @param group the new group
		 */
		public void setGroup(String group);

		/**
		 * Sets the target namespace.
		 *
		 * @param targetNamespace the new target namespace
		 */
		public void setTargetNamespace(String targetNamespace);

		/**
		 * Sets the service name.
		 *
		 * @param serviceName the new service name
		 */
		public void setServiceName(String serviceName);

		/**
		 * Sets the fully qualified service name.
		 *
		 * @param fullyQualifiedServiceName the new fully qualified service name
		 */
		public void setFullyQualifiedServiceName(String fullyQualifiedServiceName);
		
		/**
		 * Sets the service interface class name.
		 *
		 * @param serviceInterfaceClassName the new service interface class name
		 */
		public void setServiceInterfaceClassName(String serviceInterfaceClassName);

		/**
		 * Gets the service impl class name.
		 *
		 * @return the service impl class name
		 */
		public String getServiceImplClassName();
		
		/**
		 * Sets the service impl class name.
		 *
		 * @param serviceImplClassName the new service impl class name
		 */
		public void setServiceImplClassName(String serviceImplClassName);
		
		/**
		 * Gets the message protocol.
		 *
		 * @return the message protocol
		 */
		public String getMessageProtocol();
		
		/**
		 * Sets the message protocol.
		 *
		 * @param messageProtocol the new message protocol
		 */
		public void setMessageProtocol(String messageProtocol);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		System.out.println(getDefaultServiceLayersFromFile());
	}
	
	/**
	 * Parses the fully qualified service name.
	 *
	 * @param serviceName the service name
	 * @return The first value is the namespace and the second is the service name
	 */
	public static String[] parseFullyQualifiedServiceName(final String serviceName) {
		if (StringUtils.isBlank(serviceName))
			throw new IllegalArgumentException("Service name must not be empty->" + serviceName);
		String[] result = new String[2];
		result[0] = StringUtils.substringBetween(serviceName, "{", "}");
		result[1] = StringUtils.substringAfter(serviceName, "}");
		return result;
	}
}
