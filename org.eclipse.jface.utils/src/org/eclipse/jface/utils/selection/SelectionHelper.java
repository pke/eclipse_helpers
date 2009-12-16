package org.eclipse.jface.utils.selection;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * 
 * @author PhilK
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class SelectionHelper {

	/**
	 * Iterates over a <code>IStructuredSelection</code> using a visitor.
	 * 
	 * <p>
	 * The visitor specifies which items it want to visit.
	 * 
	 * @param <E>
	 *            Item type
	 * @param <R>
	 *            Return value type
	 * @param selection
	 *            to iterate. This must be an instance of
	 *            {@link IStructuredSelection} or the method will return
	 *            <code>null</code>.
	 * @param visitor
	 *            must not be <code>null</code>.
	 * @return the return value of {@link SelectionVisitor#visit(Object)} or
	 *         <code>null</code>.
	 */
	public static <E, R> R visit(final ISelection selection,
			final SelectionVisitor<E, R> visitor) {
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}

		final Iterator<?> iterator = ((IStructuredSelection) selection)
				.iterator();
		while (iterator.hasNext()) {
			try {
				E item = visitor.accept(iterator.next());
				if (item != null) {
					R result = visitor.visit(item);
					if (result != null) {
						return result;
					}
				}
			} catch (Throwable e) {
			}
		}
		return null;
	}
	

	private SelectionHelper() {
	}
}
