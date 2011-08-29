/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.eclipse.ui.views.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.common.config.TypeLibraryType;
import org.ebayopensource.turmeric.eclipse.core.logging.SOALogger;
import org.ebayopensource.turmeric.eclipse.ui.UIActivator;
import org.ebayopensource.turmeric.tools.library.SOATypeRegistry;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The Class TypeLibraryContentProvider.
 *
 * @author smathew
 */
public class TypeLibraryContentProvider implements ITreeContentProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		// this is the root.
		final List<IRegistryTreeNode> children = new ArrayList<IRegistryTreeNode>();
		if (parentElement instanceof TypeTreeRoot) {
			// first level
			for (String typeLibraryCategory : UIActivator.getCategories()) {
				children.add(new CategoryTreeNode(typeLibraryCategory,
						(TypeTreeRoot) parentElement));
			}
		} else if (parentElement instanceof CategoryTreeNode) {
			final CategoryTreeNode parentNode = (CategoryTreeNode) parentElement;
			// this is second level
			SOATypeRegistry typeRegistry = parentNode.getTypeTreeRoot()
					.getTypeRegistry();
			try {
				final List<TypeLibraryType> libs = typeRegistry
						.getAllTypeLibraries();
				final List<TypeLibraryType> clonedList = new ArrayList<TypeLibraryType>(
						libs);
				// filtering out the non category elements
				CollectionUtils.filter(clonedList,
						getPredicate(((CategoryTreeNode) parentElement)
								.getCategory()));
				for (TypeLibraryType type : clonedList) {
					children.add(new TypeLibraryTreeNode(parentNode, type));
				}
			} catch (Exception e) {
				SOALogger.getLogger().error(e);
			}
		}
		return children.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof IRegistryTreeNode) {
			return ((IRegistryTreeNode) element).getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof TypeTreeRoot || element instanceof CategoryTreeNode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof SOATypeRegistry) {
			TypeTreeRoot[] typeTreeRoots = new TypeTreeRoot[1];
			typeTreeRoots[0] = new TypeTreeRoot((SOATypeRegistry) inputElement);
			return typeTreeRoots;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	private Predicate getPredicate(final String typeLibraryCategory) {
		return new Predicate() {
			@Override
			public boolean evaluate(Object arg0) {
				if (arg0 instanceof TypeLibraryType) {
					return StringUtils.equalsIgnoreCase(
							((TypeLibraryType) arg0).getCategory(),
							typeLibraryCategory);
				}
				return false;
			}

		};
	}
}
