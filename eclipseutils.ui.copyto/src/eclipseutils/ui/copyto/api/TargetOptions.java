package eclipseutils.ui.copyto.api;

import java.util.Map;

/**
 * This is usually loaded from the preferences. 
 * But it can also be used in a dialog so that the user can change the values.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface TargetOptions {
	/**
	 * The returned map will be added as HTTP post parameters.
	 */
	Map getOptions();
}
