package org.eclipse.jface.utils.selection;

/**
 * Selection visitor that accepts items that can be cast to a specified class.
 * 
 * @author PhilK
 *
 * @param <E> item type
 * @param <R> return value type
 */
public abstract class CastSelectionVisitor<E, R> extends BaseSelectionVisitor<E, R> {
	public CastSelectionVisitor(final Class<E> itemClass) {
		super(itemClass);
	}

	public E accept(Object item) {
		try {
			return getItemClass().cast(item);
		} catch (ClassCastException e) {
			return null;
		}
	}
}