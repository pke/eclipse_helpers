package eclipseutils.ui.clipboard.monitor.swt.internal;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

import eclipseutils.swt.clipboard.monitor.ClipboardEvent;
import eclipseutils.swt.clipboard.monitor.ClipboardListener;

public class SWTClipboardListener implements ClipboardListener, EventHandler {

	private static final String BASE_TOPIC = "eclipseutils/ui/clipboard/monitor/swt/";
	AtomicReference<EventAdmin> ref = new AtomicReference<EventAdmin>();

	interface TypeRunnable {
		void run(String[] types);
	}

	protected void bind(EventAdmin eventAdmin) {
		ref.set(eventAdmin);
	}

	protected void unbind(EventAdmin eventAdmin) {
		ref.compareAndSet(eventAdmin, null);
	}

	public void onEvent(ClipboardEvent event) {
		getTypes(new TypeRunnable() {
			public void run(String[] types) {
			}
		});
	}

	private void getTypes(final TypeRunnable runnable) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Clipboard clipboard = new Clipboard(Display.getDefault());
				try {
					String[] types = clipboard.getAvailableTypeNames();
					runnable.run(types);
				} finally {
					clipboard.dispose();
				}
			}
		});
	}

	public void handleEvent(Event event) {
		final EventAdmin eventAdmin = ref.get();
		if (eventAdmin != null) {
			getTypes(new TypeRunnable() {
				public void run(String[] types) {
					for (String type : types) {
						String topic = BASE_TOPIC + type;
						eventAdmin
								.postEvent(new Event(topic, (Map<?, ?>) null));
					}
				}
			});
		}
	}
}
