package eclipseutils.ui.copyto.api;

import java.net.URL;

import eclipseutils.ui.copyto.internal.responses.JsonResponseHandler;

/**
 * 
 * <p>
 * Used by the {@link JsonResponseHandler} to extract the URL for the paste. 
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * 
 */
public interface JsonLocationResolver {
	/**
	 * @param object a Map, Object array or primitive array parsed from the JSON.
	 * @return The extracted URL or <code>null</code> if no URL could be extracted from <i>object</i>.
	 */
	URL getLocation(Object object);
}
