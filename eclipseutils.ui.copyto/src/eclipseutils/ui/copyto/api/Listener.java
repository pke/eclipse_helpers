package eclipseutils.ui.copyto.api;

import java.util.EventListener;

/**
 * Copyable
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface Listener extends EventListener {
	public void onEvent(Event event);
}