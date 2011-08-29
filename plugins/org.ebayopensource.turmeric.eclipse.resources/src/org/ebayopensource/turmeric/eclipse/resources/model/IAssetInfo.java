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
package org.ebayopensource.turmeric.eclipse.resources.model;

// TODO: Auto-generated Javadoc
/**
 * The Interface IAssetInfo.
 *
 * @author yayu
 * @since 1.0.0
 */
public interface IAssetInfo {
	
	/** The Constant TYPE_PROJECT. */
	public static final String TYPE_PROJECT = "Project";
	
	/** The Constant TYPE_LIBRARY. */
	public static final String TYPE_LIBRARY = "Library";
	
	/** The Constant TYPE_SERVICE_PROJECT. */
	public static final String TYPE_SERVICE_PROJECT = "Project_Service";
	
	/** The Constant TYPE_SERVICE_LIBRARY. */
	public static final String TYPE_SERVICE_LIBRARY = "Library_Service";

	/**
	 * The name of the asset.
	 * @return the name of the asset as a String.
	 */
	public String getName();
	
	/**
	 * The type of the asset.
	 * @return the type of asset.
	 */
	public String getType();

	/**
	 * The version of the asset.
	 * @return the version as a string
	 */
	public String getVersion();
	
	/**
	 * An ID that could uniquely identify the asset. 
	 * @return a uniqueID as a String.
	 */
	public String getUniqueID();

	/**
	 * A long description of the asset.
	 * @return the description as a string.
	 */
	public String getDescription();
	
	/**
	 * A brief description of the asset.
	 * 
	 * @return a short description as a string
	 */
	public String getShortDescription();

}
