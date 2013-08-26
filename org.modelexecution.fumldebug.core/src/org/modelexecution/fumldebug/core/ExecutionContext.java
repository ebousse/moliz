/*
 * Copyright (c) 2012 Vienna University of Technology.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Tanja Mayerhofer - initial API and implementation
 */
package org.modelexecution.fumldebug.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.modeldriven.alf.fuml.impl.environment.AlfOpaqueBehaviorExecution;
import org.modeldriven.fuml.library.Library;
import org.modeldriven.fuml.repository.Repository;
import org.modelexecution.fumldebug.core.trace.tracemodel.Trace;

import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.TokenList;
import fUML.Semantics.Classes.Kernel.ExtensionalValueList;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.RedefinitionBasedDispatchStrategy;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.OpaqueBehaviorExecution;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Semantics.CommonBehaviors.Communications.FIFOGetNextEventStrategy;
import fUML.Semantics.Loci.LociL1.ExecutionFactory;
import fUML.Semantics.Loci.LociL1.Executor;
import fUML.Semantics.Loci.LociL1.FirstChoiceStrategy;
import fUML.Semantics.Loci.LociL1.Locus;
import fUML.Semantics.Loci.LociL3.ExecutionFactoryL3;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
import fUML.Syntax.Classes.Kernel.PrimitiveType;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.Behavior;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;

public class ExecutionContext {

	/**
	 * The language name to be used in opaque behaviors and expressions
	 * containing ALF code
	 */
	public static final String ALF_LANGUAGE_NAME = "Alf";

	protected static final String exception_illegalexecutionid = "Illegal execution id.";
	protected static final String exception_noenablednodes = "No enabled nodes available.";
	protected static final String exception_illegalactivitynode = "Illegal activity node. Activity node is not enabled.";

	private static ExecutionContext instance;

	private Locus locus = null;

	private Hashtable<String, OpaqueBehavior> opaqueBehaviors = new Hashtable<String, OpaqueBehavior>();

	private NodeSelectionStrategy nextNodeStrategy = new NodeSelectionStrategyImpl();

	private HashMap<Integer, ParameterValueList> activityExecutionOutput = new HashMap<Integer, ParameterValueList>();

	private HashMap<ActivityNode, Breakpoint> breakpoints = new HashMap<ActivityNode, Breakpoint>();

	protected ExecutionStatus executionStatus = new ExecutionStatus();

	private TraceHandler traceHandler = new TraceHandler(executionStatus);

	protected EventHandler eventHandler = new EventHandler(executionStatus);

	/**
	 * Returns the singleton instance.
	 * 
	 * @return singleton instance
	 */
	public static ExecutionContext getInstance() {
		if (instance == null) {
			instance = new ExecutionContext();
		}
		return instance;
	}

	protected ExecutionContext() {
		eventHandler.addPrimaryEventListener(traceHandler);

		Library.getInstance();

		this.locus = new Locus();
		this.locus.setFactory(createExecutionFactory());
		this.locus.setExecutor(new Executor());

		this.locus.factory.setStrategy(new RedefinitionBasedDispatchStrategy());
		this.locus.factory.setStrategy(new FIFOGetNextEventStrategy());
		this.locus.factory.setStrategy(new FirstChoiceStrategy());

		this.addBuiltInType("Boolean");
		this.addBuiltInType("String");
		this.addBuiltInType("Integer");
		this.addBuiltInType("Real");
		this.addBuiltInType("UnlimitedNatural");

		initializeProvidedBehaviors();
	}

	/**
	 * Creates an {@link ExecutionFactory} that also handles the execution of
	 * ALF {@link OpaqueBehavior opaque behaviors}.
	 * 
	 * @return the created {@link ExecutionFactoryL3}
	 */
	private ExecutionFactory createExecutionFactory() {
		return new ExecutionFactoryL3() {
			public OpaqueBehaviorExecution instantiateOpaqueBehaviorExecution(
					OpaqueBehavior behavior) {
				for (String language : behavior.language) {
					if (language.equals(ALF_LANGUAGE_NAME)) {
						OpaqueBehaviorExecution execution = new AlfOpaqueBehaviorExecution();
						execution.types.add(behavior);
						return execution;
					}
				}
				return super.instantiateOpaqueBehaviorExecution(behavior);
			}
		};
	}

	private PrimitiveType addBuiltInType(String name) {
		PrimitiveType type = (PrimitiveType) Repository.INSTANCE
				.getClassifierByName(name).getDelegate();
		this.locus.factory.addBuiltInType(type);
		return type;
	}

	/**
	 * Adds opaque behaviors which are by default available.
	 */
	private void initializeProvidedBehaviors() {
		OpaqueBehaviorFactory behaviorFactory = new OpaqueBehaviorFactory();
		behaviorFactory.initialize();

		addOpaqueBehavior(behaviorFactory.getListgetBehavior());
		addOpaqueBehavior(behaviorFactory.getListsizeBehavior());
		addOpaqueBehavior(behaviorFactory.getAddBehavior());
		addOpaqueBehavior(behaviorFactory.getSubtractBehavior());
		addOpaqueBehavior(behaviorFactory.getGreaterBehavior());
		addOpaqueBehavior(behaviorFactory.getLessBehavior());
		addOpaqueBehavior(behaviorFactory.getLessOrEqualsBehavior());
		addOpaqueBehavior(behaviorFactory.getMultiplyBehavior());
		addOpaqueBehavior(behaviorFactory.getDivideBehavior());
		addOpaqueBehavior(behaviorFactory.getListindexofBehavior());
	}

	/**
	 * Executed an behavior.
	 * 
	 * @param behavior
	 *            behavior to be executed
	 * @param context
	 *            context of the execution
	 * @param inputs
	 *            input to the execution
	 */
	public void execute(Behavior behavior, Object_ context,
			ParameterValueList inputs) {
		if (inputs == null) {
			inputs = new ParameterValueList();
		}
		this.locus.executor.execute(behavior, context, inputs);
	}

	/**
	 * Executes a behavior stepwise.
	 * 
	 * @param behavior
	 *            behavior to be executed
	 * @param context
	 *            context of the execution
	 * @param inputs
	 *            input to the execution
	 */
	public void executeStepwise(Behavior behavior, Object_ context,
			ParameterValueList inputs) {
		if (inputs == null) {
			inputs = new ParameterValueList();
		}
		this.locus.executor.execute(behavior, context, inputs);
	}

	/**
	 * Performs the next step of an execution.
	 * 
	 * @param executionID
	 *            executionID of the execution for which the next step is
	 *            executed
	 * @throws IllegalArgumentException
	 *             if an invalid executionID was provided
	 */
	public void nextStep(int executionID) throws IllegalArgumentException {
		nextStep(executionID, null);
	}

	/**
	 * Performs the next step of an execution by executing the provided node.
	 * 
	 * @param executionID
	 *            executionID of the execution for which the next step is
	 *            executed
	 * @param node
	 *            activity node which is executed in the next step
	 * @throws IllegalArgumentException
	 *             if the executionID is invalid or the provided node is invalid
	 *             (i.e., null or not enabled in this execution)
	 */
	public void nextStep(int executionID, ActivityNode node)
			throws IllegalArgumentException {
		ActivityNodeChoice nextnode = null;

		if (node == null) {
			nextnode = getNextNode(executionID);
		} else {
			nextnode = new ActivityNodeChoice(executionID, node);
		}

		ActivityExecutionStatus activityExecutionStatus = executionStatus
				.getActivityExecutionStatus(nextnode.getExecutionID());

		boolean activityNodeWasEnabled = activityExecutionStatus
				.isNodeEnabled(nextnode.getActivityNode());
		if (!activityNodeWasEnabled) {
			throw new IllegalArgumentException(exception_illegalactivitynode);
		}

		activityExecutionStatus.addExecutingActivation(nextnode
				.getActivityNode());
		ActivityNodeActivation activation = activityExecutionStatus
				.getEnabledActivation(nextnode.getActivityNode());
		TokenList tokens = activityExecutionStatus.getTokens(nextnode
				.getActivityNode());

		if (activation == null || tokens == null) {
			activityExecutionStatus.removeExecutingActivation(nextnode
					.getActivityNode());
			throw new IllegalArgumentException(exception_noenablednodes);
		}

		activation.fire(tokens);

		if (executionStatus.isExecutionRunning(executionID)
				&& activityExecutionStatus.isInResumeMode()) {
			nextStep(executionID);
		}
	}

	/**
	 * Selects the next node to be executed.
	 * 
	 * @param executionID
	 *            executionID of the execution for which a node is selected
	 * @return selected node
	 * @throws IllegalArgumentException
	 *             if the executionID is invalid
	 */
	private ActivityNodeChoice getNextNode(int executionID)
			throws IllegalArgumentException {
		if (!executionStatus.isExecutionRunning(executionID)) {
			if (!activityExecutionOutput.containsKey(executionID)) {
				throw new IllegalArgumentException(exception_illegalexecutionid);
			}
		}

		ActivityNodeChoice nextnode = this.nextNodeStrategy.chooseNextNode(
				executionID, executionStatus);

		if (nextnode == null) {
			throw new IllegalArgumentException(exception_noenablednodes);
		}
		return nextnode;
	}

	/**
	 * Resumes an execution.
	 * 
	 * @param executionID
	 *            executionID of the execution which is resumed
	 * @throws IllegalArgumentException
	 *             if the executionID is invalid
	 */
	public void resume(int executionID) throws IllegalArgumentException {
		ActivityExecutionStatus activityExecutionStatus = executionStatus
				.getActivityExecutionStatus(executionID);
		activityExecutionStatus.setWholeExecutionInResumeMode(true);
		nextStep(executionID);
	}

	/**
	 * Returns the provided opaque behavior with the given name.
	 * 
	 * @param name
	 *            name of the opaque behavior to be retrieved
	 * @return provided opaque behavior, null if it is not provided
	 */
	public OpaqueBehavior getOpaqueBehavior(String name) {
		if (opaqueBehaviors.containsKey(name)) {
			return opaqueBehaviors.get(name);
		}
		return null;
	}

	/**
	 * Returns the extensional values held by the locus of this execution
	 * context.
	 * 
	 * @return extensional values held by the locus
	 */
	public ExtensionalValueList getExtensionalValues() {
		return locus.extensionalValues;
	}

	/**
	 * Resets the execution context.
	 */
	public void reset() {
		locus.extensionalValues = new ExtensionalValueList();
		breakpoints = new HashMap<ActivityNode, Breakpoint>();
		activityExecutionOutput = new HashMap<Integer, ParameterValueList>();
		executionStatus = new ExecutionStatus();
		traceHandler = new TraceHandler(executionStatus);
		eventHandler = new EventHandler(executionStatus);
		eventHandler.addPrimaryEventListener(traceHandler);
	}

	/**
	 * Returns the nodes enabled in an execution.
	 * 
	 * @param executionID
	 *            executionID of the execution for which the enabled nodes are
	 *            determined
	 * @return nodes enabled in the execution
	 */
	public List<ActivityNode> getEnabledNodes(int executionID) {
		List<ActivityNode> enablednodes = new ArrayList<ActivityNode>();

		ActivityExecutionStatus activityExecutionStatus = executionStatus
				.getActivityExecutionStatus(executionID);
		if (activityExecutionStatus != null) {
			enablednodes.addAll(activityExecutionStatus.getEnabledNodes());
		}

		return enablednodes;
	}

	/**
	 * Returns the output of an execution.
	 * 
	 * @param executionID
	 *            executionID of the execution for which the output is provided
	 * @return output of the execution
	 */
	public ParameterValueList getActivityOutput(int executionID) {
		return activityExecutionOutput.get(executionID);
	}

	/**
	 * Returns the trace of an execution.
	 * 
	 * @param executionID
	 *            executionID of the execution for which the trace is provided
	 * @return trace of the execution
	 */
	public Trace getTrace(int executionID) {
		return traceHandler.getTrace(executionID);
	}

	/**
	 * Adds a breakpoint for the specified activity node.
	 * 
	 * @param breakpoint
	 *            Breakpoint that shall be added
	 */
	public void addBreakpoint(Breakpoint breakpoint) {
		if (breakpoint == null) {
			return;
		}
		ActivityNode activitynode = breakpoint.getActivityNode();
		if (activitynode == null || activitynode.activity == null) {
			return;
		}

		breakpoints.put(activitynode, breakpoint);
	}

	/**
	 * Provides information if a breakpoint is set for the given activity node.
	 * 
	 * @param activitynode
	 *            activity node for which it is checked if a breakpoint is set
	 * @return breakpoint that is set for the given activity node, null if no
	 *         breakpoint is set
	 */
	public Breakpoint getBreakpoint(ActivityNode activitynode) {
		if (activitynode == null || activitynode.activity == null) {
			return null;
		}
		return this.breakpoints.get(activitynode);
	}

	/**
	 * Removes a breakpoint.
	 * 
	 * @param breakpoint
	 *            breakpoint to be removed
	 */
	public void removeBreakpoint(Breakpoint breakpoint) {
		if (breakpoint == null) {
			return;
		}
		ActivityNode activitynode = breakpoint.getActivityNode();
		if (activitynode == null || activitynode.activity == null) {
			return;
		}
		this.breakpoints.remove(activitynode);
	}

	/**
	 * Sets the used strategy for selecting nodes to be executed.
	 * 
	 * @param nextNodeStrategy
	 *            strategy to be set
	 */
	public void setNextNodeStrategy(NodeSelectionStrategy nextNodeStrategy) {
		this.nextNodeStrategy = nextNodeStrategy;
	}

	/**
	 * Returns the primitive type String.
	 * 
	 * @return primitive type String
	 */
	public PrimitiveType getPrimitiveStringType() {
		return this.locus.factory.getBuiltInType("String");
	}

	/**
	 * Returns the primitive type Integer.
	 * 
	 * @return primitive type Integer
	 */
	public PrimitiveType getPrimitivIntegerType() {
		return this.locus.factory.getBuiltInType("Integer");
	}

	/**
	 * Returns the primitive type Boolean.
	 * 
	 * @return primitive type Boolean
	 */
	public PrimitiveType getPrimitiveBooleanType() {
		return this.locus.factory.getBuiltInType("Boolean");
	}

	/**
	 * Returns the primitive type UnlimitedNatural.
	 * 
	 * @return primitive type UnlimitedNatural
	 */
	public PrimitiveType getPrimitiveUnlimitedNaturalType() {
		return this.locus.factory.getBuiltInType("UnlimitedNatural");
	}

	/**
	 * Terminates an execution. Also all called and all calling activities are
	 * terminated.
	 * 
	 * @param executionID
	 *            executionID of the execution that is terminated.
	 */
	public void terminate(int executionID) {
		ActivityExecutionStatus activityExecutionStatus = executionStatus
				.getActivityExecutionStatus(executionID);
		if (activityExecutionStatus == null) {
			// activity execution has been terminated already
			return;
		}
		ActivityExecutionStatus rootActivityExecutionStatus = activityExecutionStatus
				.getRootCallerExecutionStatus();
		executionStatus.removeActivityExecution(rootActivityExecutionStatus
				.getExecutionID());
	}

	/**
	 * Adds a provided opaque behavior.
	 * 
	 * @param behaviorexecution
	 *            opaque behavior to be provided
	 */
	public void addOpaqueBehavior(OpaqueBehaviorExecution behaviorexecution) {
		locus.factory.addPrimitiveBehaviorPrototype(behaviorexecution);
		OpaqueBehavior behavior = (OpaqueBehavior) behaviorexecution.types
				.get(0);
		this.opaqueBehaviors.put(behavior.name, behavior);
	}

	/**
	 * Returns the locus of the execution context.
	 * 
	 * @return locus of the execution context
	 */
	public Locus getLocus() {
		return this.locus;
	}

	/**
	 * Sets the output of an execution.
	 * 
	 * @param executionID
	 *            executionID of the execution for which an output is set
	 * @param output
	 *            output that is set
	 */
	protected void setActivityExecutionOutput(int executionID,
			ParameterValueList output) {
		this.activityExecutionOutput.put(executionID, output);
	}

	public void addEventListener(ExecutionEventListener listener) {
		eventHandler.addEventListener(listener);
	}

	public void removeEventListener(ExecutionEventListener listener) {
		eventHandler.removeEventListener(listener);
	}

}