package eclipseutils.ui.copyto.internal.responses;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

import eclipseutils.ui.copyto.api.ResponseHandler;

public class RedirectResponseHandler implements ResponseHandler {

	public URL getLocation(final HttpMethod method) throws Exception {
		if (302 == method.getStatusCode()) {
			final Header locationHeader = method.getResponseHeader("Location"); //$NON-NLS-1$
			String value = locationHeader.getValue();
			if (value.charAt(0) == '/') {
				value = "http://" + method.getRequestHeader("Host").getValue() + value; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return new URL(value);
		}
		throw new IOException("Response did not contain a redirect location");
	}

}
