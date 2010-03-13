package org.eclipse.jface.utils.sync;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.utils.sync.ControlUpdater;
import org.eclipse.swt.utils.sync.SWTSync;
import org.eclipse.swt.widgets.Control;

public final class JFaceSync {

	public static <E extends Viewer> void update(final E viewer, final ViewerUpdater<E> updater) {
		SWTSync.update(viewer.getControl(), new ControlUpdater<Control>() {

			public void update(final Control control) {
				updater.updateViewer(viewer);
			}
		});
	}

	public static void refresh(final Viewer viewer) {
		update(viewer, new ViewerUpdater<Viewer>() {

			public void updateViewer(final Viewer viewer) {
				viewer.refresh();
			}
		});
	}

	public static void reveal(final StructuredViewer viewer, final Object element) {
		update(viewer, new ViewerUpdater<StructuredViewer>() {
			public void updateViewer(final StructuredViewer viewer) {
				viewer.reveal(element);
			};
		});
	}

	private JFaceSync() {
	}
}
