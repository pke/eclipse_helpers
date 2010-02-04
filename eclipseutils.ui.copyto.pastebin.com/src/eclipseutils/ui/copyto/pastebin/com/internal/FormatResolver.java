package eclipseutils.ui.copyto.pastebin.com.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

public class FormatResolver implements IDynamicVariableResolver {

	public String resolveValue(final IDynamicVariable variable, final String argument) throws CoreException {
		if (argument.startsWith("org.eclipse.jdt")) { //$NON-NLS-1$
			return "java"; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

}
