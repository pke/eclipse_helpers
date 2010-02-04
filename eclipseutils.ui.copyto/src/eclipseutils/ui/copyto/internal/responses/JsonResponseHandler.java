package eclipseutils.ui.copyto.internal.responses;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.mortbay.util.ajax.JSON;

import eclipseutils.ui.copyto.api.ResponseHandler;
import eclipseutils.ui.copyto.api.json.MapResponse;
import eclipseutils.ui.copyto.api.json.Response;

/**
 * This tries to convert the JSON result into an URL by asking for an adapter that can adapt {@link Response} to {@link URL}. 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public class JsonResponseHandler implements ResponseHandler {

	private final IAdapterManager adapterManager = Platform.getAdapterManager();

	public URL getLocation(final HttpMethod method) throws Exception {
		if (200 == method.getStatusCode()) {
			final Object result = JSON.parse(new InputStreamReader(method.getResponseBodyAsStream()), true);
			if (result != null) {
				Response response = null;
				if (result instanceof Map) {
					response = new MapResponse() {
						public Map getMap() {
							return (Map) result;
						}
					};
				}
				if (response != null) {
					final URL url = (URL) this.adapterManager.loadAdapter(response, URL.class.getName());
					if (url != null) {
						return url;
					}
				}
			}
		}
		throw new IOException("Response was not a proper JSON response");
	}

}
