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
package org.ebayopensource.turmeric.eclipse.core.model;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class AbstractSOAProjetParamModel.
 *
 * @author yayu
 * @since 1.0.0
 */
public abstract class AbstractSOAProjetParamModel extends BaseServiceParamModel {
	private String projectName;
	
	private final List<String> eclipseNatures = new ArrayList<String>(5);
	
	private final List<UIModelProjectLinkedResource> linkedResources = new ArrayList<UIModelProjectLinkedResource>(3);
	
	/**
	 * Instantiates a new abstract soa projet param model.
	 */
	public AbstractSOAProjetParamModel() {
		super();
	}
	
	/**
	 * Gets the project name.
	 *
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Sets the project name.
	 *
	 * @param projectName the project name
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Gets the eclipse natures.
	 *
	 * @return a List of eclispe natures
	 */
	public List<String> getEclipseNatures() {
		return eclipseNatures;
	}
	
	/**
	 * Adds the eclipse nature.
	 *
	 * @param eclipseNature a eclipse nature name
	 * @return whether the nature was added successfully.
	 */
	public boolean addEclipseNature(String eclipseNature) {
		return eclipseNatures.add(eclipseNature);
	}
	
	/**
	 * Adds the project linked resource.
	 *
	 * @param linkedResource a UIModelProjectLinkedResource
	 * @return whether the linked resource was added successfully.
	 */
	public boolean addProjectLinkedResource(UIModelProjectLinkedResource linkedResource) {
		return linkedResources.add(linkedResource);
	}

	/**
	 * Gets the linked resources.
	 *
	 * @return a List of UIModelProjectLinkedResource.
	 */
	public List<UIModelProjectLinkedResource> getLinkedResources() {
		return linkedResources;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.ebayopensource.turmeric.eclipse.core.model.ISOAServiceParamModel#validate()
	 */
	@Override
	public boolean validate() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append("Project Name: ");
		buf.append(this.projectName);
		return buf.toString();
	}
	
	/**
	 * The Class UIModelProjectLinkedResource.
	 */
	public static class UIModelProjectLinkedResource {
		/**
		 * a type resource. 
		 */
		public static final int LINK_TYPE_RESOURCE = 1;
		
		/**
		 * a type folder.
		 */
		public static final int LINK_TYPE_FOLDER = 2;
		
		private String name;
		private int type = LINK_TYPE_RESOURCE;
		private String location;
		
		/**
		 * Instantiates a new uI model project linked resource.
		 *
		 * @param name resource name
		 * @param type type
		 * @param location location
		 */
		public UIModelProjectLinkedResource(String name, int type, String location) {
			super();
			this.name = name;
			this.type = type;
			this.location = location;
		}
		
		/**
		 * Instantiates a new uI model project linked resource.
		 *
		 * @param name resource name
		 * @param location location
		 */
		public UIModelProjectLinkedResource(String name, String location) {
			this(name, LINK_TYPE_RESOURCE, location);
		}

		/**
		 * Gets the name.
		 *
		 * @return resource name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets the name.
		 *
		 * @param name resource name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Gets the type.
		 *
		 * @return resource type
		 */
		public int getType() {
			return type;
		}

		/**
		 * Sets the type.
		 *
		 * @param type linked resource type
		 */
		public void setType(int type) {
			this.type = type;
		}

		/**
		 * Gets the location.
		 *
		 * @return the location
		 */
		public String getLocation() {
			return location;
		}

		/**
		 * Sets the location.
		 *
		 * @param location location
		 */
		public void setLocation(String location) {
			this.location = location;
		}
	}

}
