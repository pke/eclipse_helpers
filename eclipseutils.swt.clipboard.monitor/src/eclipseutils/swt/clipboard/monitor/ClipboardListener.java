package eclipseutils.swt.clipboard.monitor;

import java.util.EventListener;

/**
 * Services publishing this interface will be notified about changes in the
 * system clipboard.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public interface ClipboardListener extends EventListener {
	void onEvent(ClipboardEvent event);
}
