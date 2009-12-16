package org.eclipse.jface.utils.selection;

import org.eclipse.core.runtime.IAdapterManager;

/**
 * Selection visitor that uses an adapter manager to selection items to the given class.
 *  
 * @author PhilK
 *
 * @param <E> item type to adapt to
 * @param <R> return type
 */
public abstract class AdapterSelectionVisitor<E, R> extends BaseSelectionVisitor<E, R> {
	private final IAdapterManager am;

	public AdapterSelectionVisitor(Class<E> itemClass, IAdapterManager adapterManager) {
		super(itemClass);
		this.am = adapterManager;
	}

	@SuppressWarnings("unchecked")
	public E accept(Object item) {
		return (E) am.getAdapter(item, getItemClass());
	}
}