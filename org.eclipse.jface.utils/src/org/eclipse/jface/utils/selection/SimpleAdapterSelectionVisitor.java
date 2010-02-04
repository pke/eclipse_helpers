package org.eclipse.jface.utils.selection;

import org.eclipse.core.runtime.IAdapterManager;

/**
 * Use this if you do not care about any return values.
 * 
 * @author PhilK
 *
 * @param <E>
 */
public abstract class SimpleAdapterSelectionVisitor<E> extends AdapterSelectionVisitor<E, Object> {

	public SimpleAdapterSelectionVisitor(Class<E> itemClass, IAdapterManager adapterManager) {
		super(itemClass, adapterManager);
	}

	public final Object visit(E item) {
		visitItem(item);
		return null;
	}
	
	abstract protected void visitItem(E item);
}
