package eclipseutils.ui.clipboard.monitor.swt.internal;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import eclipseutils.swt.clipboard.monitor.ClipboardEvent;
import eclipseutils.swt.clipboard.monitor.ClipboardListener;

public class SWTClipboardListener implements ClipboardListener, EventHandler {

	public void onEvent(ClipboardEvent event) {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		try {
			final String[] typeNames = clipboard.getAvailableTypeNames();
			// Find listeners that match the types
			// (|(type=typeNames...))
		} finally {
			clipboard.dispose();
		}
	}

	public void handleEvent(Event event) {
		// Republish using the EventAdmin with the types
	}
}
