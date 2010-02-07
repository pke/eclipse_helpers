package eclipseutils.ui.copyto.internal;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.commands.ICommandService;

public class LastCommandState extends PersistentState {

	private static final String SPLIT_CHAR = "@";

	public LastCommandState() {
		setShouldPersist(true);
	}

	@Override
	public void load(IPreferenceStore store, String preferenceKey) {
		if (store.contains(preferenceKey)) {
			final String value = store.getString(preferenceKey);
			setValue(value);
		}
	}

	@Override
	public final void save(final IPreferenceStore store,
			final String preferenceKey) {
		final Object value = getValue();
		if (value instanceof String) {
			store.setValue(preferenceKey, (String) value);
		}
	}

	@Override
	public void setValue(final Object value) {
		if (!(value instanceof String)) {
			throw new IllegalArgumentException(
					"LastIdState takes a String as a value"); //$NON-NLS-1$
		}

		super.setValue(value);
	}

	public static ParameterizedCommand getId(ICommandService commandService,
			State state) {
		String value = (String) state.getValue();
		if (value != null) {
			String[] split = value.split(SPLIT_CHAR);
			if (split.length == 2) {
				try {
					return commandService.deserialize(split[1]);
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	public static String getLabel(State state) {
		String value = (String) state.getValue();
		if (value != null) {
			String[] split = value.split(SPLIT_CHAR);
			if (split.length == 2) {
				return split[0];
			}
		}
		return "";
	}

	public static void setValue(State state, ParameterizedCommand command,
			String label) {
		state.setValue(label + SPLIT_CHAR + command.serialize());
	}
}
