package eclipseutils.ui.copyto.internal;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import eclipseutils.ui.copyto.api.Copyable;

class TextSelectionCopyable implements Copyable {
	private final ISelection selection;

	TextSelectionCopyable(final ISelection selection) {
		this.selection = selection;
	}

	public String getText() {
		return ((ITextSelection) this.selection).getText();
	}

	public String getMimeType() {
		return "plain/text"; //$NON-NLS-1$
	}

	public Object getSource() {
		return this.selection;
	}
}