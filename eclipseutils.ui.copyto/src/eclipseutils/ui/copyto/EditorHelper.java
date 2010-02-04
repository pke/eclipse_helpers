package eclipseutils.ui.copyto;

import java.lang.reflect.Method;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public final class EditorHelper {

	/**
	 * Calls AbstractTextEditor.getSourceViewer() through reflection, as that method is normally protected (for some
	 * ungodly reason).
	 * 
	 * @param AbstractTextEditor to run reflection on
	 */
	public static ITextViewer getSourceViewer(final AbstractTextEditor editor) {
		try {
			final Method method = AbstractTextEditor.class.getDeclaredMethod("getSourceViewer", null); //$NON-NLS-1$
			method.setAccessible(true);
			return (ITextViewer) method.invoke(editor, null);
		} catch (final Exception e) {
			return null;
		}
	}

	private EditorHelper() {
	}
}
