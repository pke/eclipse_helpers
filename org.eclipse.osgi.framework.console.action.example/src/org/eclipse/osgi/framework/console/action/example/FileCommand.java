package org.eclipse.osgi.framework.console.action.example;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.action.ActionCommand;

public class FileCommand extends ActionCommand {

	public void _file(final CommandInterpreter ci) {
		execute("file", ci); //$NON-NLS-1$
	}
}
