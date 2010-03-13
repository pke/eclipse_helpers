package org.eclipse.core.commands.extender;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Generic command that toggles the executed command and re-evaluates property testers for the 
 * <code>org.eclipse.core.commands.toggle</code> property.
 * 
 */
public class ToggleCommandHandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		HandlerUtil.toggleCommandState(event.getCommand());
		final IWorkbenchWindow ww = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final IEvaluationService service = (IEvaluationService) ww.getService(IEvaluationService.class);
		if (service != null) {
			service.requestEvaluation("org.eclipse.core.commands.toggle");
		}
		return null;
	}

}
