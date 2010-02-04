package eclipseutils.ui.copyto.api;

import java.net.URL;

import org.apache.commons.httpclient.HttpMethod;

/**
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface ResponseHandler {
	/**
	 * Retrieves a location URL from a HTTP method response.
	 * 
	 * @param method to use for extracting the location URL.
	 * @return a valid URL object
	 * @throws Exception if there was an error creating a location URL from the given <code>method</code>.
	 */
	URL getLocation(HttpMethod method) throws Exception;
}
