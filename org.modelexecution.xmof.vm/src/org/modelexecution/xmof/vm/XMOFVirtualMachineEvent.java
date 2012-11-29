/*
 * Copyright (c) 2012 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Langer - initial API and implementation
 */
package org.modelexecution.xmof.vm;

public class XMOFVirtualMachineEvent {
	
	public enum Type {
		START, STOP;
	}
	
	private Type type;

	protected XMOFVirtualMachineEvent(Type type) {
		super();
		this.type = type;
	}

	public Type getType() {
		return type;
	}

}