package eclipseutils.ui.copyto.internal.preferences;

import java.util.Iterator;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import eclipseutils.ui.copyto.internal.preferences.CopyToPreferencePage.Target;

public abstract class TableViewerFieldEditor<T> extends FieldEditor {

	private TableViewer viewer;
	private Composite buttonBox;
	private Button addButton;
	private Button removeButton;
	private final String preferencePath;
	private final IObservableList items = new WritableList();
	private DataBindingContext ctx;

	protected TableViewerFieldEditor(String preferencePath, String labelText,
			Composite parent) {
		this.preferencePath = preferencePath;
		setLabelText(labelText);
		createControl(parent);
	}

	@Override
	public void setFocus() {
		if (viewer != null) {
			viewer.getControl().setFocus();
		}
	}

	/**
	 * Not called, since we do not have a PreferenceStore set.
	 */
	@Override
	protected void doLoad() {
		throw new IllegalAccessError();
	}

	/**
	 * Not called, since we do not have a PreferenceStore set.
	 */
	@Override
	protected void doLoadDefault() {
		throw new IllegalAccessError();
	}

	/**
	 * Not called, since we do not have a PreferenceStore set.
	 */
	@Override
	protected void doStore() {
		throw new IllegalAccessError();
	}

	protected abstract String[] getColumnNames();

	public Table getTableControl(Composite parent) {
		if (viewer == null) {
			viewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE
					| SWT.V_SCROLL | SWT.H_SCROLL);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setInput(items);
			final Table table = viewer.getTable();
			ctx = new DataBindingContext();
			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					ctx.dispose();
				}
			});
			ViewerSupport.bind(viewer, items, BeanProperties.values(
					Target.class, getColumnNames()));
			table.setHeaderVisible(true);
			table.setFont(parent.getFont());
			// table.addSelectionListener(getSelectionListener());
			table.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					viewer = null;
				}
			});
		} else {
			checkParent(viewer.getControl(), parent);
		}
		return viewer.getTable();
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTableControl(parent).setEnabled(enabled);
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) viewer.getControl().getLayoutData()).horizontalSpan = numColumns - 1;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		Table table = getTableControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	protected abstract T createItem(Preferences preferences);

	private Button createPushButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		return button;
	}

	private void createButtons(Composite box) {
		addButton = createPushButton(box, JFaceResources
				.getString("ListEditor.add"));
		removeButton = createPushButton(box, JFaceResources
				.getString("ListEditor.remove"));//$NON-NLS-1$
		ctx.bindValue(SWTObservables.observeEnabled(removeButton),
				SWTObservables.observeSelection(viewer.getControl()), null,
				null);
	}

	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					addButton = null;
					removeButton = null;
					buttonBox = null;
				}
			});

		} else {
			checkParent(buttonBox, parent);
		}

		// selectionChanged();
		return buttonBox;
	}

	@Override
	public void load() {
		if (viewer != null) {
			Preferences node = new InstanceScope().getNode(preferencePath);
			try {
				for (String key : node.childrenNames()) {
					T item = createItem(node.node(key));
					if (item != null) {
						this.items.add(item);
					}
				}
			} catch (BackingStoreException e) {
			}
		}
		setPresentsDefaultValue(false);
		refreshValidState();
	}

	@Override
	public void loadDefault() {
		if (viewer != null) {
			items.clear();
			new DefaultScope().getNode(preferencePath);
		}
		setPresentsDefaultValue(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void store() {
		if (presentsDefaultValue()) {
			Preferences node = new InstanceScope().getNode(preferencePath);
			Iterator<T> it = items.iterator();
			while (it.hasNext()) {
				T item = it.next();
				store(item, node.node(getId(item)));
			}
		}
	}

	protected abstract String getId(T item);

	protected abstract void store(T item, Preferences node);

	@Override
	public int getNumberOfControls() {
		return 2;
	}

}
