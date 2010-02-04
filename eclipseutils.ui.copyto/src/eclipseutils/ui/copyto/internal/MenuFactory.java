package eclipseutils.ui.copyto.internal;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.FrameworkUtil;

public class MenuFactory extends ExtensionContributionFactory {
	private final String symbolicName = FrameworkUtil.getBundle(getClass()).getSymbolicName();

	public void createContributionItems(final IServiceLocator locator, final IContributionRoot root) {
		final IMenuService menuService = (IMenuService) locator.getService(IMenuService.class);
		final MenuManager menuManager = new MenuManager("Copy To", this.symbolicName + ".menu") { //$NON-NLS-2$
			public void dispose() {
				menuService.releaseContributions(this);
				super.dispose();
			};
		};
		menuService.populateContributionManager(menuManager, "menu:" + menuManager.getId());
		if (menuManager.getSize() == 1) {
			root.addContributionItem(menuManager.getItems()[0], null);
			menuManager.dispose();
		} else {
			root.addContributionItem(menuManager, null);
		}
	}

}
