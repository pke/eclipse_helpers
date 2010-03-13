package org.eclipse.swt.utils.sync;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public final class SWTSync {

	/**
	 * Runs a given runnable on the UI thread if the control has not been disposed.
	 * 
	 * <p>
	 * Runs synchronously if the calling thread is the same as the UI-thread.
	 * Otherwise the method calls the given runnable asynchronously but checks the controls disposal right before
	 * it calls the given runnable. So the caller can be sure to be only called while the control is not disposed.
	 * @param <E>
	 *  
	 * @param control to check for disposal. Its display is used to to check for the display thread.  
	 * @param runnable
	 */
	public static <E extends Control> void update(final E control, final ControlUpdater<E> updater) {
		if (control == null || control.isDisposed()) {
			return;
		}
		final Display display = control.getDisplay();
		if (display.isDisposed()) {
			return;
		}
		if (Thread.currentThread() == display.getThread()) {
			updater.update(control);
		} else {
			display.asyncExec(new Runnable() {
				public void run() {
					if (!control.isDisposed()) {
						updater.update(control);
					}
				}
			});
		}
	}

	private SWTSync() {
	}
}
