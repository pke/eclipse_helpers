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

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.FrameworkUtil;

public class MenuFactory extends ExtensionContributionFactory {
	private final String symbolicName = FrameworkUtil.getBundle(getClass())
			.getSymbolicName();

	@Override
	public void createContributionItems(final IServiceLocator locator,
			final IContributionRoot root) {
		final IMenuService menuService = (IMenuService) locator
				.getService(IMenuService.class);
		final MenuManager menuManager = new MenuManager(
				"Copy To", this.symbolicName + ".menu") { //$NON-NLS-2$
			@Override
			public void dispose() {
				menuService.releaseContributions(this);
				super.dispose();
			};
		};
		menuService.populateContributionManager(menuManager, "menu:"
				+ menuManager.getId());
		if (menuManager.getSize() == 1) {
			root.addContributionItem(menuManager.getItems()[0], null);
			menuManager.dispose();
		} else {
			root.addContributionItem(menuManager, null);
		}
	}

}
