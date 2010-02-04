package eclipseutils.ui.copyto.from.jdt.internal;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import eclipseutils.ui.copyto.EditorHelper;
import eclipseutils.ui.copyto.api.Copyable;

public class AdapterFactory implements IAdapterFactory {

	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof AbstractTextEditor) {
			final AbstractTextEditor textEditor = (AbstractTextEditor) adaptableObject;
			final ITypeRoot element = JavaUI.getEditorInputTypeRoot(textEditor.getEditorInput());
			if (element != null) {
				final ITextViewer textViewer = EditorHelper.getSourceViewer(textEditor);
				if (textViewer instanceof ISourceViewer) {
					return new SourceViewerCopyable(element, (ISourceViewer) textViewer);
				}
			}
		} else if (adaptableObject instanceof IMember) {
			return new MemberCopyable((IMember) adaptableObject);
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { Copyable.class };
	}

}
