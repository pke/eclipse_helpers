package org.eclipse.osgi.framework.console.action;

/**
 * OSGi service interface for command actions.
 * 
 * <p>
 * You need to specify the following service properties:
 * <ul>
 * <li><code>action</code> - the name of the action. This must be single string, without spaces
 * <li><code>command</code> - name of the command to supply the action for.
 * </ul>
 * 
 * <h2>Example</h2>
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
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public interface CommandAction {
	/**
	 * Executes the action.
	 * 
	 * @param context for the execution.
	 * @throws Exception
	 */
	void execute(CommandActionContext context) throws Exception;
}
