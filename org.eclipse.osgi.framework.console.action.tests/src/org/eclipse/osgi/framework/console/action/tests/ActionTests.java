package org.eclipse.osgi.framework.console.action.tests;

import java.util.Dictionary;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

public class ActionTests {

	class TestCommandInterpreter implements CommandInterpreter {

		public String nextArgument() {
			return null;
		}

		public Object execute(final String cmd) {
			return null;
		}

		public void print(final Object o) {

		}

		public void println() {

		}

		public void println(final Object o) {

		}

		public void printStackTrace(final Throwable t) {

		}

		public void printDictionary(final Dictionary dic, final String title) {

		}

		public void printBundleResource(final Bundle bundle, final String resource) {

		}
	}
}
