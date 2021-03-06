/*
 * Copyright (c) 2013 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tanja Mayerhofer - initial API and implementation
 */
package org.modelexecution.fumldebug.debugger.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

import fUML.Semantics.Classes.Kernel.FeatureValue;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.PrimitiveValue;
import fUML.Semantics.Classes.Kernel.Reference;
import fUML.Semantics.Classes.Kernel.Value;

public class FeatureValueValue extends org.modelexecution.fumldebug.debugger.model.Value {

	private static final String FEATURE_VALUE_REFERENCED_TYPE_NAME = "feature value";
	private FeatureValue value = null;	
	private HashMap<PrimitiveFeatureValueVariable, Value> primitiveValueVariables = new HashMap<PrimitiveFeatureValueVariable, Value>();

	public FeatureValueValue(ActivityDebugTarget target, FeatureValue value) {
		super(target);
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		return FEATURE_VALUE_REFERENCED_TYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	@Override
	public String getValueString() throws DebugException {
		String typeName = "";
		if(value.feature.typedElement != null) 
			if(value.feature.typedElement.type != null)
				typeName = value.feature.typedElement.type.qualifiedName + " ";
		String multiplicity = "";
			if(value.feature.multiplicityElement != null) {
				multiplicity = "[" + value.feature.multiplicityElement.lower + ".." + value.feature.multiplicityElement.upper.naturalValue + "]";				
			}
		return typeName + multiplicity;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	@Override
	public IVariable[] getVariables() throws DebugException{
		List<IVariable> variables = new ArrayList<IVariable>();
		int i=0;
		for (Value v : value.values) {
			IVariable variable = null;
			if(v instanceof PrimitiveValue) {
				PrimitiveValue primitiveValue = (PrimitiveValue)v;
				variable = new PrimitiveFeatureValueVariable(getActivityDebugTarget(), "[" + i + "]", this);
				primitiveValueVariables.put((PrimitiveFeatureValueVariable)variable, primitiveValue);
			} else if(v instanceof Reference) {
				Object_ object = ((Reference)v).referent;
				variable = obtainObjectVariable(object);				
			}
			if(variable != null) {
				variables.add(variable);
				++i;
			}
		}		 
		return variables.toArray(new IVariable[variables.size()]);
	}
	
	private ObjectVariable obtainObjectVariable(Object_ object) {
		IVariable[] locusVariables = getActivityDebugTarget()
				.getLocusVariables();
		for (int i = 0; i < locusVariables.length; ++i) {
			if (locusVariables[i] instanceof LocusVariable) {
				LocusVariable locusVariable = (LocusVariable) locusVariables[i];
				IValue locusVariableValue = getActivityDebugTarget()
						.getVariableValue(locusVariable);
				if (locusVariableValue instanceof LocusValue) {
					LocusValue locusValue = (LocusValue) locusVariableValue;
					return locusValue.getObjectVariable((Object_) object);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	@Override
	public boolean hasVariables() throws DebugException {
		for(Value v : value.values) {
			if(v instanceof PrimitiveValue || v instanceof Reference) {
				return true;
			}
		}
		return false;
	}
		
	public Value getValue(PrimitiveFeatureValueVariable variable) {
		return primitiveValueVariables.get(variable);
	}

}
