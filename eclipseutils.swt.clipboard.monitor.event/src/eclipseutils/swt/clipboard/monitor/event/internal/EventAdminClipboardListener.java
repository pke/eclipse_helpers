package eclipseutils.swt.clipboard.monitor.event.internal;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import eclipseutils.swt.clipboard.monitor.ClipboardEvent;
import eclipseutils.swt.clipboard.monitor.ClipboardListener;

public class EventAdminClipboardListener implements ClipboardListener {
	private static final String TOPIC = "eclipseutils/swt/clipboard/monitor/event";
	AtomicReference<EventAdmin> ref = new AtomicReference<EventAdmin>();

	protected void bind(EventAdmin eventAdmin) {
		ref.set(eventAdmin);
	}

	protected void unbind(EventAdmin eventAdmin) {
		ref.compareAndSet(eventAdmin, null);
	}

	public void onEvent(ClipboardEvent event) {
		EventAdmin eventAdmin = ref.get();
		if (eventAdmin != null) {
			eventAdmin.postEvent(new Event(TOPIC, (Map<?, ?>) null));
		}
	}
}
