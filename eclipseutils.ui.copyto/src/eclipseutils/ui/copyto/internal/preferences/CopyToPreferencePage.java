package eclipseutils.ui.copyto.internal.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import eclipseutils.ui.copyto.internal.results.ClipboardResultsHandler;

public class CopyToPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private IWorkbench workbench;

	public CopyToPreferencePage() {
		final IPreferenceStore prefs = new ScopedPreferenceStore(
				new InstanceScope(), FrameworkUtil.getBundle(getClass())
						.getSymbolicName());
		setPreferenceStore(prefs);
	}

	private static class TargetWorkbenchAdapter extends WorkbenchAdapter {

		@Override
		public String getLabel(Object object) {
			return ((Target) object).label;
		}

		static IWorkbenchAdapter instance;

		static IWorkbenchAdapter getInstance() {
			if (instance == null) {
				instance = new TargetWorkbenchAdapter();
			}
			return instance;
		}
	}

	static class Target extends PlatformObject implements Serializable {
		private static final long serialVersionUID = -395321611927968738L;

		public Target(Preferences node) {
			id = node.name();
			label = node.get("label", null);
			url = node.get("url", null);
			try {
				if (node.nodeExists("params")) {
					Preferences paramsNode = node.node("params");
					for (String key : paramsNode.keys()) {
						additionalParams.put(key, paramsNode.get(key, ""));
					}
				}
			} catch (BackingStoreException e) {
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class) {
				return TargetWorkbenchAdapter.getInstance();
			}
			return super.getAdapter(adapter);
		}

		void save(Preferences node) {
			node.put("label", label);
			node.put("url", url);
			if (!additionalParams.isEmpty()) {
				Preferences paramsNode = node.node("params");
				for (Entry<String, String> entry : additionalParams.entrySet()) {
					paramsNode.put(entry.getKey(), entry.getValue());
				}
			}
		}

		public String toBase64() {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream outputStream;
			try {
				outputStream = new ObjectOutputStream(out);
				outputStream.writeObject(this);
				return new String(Base64.encodeBase64(out.toByteArray()));
			} catch (IOException e) {
			}
			return null;
		}

		static Target valueOf(String base64Encoding) {
			try {
				Object target = new ObjectInputStream(new ByteArrayInputStream(
						Base64.decodeBase64(base64Encoding.getBytes())))
						.readObject();
				if (target instanceof Target) {
					return (Target) target;
				}
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
			return null;
		}

		private final String id;
		private final String label;
		private final String url;
		private final Map<String, String> additionalParams = new HashMap<String, String>();
		private IStatus connectionStatus = new Status(IStatus.OK, "test",
				"Not tested yet");

		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		public String getUrl() {
			return url;
		}

		public Map<String, String> getAdditionalParams() {
			return additionalParams;
		}

		public void testConnection() {
			try {
				URL url = new URL(getUrl());
				URLConnection connection = url.openConnection();
				connection.connect();
				setConnectionStatus(Status.OK_STATUS);
			} catch (Exception e) {
				setConnectionStatus(new Status(IStatus.ERROR, "test", "Error",
						e));
			}
		}

		public void setConnectionStatus(IStatus connectionStatus) {
			this.connectionStatus = connectionStatus;
		}
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				ClipboardResultsHandler.CLIPBOARD_ALWAYS_OVERWRITE,
				"Copy result URLs to clipboard", BooleanFieldEditor.DEFAULT,
				getFieldEditorParent()));
		addField(new TableViewerFieldEditor<Target>(FrameworkUtil.getBundle(
				getClass()).getSymbolicName()
				+ "/targets", "Targets", getFieldEditorParent()) {

			@Override
			protected Target createItem(Preferences preferences) {
				if (preferences != null) {
					return new Target(preferences);
				}
				return null;
			}

			@Override
			protected void addCustomButtons(Composite parent) {
				final Button testButton = createPushButton(parent, "Test");
				testButton
						.setToolTipText("Test the connectivity to the selected URL");
				testButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

					}
				});
				bindToViewerSelection(SWTObservables.observeEnabled(testButton));
				testButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						testButton.setEnabled(false);
						SafeRunner.run(new SafeRunnable() {
							public void run() throws Exception {
								workbench.getProgressService().run(true, true,
										new IRunnableWithProgress() {
											public void run(
													final IProgressMonitor monitor)
													throws InvocationTargetException,
													InterruptedException {
												visitViewerSelection(new Visitor<Target>() {
													public void visit(
															final Target target) {
														for (int i = 0; i < 10; ++i) {

														}
														target.testConnection();
														monitor.worked(1);
													}

													public void start(int items) {
														monitor.beginTask(
																"Connecting...",
																items);
													}
												});
												monitor.done();
											}
										});
							}
						});
						testButton.setEnabled(true);
					}
				});
			}

			@Override
			protected void store(Target item, Preferences node) {
				item.save(node);
			}

			@Override
			protected String getId(Target item) {
				return item.id;
			}

			@Override
			protected String[] getColumnNames() {
				return new String[] { "label", "url" };
			}
		});
	}

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}
}
