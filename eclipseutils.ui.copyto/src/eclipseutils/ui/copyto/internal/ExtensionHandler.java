package eclipseutils.ui.copyto.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IConfigurationElement;


public class ExtensionHandler extends HttpCopyToHandler {
	private final IConfigurationElement configElement;

	public ExtensionHandler(final IConfigurationElement configElement) {
		this.configElement = configElement;
	}

	protected HttpMethod getMethod() {
		return new PostMethod(this.configElement.getAttribute("url")); //$NON-NLS-1$
	}

	public Map getParams() {
		final Map params = new HashMap();

		final IConfigurationElement[] paramElements = this.configElement.getChildren("param"); //$NON-NLS-1$
		for (int i = 0; i < paramElements.length; ++i) {
			final IConfigurationElement element = paramElements[i];
			params.put(element.getAttribute("name"), element.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return params;
	}

	public String getId() {
		return this.configElement.getDeclaringExtension().getUniqueIdentifier();
	}

}
