/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.template.wsdl.resources;

import org.eclipse.osgi.util.NLS;

/**
 * Standard messages class.
 * 
 * @author smathew
 * @author yayu
 * 
 */
public class SOAMessages extends NLS {
	private static final String BUNDLE_NAME = "org.ebayopensource.turmeric.eclipse.template.wsdl.resources.messages"; //$NON-NLS-1$
	
	
	
	/** The IMPOR t_ err. */
	public static String IMPORT_ERR;

	/** The INPU t_ err. */
	public static String INPUT_ERR;
	
	/** The O p_ err. */
	public static String OP_ERR;
	
	/** The TYP e_ no t_ found. */
	public static String TYPE_NOT_FOUND;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SOAMessages.class);
	}
}
