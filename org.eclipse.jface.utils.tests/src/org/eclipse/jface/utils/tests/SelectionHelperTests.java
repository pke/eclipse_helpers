package org.eclipse.jface.utils.tests;

import junit.framework.Assert;

import org.eclipse.jface.utils.selection.CastSelectionVisitor;
import org.eclipse.jface.utils.selection.SelectionHelper;
import org.eclipse.jface.utils.selection.SimpleCastSelectionVisitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

public class SelectionHelperTests {

	@Test
	public void testStringIteration() {
		Object[] elements = new Object[]{"a", "b", "c"};
		IStructuredSelection selection = new StructuredSelection(elements);
		final int count[] = new int[1];
		SelectionHelper.visit(selection, new SimpleCastSelectionVisitor<String>(String.class) {
			@Override
			protected void visitItem(String item) {
				count[0]++;
			}
		});
		Assert.assertEquals(elements.length, count[0]);
	}
	
	@Test
	public void testShouldBreakOnB() {
		Object[] elements = new Object[]{"a", "b", "c"};
		IStructuredSelection selection = new StructuredSelection(elements);
		final int count[] = new int[1];
		Assert.assertEquals("b", SelectionHelper.visit(selection, new CastSelectionVisitor<String, String>(String.class) {
			public String visit(String item) {
				count[0]++;
				if (item.equals("b")) return item;
				return null;				
			}
		}));
		Assert.assertEquals(2, count[0]);
	}
	
	
	@Test
	public void testNotAllStringIteration() {
		Object[] elements = new Object[]{"a", 1, "c"};
		IStructuredSelection selection = new StructuredSelection(elements);
		final int count[] = new int[1];
		SelectionHelper.visit(selection, new SimpleCastSelectionVisitor<String>(String.class) {
			@Override
			protected void visitItem(String item) {
				count[0]++;
			}
		});
		Assert.assertEquals(elements.length-1, count[0]);
	}
}
