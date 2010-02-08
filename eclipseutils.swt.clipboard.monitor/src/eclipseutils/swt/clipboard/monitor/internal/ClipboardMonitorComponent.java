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

public class ClipboardMonitorComponent implements Runnable {

	private final Thread thread = new Thread(this, "Clipboard Monitor");;
	private ComponentContext context;
	User32 user32 = User32.INSTANCE;
	HANDLE event = Kernel32.INSTANCE.CreateEvent(null, false, false, null);

	protected void activate(ComponentContext context) {
		this.context = context;

		thread.start();
	}

	protected void deactivate() {
		Kernel32.INSTANCE.SetEvent(event);
	}

	private static final HWND HWND_DESKTOP = new HWND(Pointer
			.createConstant(0x10014));
	private static final int WS_POPUPWINDOW = 0x80000000 | 0x00800000 | 0x00080000;
	private static final int CW_USEDEFAULT = 0x80000000;
	private static final int WM_USER = 0x0400;
	private static final int WM_DESTROY = 0x0002;
	private static final int WM_CHANGECBCHAIN = 0x030D;
	private static final int WM_DRAWCLIPBOARD = 0x0308;
	private HWND viewer;
	private HWND nextViewer;
	private User32.WNDPROC callback;

	public void run() {
		viewer = user32.CreateWindowEx(0, "STATIC", "", 0, CW_USEDEFAULT,
				CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, null, 0, 0, null);

		/*user32.SetWindowPos(viewer, User32.HWND_TOPMOST, 0, 0, 0, 0,
				(short) User32.SWP_NOSIZE);
		*/
		MSG msg = new MSG();
		user32.PeekMessage(msg, viewer, WM_USER, WM_USER, User32.PM_NOREMOVE);

		nextViewer = user32.SetClipboardViewer(viewer);

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
					break;
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
					break;
				case WM_DESTROY:
					user32.ChangeClipboardChain(viewer, nextViewer);
					// Fall through... not sure if that is required
				default:
					return user32.DefWindowProc(hWnd, uMsg, wParam, lParam);
				}
				return new LRESULT(0);
			}
		};
		user32.SetWindowLong(viewer, User32.GWL_WNDPROC, callback);

		HANDLE handles[] = { event };
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
