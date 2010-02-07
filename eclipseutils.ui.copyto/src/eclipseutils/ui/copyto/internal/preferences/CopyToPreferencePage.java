package eclipseutils.ui.copyto.internal.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
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

	class Target extends PlatformObject {
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

		private final String id;
		private final String label;
		private final String url;
		private final Map<String, String> additionalParams = new HashMap<String, String>();
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
	}
}
