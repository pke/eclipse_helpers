package eclipseutils.ui.copyto.internal;

import java.net.URL;

import org.eclipse.core.runtime.IAdapterFactory;

import eclipseutils.ui.copyto.api.json.MapResponse;

public class AdapterFactory implements IAdapterFactory {

	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof MapResponse) {
			try {
				return new URL(((MapResponse) adaptableObject).getMap().get("url").toString()); //$NON-NLS-1$
			} catch (final Exception e) {
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return null;
	}

}
