package eclipseutils.ui.copyto.api;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;


/**
 * Result of a copyTo action.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * @since 1.0
 */
public interface Result {
	/**
	 * @return the name of the location. (ie. <code>pastebin.com</code>)
	 */
	String getTargetName();

	/**
	 * @return the Copyable used.
	 */
	Copyable getCopyable();

	/**
	 * @return the location URL where the result was stored. Can be <code>null</code> if the copyable could not be stored. 
	 * Check {@link #getStatus()} for a description of the error.
	 */
	URL getLocation();

	/**
	 * @return time when the result was created.
	 */
	long getTimeStamp();

	/**
	 * @return the status of this result. It can contain additional informations about the errors that prevented the 
	 * copyable to be stored at a remote location.
	 */
	IStatus getStatus();
}