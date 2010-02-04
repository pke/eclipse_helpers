package eclipseutils.ui.copyto.api;

import java.util.Collection;

/**
 * Performs some actions on a set of Results.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface ResultsHandler {
	void handleResults(Collection successes, Collection failures);
}
