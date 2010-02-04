package eclipseutils.ui.copyto.api;

import java.util.EventObject;

import eclipseutils.ui.copyto.api.Copyable;
import eclipseutils.ui.copyto.internal.Handler;

public class Event extends EventObject {
	private static final long serialVersionUID = -2873933424257746963L;
	private final Copyable copyable;

	public Event(final Handler handler, final Copyable copyable) {
		super(handler);
		this.copyable = copyable;
	}

	public Copyable getCopyable() {
		return this.copyable;
	}
}
