package eclipseutils.ui.copyto.from.jdt.internal;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;

class SourceViewerCopyable extends ASTNodeCopyable {
	private final ISourceViewer viewer;
	private final ITypeRoot root;

	public SourceViewerCopyable(final ITypeRoot root, final ISourceViewer viewer) {
		this.viewer = viewer;
		this.root = root;
	}

	protected ASTNode createNode() {
		return normalize(getSelectedNode(this.root, this.viewer));
	}

	private static ASTNode getSelectedNode(final ITypeRoot root, final ISourceViewer viewer) {
		final Point selectedRange = viewer.getSelectedRange();
		final int length = selectedRange.y;
		final int offset = selectedRange.x;

		final CompilationUnit ast = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
		if (ast == null) {
			return null;
		}

		final NodeFinder finder = new NodeFinder(ast, offset, length);
		return finder.getCoveringNode();
	}
}
