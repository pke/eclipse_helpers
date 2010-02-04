package org.eclipse.osgi.framework.console.action.example;

import org.eclipse.osgi.framework.console.action.CommandAction;
import org.eclipse.osgi.framework.console.action.CommandActionContext;

public class FileCopyAction implements CommandAction {

	public void execute(final CommandActionContext context) throws Exception {
		final String from = context.getArgument(0);
		final String to = context.getArgument(1);
		if (from == null || to == null) {
			throw new IllegalArgumentException("You must specify \"from\" and \"to\" arguments"); //$NON-NLS-1$
		}
		context.println("Copying \"" + from + "\" to \"" + to + "\"..."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}
