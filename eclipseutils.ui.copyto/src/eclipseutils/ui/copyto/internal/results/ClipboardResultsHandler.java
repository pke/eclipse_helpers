package eclipseutils.ui.copyto.internal.results;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import eclipseutils.ui.copyto.api.Result;
import eclipseutils.ui.copyto.api.ResultsHandler;

public class ClipboardResultsHandler implements ResultsHandler {

	public void handleResults(final Collection successes, final Collection failures) {

		final String joinedURLs = joinURLs(successes);
		if (joinedURLs.length() > 0) {
			final Clipboard clipboard = new Clipboard(Display.getDefault());
			try {
				final Transfer[] dataTypes = { TextTransfer.getInstance() };
				clipboard.setContents(new String[] { joinedURLs }, dataTypes);
			} catch (final SWTError e) {
				if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
					throw e;
				}
				/*if (MessageDialog.openQuestion(getShell(), ActionMessages.CopyQualifiedNameAction_ErrorTitle, ActionMessages.CopyQualifiedNameAction_ErrorDescription)) {
					clipboard.setContents(data, dataTypes);
				}*/
			} finally {
				clipboard.dispose();
			}
		}
	}

	private String joinURLs(final Collection results) {
		final StringBuffer sb = new StringBuffer();
		final Iterator it = results.iterator();
		while (it.hasNext()) {
			final Result result = (Result) it.next();
			if (result.getStatus().isOK()) {
				final URL url = result.getLocation();
				if (url != null) {
					sb.append(url.toString());
					if (it.hasNext()) {
						sb.append(","); //$NON-NLS-1$
					}
				}
			}
		}
		return sb.toString();
	}
}
