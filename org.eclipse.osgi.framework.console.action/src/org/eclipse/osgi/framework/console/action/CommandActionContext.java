package org.eclipse.osgi.framework.console.action;

/**
 * An actions execution context. The context is used to grant access to 
 * the actions arguments and options given at the command line.
 * 
 * <p>
 * 
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 */
public interface CommandActionContext {
	/**
	 * Gets a given action argument at the specified 0-based index.
	 * 
	 * @param index of the argument to get. 
	 * Can also return an empty string if <code>""</code> was specified at the 
	 * command line at the position of <code>index</code>.
	 * @return the argument or <code>null</code> if <code>index</code> was out-of-range. 
	 */
	String getArgument(int index);

	/**
	 * Checks whether an option was specified for the action at the command line.
	 * 
	 * <p>
	 * If the user entered:<br>
	 * <code>example -t --force "Test"</code><br>
	 * Then you would check for the options like this:<br>
	 * <code>hasOption("t")</code><br>
	 * <code>hasOption("-force")</code><br>
	 * <b>Note</b>, that you have to specify the "-" sign for special options
	 * that are given at the command line using 2 "--".
	 * @param name of the option to check
	 * @return whether the given option was specified or not
	 */
	boolean hasOption(String name);

	/**
	 * This is a shortcut for hasOption("verbose"), hasOption("v") and hasOption("-verbose").
	 * @return whether the action should be executed verbose 
	 */
	boolean isVerbose();

	/**
	 * @param text
	 */
	void print(String text);

	/**
	 * @param text
	 */
	void println(String text);
}
