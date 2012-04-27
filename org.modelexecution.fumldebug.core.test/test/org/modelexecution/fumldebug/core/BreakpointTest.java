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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeExitEvent;
import org.modelexecution.fumldebug.core.event.BreakpointEvent;
import org.modelexecution.fumldebug.core.event.Event;
import org.modelexecution.fumldebug.core.event.StepEvent;
import org.modelexecution.fumldebug.core.impl.BreakpointImpl;
import org.modelexecution.fumldebug.core.util.ActivityFactory;

import fUML.Semantics.Activities.IntermediateActivities.ActivityExecution;
import fUML.Semantics.Classes.Kernel.ExtensionalValueList;
import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Syntax.Actions.BasicActions.CallBehaviorAction;
import fUML.Syntax.Actions.IntermediateActions.CreateObjectAction;
import fUML.Syntax.Activities.IntermediateActivities.Activity;
import fUML.Syntax.Activities.IntermediateActivities.ForkNode;
import fUML.Syntax.Activities.IntermediateActivities.InitialNode;
import fUML.Syntax.Classes.Kernel.Class_;

/**
 * @author Tanja Mayerhofer
 *
 */
public class BreakpointTest extends MolizTest implements ExecutionEventListener {

	private List<Event> eventlist = new ArrayList<Event>();
	private List<ExtensionalValueList> extensionalValueLists = new ArrayList<ExtensionalValueList>();
	
	public BreakpointTest() {
		ExecutionContext.getInstance().getExecutionEventProvider().addEventListener(this);
		ExecutionContext.getInstance().activityExecutionOutput = new HashMap<ActivityExecution, ParameterValueList>();
		ExecutionContext.getInstance().activityExecutions = new HashMap<Integer, ActivityExecution>();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		eventlist = new ArrayList<Event>();
		extensionalValueLists = new ArrayList<ExtensionalValueList>();
		ExecutionContext.getInstance().reset();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Breakpoint is set for CreateObjectAction2.
	 * After this breakpoint is hit, resume is called. 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 */
	@Test
	public void testSingleBreakpoint1() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Activity activity = ActivityFactory.createActivity("testSingleBreakpoint1");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);

		// Set Breakpoint
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));		
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(1));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(6, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(4);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());
		
		assertTrue(eventlist.get(5) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(5));
		assertEquals(create1, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(create2, step2.getNewEnabledNodes().get(0));
		
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(11, eventlist.size());
		
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(6);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(7) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(7)).getNode());
		assertEquals(create2entry, eventlist.get(7).getParent());	
		
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(create3entry, eventlist.get(9).getParent());
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(10)).getActivity());
		assertEquals(activityentry, eventlist.get(10).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));		
		
		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}		
	
	/**
	 * Breakpoint is set for CreateObjectAction3.
	 * After this breakpoint is hit, resume is called. 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 */
	@Test
	public void testSingleBreakpoint2() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Activity activity = ActivityFactory.createActivity("testSingleBreakpoint2");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);

		// Set Breakpoint
		BreakpointImpl breakpointcreate3 = new BreakpointImpl(create3);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate3);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(1));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
		
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(8, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(4);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(5)).getNode());
		assertEquals(create2entry, eventlist.get(5).getParent());	
		
		assertTrue(eventlist.get(6) instanceof BreakpointEvent);
		BreakpointEvent create3breakpoint = (BreakpointEvent)eventlist.get(6);
		assertEquals(breakpointcreate3, create3breakpoint.getBreakpoint());
		assertEquals(activityentry, create3breakpoint.getParent());
		
		assertTrue(eventlist.get(7) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(7));
		assertEquals(create2, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(create3, step2.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(11, eventlist.size());
				
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(create3entry, eventlist.get(9).getParent());
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(10)).getActivity());
		assertEquals(activityentry, eventlist.get(10).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));		
		
		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * No Breakpoint is set and resume is called after the first step. 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 */
	@Test
	public void testResumeWihtoutBreakpoint() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Activity activity = ActivityFactory.createActivity("testResumeWihtoutBreakpoint");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);

		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(1));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(9, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(4);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(5)).getNode());
		assertEquals(create2entry, eventlist.get(5).getParent());			
				
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(6);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(7) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(7)).getNode());
		assertEquals(create3entry, eventlist.get(7).getParent());
		assertTrue(eventlist.get(8) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(8)).getActivity());
		assertEquals(activityentry, eventlist.get(8).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));		
		
		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));		
	}
	
	/**
	 * Breakpoints are set for every node.
	 * After each breakpoint resume is called. 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 */
	@Test
	public void testBreakpointForEveryActivityNode() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Activity activity = ActivityFactory.createActivity("testBreakpointForEveryActivityNode");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);

		// Set Breakpoint
		BreakpointImpl breakpointcreate1 = new BreakpointImpl(create1);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate1);
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		BreakpointImpl breakpointcreate3 = new BreakpointImpl(create3);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate3);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(3, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof BreakpointEvent);
		BreakpointEvent create1breakpoint = (BreakpointEvent)eventlist.get(1);
		assertEquals(breakpointcreate1, create1breakpoint.getBreakpoint());
		assertEquals(activityentry, create1breakpoint.getParent());
		assertTrue(eventlist.get(2) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(2));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(7, eventlist.size());
		
		assertTrue(eventlist.get(3) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(3);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(4) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(4)).getNode());
		assertEquals(create1entry, eventlist.get(4).getParent());
					
		assertTrue(eventlist.get(5) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(5);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());	
		assertTrue(eventlist.get(6) instanceof StepEvent);
		StepEvent step2 = (StepEvent) eventlist.get(6);
		assertEquals(create1, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(create2, step2.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(11, eventlist.size());
		
		assertTrue(eventlist.get(7) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(7);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(8) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(8)).getNode());
		assertEquals(create2entry, eventlist.get(8).getParent());	
		
		assertTrue(eventlist.get(9) instanceof BreakpointEvent);
		BreakpointEvent create3breakpoint = (BreakpointEvent)eventlist.get(9);
		assertEquals(breakpointcreate3, create3breakpoint.getBreakpoint());
		assertEquals(activityentry, create3breakpoint.getParent());	
		assertTrue(eventlist.get(10) instanceof StepEvent);
		StepEvent step3 = ((StepEvent)eventlist.get(10));
		assertEquals(create2, step3.getLocation());
		assertEquals(activityentry, step3.getParent());
		assertEquals(1, step3.getNewEnabledNodes().size());
		assertEquals(create3, step3.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
				
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(14, eventlist.size());
		
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(11);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(12)).getNode());
		assertEquals(create3entry, eventlist.get(12).getParent());
		assertTrue(eventlist.get(13) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(13)).getActivity());
		assertEquals(activityentry, eventlist.get(13).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));		

		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoints are set for CreateObjectAction1 and CreateObjectAction3.
	 * After each breakpoint resume is called. 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 */
	@Test
	public void testMultipleBreakpoints() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Activity activity = ActivityFactory.createActivity("testMultipleBreakpoints");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);

		// Set Breakpoint
		BreakpointImpl breakpointcreate1 = new BreakpointImpl(create1);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate1);
		BreakpointImpl breakpointcreate3 = new BreakpointImpl(create3);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate3);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(3, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof BreakpointEvent);
		BreakpointEvent create1breakpoint = (BreakpointEvent)eventlist.get(1);
		assertEquals(breakpointcreate1, create1breakpoint.getBreakpoint());
		assertEquals(activityentry, create1breakpoint.getParent());
		assertTrue(eventlist.get(2) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(2));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(9, eventlist.size());
		
		assertTrue(eventlist.get(3) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(3);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(4) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(4)).getNode());
		assertEquals(create1entry, eventlist.get(4).getParent());							
		
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(create2entry, eventlist.get(6).getParent());	
		
		assertTrue(eventlist.get(7) instanceof BreakpointEvent);
		BreakpointEvent create3breakpoint = (BreakpointEvent)eventlist.get(7);
		assertEquals(breakpointcreate3, create3breakpoint.getBreakpoint());
		assertEquals(activityentry, create3breakpoint.getParent());	
		assertTrue(eventlist.get(8) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(8));
		assertEquals(create2, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(create3, step2.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
				
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(12, eventlist.size());
		
		assertTrue(eventlist.get(9) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(9);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(10) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(10)).getNode());
		assertEquals(create3entry, eventlist.get(10).getParent());
		assertTrue(eventlist.get(11) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(11)).getActivity());
		assertEquals(activityentry, eventlist.get(11).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));	

		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoints are set for CreateObjectAction2 and CreateObjectAction4.
	 * After each breakpoint resume is called. 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * CreateObjectAction4 (class = Class4)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 * CreateObjectAction3 --> CreateObjectAction4
	 */
	@Test
	public void testMultipleBreakpoints2() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Class_ class4 = ActivityFactory.createClass("Class4");
		Activity activity = ActivityFactory.createActivity("testMultipleBreakpoints");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		CreateObjectAction create4 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class4", class4);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);
		ActivityFactory.createControlFlow(activity, create3, create4);

		// Set Breakpoint		
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		BreakpointImpl breakpointcreate4 = new BreakpointImpl(create4);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate4);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(1));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(6, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());							
		
		assertTrue(eventlist.get(4) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(4);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());	
		assertTrue(eventlist.get(5) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(5));
		assertEquals(create1, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(create2, step2.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
				
		assertEquals(12, eventlist.size());
		
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(6);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(7) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(7)).getNode());
		assertEquals(create2entry, eventlist.get(7).getParent());	
		
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(create3entry, eventlist.get(9).getParent());	
		
		assertTrue(eventlist.get(10) instanceof BreakpointEvent);
		BreakpointEvent create4breakpoint = (BreakpointEvent)eventlist.get(10);
		assertEquals(breakpointcreate4, create4breakpoint.getBreakpoint());
		assertEquals(activityentry, create4breakpoint.getParent());	
		assertTrue(eventlist.get(11) instanceof StepEvent);
		StepEvent step3 = ((StepEvent)eventlist.get(11));
		assertEquals(create3, step3.getLocation());
		assertEquals(activityentry, step3.getParent());
		assertEquals(1, step3.getNewEnabledNodes().size());
		assertEquals(create4, step3.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create4, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));				
				
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(15, eventlist.size());
		
		assertTrue(eventlist.get(12) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create4entry = (ActivityNodeEntryEvent)eventlist.get(12);
		assertEquals(create4, create4entry.getNode());	
		assertEquals(activityentry, create4entry.getParent());
		assertTrue(eventlist.get(13) instanceof ActivityNodeExitEvent);
		assertEquals(create4, ((ActivityNodeExitEvent)eventlist.get(13)).getNode());
		assertEquals(create4entry, eventlist.get(13).getParent());
		assertTrue(eventlist.get(14) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(14)).getActivity());
		assertEquals(activityentry, eventlist.get(14).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(4, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));
		Object_ o4= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(3));
		assertEquals(1, o4.getTypes().size());
		assertEquals(class4, o4.getTypes().get(0));	
		
		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoints are set for CreateObjectAction2 and CreateObjectAction4.
	 * After each breakpoint/step, nextStep is called 
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * CreateObjectAction4 (class = Class4)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 * CreateObjectAction3 --> CreateObjectAction4
	 */
	@Test
	public void testNextStepWithBreakpoints() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Class_ class4 = ActivityFactory.createClass("Class4");
		Activity activity = ActivityFactory.createActivity("testNextStepWithBreakpoints");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		CreateObjectAction create4 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class4", class4);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);
		ActivityFactory.createControlFlow(activity, create3, create4);

		// Set Breakpoint
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		BreakpointImpl breakpointcreate4 = new BreakpointImpl(create4);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate4);
				
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(1));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Next step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID());
		
		assertEquals(6, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());							
		
		assertTrue(eventlist.get(4) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(4);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());	
		assertTrue(eventlist.get(5) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(5));
		assertEquals(create1, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(create2, step2.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		
		// Next step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID());
				
		assertEquals(9, eventlist.size());
		
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(6);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(7) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(7)).getNode());
		assertEquals(create2entry, eventlist.get(7).getParent());	
		
		assertTrue(eventlist.get(8) instanceof StepEvent);
		StepEvent step3 = ((StepEvent)eventlist.get(8));
		assertEquals(create2, step3.getLocation());
		assertEquals(activityentry, step3.getParent());
		assertEquals(1, step3.getNewEnabledNodes().size());
		assertEquals(create3, step3.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		
		// Next step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID());
				
		assertEquals(13, eventlist.size());
				
		assertTrue(eventlist.get(9) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(9);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(10) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(10)).getNode());
		assertEquals(create3entry, eventlist.get(10).getParent());	
		
		assertTrue(eventlist.get(11) instanceof BreakpointEvent);
		BreakpointEvent create4breakpoint = (BreakpointEvent)eventlist.get(11);
		assertEquals(breakpointcreate4, create4breakpoint.getBreakpoint());
		assertEquals(activityentry, create4breakpoint.getParent());			
		assertTrue(eventlist.get(12) instanceof StepEvent);
		StepEvent step4 = ((StepEvent)eventlist.get(12));
		assertEquals(create3, step4.getLocation());
		assertEquals(activityentry, step4.getParent());
		assertEquals(1, step4.getNewEnabledNodes().size());
		assertEquals(create4, step4.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create4, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));				
				
		// Next step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID());
		
		assertEquals(16, eventlist.size());
		
		assertTrue(eventlist.get(13) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create4entry = (ActivityNodeEntryEvent)eventlist.get(13);
		assertEquals(create4, create4entry.getNode());	
		assertEquals(activityentry, create4entry.getParent());
		assertTrue(eventlist.get(14) instanceof ActivityNodeExitEvent);
		assertEquals(create4, ((ActivityNodeExitEvent)eventlist.get(14)).getNode());
		assertEquals(create4entry, eventlist.get(14).getParent());
		assertTrue(eventlist.get(15) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(15)).getActivity());
		assertEquals(activityentry, eventlist.get(15).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(4, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));
		Object_ o4= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(3));
		assertEquals(1, o4.getTypes().size());
		assertEquals(class4, o4.getTypes().get(0));	
		
		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoint is set for CreateObjectAction2.
	 * 
	 *  After the first step (get enabled nodes), NEXTSTEP is called.
	 *  Then the initialNode is executed and RESUME is called.
	 *  Then create 1 is executed and the breakpoint for create 2 is hit.
	 *  Then NEST STEP is called (--> create 2 is executed) 
	 *  and RESUME afterwards (--> create3, create4 are executed).
	 * 
	 * Activity:
	 * InitialNode
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * CreateObjectAction4 (class = Class4)
	 * 
	 * Activity ControlFlow:
	 * InitialNode --> CreateObjectAction1
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 * CreateObjectAction3 --> CreateObjectAction4
	 */
	@Test
	public void testNextStepResumeMix() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Class_ class4 = ActivityFactory.createClass("Class4");
		Activity activity = ActivityFactory.createActivity("testNextStepWithBreakpoints");
		InitialNode initial = ActivityFactory.createInitialNode(activity, "InitialNode");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		CreateObjectAction create4 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class4", class4);
		
		ActivityFactory.createControlFlow(activity, initial, create1);
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);
		ActivityFactory.createControlFlow(activity, create3, create4);

		// Set Breakpoint
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(activity, ((StepEvent)eventlist.get(1)).getLocation());
		assertEquals(activityentry, eventlist.get(1).getParent());
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(initial, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Next step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID());
		
		assertEquals(5, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initial, initialentry.getNode());	
		assertEquals(activityentry, initialentry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initial, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialentry, eventlist.get(3).getParent());							
		assertTrue(eventlist.get(4) instanceof StepEvent);
		assertEquals(initial, ((StepEvent)eventlist.get(4)).getLocation());
		assertEquals(activityentry, eventlist.get(4).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());				
		
		assertEquals(9, eventlist.size());
		
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(5);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(6)).getNode());
		assertEquals(create1entry, eventlist.get(6).getParent());							
		
		assertTrue(eventlist.get(7) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(7);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());	
		assertTrue(eventlist.get(8) instanceof StepEvent);
		assertEquals(create1, ((StepEvent)eventlist.get(8)).getLocation());
		assertEquals(activityentry, eventlist.get(8).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		
		// Next step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID());
				
		assertEquals(12, eventlist.size());
		
		assertTrue(eventlist.get(9) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(9);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(10) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(10)).getNode());
		assertEquals(create2entry, eventlist.get(10).getParent());			
		assertTrue(eventlist.get(11) instanceof StepEvent);
		assertEquals(create2, ((StepEvent)eventlist.get(11)).getLocation());
		assertEquals(activityentry, eventlist.get(11).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		
		// Resume execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
				
		assertEquals(17, eventlist.size());
				
		assertTrue(eventlist.get(12) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(12);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(13) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(13)).getNode());
		assertEquals(create3entry, eventlist.get(13).getParent());		
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create4entry = (ActivityNodeEntryEvent)eventlist.get(14);
		assertEquals(create4, create4entry.getNode());	
		assertEquals(activityentry, create4entry.getParent());
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(create4, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertEquals(create4entry, eventlist.get(15).getParent());
		assertTrue(eventlist.get(16) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(16)).getActivity());
		assertEquals(activityentry, eventlist.get(16).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(4, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));
		Object_ o4= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(3));
		assertEquals(1, o4.getTypes().size());
		assertEquals(class4, o4.getTypes().get(0));	
		
		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
		
	/**
	 * Breakpoint is set for CreateObjectAction3.
	 * After this breakpoint is hit, RESUME is called. 
	 * Then the breakpoint is deleted and the activity is
	 * debugged again, after the first step, RESUME is called.
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)
	 * 
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CreateObjectAction2
	 * CreateObjectAction2 --> CreateObjectAction3
	 */
	@Test
	public void testRemoveBreakpoint() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Activity activity = ActivityFactory.createActivity("testSingleBreakpoint2");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, create1, create2);
		ActivityFactory.createControlFlow(activity, create2, create3);

		// Set Breakpoint
		BreakpointImpl breakpointcreate3 = new BreakpointImpl(create3);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate3);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(activity, ((StepEvent)eventlist.get(1)).getLocation());
		assertEquals(activityentry, eventlist.get(1).getParent());
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(8, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(4);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(5)).getNode());
		assertEquals(create2entry, eventlist.get(5).getParent());	
		
		assertTrue(eventlist.get(6) instanceof BreakpointEvent);
		BreakpointEvent create3breakpoint = (BreakpointEvent)eventlist.get(6);
		assertEquals(breakpointcreate3, create3breakpoint.getBreakpoint());
		assertEquals(activityentry, create3breakpoint.getParent());
		
		assertTrue(eventlist.get(7) instanceof StepEvent);
		assertEquals(create2, ((StepEvent)eventlist.get(7)).getLocation());
		assertEquals(activityentry, eventlist.get(7).getParent());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(11, eventlist.size());
				
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(create3entry, eventlist.get(9).getParent());
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(10)).getActivity());
		assertEquals(activityentry, eventlist.get(10).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));		
		
		
		// Remove breakpoint
		ExecutionContext.getInstance().removeBreakpoint(breakpointcreate3);
		
		// Reset data structures
		eventlist = new ArrayList<Event>();
		extensionalValueLists = new ArrayList<ExtensionalValueList>();
				
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		assertEquals(activity, ((StepEvent)eventlist.get(1)).getLocation());
		assertEquals(activityentry, eventlist.get(1).getParent());
	
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		// The objects from the first execution still exist at the locus
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(9, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		create2entry = (ActivityNodeEntryEvent)eventlist.get(4);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(5)).getNode());
		assertEquals(create2entry, eventlist.get(5).getParent());	
				
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		create3entry = (ActivityNodeEntryEvent)eventlist.get(6);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(7) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(7)).getNode());
		assertEquals(create3entry, eventlist.get(7).getParent());
		assertTrue(eventlist.get(8) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(8)).getActivity());
		assertEquals(activityentry, eventlist.get(8).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(6, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(3));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(4));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(5));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));		

		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoints are set for CreateObjectAction1 and CreateObjectAction2.
	 * Therefore after the execution of ForkNode, two BreakPointEvents should be thrown
	 * 
	 * Activity:
	 * InitialNode
	 * ForkNode
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3) 
	 * 
	 * Activity Control Flow:
	 * InitialNode --> ForkNode
	 * ForkNode --> CreateObjectAction1
	 * ForkNode --> CreateObjectAction2
	 * ForkNode --> CreateObjectAction3
	 */
	@Test
	public void testMultipleBreakpointsHitInOneStep() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		
		Activity activity = ActivityFactory.createActivity("testMultipleBreakpointsHitInOneStep");
		InitialNode initial = ActivityFactory.createInitialNode(activity, "InitialNode");
		ForkNode fork = ActivityFactory.createForkNode(activity, "ForkNode");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
		
		ActivityFactory.createControlFlow(activity, initial, fork);
		ActivityFactory.createControlFlow(activity, fork, create1);
		ActivityFactory.createControlFlow(activity, fork, create2);
		ActivityFactory.createControlFlow(activity, fork, create3);
		
		// Set Breakpoint
		BreakpointImpl breakpointcreate1 = new BreakpointImpl(create1);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate1);
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1= ((StepEvent)eventlist.get(1));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(initial, step1.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(initial, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(9, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent initialentry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(initial, initialentry.getNode());	
		assertEquals(activityentry, initialentry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(initial, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(initialentry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent forkentry = (ActivityNodeEntryEvent)eventlist.get(4);
		assertEquals(fork, forkentry.getNode());	
		assertEquals(activityentry, forkentry.getParent());
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		assertEquals(fork, ((ActivityNodeExitEvent)eventlist.get(5)).getNode());
		assertEquals(forkentry, eventlist.get(5).getParent());	
		
		assertTrue(eventlist.get(6) instanceof BreakpointEvent);
		BreakpointEvent create1breakpoint = (BreakpointEvent)eventlist.get(6);
		assertEquals(breakpointcreate1, create1breakpoint.getBreakpoint());
		assertEquals(activityentry, create1breakpoint.getParent());		
		assertTrue(eventlist.get(7) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(7);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());
		
		assertTrue(eventlist.get(8) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(8));
		assertEquals(fork, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(3, step2.getNewEnabledNodes().size());
		assertTrue(step2.getNewEnabledNodes().contains(create1));
		assertTrue(step2.getNewEnabledNodes().contains(create2));
		assertTrue(step2.getNewEnabledNodes().contains(create3));
		
		assertEquals(3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create1));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create2));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create3));						
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Next Step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID(), create1);
		
		assertEquals(12, eventlist.size());
		
		assertTrue(eventlist.get(9) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(9);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(10) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(10)).getNode());
		assertEquals(create1entry, eventlist.get(10).getParent());
		assertTrue(eventlist.get(11) instanceof StepEvent);
		StepEvent step3 = ((StepEvent)eventlist.get(11));
		assertEquals(create1, step3.getLocation());
		assertEquals(activityentry, step3.getParent());
		assertEquals(0, step3.getNewEnabledNodes().size());
		
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create2));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create3));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		
		// Next Step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID(), create2);
		
		assertEquals(15, eventlist.size());
		
		assertTrue(eventlist.get(12) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(12);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(13) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(13)).getNode());
		assertEquals(create2entry, eventlist.get(13).getParent());
		assertTrue(eventlist.get(14) instanceof StepEvent);
		StepEvent step4 = ((StepEvent)eventlist.get(14));
		assertEquals(create2, step4.getLocation());
		assertEquals(activityentry, step4.getParent());
		assertEquals(0, step4.getNewEnabledNodes().size());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create3));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));	
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		
		// Next Step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID(), create3);
		
		assertEquals(18, eventlist.size());
		
		assertTrue(eventlist.get(15) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(15);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(16) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(16)).getNode());
		assertEquals(create3entry, eventlist.get(16).getParent());
		assertTrue(eventlist.get(17) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(17)).getActivity());
		assertEquals(activityentry, eventlist.get(17).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));	
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));

		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoints are set for CreateObjectAction1 and CreateObjectAction2.
	 * Therefore two BreakPointEvents should be sent right after starting the Activity.
	 * 
	 * Activity:
	 * CreateObjectAction1 (class = Class1)
	 * CreateObjectAction2 (class = Class2)
	 * CreateObjectAction3 (class = Class3)  
	 */
	@Test
	public void testMultipleBreakpointsHitInOneStep2() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		
		Activity activity = ActivityFactory.createActivity("testMultipleBreakpointsHitInOneStep2");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class2", class2);
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity, "CreateObject Class3", class3);
				
		// Set Breakpoint
		BreakpointImpl breakpointcreate1 = new BreakpointImpl(create1);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate1);
		BreakpointImpl breakpointcreate2 = new BreakpointImpl(create2);
		ExecutionContext.getInstance().addBreakpoint(breakpointcreate2);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity, null, new ParameterValueList());
				
		assertEquals(4, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));
		assertEquals(activity, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		
		assertTrue(eventlist.get(1) instanceof BreakpointEvent);
		BreakpointEvent create1breakpoint = (BreakpointEvent)eventlist.get(1);
		assertEquals(breakpointcreate1, create1breakpoint.getBreakpoint());
		assertEquals(activityentry, create1breakpoint.getParent());		
		assertTrue(eventlist.get(2) instanceof BreakpointEvent);
		BreakpointEvent create2breakpoint = (BreakpointEvent)eventlist.get(2);
		assertEquals(breakpointcreate2, create2breakpoint.getBreakpoint());
		assertEquals(activityentry, create2breakpoint.getParent());
		
		assertTrue(eventlist.get(3) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(3));
		assertEquals(activity, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(3, step1.getNewEnabledNodes().size());
		assertTrue(step1.getNewEnabledNodes().contains(create1));
		assertTrue(step1.getNewEnabledNodes().contains(create2));
		assertTrue(step1.getNewEnabledNodes().contains(create3));
	
		assertEquals(3, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create1));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create2));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create3));		
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());			
		
		// Next Step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID(), create1);
		
		assertEquals(7, eventlist.size());
		
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(4);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(5)).getNode());
		assertEquals(create1entry, eventlist.get(5).getParent());
		assertTrue(eventlist.get(6) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(6));
		assertEquals(create1, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(0, step2.getNewEnabledNodes().size());
		
		assertEquals(2, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create2));
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create3));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		
		// Next Step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID(), create2);
		
		assertEquals(10, eventlist.size());
		
		assertTrue(eventlist.get(7) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(7);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(8) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(8)).getNode());
		assertEquals(create2entry, eventlist.get(8).getParent());
		assertTrue(eventlist.get(9) instanceof StepEvent);
		StepEvent step3 = ((StepEvent)eventlist.get(9));
		assertEquals(create2, step3.getLocation());
		assertEquals(activityentry, step3.getParent());
		assertEquals(0, step3.getNewEnabledNodes().size());
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertTrue(ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).contains(create3));
		
		assertEquals(2, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));	
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		
		// Next Step
		ExecutionContext.getInstance().nextStep(activityentry.getActivityExecutionID(), create3);
		
		assertEquals(13, eventlist.size());
		
		assertTrue(eventlist.get(10) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(10);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activityentry, create3entry.getParent());
		assertTrue(eventlist.get(11) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(11)).getNode());
		assertEquals(create3entry, eventlist.get(11).getParent());
		assertTrue(eventlist.get(12) instanceof ActivityExitEvent);
		assertEquals(activity, ((ActivityExitEvent)eventlist.get(12)).getActivity());
		assertEquals(activityentry, eventlist.get(12).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(3, extensionalValueLists.get(extensionalValueLists.size()-1).size());		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));	
		o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class2, o2.getTypes().get(0));
		Object_ o3 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class3, o3.getTypes().get(0));

		// All events have the same activityExecutionID
		assertTrue(checkSameActivityExecutionID(eventlist));
	}
	
	/**
	 * Breakpoint is set for CallBehaviorAction.
	 * After this breakpoint is hit, resume is called. 
	 * 
	 * Activity 1:
	 * CreateObjectAction1 (class = Class1)
	 * CallBehaviorAction (activity = Activity 2)
	 * CreateObjectAction2 (class = Class2)
	 *
	 * Activity ControlFlow:
	 * CreateObjectAction1 --> CallBehaviorAction
	 * CallBehaviorAction --> CreateObjectAction2
	 * 
	 * Activity 2:
	 * CreateObjectAction3 (class = Class3)
	 * CreateObjectAction4 (class = Class4)
	 *
	 * Activity ControlFlow:
	 * CreateObjectAction3 --> CreateObjectAction4
	 */
	@Ignore
	@Test
	public void testCallBehaviorAction() {
		Class_ class1 = ActivityFactory.createClass("Class1");
		Class_ class2 = ActivityFactory.createClass("Class2");
		Class_ class3 = ActivityFactory.createClass("Class3");
		Class_ class4 = ActivityFactory.createClass("Class4");
		
		Activity activity2 = ActivityFactory.createActivity("testCallBehaviorAction activity2");
		CreateObjectAction create3 = ActivityFactory.createCreateObjectAction(activity2, "CreateObject Class3", class3);
		CreateObjectAction create4 = ActivityFactory.createCreateObjectAction(activity2, "CreateObject Class4", class4);
		ActivityFactory.createControlFlow(activity2, create3, create4);
		
		Activity activity1 = ActivityFactory.createActivity("testCallBehaviorAction activity1");
		CreateObjectAction create1 = ActivityFactory.createCreateObjectAction(activity1, "CreateObject Class1", class1);
		CreateObjectAction create2 = ActivityFactory.createCreateObjectAction(activity1, "CreateObject Class2", class2);
		CallBehaviorAction call = ActivityFactory.createCallBehaviorAction(activity1, "CallBehaviorAction activity=activity2", activity2);
		ActivityFactory.createControlFlow(activity1, create1, call);
		ActivityFactory.createControlFlow(activity1, call, create2);

		// Set Breakpoint
		Breakpoint breakpointcall = new BreakpointImpl(call);
		ExecutionContext.getInstance().addBreakpoint(breakpointcall);
		
		// Start Debugging
		ExecutionContext.getInstance().debug(activity1, null, new ParameterValueList());
				
		assertEquals(2, eventlist.size());
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		ActivityEntryEvent activityentry = ((ActivityEntryEvent)eventlist.get(0));		
		assertEquals(activity1, activityentry.getActivity());		
		assertNull(activityentry.getParent());
		assertTrue(eventlist.get(1) instanceof StepEvent);
		StepEvent step1 = ((StepEvent)eventlist.get(1));
		assertEquals(activity1, step1.getLocation());
		assertEquals(activityentry, step1.getParent());
		assertEquals(1, step1.getNewEnabledNodes().size());
		assertEquals(create1, step1.getNewEnabledNodes().get(0));
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(create1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(0, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(6, eventlist.size());
		
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create1entry = (ActivityNodeEntryEvent)eventlist.get(2);
		assertEquals(create1, create1entry.getNode());	
		assertEquals(activityentry, create1entry.getParent());
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		assertEquals(create1, ((ActivityNodeExitEvent)eventlist.get(3)).getNode());
		assertEquals(create1entry, eventlist.get(3).getParent());
					
		assertTrue(eventlist.get(4) instanceof BreakpointEvent);
		BreakpointEvent breakpointeventcall = (BreakpointEvent)eventlist.get(4);
		assertEquals(breakpointcall, breakpointeventcall.getBreakpoint());
		assertEquals(activityentry, breakpointeventcall.getParent());
		
		assertTrue(eventlist.get(5) instanceof StepEvent);
		StepEvent step2 = ((StepEvent)eventlist.get(5));
		assertEquals(create1, step2.getLocation());
		assertEquals(activityentry, step2.getParent());
		assertEquals(1, step2.getNewEnabledNodes().size());
		assertEquals(call, step2.getNewEnabledNodes().get(0));
		
		
		assertEquals(1, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		assertEquals(call, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).get(0));
		
		assertEquals(1, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		Object_ o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));				
		
		// Resume Execution
		ExecutionContext.getInstance().resume(activityentry.getActivityExecutionID());
		
		assertEquals(17, eventlist.size());
		
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent callentry = (ActivityNodeEntryEvent)eventlist.get(6);
		assertEquals(call, callentry.getNode());	
		assertTrue(eventlist.get(7) instanceof ActivityEntryEvent);
		ActivityEntryEvent activity2entry = ((ActivityEntryEvent)eventlist.get(7));		
		assertEquals(activity2, activity2entry.getActivity());		
		assertEquals(callentry, activity2entry.getParent());
		
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create3entry = (ActivityNodeEntryEvent)eventlist.get(8);
		assertEquals(create3, create3entry.getNode());	
		assertEquals(activity2entry, create3entry.getParent());
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		assertEquals(create3, ((ActivityNodeExitEvent)eventlist.get(9)).getNode());
		assertEquals(create3entry, eventlist.get(9).getParent());	
		
		assertTrue(eventlist.get(10) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create4entry = (ActivityNodeEntryEvent)eventlist.get(10);
		assertEquals(create4, create4entry.getNode());	
		assertEquals(activity2entry, create4entry.getParent());
		assertTrue(eventlist.get(11) instanceof ActivityNodeExitEvent);
		assertEquals(create4, ((ActivityNodeExitEvent)eventlist.get(11)).getNode());
		assertEquals(create4entry, eventlist.get(11).getParent());
		assertTrue(eventlist.get(12) instanceof ActivityExitEvent);
		assertEquals(activity2, ((ActivityExitEvent)eventlist.get(12)).getActivity());
		assertEquals(activity2entry, eventlist.get(12).getParent());
		assertTrue(eventlist.get(13) instanceof ActivityNodeExitEvent);
		assertEquals(call, ((ActivityNodeExitEvent)eventlist.get(13)).getNode());
		assertEquals(callentry, eventlist.get(13).getParent());
		
		assertTrue(eventlist.get(14) instanceof ActivityNodeEntryEvent);
		ActivityNodeEntryEvent create2entry = (ActivityNodeEntryEvent)eventlist.get(14);
		assertEquals(create2, create2entry.getNode());	
		assertEquals(activityentry, create2entry.getParent());
		assertTrue(eventlist.get(15) instanceof ActivityNodeExitEvent);
		assertEquals(create2, ((ActivityNodeExitEvent)eventlist.get(15)).getNode());
		assertEquals(create2entry, eventlist.get(15).getParent());	
		assertTrue(eventlist.get(16) instanceof ActivityExitEvent);
		assertEquals(activity1, ((ActivityExitEvent)eventlist.get(16)).getActivity());
		assertEquals(activityentry, eventlist.get(16).getParent());
		
		assertEquals(0, ExecutionContext.getInstance().getEnabledNodes(activityentry.getActivityExecutionID()).size());
		
		assertEquals(4, extensionalValueLists.get(extensionalValueLists.size()-1).size());
		
		o1 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(0));
		assertEquals(1, o1.getTypes().size());
		assertEquals(class1, o1.getTypes().get(0));
		Object_ o2 = (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(1));
		assertEquals(1, o2.getTypes().size());
		assertEquals(class3, o2.getTypes().get(0));
		Object_ o3= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(2));
		assertEquals(1, o3.getTypes().size());
		assertEquals(class4, o3.getTypes().get(0));		
		Object_ o4= (Object_)(extensionalValueLists.get(extensionalValueLists.size()-1).get(3));
		assertEquals(1, o4.getTypes().size());
		assertEquals(class2, o4.getTypes().get(0));	
		
		int executionIDactivity1 = activityentry.getActivityExecutionID();
		int executionIDactivity2 = activity2entry.getActivityExecutionID();
		assertTrue(executionIDactivity1 != executionIDactivity2);
		for(int i=0;i<7;++i) {
			assertEquals(executionIDactivity1, eventlist.get(i).getActivityExecutionID());
		}
		for(int i=7;i<13;++i){
			assertEquals(executionIDactivity2, eventlist.get(i).getActivityExecutionID());
		}
		for(int i=13;i<17;++i) {
			assertEquals(executionIDactivity1, eventlist.get(i).getActivityExecutionID());
		}
	}	
	
	@Override
	public void notify(Event event) {		
		eventlist.add(event);
		
		if(event instanceof StepEvent || event instanceof ActivityExitEvent) {
			ExtensionalValueList list = new ExtensionalValueList();
			for(int i=0;i<ExecutionContext.getInstance().getExtensionalValues().size();++i) {
				if(ExecutionContext.getInstance().getExtensionalValues().get(i).getClass() == Object_.class) {
					//list.add(ExecutionContext.getInstance().getExtensionalValues().get(i));
					list.add(copyObject((Object_)ExecutionContext.getInstance().getExtensionalValues().get(i)));
				}
			}
			extensionalValueLists.add(list);
		}
	}
			
}