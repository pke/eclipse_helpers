package eclipseutils.ui.copyto.internal;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import eclipseutils.ui.copyto.api.Copyable;
import eclipseutils.ui.copyto.api.Result;

/**
 * Handles the copying of a specific text.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * @since 1.0
 */
public interface Handler {
	/**
	 * @param text contained in the item
	 * @param item the text was generated from
	 * @param monitor to report progress on the copying of the text
	 * @return
	 * @throws Exception
	 */
	Result copy(final Copyable copyable, IProgressMonitor monitor);

	/**
	 * @return the declared params
	 */
	Map getParams();

	String getId();
}