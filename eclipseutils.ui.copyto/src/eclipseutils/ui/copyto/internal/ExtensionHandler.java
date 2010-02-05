/*******************************************************************************
 * Copyright (c) 2010 Philipp Kursawe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philipp Kursawe (phil.kursawe@gmail.com) - initial API and implementation
 ******************************************************************************/
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

	@Override
	protected HttpMethod getMethod() {
		return new PostMethod(this.configElement.getAttribute("url")); //$NON-NLS-1$
	}

	@Override
	public Map<String, String> getParams() {
		final Map<String, String> params = new HashMap<String, String>();

		final IConfigurationElement[] paramElements = this.configElement
				.getChildren("param"); //$NON-NLS-1$
		for (int i = 0; i < paramElements.length; ++i) {
			final IConfigurationElement element = paramElements[i];
			params.put(
					element.getAttribute("name"), element.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return params;
	}

	public String getId() {
		return this.configElement.getDeclaringExtension().getUniqueIdentifier();
	}

}
