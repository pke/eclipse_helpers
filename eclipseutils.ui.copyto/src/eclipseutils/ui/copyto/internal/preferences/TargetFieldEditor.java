package eclipseutils.ui.copyto.internal.preferences;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.CellEditorProperties;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.Preferences;

class TargetFieldEditor extends TableViewerFieldEditor<Target> {
	TargetFieldEditor(String preferencePath, String labelText, Composite parent) {
		super(preferencePath, labelText, parent);
	}

	@Override
	protected Target createItem(Preferences preferences) {
		if (preferences != null) {
			return new Target(preferences);
		}
		return new Target();
	}

	@Override
	protected void createCustomButtons(Composite parent) {
		final Button testButton = createPushButton(parent, "Test");
		testButton.setToolTipText("Test the connectivity to the selected URL");
		enableWithSelection(testButton, SWT.SINGLE | SWT.MULTI);
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				testButton.setEnabled(false);
				visitViewerSelection("Testing connection",
						new Visitor<Target>() {
							public void visit(final Target target,
									IProgressMonitor monitor) {
								monitor.beginTask(NLS.bind("Testing {0}: ",
										target.getLabel(), target.getUrl()),
										IProgressMonitor.UNKNOWN);
								for (int i = 0; i < 10; ++i) {
									target.testConnection();
								}
							}
						});
				testButton.setEnabled(true);
			}
		});
		Button copyButton = createPushButton(parent, "Copy");
		enableWithSelection(copyButton, SWT.SINGLE);
		copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				visitViewerSelection("Copying...", new Visitor<Target>() {
					public void visit(Target item, IProgressMonitor monitor) {
						final String base64 = item.toBase64();
						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								Clipboard clipboard = new Clipboard(Display
										.getDefault());
								try {
									clipboard.setContents(
											new Object[] { base64 },
											new Transfer[] { TextTransfer
													.getInstance() });
								} finally {
									clipboard.dispose();
								}
							}
						});
					}
				});
			}
		});

		final Button pasteButton = createPushButton(parent, "Paste");
		pasteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = new Clipboard(Display.getDefault());
				try {
					String base64 = (String) clipboard.getContents(TextTransfer
							.getInstance());
					Target item = Target.valueOf(base64);
					if (item != null) {
						add(item);
					}
				} catch (Exception ex) {
					MessageDialog
							.openError(pasteButton.getShell(),
									"Error pasting CopyTo target",
									"The clipboard does not contain a valid CopyTo target for pasting");
					ex.printStackTrace();
				} finally {
					clipboard.dispose();
				}
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

	@Override
	protected String[] getColumnLabels() {
		return new String[] { "Name", "URL" };
	}

	@Override
	protected EditingSupport createEditingSupport(String name,
			TableViewer viewer, DataBindingContext context) {
		if ("label".equals(name)) {
			IValueProperty cellEditorControlText = CellEditorProperties
					.control().value(WidgetProperties.text());
			return ObservableValueEditingSupport.create(viewer, context,
					new TextCellEditor(viewer.getTable()),
					cellEditorControlText, BeanProperties.value(Target.class,
							"label"));
		}
		return super.createEditingSupport(name, viewer, context);
	}
}