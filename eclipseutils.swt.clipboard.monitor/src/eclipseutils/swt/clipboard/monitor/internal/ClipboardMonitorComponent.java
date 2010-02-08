package eclipseutils.swt.clipboard.monitor.internal;

import org.osgi.service.component.ComponentContext;

import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.examples.win32.W32API.HWND;
import com.sun.jna.examples.win32.W32API.LPARAM;
import com.sun.jna.examples.win32.W32API.LRESULT;
import com.sun.jna.examples.win32.W32API.WPARAM;

import eclipseutils.swt.clipboard.monitor.ClipboardEvent;
import eclipseutils.swt.clipboard.monitor.ClipboardListener;
import eclipseutils.swt.clipboard.monitor.User32;
import eclipseutils.swt.clipboard.monitor.User32.MSG;

public class ClipboardMonitorComponent extends Thread {

	private ComponentContext context;
	private final User32 user32 = User32.INSTANCE;
	private final HANDLE event = Kernel32.INSTANCE.CreateEvent(null, false,
			false, null);
	private HWND viewer;
	private HWND nextViewer;
	private User32.WNDPROC callback;

	public ClipboardMonitorComponent() {
		super("Clipboard Monitor");
	}

	protected void activate(ComponentContext context) {
		this.context = context;
		start();
	}

	protected void deactivate() {
		Kernel32.INSTANCE.SetEvent(event);
	}

	final int WM_DESTROY = 0x0002;
	final int WM_CHANGECBCHAIN = 0x030D;
	final int WM_DRAWCLIPBOARD = 0x0308;

	@Override
	public void run() {
		viewer = user32.CreateWindowEx(0, "STATIC", "", 0, 0, 0, 0, 0, null, 0,
				0, null);
		nextViewer = user32.SetClipboardViewer(viewer);

		// Need to keep a reference to the callback, or it will be garbage
		// collected
		this.callback = new User32.WNDPROC() {
			public LRESULT callback(HWND hWnd, int uMsg, WPARAM wParam,
					LPARAM lParam) {
				switch (uMsg) {
				case WM_CHANGECBCHAIN:
					// If the next window is closing, repair the chain.
					if (nextViewer.toNative().equals(wParam)) {
						nextViewer = new HWND(Pointer.createConstant(lParam
								.intValue()));
					} // Otherwise, pass the message to the next link.
					else if (nextViewer != null) {
						user32.SendMessage(nextViewer, uMsg, wParam, lParam);
					}
					return new LRESULT(0);
				case WM_DRAWCLIPBOARD:
					ClipboardEvent event = new ClipboardEvent(this);
					Object[] listeners = context
							.locateServices("ClipboardListener");
					if (listeners != null) {
						for (Object listener : listeners) {
							try {
								((ClipboardListener) listener).onEvent(event);
							} catch (Throwable t) {
							}
						}
					}
					user32.SendMessage(nextViewer, uMsg, wParam, lParam);
					return new LRESULT(0);
				case WM_DESTROY:
					user32.ChangeClipboardChain(viewer, nextViewer);
					break;
				}
				return user32.DefWindowProc(hWnd, uMsg, wParam, lParam);
			}
		};
		user32.SetWindowLong(viewer, User32.GWL_WNDPROC, callback);

		MSG msg = new MSG();

		final HANDLE handles[] = { event };
		while (true) {
			int result = User32.INSTANCE.MsgWaitForMultipleObjects(
					handles.length, handles, false, Kernel32.INFINITE,
					User32.QS_ALLINPUT);

			if (result == Kernel32.WAIT_OBJECT_0) {
				User32.INSTANCE.DestroyWindow(viewer);
				return;
			}
			if (result != Kernel32.WAIT_OBJECT_0 + handles.length) {
				// Serious problem!
				break;
			}

			while (user32.PeekMessage(msg, null, 0, 0, User32.PM_REMOVE)) {
				user32.TranslateMessage(msg);
				user32.DispatchMessage(msg);
				if (Kernel32.WAIT_OBJECT_0 == Kernel32.INSTANCE
						.WaitForSingleObject(event, 0)) {
					return;
				}
			}
		}

	}
}
