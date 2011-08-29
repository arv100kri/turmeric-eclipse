/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.typelibrary.builders;

import org.ebayopensource.turmeric.eclipse.core.buildsystem.AbstractSOANature;
import org.ebayopensource.turmeric.eclipse.core.resources.constants.SOAProjectConstants.SupportedProjectType;
import org.ebayopensource.turmeric.eclipse.repositorysystem.core.GlobalRepositorySystem;
import org.ebayopensource.turmeric.eclipse.typelibrary.TypeLibraryActivator;


// TODO: Auto-generated Javadoc
/**
 * The Class TypeLibraryProjectNature.
 *
 * @author smathew
 * 
 * Type Lib nature
 */
public class TypeLibraryProjectNature extends AbstractSOANature {

	/** The Constant NATURE_ID. */
	public static final String NATURE_ID = TypeLibraryActivator.PLUGIN_ID
			+ ".TypeLibraryProjectNature";
	
	/**
	 * Gets the type library nature id.
	 *
	 * @return the type library nature id
	 */
	public static String getTypeLibraryNatureId() {
		return GlobalRepositorySystem.instanceOf().getActiveRepositorySystem()
		.getProjectNatureId(SupportedProjectType.TYPE_LIBRARY);
	}

	/**
	 * Instantiates a new type library project nature.
	 */
	public TypeLibraryProjectNature() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.eclipse.core.buildsystem.AbstractSOANature#getBuilderName()
	 */
	@Override
	public String getBuilderName() {
		return TypeLibraryProjectBuilder.BUILDER_ID;
	}

}
