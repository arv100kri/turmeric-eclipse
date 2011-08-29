/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.ui.components;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.eclipse.resources.model.AssetInfo;
import org.eclipse.jface.viewers.LabelProvider;


/**
 * The Class QualifiedRenderer.
 *
 * @author smathew
 * 
 * Qualified Renderer for Dependency Selector
 * @see DependencySelector
 */
public class QualifiedRenderer extends LabelProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(final Object element) {
		if (element instanceof AssetInfo) {
			final AssetInfo info = (AssetInfo) element;
			String postFix = "";
			if (StringUtils.isNotBlank(info.getDir()) == true) {
				postFix = " - " + info.getDir();
			}
			return info.getDescription() + postFix;
		}
		return super.getText(element);

	}
}
