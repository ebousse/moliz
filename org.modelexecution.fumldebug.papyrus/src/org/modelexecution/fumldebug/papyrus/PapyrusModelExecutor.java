/*
 * Copyright (c) 2012 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Langer - initial API and implementation
 */
package org.modelexecution.fumldebug.papyrus;

import java.io.File;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.uml.tools.model.UmlModel;
import org.eclipse.uml2.uml.NamedElement;
import org.modelexecution.fuml.convert.ConverterRegistry;
import org.modelexecution.fuml.convert.IConversionResult;
import org.modelexecution.fuml.convert.IConverter;
import org.modelexecution.fumldebug.core.ExecutionContext;
import org.modelexecution.fumldebug.core.ExecutionEventListener;
import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
import org.modelexecution.fumldebug.core.event.Event;
import org.modelexecution.fumldebug.core.trace.tracemodel.Trace;
import org.modelexecution.fumldebug.libraryregistry.LibraryRegistry;
import org.modelexecution.fumldebug.libraryregistry.OpaqueBehaviorCallReplacer;
import org.modelexecution.fumldebug.papyrus.util.PapyrusResourceUtil;

import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Syntax.Activities.IntermediateActivities.Activity;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;

public class PapyrusModelExecutor {

	private static final ConverterRegistry converterRegistry = ConverterRegistry
			.getInstance();
	private static final String PLATFORM_RESOURCE = "platform:/resource";

	private UmlModel umlModel;

	private int executionID = -1;
	private Trace trace;
	private String modelPath;
	private IConversionResult conversionResult;

	public PapyrusModelExecutor(String modelpath) {
		this.modelPath = modelpath;
	}

	public Trace executeActivity(String name, Object_ context,
			ParameterValueList parameterValues) {
		loadModel(modelPath);
		conversionResult = convertResource();
		registerOpaqueBehaviors();
		Activity activity = conversionResult.getActivity(name);
		executeActivity(activity, context, parameterValues);
		return getTrace();
	}
	
	private void registerOpaqueBehaviors() {
		LibraryRegistry libraryRegistry = new LibraryRegistry(getExecutionContext());
		Map<String, OpaqueBehavior> registeredOpaqueBehaviors = libraryRegistry.loadRegisteredLibraries();
		OpaqueBehaviorCallReplacer.instance.replaceOpaqueBehaviorCalls(conversionResult.getAllActivities(), registeredOpaqueBehaviors);		
	}
	
	public Trace executeActivity(String name) {
		return executeActivity(name, null, new ParameterValueList());
	}

	private void loadModel(String path) {
		URI modelUri = null;
		if(path.contains(PLATFORM_RESOURCE)) {
			modelUri = getResourceURI(path);
		} else {
			modelUri = getFileURI(path);
		}
		
		umlModel = PapyrusResourceUtil.loadModel(modelUri);
	}

	private URI getResourceURI(String path) {
		return URI.createPlatformResourceURI(path.replace(PLATFORM_RESOURCE, ""), true);
	}

	private URI getFileURI(String path) {
		return URI.createFileURI(new File(path).getAbsolutePath());
	}

	private IConversionResult convertResource() {
		NamedElement namedElement = PapyrusResourceUtil.obtainFirstNamedElement(umlModel);
		IConverter converter = getConverter(namedElement);
		return converter.convert(namedElement);
	}

	private IConverter getConverter(NamedElement namedElement) {
		return converterRegistry.getConverter(namedElement);
	}

	private void executeActivity(Activity activity, Object_ context,
			ParameterValueList parameterValues) {
		addEventListener(new ExecutionEventListener() {
			@Override
			public void notify(Event event) {
				if (executionID == -1) {
					if (event instanceof ActivityEntryEvent) {
						ActivityEntryEvent activityEntryEvent = (ActivityEntryEvent) event;
						executionID = activityEntryEvent
								.getActivityExecutionID();
					}
				}
			}
		});
		getExecutionContext().execute(activity, context, parameterValues);
		trace = getExecutionContext().getTrace(executionID);
		executionID = -1;
	}

	public void addEventListener(ExecutionEventListener eventListener) {
		getExecutionContext().addEventListener(eventListener);
	}

	public void removeEventListener(ExecutionEventListener eventListener) {
		getExecutionContext().addEventListener(eventListener);
	}

	public ExecutionContext getExecutionContext() {
		return ExecutionContext.getInstance();
	}

	public Trace getTrace() {
		return trace;
	}

	public Resource getModelResource() {
		return umlModel.getResource();
	}

	public IConversionResult getConversionResult() {
		return conversionResult;
	}

}
