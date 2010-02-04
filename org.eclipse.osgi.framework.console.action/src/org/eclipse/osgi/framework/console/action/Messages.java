package org.eclipse.osgi.framework.console.action;

import org.eclipse.osgi.util.NLS;

/***/
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.osgi.framework.console.action.messages"; //$NON-NLS-1$
	/***/
	public static String ActionCommand_DidYouMean;
	/***/
	public static String ActionCommand_Examples;
	/***/
	public static String ActionCommand_ForHelpEnter;
	/***/
	public static String ActionCommand_NoActions;
	/***/
	public static String ActionCommand_NoHelp;
	/***/
	public static String ActionCommand_UnknownAction;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
