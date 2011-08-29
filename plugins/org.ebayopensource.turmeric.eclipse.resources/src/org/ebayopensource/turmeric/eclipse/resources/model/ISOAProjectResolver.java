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
 * The Interface ISOAProjectResolver.
 *
 * @param <P> the generic type
 * @author yayu
 * @since 1.0.0
 */
public interface ISOAProjectResolver<P extends ISOAProject> {
	
	/**
	 * Load the SOA project instance.
	 *
	 * @param eclipseMetadata the eclipse metadata
	 * @return an ISOAProject
	 * @throws Exception the exception
	 */
	public P loadProject(SOAProjectEclipseMetadata eclipseMetadata) throws Exception;
}
