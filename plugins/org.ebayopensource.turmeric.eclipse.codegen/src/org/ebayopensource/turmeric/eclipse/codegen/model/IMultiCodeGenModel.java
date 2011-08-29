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
package org.ebayopensource.turmeric.eclipse.codegen.model;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * A code gen model that should be executed more than once.
 *
 * @author yayu
 */
public interface IMultiCodeGenModel {
	
	/**
	 * Iterator.
	 *
	 * @return the i multi code gen model iterator
	 */
	public IMultiCodeGenModelIterator iterator();
	
	/**
	 * The Interface IMultiCodeGenModelIterator.
	 */
	public static interface IMultiCodeGenModelIterator {
		
		/**
		 * Checks for next.
		 *
		 * @return true, if successful
		 */
		public boolean hasNext();
		
		/**
		 * Next input options.
		 *
		 * @return the map
		 */
		public Map<String, String> nextInputOptions();
	}
}
