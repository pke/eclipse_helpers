package org.eclipse.jface.utils.selection;


public abstract class BaseSelectionVisitor<E, R> implements SelectionVisitor<E, R> {
	private final Class<E> itemClass;
	
	public BaseSelectionVisitor(Class<E> itemClass) {
		this.itemClass = itemClass;
	}
	
	protected Class<E> getItemClass() {
		return itemClass;
	}
}