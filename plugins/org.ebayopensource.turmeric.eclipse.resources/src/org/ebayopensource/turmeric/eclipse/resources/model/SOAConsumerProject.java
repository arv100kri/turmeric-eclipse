/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.resources.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants;
import org.ebayopensource.turmeric.eclipse.resources.util.SOAConsumerUtil;
import org.ebayopensource.turmeric.eclipse.utils.collections.ListUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;


// TODO: Auto-generated Javadoc
/**
 * The Class SOAConsumerProject.
 *
 * @author yayu
 */
public class SOAConsumerProject extends SOABaseProject implements ISOAConsumerProject{
	/**
	 * An array of source directories.
	 */
	public static final String[] SOURCE_DIRECTORIES = {
		SOAProjectConstants.FOLDER_SRC,
		SOAProjectConstants.FOLDER_META_SRC };
	
	/** The Constant FOLDER_CLIENT. */
	public static final String FOLDER_CLIENT = "/soa/client/";
	
	/** The Constant FOLDER_CLIENT_CONFIG. */
	public static final String FOLDER_CLIENT_CONFIG = FOLDER_CLIENT + "config/";
	
	/** The Constant FOLDER_META_SRC_ClIENT. */
	public static final String FOLDER_META_SRC_ClIENT = SOAProjectConstants.META_SRC_META_INF
	+ FOLDER_CLIENT;
	
	/** The Constant META_SRC_ClIENT_CONFIG. */
	public static final String META_SRC_ClIENT_CONFIG = SOAProjectConstants.META_SRC_META_INF
	+ FOLDER_CLIENT_CONFIG;
	
    /** The required services. */
    private Map<String, SOAIntfMetadata> requiredServices = new TreeMap<String, SOAIntfMetadata>();
    
    /** The client configs. */
    private Map<SOAClientEnvironment, SOAClientConfig> clientConfigs = new TreeMap<SOAClientEnvironment, SOAClientConfig>();

    /**
     * Instantiates a new sOA consumer project.
     */
	public SOAConsumerProject() {
		super();
	}
    
	/**
	 * Creates the.
	 *
	 * @param consumerMetadata the consumer Meta Data
	 * @param eclipseMetadata  the eclispe meta data
	 * @return an instance of SOAConsumerProject
	 * @throws Exception the exception
	 */
	public static SOAConsumerProject create(SOAConsumerMetadata consumerMetadata,
			SOAProjectEclipseMetadata eclipseMetadata) throws Exception{
		SOAConsumerProject consumerProject = new SOAConsumerProject();
		consumerProject.setMetadata(consumerMetadata);
		consumerProject.setEclipseMetadata(eclipseMetadata);
		return consumerProject;
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.resources.model.SOABaseProject#getMetadata()
	 */
	@Override
	public SOAConsumerMetadata getMetadata() {
		final AbstractSOAMetadata metadata = super.getMetadata();
		return metadata instanceof SOAConsumerMetadata ? (SOAConsumerMetadata)metadata : null;
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.resources.model.SOABaseProject#getSOAMetadataClass()
	 */
	@Override
	protected Class<? extends AbstractSOAMetadata> getSOAMetadataClass() {
		return SOAConsumerMetadata.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, SOAIntfMetadata> getRequiredServices() {
		return requiredServices;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRequiredServices(Map<String, SOAIntfMetadata> requiredServices) {
		this.requiredServices = requiredServices;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<SOAClientEnvironment, IFile> getClientConfigFiles() throws CoreException {
		return SOAConsumerUtil.getClientConfigFiles(getProject());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IFile getClientConfigFile(final SOAClientEnvironment clientEnv) throws CoreException, IOException {
		return SOAConsumerUtil.getClientConfig(getProject(), clientEnv.getEnvironment()
				, clientEnv.getServiceName());
	}
	
	/**
	 * {@inheritDoc}
	 * @return an IFile that represents the GlobalClientConfig File
	 */
	public IFile getGlobalClientConfigFile() {
		return getProject().getFile(ISOAConsumerProject.META_SRC_GLOBAL_ClIENT_CONFIG);
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.resources.model.SOABaseProject#getSourceSubFolders()
	 */
	@Override
	public List<String> getSourceSubFolders() {
		List<String> subFolders = new ArrayList<String>();
		subFolders.add(META_SRC_ClIENT_CONFIG);
		return subFolders;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getSourceDirectoryNames() {
		final List<String> result = ListUtil.arrayList(SOURCE_DIRECTORIES);
		if (getMetadata() != null) {
			final String baseConsumerDir = getMetadata().getBaseConsumerSrcDir();
			if (StringUtils.isNotBlank(baseConsumerDir) && 
					result.contains(baseConsumerDir) == false) {
				result.add(baseConsumerDir);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<SOAClientEnvironment, SOAClientConfig> getClientConfigs() {
		return clientConfigs;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClientConfigs(Map<SOAClientEnvironment, SOAClientConfig> clientConfigs) {
		if (clientConfigs != null)
			this.clientConfigs = clientConfigs;
	}
	
	/**
	 * Adds the client config.
	 *
	 * @param envName environment name
	 * @param clientConfig the client configuration file.
	 */
	public void addClientConfig(String envName, SOAClientConfig clientConfig) {
		if (clientConfig != null) {
			final SOAClientConfig config = new SOAClientConfig();
			config.setServiceName(clientConfig.getServiceName());
			config.setFullyQualifiedServiceName(clientConfig.getFullyQualifiedServiceName());
			config.setGroup(clientConfig.getGroup());
			config.setInvocationUseCase(clientConfig.getInvocationUseCase());
			config.setMessageProtocol(clientConfig.getMessageProtocol());
			config.setRequestDataBinding(clientConfig.getRequestDataBinding());
			config.setResponseDataBinding(clientConfig.getResponseDataBinding());
			config.setServiceBinding(clientConfig.getServiceBinding());
			config.setServiceInterfaceClassName(clientConfig.getServiceInterfaceClassName());
			config.setServiceLocation(clientConfig.getServiceLocation());
			config.setTargetNamespace(clientConfig.getTargetNamespace());
			config.setWsdlLocation(clientConfig.getWsdlLocation());
			clientConfigs.put(new SOAClientEnvironment(envName, clientConfig.getServiceName())
			, config);
		}
		
	}
}
