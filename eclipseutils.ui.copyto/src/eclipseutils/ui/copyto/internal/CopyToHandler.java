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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.IProgressConstants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import eclipseutils.ui.copyto.api.Copyable;
import eclipseutils.ui.copyto.api.CustomParamControl;
import eclipseutils.ui.copyto.api.Result;
import eclipseutils.ui.copyto.api.Results;
import eclipseutils.ui.copyto.api.ResultsHandler;
import eclipseutils.ui.copyto.internal.results.ClipboardResultsHandler;

/**
 * 
 * There are 2 types of handlers: 1. ElementHandler 2. TextEditorHandler
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * @since 1.0
 */
public class CopyToHandler extends AbstractHandler {

	public static final String COMMAND_ID = "eclipseutils.ui.copyto"; //$NON-NLS-1$
	public static final String COMMAND_TARGET_PARAM = "targets"; //$NON-NLS-1$

	private final IAdapterManager adapterManager = Platform.getAdapterManager();
	private final ServiceTracker resultsHandlerTracker = new ServiceTracker(
			FrameworkUtil.getBundle(CopyToHandler.class).getBundleContext(),
			ResultsHandler.class.getName(), null) {
		{
			open();
		}
	};

	@Override
	public void dispose() {
		resultsHandlerTracker.close();
		super.dispose();
	}

	/**
	 * Creates a boolen control on the standard grid.
	 * 
	 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
	 * 
	 */
	class BooleanParamControl implements CustomParamControl {
		private final String label;
		private final String desc;

		public BooleanParamControl(String label, String desc) {
			this.label = label;
			this.desc = desc;
		}

		public IObservableValue createControl(Composite parent) {
			final Button button = new Button(parent, SWT.CHECK);
			button.setText(label);
			if (desc != null) {
				button.setToolTipText(desc);
			}
			GridDataFactory.swtDefaults().span(2, 1).applyTo(button);
			return SWTObservables.observeSelection(button);
		}
	}

	private class RequestParamsDialog extends TitleAreaDialog {
		private final Handler handler;

		public RequestParamsDialog(final Shell parentShell,
				final Handler handler) {
			super(parentShell);
			this.handler = handler;
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			final DataBindingContext dbx = new DataBindingContext();

			final IMapProperty selfMap = Properties.selfMap(String.class,
					String.class);
			final IObservableMap observableParams = selfMap
					.observe(this.handler.getParams());

			final Map<String, IConfigurationElement> paramInfos = new HashMap<String, IConfigurationElement>();

			// Add all handler params to the paramInfos map first, as we will
			// later iterate over it
			final Iterator<String> it = this.handler.getParams().keySet()
					.iterator();
			while (it.hasNext()) {
				paramInfos.put(it.next(), null);
			}

			final IConfigurationElement[] configurationElements = Platform
					.getExtensionRegistry().getConfigurationElementsFor(
							FrameworkUtil.getBundle(getClass())
									.getSymbolicName(),
							CopyToHandler.COMMAND_TARGET_PARAM,
							this.handler.getId());
			for (int i = 0; i < configurationElements.length; ++i) {
				final IConfigurationElement configurationElement = configurationElements[i];
				if ("paramInfos".equals(configurationElement.getName())) {
					final IConfigurationElement[] paramConfigs = configurationElement
							.getChildren("paramInfo");
					for (int j = 0; j < paramConfigs.length; ++j) {
						paramInfos.put(paramConfigs[j].getAttribute("name"),
								paramConfigs[j]);
					}
					final String hiddenAttribute = configurationElement
							.getAttribute("hidden");
					if (hiddenAttribute != null && hiddenAttribute.length() > 0) {
						for (String key : hiddenAttribute.split(",")) {
							paramInfos.remove(key);
						}
					}
				}
			}

			final Composite client = new Composite((Composite) super
					.createDialogArea(parent), SWT.NULL);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(client);

			final Iterator<Entry<String, IConfigurationElement>> entries = paramInfos
					.entrySet().iterator();
			while (entries.hasNext()) {
				final Entry<String, IConfigurationElement> entry = entries
						.next();
				final String key = entry.getKey();

				final IObservableValue controlObservable[] = { null };

				Runnable editorCreator = new Runnable() {
					public void run() {
						final Text text = new Text(client, SWT.SINGLE
								| SWT.BORDER);
						controlObservable[0] = SWTObservables.observeText(text);
						// text.setText(entry.getValue().toString());
					}
				};
				final String defaultLabelText = key.toString();
				String labelText = defaultLabelText;
				String desc = null;
				final IConfigurationElement configElement = entry.getValue();
				if (configElement != null) {
					if (Boolean.valueOf(configElement.getAttribute("hidden"))
							.booleanValue()) {
						continue;
					}
					final String text = configElement.getAttribute("label");
					if (text != null && text.length() > 0) {
						labelText = text;
					}
					desc = configElement.getAttribute("description");
					final String className = configElement.getAttribute("type");
					if ("bool".equals(className) || "boolean".equals(className)
							|| Boolean.class.getName().equals(className)
							|| boolean.class.getName().equals(className)) {
						editorCreator = new Runnable() {
							public void run() {
								final Button button = new Button(client,
										SWT.CHECK);
								controlObservable[0] = SWTObservables
										.observeSelection(button);
							}
						};
					} else if (className != null) {
						try {
							final Object typeInstance = configElement
									.createExecutableExtension("type");
							if (typeInstance instanceof IParameterValues) {
								editorCreator = new Runnable() {
									public void run() {
										final ComboViewer combo = new ComboViewer(
												client, SWT.DROP_DOWN);
										combo.setContentProvider(ArrayContentProvider
												.getInstance());
										controlObservable[0] = SWTObservables
												.observeText(combo.getControl());
										final Map<?, ?> params = ((IParameterValues) typeInstance)
												.getParameterValues();
										combo.setInput(params.values());
									}
								};
							}
						} catch (final Exception e) {
							continue;
						}
					}
				}
				final Label label = new Label(client, SWT.RIGHT);
				label.setText(labelText);
				if (desc != null && desc.length() > 0) {
					label.setToolTipText(desc);
				}
				editorCreator.run();
				if (controlObservable[0] != null) {
					final IObservableValue observeMapEntry = Observables
							.observeMapEntry(observableParams, key);
					dbx.bindValue(controlObservable[0], observeMapEntry);
				}
			}

			return client;
		}
	}

	class EventHttpCopyHandler extends HttpCopyToHandler {
		private final Map<String, String> params = new HashMap<String, String>();
		private final HttpMethod method;
		private final String id;

		public EventHttpCopyHandler(final ExecutionEvent event) {
			this.id = event.getParameter("id"); //$NON-NLS-1$
			event.getParameter("method");
			final String url = event.getParameter("url");
			this.method = new PostMethod(url);
			final String params = event.getParameter("params");
			final String[] pairs = params.split("&"); //$NON-NLS-1$
			for (int i = 0; i < pairs.length; ++i) {
				final String[] keyValue = pairs[i].split("="); //$NON-NLS-1$
				this.params.put(keyValue[0], keyValue.length == 1 ? ""
						: keyValue[1]);
			}
		}

		@Override
		protected HttpMethod getMethod() {
			return this.method;
		}

		@Override
		public Map<String, String> getParams() {
			return this.params;
		}

		public String getId() {
			return this.id;
		}

	}

	/**
	 * If CTRL key hold down 1. First collect all Copyable 2. Group them by
	 * mime-type 3. Show wizard page for each mime-type (resolve vars before
	 * displaying page) 4. Upon "Finish click", send to server -> report
	 * progress
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISelection selection[] = { HandlerUtil
				.getActiveMenuSelection(event) };
		if (selection[0] == null) {
			selection[0] = HandlerUtil.getCurrentSelectionChecked(event);
		}

		if (selection[0].isEmpty()) {
			return null;
		}

		final IPreferenceStore prefs = new ScopedPreferenceStore(
				new InstanceScope(), FrameworkUtil.getBundle(getClass())
						.getSymbolicName());
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);
		final IWorkbenchWindow workbenchWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		final Results results[] = { null };

		Job job = new Job("Gathering copyable items") {

			final List<Result> successes = new ArrayList<Result>();
			final List<Result> failures = new ArrayList<Result>();

			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(
						"Gathering copyable information from selection",
						IProgressMonitor.UNKNOWN);

				final Map<String, Copyable> items = new HashMap<String, Copyable>();

				if (selection[0] instanceof IStructuredSelection) {
					final IStructuredSelection ss = (IStructuredSelection) selection[0];
					final Iterator<?> it = ss.iterator();
					while (it.hasNext()) {
						final Object item = it.next();

						Copyable copyable = (Copyable) adapterManager
								.loadAdapter(item, Copyable.class.getName());
						if (copyable == null) {
							final IResource resource = (IResource) adapterManager
									.loadAdapter(item, IResource.class
											.getName());
							if (resource != null) {
								copyable = (Copyable) adapterManager
										.loadAdapter(resource, Copyable.class
												.getName());
							}
						}

						if (copyable != null) {
							items.put(copyable.getMimeType(), copyable);
						}
					}
				} else if (selection[0] instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection[0];
					Copyable copyable = (Copyable) adapterManager.loadAdapter(
							editor, Copyable.class.getName());
					if (null == copyable) {
						copyable = new TextSelectionCopyable(textSelection);
					}
					items.put(copyable.getMimeType(), copyable);
				}

				monitor.beginTask("Copying...", items.size());
				boolean showDialog = showDialog(event.getTrigger());
				final EventHttpCopyHandler target = new EventHttpCopyHandler(
						event);

				for (Entry<String, Copyable> item : items.entrySet()) {
					final Result result = target.copy(item.getValue(), monitor);
					if (result.getStatus().isOK()
							&& result.getLocation() != null) {
						successes.add(result);
					} else {
						failures.add(result);
					}
					monitor.worked(1);
				}

				if (!successes.isEmpty() || !failures.isEmpty()) {
					results[0] = new Results() {

						public Collection<Result> getFailures() {
							return failures;
						}

						public Collection<Result> getSuccesses() {
							return successes;
						}

					};
					Object[] services = resultsHandlerTracker.getServices();
					if (services != null) {
						for (Object service : services) {
							try {
								((ResultsHandler) service).handleResults(
										results[0], workbenchWindow);
							} catch (Throwable t) {
							}
						}
					}
				}
				monitor.done();

				return Status.OK_STATUS;
			}
		};
		job.setProperty(IProgressConstants.KEEP_PROPERTY, true);
		job.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
				true);
		job.setProperty(IProgressConstants.ACTION_PROPERTY, new Action(
				"Copy to clipboard") {
			@Override
			public void run() {
				new ClipboardResultsHandler().handleResults(results[0],
						workbenchWindow);
			}
		});
		job.schedule();

		return null;
	}

	private boolean showDialog(Object trigger) {
		if (trigger instanceof Event) {
			final Event triggerEvent = (Event) trigger;
			final int modifier = triggerEvent.stateMask & SWT.MODIFIER_MASK;
			return ((modifier & SWT.CTRL) == SWT.CTRL);
		}
		return false;
	}

	private IJavaElement getSelectedElement(final IEditorPart editor,
			final ISourceViewer viewer) {
		final Point selectedRange = viewer.getSelectedRange();
		final int length = selectedRange.y;
		final int offset = selectedRange.x;

		final ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor
				.getEditorInput());
		if (element == null) {
			return null;
		}

		final CompilationUnit ast = SharedASTProvider.getAST(element,
				SharedASTProvider.WAIT_YES, null);
		if (ast == null) {
			return null;
		}

		final NodeFinder finder = new NodeFinder(ast, offset, length);
		final ASTNode node = finder.getCoveringNode();

		IBinding binding = null;
		if (node instanceof Name) {
			binding = ((Name) node).resolveBinding();
		} else if (node instanceof MethodInvocation) {
			binding = ((MethodInvocation) node).resolveMethodBinding();
		} else if (node instanceof MethodDeclaration) {
			binding = ((MethodDeclaration) node).resolveBinding();
		} else if (node instanceof Type) {
			binding = ((Type) node).resolveBinding();
		} else if (node instanceof AnonymousClassDeclaration) {
			binding = ((AnonymousClassDeclaration) node).resolveBinding();
		} else if (node instanceof TypeDeclaration) {
			binding = ((TypeDeclaration) node).resolveBinding();
		} else if (node instanceof CompilationUnit) {
			return ((CompilationUnit) node).getJavaElement();
		} else if (node instanceof Expression) {
			binding = ((Expression) node).resolveTypeBinding();
		} else if (node instanceof ImportDeclaration) {
			binding = ((ImportDeclaration) node).resolveBinding();
		} else if (node instanceof MemberRef) {
			binding = ((MemberRef) node).resolveBinding();
		} else if (node instanceof MemberValuePair) {
			binding = ((MemberValuePair) node).resolveMemberValuePairBinding();
		} else if (node instanceof PackageDeclaration) {
			binding = ((PackageDeclaration) node).resolveBinding();
		} else if (node instanceof TypeParameter) {
			binding = ((TypeParameter) node).resolveBinding();
		} else if (node instanceof VariableDeclaration) {
			binding = ((VariableDeclaration) node).resolveBinding();
		}

		if (binding != null) {
			return binding.getJavaElement();
		}

		return null;
	}

}
