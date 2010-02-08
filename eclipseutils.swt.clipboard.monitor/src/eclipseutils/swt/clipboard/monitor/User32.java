package eclipseutils.swt.clipboard.monitor;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;

public interface User32 extends W32API {
	User32 INSTANCE = (User32) Native.loadLibrary(
			"user32", User32.class, DEFAULT_OPTIONS); //$NON-NLS-1$
	int GWL_EXSTYLE = -20;
	int GWL_STYLE = -16;
	int GWL_WNDPROC = -4;
	int GWL_HINSTANCE = -6;
	int GWL_ID = -12;
	int GWL_USERDATA = -21;
	int DWL_DLGPROC = 4;
	int DWL_MSGRESULT = 0;
	int DWL_USER = 8;
	int WS_EX_COMPOSITED = 0x20000000;
	int WS_EX_LAYERED = 0x80000;
	int WS_EX_TRANSPARENT = 32;

	int GetWindowLong(HWND hWnd, int nIndex);

	int SetWindowLong(HWND hWnd, int nIndex, int dwNewLong);

	interface WNDPROC extends StdCallCallback {
		LRESULT callback(HWND hWnd, int uMsg, WPARAM uParam, LPARAM lParam);
	}

	int SetWindowLong(HWND hWnd, int nIndex, WNDPROC proc);

	HWND CreateWindowEx(int styleEx, String className, String windowName,
			int style, int x, int y, int width, int height, HWND hndParent,
			int hndMenu, int hndInst, Object parm);

	static final HWND HWND_TOPMOST = new HWND(Pointer.createConstant(-1));
	static final int SWP_NOSIZE = 1;

	boolean SetWindowPos(HWND hWnd, HWND hWndInsAfter, int x, int y, int cx,
			int cy, short uFlags);

	int DestroyWindow(HWND hwnd);

	HWND SetClipboardViewer(HWND hWndNewViewer);

	boolean ChangeClipboardChain(HWND hWndRemove, HWND hWndNewNext);

	// http://msdn.microsoft.com/en-us/library/ms644958(VS.85).aspx
	public static class POINT extends Structure {
		public int x;
		public int y;
	}

	/*
	 * PeekMessage() Options
	 */
	static final int PM_NOREMOVE = 0x0000;
	static final int PM_REMOVE = 0x0001;
	static final int PM_NOYIELD = 0x0002;

	class MSG extends Structure {
		public int hWnd;
		public int message;
		public short wParam;
		public int lParam;
		public int time;
		public POINT pt;
	}

	// http://msdn.microsoft.com/en-us/library/ms644936(VS.85).aspx
	int GetMessage(MSG lpMsg, HWND hWnd, int wMsgFilterMin, int wMsgFilterMax);

	boolean PeekMessage(MSG lpMsg, HWND hWnd, int wMsgFilterMin,
			int wMsgFilterMax, int wRemoveMsg);

	boolean TranslateMessage(MSG lpMsg);

	LRESULT DispatchMessage(MSG lpMsg);

	static final int QS_KEY = 0x0001;
	static final int QS_MOUSEMOVE = 0x0002;
	static final int QS_MOUSEBUTTON = 0x0004;
	static final int QS_POSTMESSAGE = 0x0008;
	static final int QS_TIMER = 0x0010;
	static final int QS_PAINT = 0x0020;
	static final int QS_SENDMESSAGE = 0x0040;
	static final int QS_HOTKEY = 0x0080;
	static final int QS_ALLPOSTMESSAGE = 0x0100;
	static final int QS_RAWINPUT = 0x0400;

	static final int QS_MOUSE = (QS_MOUSEMOVE | QS_MOUSEBUTTON);

	static final int QS_INPUT = (QS_MOUSE | QS_KEY | QS_RAWINPUT);

	static final int QS_ALLEVENTS = (QS_INPUT | QS_POSTMESSAGE | QS_TIMER
			| QS_PAINT | QS_HOTKEY);

	static final int QS_ALLINPUT = (QS_INPUT | QS_POSTMESSAGE | QS_TIMER
			| QS_PAINT | QS_HOTKEY | QS_SENDMESSAGE);

	int MsgWaitForMultipleObjects(int nCount, HANDLE[] pHandles,
			boolean bWaitAll, int dwMilliseconds, int dwWakeMask);

	void SendMessage(HWND hWnd, int message, WPARAM wParam, LPARAM lParam);

	LRESULT DefWindowProc(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);
}
