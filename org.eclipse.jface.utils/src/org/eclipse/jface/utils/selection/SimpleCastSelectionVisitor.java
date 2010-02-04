package org.eclipse.jface.utils.selection;

/**
 * Use this if you do not care about any return values.
 * 
 * @author PhilK
 *
 * @param <E>
 */
public abstract class SimpleCastSelectionVisitor<E> extends CastSelectionVisitor<E, Object> {

	public SimpleCastSelectionVisitor(Class<E> itemClass) {
		super(itemClass);
	}

	public final Object visit(E item) {
		visitItem(item);
		return null;
	}
	
	abstract protected void visitItem(E item);
}
