package eclipseutils.swt.clipboard.monitor;

import java.util.EventObject;

/**
 * Sent when a change event in the system clipboard has occured.
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * 
 */
public class ClipboardEvent extends EventObject {
	private static final long serialVersionUID = 6354639749124932240L;

	public ClipboardEvent(Object source) {
		super(source);
	}
}
