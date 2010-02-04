package eclipseutils.ui.copyto.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.Preferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		final Preferences node = new DefaultScope().getNode(FrameworkUtil.getBundle(getClass()).getSymbolicName());
		node.putBoolean("confirm.copyToClipboard", true); //$NON-NLS-1$
	}
}
