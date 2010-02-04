package eclipseutils.ui.copyto.api;

/**
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface Copyable {
	/**
	 * @return textual representation.
	 */
	String getText();

	String getMimeType();

	/**
	 * @return The source this Copyable was created from. Must never be <code>null</code>.
	 */
	Object getSource();
}
