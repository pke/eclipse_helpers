package org.eclipse.jface.utils.sync;

import org.eclipse.jface.viewers.Viewer;

public interface ViewerUpdater<E extends Viewer> {
	void updateViewer(E viewer);
}
