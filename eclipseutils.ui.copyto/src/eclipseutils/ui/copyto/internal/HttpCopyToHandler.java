package eclipseutils.ui.copyto.internal;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.osgi.framework.FrameworkUtil;

import eclipseutils.ui.copyto.api.Copyable;
import eclipseutils.ui.copyto.api.ResponseHandler;
import eclipseutils.ui.copyto.api.Result;
import eclipseutils.ui.copyto.internal.responses.RedirectResponseHandler;

public abstract class HttpCopyToHandler implements Handler {

	public static final String symbolicName = FrameworkUtil.getBundle(HttpCopyToHandler.class).getSymbolicName();

	private ResponseHandler getResponseHandler() {
		try {
			final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(HttpCopyToHandler.symbolicName, CopyToHandler.COMMAND_TARGET_PARAM,
							getId());
			for (int i = 0; i < configurationElements.length; ++i) {
				final IConfigurationElement configurationElement = configurationElements[i];
				if ("responseHandler".equals(configurationElement.getName())) { //$NON-NLS-1$
					return (ResponseHandler) configurationElement.createExecutableExtension("class"); //$NON-NLS-1$
				}
			}
			//return (ResponseHandler) this.configElement.createExecutableExtension("responseHandler"); //$NON-NLS-1$
		} catch (final Exception e) {
			// Catches ClassCastException and CoreException
		}
		return new RedirectResponseHandler();
	}

	protected abstract HttpMethod getMethod();

	public abstract Map getParams();

	public Result copy(final Copyable copyable, final IProgressMonitor monitor) {
		final HttpMethod method = getMethod();
		final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
		final String text = copyable.getText();
		final IValueVariable vars[] = {
				variableManager.newValueVariable(
						"copyto.source", "Source", true, copyable.getSource().getClass().getName()), //$NON-NLS-1$
				variableManager.newValueVariable("copyto.text", "Text to copy", true, text), //$NON-NLS-1$
				variableManager.newValueVariable("copyto.mime-type", "MIME-Type", true, copyable.getMimeType()) }; //$NON-NLS-1$

		// Make sure they are not registered
		variableManager.removeVariables(vars);
		try {
			variableManager.addVariables(vars);
		} catch (final CoreException e) {
		}

		final Map params = getParams();
		final Iterator it = params.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry entry = (Entry) it.next();
			final String name = entry.getKey().toString();
			try {
				final String value = variableManager.performStringSubstitution(entry.getValue().toString(), false);
				if (method instanceof PostMethod) {
					((PostMethod) method).addParameter(name, value);
				}
			} catch (final CoreException e) {
			}
		}
		variableManager.removeVariables(vars);

		final HttpClient httpClient = new HttpClient();

		try {
			final int status = httpClient.executeMethod(method);
			final URL location = getResponseHandler().getLocation(method);
			return new ResultImpl(copyable, location);
		} catch (final Throwable t) {
			return new ResultImpl(copyable, t);
		}
	}

}
