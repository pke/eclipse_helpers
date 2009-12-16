package org.eclipse.jface.utils.selection;

public interface SelectionVisitor<E, R> {
	/**
	 * Checks if the visitor will accept the given object and returns it in the
	 * type it want to visit it.
	 * 
	 * @param item
	 * @return <code>null</code> if the visitor does not accept the item.
	 */
	E accept(Object item);

	/**
	 * 
	 * @param item
	 */
	R visit(E item);
}