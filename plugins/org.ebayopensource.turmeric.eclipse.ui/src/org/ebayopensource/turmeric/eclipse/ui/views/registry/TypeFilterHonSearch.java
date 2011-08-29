/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.ui.views.registry;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.common.config.LibraryType;
import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

// TODO: Auto-generated Javadoc
/**
 * The Class TypeFilterHonSearch.
 *
 * @author smathew
 */
public class TypeFilterHonSearch extends ViewerFilter {

	/** The pattern. */
	private Pattern pattern;
	
	/** The empty key. */
	private boolean emptyKey = false;

	/**
	 * Instantiates a new type filter hon search.
	 *
	 * @param key the key
	 */
	public TypeFilterHonSearch(String key) {
		emptyKey = StringUtils.isEmpty(key);
		if (emptyKey == false) {
			key = key.trim().toLowerCase();
			if (key.length() > 0) {
				if (key.charAt(key.length() - 1) != '*') {
					key += '*';
				}
				pattern = Pattern.compile(StringUtils.replace(key, "*", ".*"));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (emptyKey == true) {
			return true;
		}
		if (element instanceof LibraryType) {
			LibraryType type = (LibraryType) element;
			if (StringUtils.isEmpty(type.getName()) == false) {
				return patternMatch(type.getName().toLowerCase());
			}
		}
		return true;
	}

	/**
	 * Pattern match.
	 *
	 * @param searchStr the search str
	 * @return true, if successful
	 */
	private boolean patternMatch(String searchStr) {
		try {
			return pattern.matcher(searchStr).matches();
		} catch (PatternSyntaxException e) {
			// Ignore it. User has messed with the search string. Plugin cant do
			// anything here.
			// Stay Silent
			SOALogger.getLogger().error(e);
		}
		return false;
	}

}
