package org.eclipse.osgi.framework.console.action;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Base class for all CommandProvider that can provide commands that can be extended by actions.
 * 
 * <h2>Usage</h2>
 * Subclasses implement a {@link CommandProvider} according to the specs. In the public command methods they just call
 * this classes {@link #execute(String, CommandInterpreter)} method.<br>
 * Example:
 * <pre>
 * public class FileCommand extends ActionCommand {
 *   public void _file(CommandInterpreter ci) {
 *     execute("file", ci);
 *   }
 * }
 * </pre>
 * An action for this file command would look like this:
 * <pre>
 * public class FileCopyAction implements CommandAction {
 *   void execute(CommandActionContext context) throws Exception {
 *     String from = context.getArgument(0);
 *     String to = context.getArgument(1);
 *     if (from == null || to == null) {
 *       throw new IllegalArgumentException("You must specify from and to arguments");
 *     }
 *   }
 * }
 * </pre>
 * 
 * 
 * <h2>Localization</h2>
 * Localized texts for the commands and actions are loaded from the bundle localization of the command/action defining 
 * bundle.
 * <p>
 * <table border="1">
 * <tr><th>What</th><th>Key</th><th>Example</th></tr>
 * <tr><td>command name/title</td><td>name of the command</td><td></td></tr>
 * <tr><td>action</td><td>command.action</td><td><code>file.copy</code> - <i>file</i> is the command, <i>copy</i> the action</td></tr>
 * </table>
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 * @since 1.0
 */
public abstract class ActionCommand implements CommandProvider {

	private static final String COMMAND_PROPERTY = "command"; //$NON-NLS-1$
	private static final String ACTION_PROPERTY = "action"; //$NON-NLS-1$

	private final BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();

	final public String getHelp() {
		final StringBuilder sb = new StringBuilder();
		for (final String method : getCommandMethods()) {
			sb.append(getHelpForCommand(method));
		}
		return sb.toString();
	}

	protected BundleContext getBundleContext() {
		return this.bundleContext;
	}

	/**
	 * This has to be called by the subclass.
	 * 
	 * <p>
	 * This method collects the name of the action, the actions arguments and options 
	 * from the given command interpreter.<br>
	 * If the user specified the help parameter it simply prints help. 
	 * Otherwise it executes the action and catches exceptions during the execution.
	 * 
	 * <h2>Example</h2>
	 * <pre>
	 * public void _file(CommandInterpreter ci) {
	 *   execute("file", ci);
	 * }
	 * </pre>
	 * @param command name of the command
	 * @param ci command interpreter to use
	 */
	protected void execute(final String command, final CommandInterpreter ci) {
		String actionName = ci.nextArgument();

		if ("-h".equals(actionName) || "-help".equals(actionName) || "help".equals(actionName)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			actionName = ci.nextArgument();
			if (null != actionName) {
				ci.println(getHelpForCommandAction(command, actionName));
			} else {
				ci.println(getHelpForCommand(command));
			}
			return;
		}

		if (actionName == null || "help".equals(actionName)) { //$NON-NLS-1$
			ci.println(getHelpForCommand(command));
		} else {
			executeAction(command, actionName, ci);
		}
	}

	/**
	 * Returns an array of service references of actions for a given command.
	 * 
	 * <p>
	 * The action services are queried using a filter composed of the given command and actionName.
	 * <code>(&(action=<i>actionName</i>)(command=<i>command</i>))</code>
	 * @param command name of the command
	 * @param actionName name of the action to get. Can contain wild-cards. To query all actions for a command, use "*".
	 * @return an array of service references for found actions or <code>null</code>
	 * if no actions for the given command were found. 
	 */
	private ServiceReference[] getCommandActionRefs(final String command, final String actionName) {
		try {
			return this.bundleContext.getServiceReferences(CommandAction.class.getName(), "(&(action=" + actionName //$NON-NLS-1$
					+ ")(command=" + command + "))"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (final InvalidSyntaxException e) {
			return null;
		}
	}

	private ServiceReference getCommandActionRef(final String command, final String actionName) {
		final ServiceReference[] refs = getCommandActionRefs(command, actionName);
		return refs != null ? refs[0] : null;
	}

	/**
	 * @param flags to check
	 * @return whether the flags contain "verbose", "v" or "-verbose"
	 */
	private static boolean isVerbose(final Collection<String> flags) {
		return flags.contains("verbose") || flags.contains("v") || flags.contains("-verbose"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void executeAction(final String command, final String actionName, final CommandInterpreter ci) {
		final ServiceReference reference = getCommandActionRef(command, actionName);
		final Collection<String> flags = new HashSet<String>();
		final CommandAction action = reference != null ? (CommandAction) this.bundleContext.getService(reference)
				: null;

		try {
			if (null == action) {
				ci.println(Messages.ActionCommand_UnknownAction + ':' + actionName);
				final String suggestSimilarActions = suggestSimilarActions(command, actionName);
				if (suggestSimilarActions != null) {
					ci.println("\n" + suggestSimilarActions); //$NON-NLS-1$
				}
				return;
			}

			final List<String> params = new ArrayList<String>();

			String flag;
			while ((flag = ci.nextArgument()) != null) {
				if (flag.charAt(0) == '-') {
					flags.add(flag.substring(1));
				} else {
					params.add(flag);
				}
			}

			action.execute(new CommandActionContext() {

				public void println(final String text) {
					ci.println(text);
				}

				public void print(final String text) {
					ci.print(text);

				}

				public boolean isVerbose() {
					return ActionCommand.isVerbose(flags);
				}

				public boolean hasOption(final String name) {
					return flags.contains(name);
				}

				public String getArgument(final int index) {
					try {
						return params.get(index);
					} catch (final IndexOutOfBoundsException e) {
						return null;
					}
				}
			});
		} catch (final Throwable e) {
			if (isVerbose(flags)) {
				ci.printStackTrace(e);
			} else {
				ci.println(e.getMessage());
			}
			if (e instanceof IllegalArgumentException) {
				String help = Messages.ActionCommand_ForHelpEnter + ": " + command + " -help"; //$NON-NLS-1$ //$NON-NLS-2$
				if (action != null) {
					help += ' ' + actionName;
				}
				ci.println(help);
			}
		} finally {
			if (reference != null) {
				this.bundleContext.ungetService(reference);
			}
		}
	}

	private Collection<String> getCommandMethods() {
		final Collection<String> methods = new ArrayList<String>();
		for (final Method method : getClass().getMethods()) {
			if (Modifier.isPublic(method.getModifiers()) && method.getName().charAt(0) == '_') {
				final Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 1 && parameterTypes[0].equals(CommandInterpreter.class)) {
					methods.add(method.getName().substring(1));
				}
			}
		}
		return methods;
	}

	private String getString(final ResourceBundle bundle, final String key, final String def) {
		try {
			return bundle.getString(key);
		} catch (final MissingResourceException e) {
			return def;
		}
	}

	/**
	 * Composes a resource bundle key from a command, action and special argument.
	 * 
	 * <p>
	 * Given <i>command</i>=example, <i>action</i>=action, and <i>what</i> empty:<br>
	 * <code>example.action</code><br>
	 * Given <i>command</i>=example, <i>action</i>=action, and <i>what</i>=args:<br>
	 * <code>example.action.args</code><br>
	 * @param bundle
	 * @param command
	 * @param action
	 * @param what can be <code>null</code>.
	 * @param defaultValue to return if the given key was not found in the resource bundle.
	 * @return the value for the composed key or <i>defaultValue</i> if the key was not found in the resource bundle.
	 */
	private String localize(final ResourceBundle bundle, final String command, final String action, final String what,
			final String defaultValue) {
		return getString(bundle, command + '.' + action + (what == null ? "" : '.' + what), defaultValue); //$NON-NLS-1$
	}

	protected String suggestSimilarActions(final String command, final String action) {
		final StringBuilder sb = new StringBuilder();
		final ServiceReference[] references = getCommandActionRefs(command, "*" + action + "*"); //$NON-NLS-1$ //$NON-NLS-2$
		if (references != null) {
			sb.append(Messages.ActionCommand_DidYouMean);
			sb.append('\n');

			for (final ServiceReference reference : references) {
				sb.append(joinPropertyString(reference.getProperty(ACTION_PROPERTY)));
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	private String localize(final ResourceBundle bundle, final String command, final Object key, final String what,
			final String def) {
		if (key instanceof String) {
			return localize(bundle, command, (String) key, what, def);
		} else if (key instanceof String[]) {
			final String[] items = (String[]) key;
			for (final String item : items) {
				final String result = localize(bundle, command, item, what, null);
				if (result != null) {
					return result;
				}
			}
		}
		return def;
	}

	private String getHelpForCommandAction(final String command, final String action) {
		final StringBuilder sb = new StringBuilder();

		try {
			final ServiceReference reference = getCommandActionRef(command, action);
			if (null == reference) {
				sb.append(Messages.ActionCommand_UnknownAction + ": " + action //$NON-NLS-1$
						+ "\n" + suggestSimilarActions(command, action)); //$NON-NLS-1$
				return sb.toString();
			}

			final ResourceBundle bundle = getResourceBundle(reference.getBundle());

			if (bundle != null) {
				final Object actions = reference.getProperty(ACTION_PROPERTY);
				sb.append(command);
				sb.append(' ');
				sb.append(joinPropertyString(actions));
				sb.append(" - "); //$NON-NLS-1$
				final String help = localize(bundle, command, actions, null, Messages.ActionCommand_NoHelp).trim();
				int shortHelpIndex = help.indexOf('.');
				// If only one line of help
				if (shortHelpIndex + 1 == help.length()) {
					shortHelpIndex = -1;
				}
				if (shortHelpIndex != -1) {
					sb.append(help.substring(0, shortHelpIndex));
				} else {
					sb.append(help);
				}
				sb.append('\n');
				// Print extensive help
				if (shortHelpIndex != -1) {
					sb.append('\t');
					sb.append(help.substring(shortHelpIndex + 1).trim().replace("\n", "\n\t")); //$NON-NLS-1$//$NON-NLS-2$
					sb.append('\n');
				}
				String args = localize(bundle, command, actions, "args", null); //$NON-NLS-1$
				if (args != null) {
					args = args.replace("\n", "\n  "); //$NON-NLS-1$//$NON-NLS-2$
					sb.append("\t  "); //$NON-NLS-1$
					sb.append(args);
					sb.append('\n');
				}
				String examples = localize(bundle, command, actions, "examples", null); //$NON-NLS-1$
				if (examples != null) {
					final String examplePrefix = command + ' ' + action + ' ';
					examples = examplePrefix + examples;
					examples = examples.replace("\n", examplePrefix + "\n\t  "); //$NON-NLS-1$//$NON-NLS-2$
					sb.append("\t" + Messages.ActionCommand_Examples + ':'); //$NON-NLS-1$ 
					sb.append('\n');
					sb.append("\t  "); //$NON-NLS-1$
					sb.append(examples);
					sb.append('\n');
				}
			} else {
				sb.append(Messages.ActionCommand_NoHelp);
			}
		} catch (final Exception e) {

		}
		return sb.toString();
	}

	private static String joinPropertyString(final Object object) {
		if (object instanceof String) {
			return (String) object;
		} else if (object instanceof String[]) {
			final String[] items = (String[]) object;
			String s = ""; //$NON-NLS-1$
			for (int i = 0; i < items.length; ++i) {
				s += items[i];
				if (i < items.length - 1) {
					s += ',';
				}
			}
			return s;
		}
		return ""; //$NON-NLS-1$
	}

	private String getHelpForCommand(final String command) {
		final StringBuilder sb = new StringBuilder();

		try {
			final ResourceBundle bundle = getResourceBundle(this.bundleContext.getBundle());

			final String title = getString(bundle, command, null);
			if (title != null) {
				sb.append("---"); //$NON-NLS-1$
				sb.append(title);
				sb.append("---"); //$NON-NLS-1$
				sb.append('\n');
			}
			sb.append('\t');
			sb.append(command);

			final ServiceReference[] references = getCommandActionRefs(command, "*"); //$NON-NLS-1$
			if (null == references) {
				sb.append(" - "); //$NON-NLS-1$
				sb.append(Messages.ActionCommand_NoActions);
				sb.append('\n');
				return sb.toString();
			}

			sb.append(" ["); //$NON-NLS-1$
			for (final ServiceReference reference : references) {
				sb.append(joinPropertyString(reference.getProperty(ACTION_PROPERTY)));
				if (reference != references[references.length - 1]) {
					sb.append('|');
				}
			}
			sb.append("]\n"); //$NON-NLS-1$

			for (final ServiceReference reference : references) {
				final Object actionProperty = reference.getProperty(ACTION_PROPERTY);
				final Object commandProperty = reference.getProperty(COMMAND_PROPERTY);
				String action = actionProperty.toString();
				if (actionProperty instanceof String[]) {
					final String[] items = (String[]) actionProperty;
					action = items[0];
				}
				sb.append('\t');
				sb.append(getHelpForCommandAction(commandProperty.toString(), action));
				sb.append('\n');
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private ResourceBundle getResourceBundle(final Bundle bundle) {
		final ServiceReference localizationServiceReference = getBundleContext().getServiceReference(
				BundleLocalization.class.getName());
		if (localizationServiceReference != null) {
			final BundleLocalization localization = (BundleLocalization) getBundleContext().getService(
					localizationServiceReference);
			try {
				return localization.getLocalization(bundle, null);
			} finally {
				getBundleContext().ungetService(localizationServiceReference);
			}
		}
		return null;
	}
}
