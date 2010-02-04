package eclipseutils.ui.copyto.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

import eclipseutils.ui.copyto.api.Copyable;
import eclipseutils.ui.copyto.api.Result;
import eclipseutils.ui.copyto.internal.results.ClipboardResultsHandler;

/**
 * 
 * There are 2 types of handlers:
 * 1. ElementHandler
 * 2. TextEditorHandler
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * @since 1.0
 */
public class CopyToHandler extends AbstractHandler implements IElementUpdater {

	public static final String COMMAND_ID = "eclipseutils.ui.copyto"; //$NON-NLS-1$
	public static final String COMMAND_TARGET_PARAM = "targets"; //$NON-NLS-1$

	private final IAdapterManager adapterManager = Platform.getAdapterManager();

	private class RequestParamsDialog extends TitleAreaDialog {
		private final Handler handler;

		public RequestParamsDialog(final Shell parentShell, final Handler handler) {
			super(parentShell);
			this.handler = handler;
		}

		protected Control createDialogArea(final Composite parent) {
			final DataBindingContext dbx = new DataBindingContext();

			final IMapProperty selfMap = Properties.selfMap(String.class, String.class);
			final IObservableMap observableParams = selfMap.observe(this.handler.getParams());

			final Map paramInfos = new HashMap();
			final Set hidden = new HashSet();

			// Add all handler params to the paramInfos map first, as we will later iterate over it
			final Iterator it = this.handler.getParams().keySet().iterator();
			while (it.hasNext()) {
				paramInfos.put(it.next(), null);
			}

			final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(FrameworkUtil.getBundle(getClass()).getSymbolicName(),
							CopyToHandler.COMMAND_TARGET_PARAM, this.handler.getId());
			for (int i = 0; i < configurationElements.length; ++i) {
				final IConfigurationElement configurationElement = configurationElements[i];
				if ("paramInfos".equals(configurationElement.getName())) {
					final String hiddenAttribute = configurationElement.getAttribute("hidden");
					if (hiddenAttribute != null && hiddenAttribute.length() > 0) {
						hidden.addAll(Arrays.asList(hiddenAttribute.split(",")));
					}
					final IConfigurationElement[] paramConfigs = configurationElement.getChildren("paramInfo");
					for (int j = 0; j < paramConfigs.length; ++j) {
						paramInfos.put(paramConfigs[j].getAttribute("name"), paramConfigs[j]);
					}
				}
			}

			final Composite client = new Composite((Composite) super.createDialogArea(parent), SWT.NULL);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(client);

			final Iterator entries = paramInfos.entrySet().iterator();
			while (entries.hasNext()) {
				final Entry entry = (Entry) entries.next();
				final Object key = entry.getKey();
				if (hidden.contains(key)) {
					continue;
				}

				final IObservableValue controlObservable[] = { null };

				Runnable editorCreator = new Runnable() {
					public void run() {
						final Text text = new Text(client, SWT.SINGLE | SWT.BORDER);
						controlObservable[0] = SWTObservables.observeText(text);
						// text.setText(entry.getValue().toString());
					}
				};
				final String defaultLabelText = key.toString();
				String labelText = defaultLabelText;
				String desc = null;
				final IConfigurationElement configElement = (IConfigurationElement) entry.getValue();
				if (configElement != null) {
					if (Boolean.valueOf(configElement.getAttribute("hidden")).booleanValue()) {
						continue;
					}
					final String text = configElement.getAttribute("label");
					if (text != null && text.length() > 0) {
						labelText = text;
					}
					desc = configElement.getAttribute("description");
					final String className = configElement.getAttribute("type");
					if ("bool".equals(className) || "boolean".equals(className)
							|| Boolean.class.getName().equals(className) || boolean.class.getName().equals(className)) {
						editorCreator = new Runnable() {
							public void run() {
								final Button button = new Button(client, SWT.CHECK);
								controlObservable[0] = SWTObservables.observeSelection(button);
							}
						};
					} else if (className != null) {
						try {
							final Object typeInstance = configElement.createExecutableExtension("type");
							if (typeInstance instanceof IParameterValues) {
								editorCreator = new Runnable() {
									public void run() {
										final ComboViewer combo = new ComboViewer(client, SWT.DROP_DOWN);
										combo.setContentProvider(ArrayContentProvider.getInstance());
										controlObservable[0] = SWTObservables.observeText(combo.getControl());
										final Map params = ((IParameterValues) typeInstance).getParameterValues();
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
					final IObservableValue observeMapEntry = Observables.observeMapEntry(observableParams, key);
					dbx.bindValue(controlObservable[0], observeMapEntry);
				}
			}

			return client;
		}
	}

	class EventHttpCopyHandler extends HttpCopyToHandler {
		private final Map params = new HashMap();
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
				this.params.put(keyValue[0], keyValue.length == 1 ? "" : keyValue[1]);
			}
		}

		protected HttpMethod getMethod() {
			return this.method;
		}

		public Map getParams() {
			return this.params;
		}

		public String getId() {
			return this.id;
		}

	}

	/** 
	 * If CTRL key hold down
	 * 1. First collect all Copyable
	 * 2. Group them by mime-type
	 * 3. Show wizard page for each mime-type (resolve vars before displaying page)
	 * 4. Upon "Finish click", send to server -> report progress
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IPreferenceStore prefs = new ScopedPreferenceStore(new InstanceScope(), FrameworkUtil.getBundle(
				getClass()).getSymbolicName());
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection == null) {
			selection = HandlerUtil.getCurrentSelectionChecked(event);
		}
		final Shell activeShell = HandlerUtil.getActiveShell(event);

		final EventHttpCopyHandler target = new EventHttpCopyHandler(event);

		final Object trigger = event.getTrigger();
		if (trigger instanceof Event) {
			final Event triggerEvent = (Event) trigger;
			final int modifier = triggerEvent.stateMask & SWT.MODIFIER_MASK;
			if ((modifier & SWT.CTRL) == SWT.CTRL) {
				final Dialog dialog = new RequestParamsDialog(activeShell, target);
				if (dialog.open() != Window.OK) {
					return null;
				}
			}
		}

		final List successes = new ArrayList();
		final List failures = new ArrayList();

		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection ss = (IStructuredSelection) selection;
			final Iterator it = ss.iterator();
			while (it.hasNext()) {
				final Object item = it.next();

				Copyable copyable = (Copyable) this.adapterManager.loadAdapter(item, Copyable.class.getName());
				if (copyable == null) {
					final IResource resource = (IResource) this.adapterManager.loadAdapter(item, IResource.class
							.getName());
					copyable = (Copyable) this.adapterManager.loadAdapter(resource, Copyable.class.getName());
				}

				if (copyable != null) {
					final Result result = target.copy(copyable, new NullProgressMonitor());
					if (result.getStatus().isOK() && result.getLocation() != null) {
						successes.add(result);
					} else {
						failures.add(result);
					}
				}
			}
		} else if (selection instanceof ITextSelection) {
			final IEditorPart editor = HandlerUtil.getActiveEditor(event);
			Copyable copyable = (Copyable) this.adapterManager.loadAdapter(editor, Copyable.class.getName());
			if (null == copyable) {
				copyable = new TextSelectionCopyable(selection);
			}
			if (copyable != null) {
				final Result result = target.copy(copyable, new NullProgressMonitor());
				if (result.getStatus().isOK() && result.getLocation() != null) {
					successes.add(result);
				} else {
					failures.add(result);
				}
			}
		}
		if (!successes.isEmpty() || !failures.isEmpty()) {
			new ClipboardResultsHandler().handleResults(successes, failures);
			/*if (prefs.getBoolean("config.copyToClipboard")) {
				final MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(activeShell,
						"CopyTo ...clipboard", "Do you want to copy the locations {} to the clipboard?",
						"Always. Do not ask again", false, prefs, "config.copyToClipboard");
				if (dialog.open() != 0) {
					new ClipboardResultsHandler().handleResults(results);
				}
			}*/
		}
		return null;
	}

	public void updateElement(final UIElement element, final Map parameters) {
	}

	private IJavaElement getSelectedElement(final IEditorPart editor, final ISourceViewer viewer) {
		final Point selectedRange = viewer.getSelectedRange();
		final int length = selectedRange.y;
		final int offset = selectedRange.x;

		final ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		if (element == null) {
			return null;
		}

		final CompilationUnit ast = SharedASTProvider.getAST(element, SharedASTProvider.WAIT_YES, null);
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
