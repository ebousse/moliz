/*
* generated by Xtext
*/
package org.modelexecution;

import org.eclipse.xtext.junit4.IInjectorProvider;

import com.google.inject.Injector;

public class AlfUiInjectorProvider implements IInjectorProvider {
	
	public Injector getInjector() {
		return org.modelexecution.ui.internal.AlfActivator.getInstance().getInjector("org.modelexecution.Alf");
	}
	
}