/*******************************************************************************
 * Copyright (c) 2010 Philipp Kursawe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philipp Kursawe (phil.kursawe@gmail.com) - initial API and implementation
 ******************************************************************************/
package eclipseutils.ui.copyto.internal;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class MenuFactory extends ExtensionContributionFactory {
	private final static Bundle bundle = FrameworkUtil
			.getBundle(MenuFactory.class);
	public static final String MENU_URI = "menu:" + bundle.getSymbolicName()
			+ ".menu";

	@Override
	public void createContributionItems(final IServiceLocator locator,
			final IContributionRoot root) {
		final IMenuService menuService = (IMenuService) locator
				.getService(IMenuService.class);
		URL iconEntry = FileLocator.find(bundle, new Path(
				"$nl$/icons/e16/copyto.png"), null);
		ImageDescriptor icon = (iconEntry != null) ? ImageDescriptor
				.createFromURL(iconEntry) : null;

		final MenuManager menuManager = new MenuManager("Copy To", icon, null) { //$NON-NLS-2$
			@Override
			public void dispose() {
				menuService.releaseContributions(this);
				super.dispose();
			};
		};
		menuService.populateContributionManager(menuManager, MENU_URI);
		if (menuManager.getSize() == 1) {
			root.addContributionItem(menuManager.getItems()[0], null);
			menuManager.dispose();
		} else {
			root.addContributionItem(menuManager, null);
		}
	}
}
