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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelexecution.fumldebug.core.event.ActivityEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityExitEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeEntryEvent;
import org.modelexecution.fumldebug.core.event.ActivityNodeExitEvent;
import org.modelexecution.fumldebug.core.event.Event;
import org.modelexecution.fumldebug.core.event.ExtensionalValueEvent;
import org.modelexecution.fumldebug.core.event.SuspendEvent;

import fUML.Semantics.Classes.Kernel.Object_;
import fUML.Semantics.Classes.Kernel.Reference;
import fUML.Semantics.Classes.Kernel.StringValue;
import fUML.Semantics.CommonBehaviors.BasicBehaviors.ParameterValueList;
import fUML.Syntax.Activities.IntermediateActivities.Activity;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
import fUML.Syntax.Classes.Kernel.Element;

/**
 * @author Tanja Mayerhofer
 *
 */
public class StructuredActivityNodesTests extends MolizTest implements ExecutionEventListener{

	private List<Event> eventlist = new ArrayList<Event>();
	
	public StructuredActivityNodesTests() {
		ExecutionContext.getInstance().reset();
		ExecutionContext.getInstance().getExecutionEventProvider().addEventListener(this);		
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
		ExecutionContext.getInstance().reset();
		ExecutionContext.getInstance().getExecutionEventProvider().addEventListener(this);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testStructuredActivityNode3_execute() {
		TestActivityFactory factory = new TestActivityFactory();
		TestActivityFactory.StructuredActivityNodeTestActivity3 testactivity = factory.new StructuredActivityNodeTestActivity3();
		Activity activity = testactivity.activity;
		
		// execute activity
		ExecutionContext.getInstance().execute(activity, null, testactivity.parametervaluelist);
		int executionID = ((ActivityEntryEvent)eventlist.get(0)).getActivityExecutionID();		
		
		// check events
		assertEquals(16, eventlist.size());	
		
		ActivityEntryEvent entry_activity;
		ActivityExitEvent exit_activity;
		ActivityNodeEntryEvent entry_structurednode1, entry_structurednode2, entry_initial, entry_create1, entry_create2, entry_setname_1, entry_setname_2;
		ActivityNodeExitEvent exit_structurednode1, exit_structurednode2, exit_initial, exit_create1, exit_create2, exit_setname_1, exit_setname_2;
				
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		entry_activity = (ActivityEntryEvent)eventlist.get(0);		
		assertTrue(eventlist.get(1) instanceof ActivityNodeEntryEvent);
		entry_structurednode1 = (ActivityNodeEntryEvent)eventlist.get(1);
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		entry_initial = (ActivityNodeEntryEvent)eventlist.get(2);
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		exit_initial = (ActivityNodeExitEvent)eventlist.get(3);				
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		entry_create1 = (ActivityNodeEntryEvent)eventlist.get(4);
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		exit_create1 = (ActivityNodeExitEvent)eventlist.get(5);
		assertTrue(eventlist.get(6) instanceof ActivityNodeEntryEvent);
		entry_create2 = (ActivityNodeEntryEvent)eventlist.get(6);
		assertTrue(eventlist.get(7) instanceof ActivityNodeExitEvent);
		exit_create2 = (ActivityNodeExitEvent)eventlist.get(7);
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		entry_structurednode2 = (ActivityNodeEntryEvent)eventlist.get(8);
		assertTrue(eventlist.get(9) instanceof ActivityNodeEntryEvent);
		entry_setname_1 = (ActivityNodeEntryEvent)eventlist.get(9);
		assertTrue(eventlist.get(10) instanceof ActivityNodeExitEvent);
		exit_setname_1 = (ActivityNodeExitEvent)eventlist.get(10);
		assertTrue(eventlist.get(11) instanceof ActivityNodeEntryEvent);
		entry_setname_2 = (ActivityNodeEntryEvent)eventlist.get(11);
		assertTrue(eventlist.get(12) instanceof ActivityNodeExitEvent);
		exit_setname_2 = (ActivityNodeExitEvent)eventlist.get(12);		
		assertTrue(eventlist.get(13) instanceof ActivityNodeExitEvent);
		exit_structurednode2 = (ActivityNodeExitEvent)eventlist.get(13);
		assertTrue(eventlist.get(14) instanceof ActivityNodeExitEvent);
		exit_structurednode1 = (ActivityNodeExitEvent)eventlist.get(14);		
		assertTrue(eventlist.get(15) instanceof ActivityExitEvent);
		exit_activity = (ActivityExitEvent)eventlist.get(15);
		
		assertTrue(checkActivityEntryEvent(entry_activity, activity));
		assertTrue(checkActivityExitEvent(exit_activity, activity, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode1, testactivity.structurednode1, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_initial, testactivity.initial, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_create1, testactivity.create1, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_create2, testactivity.create2, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode2, testactivity.structurednode2, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_1, testactivity.setname, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_2, testactivity.setname, entry_activity));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode1, testactivity.structurednode1, entry_structurednode1));
		assertTrue(checkActivityNodeExitEvent(exit_initial, testactivity.initial, entry_initial));
		assertTrue(checkActivityNodeExitEvent(exit_create1, testactivity.create1, entry_create1));
		assertTrue(checkActivityNodeExitEvent(exit_create2, testactivity.create2, entry_create2));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode2, testactivity.structurednode2, entry_structurednode2));
		assertTrue(checkActivityNodeExitEvent(exit_setname_1, testactivity.setname, entry_setname_1));
		assertTrue(checkActivityNodeExitEvent(exit_setname_2, testactivity.setname, entry_setname_2));
		
		// check output
		ParameterValueList outvalues = ExecutionContext.getInstance().getActivityOutput(executionID);
		assertTrue(checkStructuredActivityNode3Output(outvalues, testactivity));
	}
	
	@Test
	public void testStructuredActivityNode3_executestepwise() {
		TestActivityFactory factory = new TestActivityFactory();
		TestActivityFactory.StructuredActivityNodeTestActivity3 testactivity = factory.new StructuredActivityNodeTestActivity3();
		Activity activity = testactivity.activity;
		
		// execute activity
		ExecutionContext.getInstance().executeStepwise(activity, null, testactivity.parametervaluelist);
		int executionID = ((ActivityEntryEvent)eventlist.get(0)).getActivityExecutionID();		
		
		ExecutionContext.getInstance().nextStep(executionID, testactivity.structurednode1);
		ExecutionContext.getInstance().nextStep(executionID, testactivity.initial);
		ExecutionContext.getInstance().nextStep(executionID, testactivity.create1);
		ExecutionContext.getInstance().nextStep(executionID, testactivity.create2);
		ExecutionContext.getInstance().nextStep(executionID, testactivity.structurednode2);
		ExecutionContext.getInstance().nextStep(executionID, testactivity.setname);
		ExecutionContext.getInstance().nextStep(executionID, testactivity.setname);
		
		// check events
		assertEquals(23, eventlist.size());	
		
		ActivityEntryEvent entry_activity;
		ActivityExitEvent exit_activity;
		ActivityNodeEntryEvent entry_structurednode1, entry_structurednode2, entry_initial, entry_create1, entry_create2, entry_setname_1, entry_setname_2;
		ActivityNodeExitEvent exit_structurednode1, exit_structurednode2, exit_initial, exit_create1, exit_create2, exit_setname_1, exit_setname_2;
		SuspendEvent suspend_activity, suspend_structurednode1, suspend_initial, suspend_create1, suspend_create2, suspend_structurednode2, suspend_setname_1;		
		
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		entry_activity = (ActivityEntryEvent)eventlist.get(0);		
		assertTrue(eventlist.get(1) instanceof SuspendEvent);
		suspend_activity = (SuspendEvent)eventlist.get(1);
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		entry_structurednode1 = (ActivityNodeEntryEvent)eventlist.get(2);
		assertTrue(eventlist.get(3) instanceof SuspendEvent);
		suspend_structurednode1 = (SuspendEvent)eventlist.get(3);
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		entry_initial = (ActivityNodeEntryEvent)eventlist.get(4);
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		exit_initial = (ActivityNodeExitEvent)eventlist.get(5);		
		assertTrue(eventlist.get(6) instanceof SuspendEvent);
		suspend_initial = (SuspendEvent)eventlist.get(6);						
		assertTrue(eventlist.get(7) instanceof ActivityNodeEntryEvent);
		entry_create1 = (ActivityNodeEntryEvent)eventlist.get(7);
		assertTrue(eventlist.get(8) instanceof ActivityNodeExitEvent);
		exit_create1 = (ActivityNodeExitEvent)eventlist.get(8);		
		assertTrue(eventlist.get(9) instanceof SuspendEvent);
		suspend_create1 = (SuspendEvent)eventlist.get(9);			
		assertTrue(eventlist.get(10) instanceof ActivityNodeEntryEvent);
		entry_create2 = (ActivityNodeEntryEvent)eventlist.get(10);
		assertTrue(eventlist.get(11) instanceof ActivityNodeExitEvent);
		exit_create2 = (ActivityNodeExitEvent)eventlist.get(11);		
		assertTrue(eventlist.get(12) instanceof SuspendEvent);
		suspend_create2 = (SuspendEvent)eventlist.get(12);			
		assertTrue(eventlist.get(13) instanceof ActivityNodeEntryEvent);
		entry_structurednode2 = (ActivityNodeEntryEvent)eventlist.get(13);
		assertTrue(eventlist.get(14) instanceof SuspendEvent);
		suspend_structurednode2 = (SuspendEvent)eventlist.get(14);			
		assertTrue(eventlist.get(15) instanceof ActivityNodeEntryEvent);
		entry_setname_1 = (ActivityNodeEntryEvent)eventlist.get(15);
		assertTrue(eventlist.get(16) instanceof ActivityNodeExitEvent);
		exit_setname_1 = (ActivityNodeExitEvent)eventlist.get(16);
		assertTrue(eventlist.get(17) instanceof SuspendEvent);
		suspend_setname_1 = (SuspendEvent)eventlist.get(17);
		assertTrue(eventlist.get(18) instanceof ActivityNodeEntryEvent);
		entry_setname_2 = (ActivityNodeEntryEvent)eventlist.get(18);
		assertTrue(eventlist.get(19) instanceof ActivityNodeExitEvent);
		exit_setname_2 = (ActivityNodeExitEvent)eventlist.get(19);		
		assertTrue(eventlist.get(20) instanceof ActivityNodeExitEvent);
		exit_structurednode2 = (ActivityNodeExitEvent)eventlist.get(20);
		assertTrue(eventlist.get(21) instanceof ActivityNodeExitEvent);
		exit_structurednode1 = (ActivityNodeExitEvent)eventlist.get(21);		
		assertTrue(eventlist.get(22) instanceof ActivityExitEvent);
		exit_activity = (ActivityExitEvent)eventlist.get(22);
		
		assertTrue(checkActivityEntryEvent(entry_activity, activity));		
		assertTrue(checkActivityExitEvent(exit_activity, activity, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode1, testactivity.structurednode1, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_initial, testactivity.initial, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_create1, testactivity.create1, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_create2, testactivity.create2, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode2, testactivity.structurednode2, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_1, testactivity.setname, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_2, testactivity.setname, entry_activity));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode1, testactivity.structurednode1, entry_structurednode1));
		assertTrue(checkActivityNodeExitEvent(exit_initial, testactivity.initial, entry_initial));
		assertTrue(checkActivityNodeExitEvent(exit_create1, testactivity.create1, entry_create1));
		assertTrue(checkActivityNodeExitEvent(exit_create2, testactivity.create2, entry_create2));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode2, testactivity.structurednode2, entry_structurednode2));
		assertTrue(checkActivityNodeExitEvent(exit_setname_1, testactivity.setname, entry_setname_1));
		assertTrue(checkActivityNodeExitEvent(exit_setname_2, testactivity.setname, entry_setname_2));
		assertTrue(checkSuspendEvent(suspend_activity, activity, entry_activity, testactivity.structurednode1));
		assertTrue(checkSuspendEvent(suspend_structurednode1, testactivity.structurednode1, entry_activity, testactivity.initial));
		assertTrue(checkSuspendEvent(suspend_initial, testactivity.initial, entry_activity, testactivity.create1));
		assertTrue(checkSuspendEvent(suspend_create1, testactivity.create1, entry_activity, testactivity.create2));
		assertTrue(checkSuspendEvent(suspend_create2, testactivity.create2, entry_activity, testactivity.structurednode2));
		assertTrue(checkSuspendEvent(suspend_structurednode2, testactivity.structurednode2, entry_activity, testactivity.setname));
		assertTrue(checkSuspendEvent(suspend_setname_1, testactivity.setname, entry_activity, testactivity.setname));
		
		// check output
		ParameterValueList outvalues = ExecutionContext.getInstance().getActivityOutput(executionID);
		assertTrue(checkStructuredActivityNode3Output(outvalues, testactivity));
	}
	
	@Test
	public void testStructuredActivityNode1_execute() {
		TestActivityFactory factory = new TestActivityFactory();
		TestActivityFactory.StructuredActivityNodeTestActivity1 testactivity = factory.new StructuredActivityNodeTestActivity1();
		Activity activity = testactivity.activity;
		
		// execute activity
		ExecutionContext.getInstance().execute(activity, null, testactivity.parametervaluelist);
		int executionID = ((ActivityEntryEvent)eventlist.get(0)).getActivityExecutionID();		
		
		// check events
		assertEquals(8, eventlist.size());	
		
		ActivityEntryEvent entry_activity;
		ActivityExitEvent exit_activity;
		ActivityNodeEntryEvent entry_structurednode, entry_setname_1, entry_setname_2;
		ActivityNodeExitEvent exit_structurednode, exit_setname_1, exit_setname_2;
				
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		entry_activity = (ActivityEntryEvent)eventlist.get(0);		
		assertTrue(eventlist.get(1) instanceof ActivityNodeEntryEvent);
		entry_structurednode = (ActivityNodeEntryEvent)eventlist.get(1);
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		entry_setname_1 = (ActivityNodeEntryEvent)eventlist.get(2);
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		exit_setname_1 = (ActivityNodeExitEvent)eventlist.get(3);				
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		entry_setname_2 = (ActivityNodeEntryEvent)eventlist.get(4);
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		exit_setname_2 = (ActivityNodeExitEvent)eventlist.get(5);		
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		exit_structurednode = (ActivityNodeExitEvent)eventlist.get(6);
		assertTrue(eventlist.get(7) instanceof ActivityExitEvent);
		exit_activity = (ActivityExitEvent)eventlist.get(7);
		
		assertTrue(checkActivityEntryEvent(entry_activity, activity));
		assertTrue(checkActivityExitEvent(exit_activity, activity, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode, testactivity.structurednode, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_1, testactivity.addaction, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_2, testactivity.addaction, entry_activity));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode, testactivity.structurednode, entry_structurednode));
		assertTrue(checkActivityNodeExitEvent(exit_setname_1, testactivity.addaction, entry_setname_1));
		assertTrue(checkActivityNodeExitEvent(exit_setname_2, testactivity.addaction, entry_setname_2));
		
		// check output
		ParameterValueList outvalues = ExecutionContext.getInstance().getActivityOutput(executionID);
		assertTrue(checkStructuredActivityNode1Output(outvalues, testactivity));
	}
	
	@Test
	public void testStructuredActivityNode1_executestepwise() {
		TestActivityFactory factory = new TestActivityFactory();
		TestActivityFactory.StructuredActivityNodeTestActivity1 testactivity = factory.new StructuredActivityNodeTestActivity1();
		Activity activity = testactivity.activity;
		
		// execute activity
		ExecutionContext.getInstance().executeStepwise(activity, null, testactivity.parametervaluelist);
		int executionID = ((ActivityEntryEvent)eventlist.get(0)).getActivityExecutionID();
		ExecutionContext.getInstance().nextStep(executionID);
		ExecutionContext.getInstance().nextStep(executionID);
		ExecutionContext.getInstance().nextStep(executionID);
		
		// check events
		assertEquals(11, eventlist.size());	
		
		ActivityEntryEvent entry_activity;
		ActivityExitEvent exit_activity;
		ActivityNodeEntryEvent entry_structurednode, entry_setname_1, entry_setname_2;
		ActivityNodeExitEvent exit_structurednode, exit_setname_1, exit_setname_2;
		SuspendEvent suspend_activity, suspend_structurednode, suspend_setname_1;
				
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		entry_activity = (ActivityEntryEvent)eventlist.get(0);		
		assertTrue(eventlist.get(1) instanceof SuspendEvent);
		suspend_activity = (SuspendEvent)eventlist.get(1);
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		entry_structurednode = (ActivityNodeEntryEvent)eventlist.get(2);
		assertTrue(eventlist.get(3) instanceof SuspendEvent);
		suspend_structurednode = (SuspendEvent)eventlist.get(3);
		assertTrue(eventlist.get(4) instanceof ActivityNodeEntryEvent);
		entry_setname_1 = (ActivityNodeEntryEvent)eventlist.get(4);
		assertTrue(eventlist.get(5) instanceof ActivityNodeExitEvent);
		exit_setname_1 = (ActivityNodeExitEvent)eventlist.get(5);		
		assertTrue(eventlist.get(6) instanceof SuspendEvent);
		suspend_setname_1 = (SuspendEvent)eventlist.get(6);
		assertTrue(eventlist.get(7) instanceof ActivityNodeEntryEvent);
		entry_setname_2 = (ActivityNodeEntryEvent)eventlist.get(7);
		assertTrue(eventlist.get(8) instanceof ActivityNodeExitEvent);
		exit_setname_2 = (ActivityNodeExitEvent)eventlist.get(8);		
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		exit_structurednode = (ActivityNodeExitEvent)eventlist.get(9);
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		exit_activity = (ActivityExitEvent)eventlist.get(10);
		
		assertTrue(checkActivityEntryEvent(entry_activity, activity));
		assertTrue(checkActivityExitEvent(exit_activity, activity, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode, testactivity.structurednode, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_1, testactivity.addaction, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_setname_2, testactivity.addaction, entry_activity));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode, testactivity.structurednode, entry_structurednode));
		assertTrue(checkActivityNodeExitEvent(exit_setname_1, testactivity.addaction, entry_setname_1));
		assertTrue(checkActivityNodeExitEvent(exit_setname_2, testactivity.addaction, entry_setname_2));
		assertTrue(checkSuspendEvent(suspend_activity, activity, entry_activity, testactivity.structurednode));
		assertTrue(checkSuspendEvent(suspend_structurednode, testactivity.structurednode, entry_activity, testactivity.addaction));
		assertTrue(checkSuspendEvent(suspend_setname_1, testactivity.addaction, entry_activity, testactivity.addaction));
		
		// check output
		ParameterValueList outvalues = ExecutionContext.getInstance().getActivityOutput(executionID);
		assertTrue(checkStructuredActivityNode1Output(outvalues, testactivity));
	}
	
	@Test
	public void testStructuredActivityNode2_empty_execute() {
		TestActivityFactory factory = new TestActivityFactory();
		TestActivityFactory.StructuredActivityNodeTestActivity2 testactivity = factory.new StructuredActivityNodeTestActivity2();
		Activity activity = testactivity.activity;
		
		// execute activity
		ExecutionContext.getInstance().execute(activity, null, null);		
		
		// check events
		assertEquals(8, eventlist.size());	
		
		ActivityEntryEvent entry_activity;
		ActivityExitEvent exit_activity;
		ActivityNodeEntryEvent entry_initial, entry_structurednode, entry_final;
		ActivityNodeExitEvent exit_initial, exit_structurednode, exit_final;
				
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		entry_activity = (ActivityEntryEvent)eventlist.get(0);		
		assertTrue(eventlist.get(1) instanceof ActivityNodeEntryEvent);
		entry_initial = (ActivityNodeEntryEvent)eventlist.get(1);
		assertTrue(eventlist.get(2) instanceof ActivityNodeExitEvent);
		exit_initial = (ActivityNodeExitEvent)eventlist.get(2);
		assertTrue(eventlist.get(3) instanceof ActivityNodeEntryEvent);
		entry_structurednode = (ActivityNodeEntryEvent)eventlist.get(3);
		assertTrue(eventlist.get(4) instanceof ActivityNodeExitEvent);
		exit_structurednode = (ActivityNodeExitEvent)eventlist.get(4);				
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		entry_final = (ActivityNodeEntryEvent)eventlist.get(5);
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		exit_final = (ActivityNodeExitEvent)eventlist.get(6);				
		assertTrue(eventlist.get(7) instanceof ActivityExitEvent);
		exit_activity = (ActivityExitEvent)eventlist.get(7);
		
		assertTrue(checkActivityEntryEvent(entry_activity, activity));
		assertTrue(checkActivityExitEvent(exit_activity, activity, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_initial, testactivity.initial, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode, testactivity.structurednode, entry_activity));		
		assertTrue(checkActivityNodeEntryEvent(entry_final, testactivity.final_, entry_activity));
		assertTrue(checkActivityNodeExitEvent(exit_initial, testactivity.initial, entry_initial));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode, testactivity.structurednode, entry_structurednode));		
		assertTrue(checkActivityNodeExitEvent(exit_final, testactivity.final_, entry_final));		
	}
	
	@Test
	public void testStructuredActivityNode2_empty_executestepwise() {
		TestActivityFactory factory = new TestActivityFactory();
		TestActivityFactory.StructuredActivityNodeTestActivity2 testactivity = factory.new StructuredActivityNodeTestActivity2();
		Activity activity = testactivity.activity;
		
		// execute activity
		ExecutionContext.getInstance().executeStepwise(activity, null, null);		
		int executionID = ((ActivityEntryEvent)eventlist.get(0)).getActivityExecutionID();
		ExecutionContext.getInstance().nextStep(executionID);
		ExecutionContext.getInstance().nextStep(executionID);
		ExecutionContext.getInstance().nextStep(executionID);
		
		// check events
		assertEquals(11, eventlist.size());	
		
		ActivityEntryEvent entry_activity;
		ActivityExitEvent exit_activity;
		ActivityNodeEntryEvent entry_initial, entry_structurednode, entry_final;
		ActivityNodeExitEvent exit_initial, exit_structurednode, exit_final;
		SuspendEvent suspend_activity, suspend_initial, suspend_structurednode;
				
		assertTrue(eventlist.get(0) instanceof ActivityEntryEvent);
		entry_activity = (ActivityEntryEvent)eventlist.get(0);		
		assertTrue(eventlist.get(1) instanceof SuspendEvent);
		suspend_activity = (SuspendEvent)eventlist.get(1);
		assertTrue(eventlist.get(2) instanceof ActivityNodeEntryEvent);
		entry_initial = (ActivityNodeEntryEvent)eventlist.get(2);
		assertTrue(eventlist.get(3) instanceof ActivityNodeExitEvent);
		exit_initial = (ActivityNodeExitEvent)eventlist.get(3);
		assertTrue(eventlist.get(4) instanceof SuspendEvent);
		suspend_initial = (SuspendEvent)eventlist.get(4);
		assertTrue(eventlist.get(5) instanceof ActivityNodeEntryEvent);
		entry_structurednode = (ActivityNodeEntryEvent)eventlist.get(5);
		assertTrue(eventlist.get(6) instanceof ActivityNodeExitEvent);
		exit_structurednode = (ActivityNodeExitEvent)eventlist.get(6);
		assertTrue(eventlist.get(7) instanceof SuspendEvent);
		suspend_structurednode = (SuspendEvent)eventlist.get(7);		
		assertTrue(eventlist.get(8) instanceof ActivityNodeEntryEvent);
		entry_final = (ActivityNodeEntryEvent)eventlist.get(8);
		assertTrue(eventlist.get(9) instanceof ActivityNodeExitEvent);
		exit_final = (ActivityNodeExitEvent)eventlist.get(9);				
		assertTrue(eventlist.get(10) instanceof ActivityExitEvent);
		exit_activity = (ActivityExitEvent)eventlist.get(10);
		
		assertTrue(checkActivityEntryEvent(entry_activity, activity));
		assertTrue(checkActivityExitEvent(exit_activity, activity, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_initial, testactivity.initial, entry_activity));
		assertTrue(checkActivityNodeEntryEvent(entry_structurednode, testactivity.structurednode, entry_activity));		
		assertTrue(checkActivityNodeEntryEvent(entry_final, testactivity.final_, entry_activity));
		assertTrue(checkActivityNodeExitEvent(exit_initial, testactivity.initial, entry_initial));
		assertTrue(checkActivityNodeExitEvent(exit_structurednode, testactivity.structurednode, entry_structurednode));		
		assertTrue(checkActivityNodeExitEvent(exit_final, testactivity.final_, entry_final));		
		assertTrue(checkSuspendEvent(suspend_activity, activity, entry_activity, testactivity.initial));
		assertTrue(checkSuspendEvent(suspend_initial, testactivity.initial, entry_activity, testactivity.structurednode));
		assertTrue(checkSuspendEvent(suspend_structurednode, testactivity.structurednode, entry_activity, testactivity.final_));
	}

	private boolean checkStructuredActivityNode1Output(ParameterValueList outvalues, TestActivityFactory.StructuredActivityNodeTestActivity1 testactivity) {
		if(outvalues.size() != 1) {
			return false;
		}
		if(outvalues.get(0).values.size() != 2) {
			return false;
		}
		if(outvalues.get(0).values.get(0) != testactivity.o1) {
			return false;
		}
		if(outvalues.get(0).values.get(1) != testactivity.o2) {
			return false;
		}
		Object_ o1_out = (Object_)outvalues.get(0).values.get(0);
		Object_ o2_out = (Object_)outvalues.get(0).values.get(1);
		if(!((StringValue)o1_out.featureValues.get(0).values.get(0)).value.equals(testactivity.string1.value)) {
			return false;
		}
		if(!((StringValue)o2_out.featureValues.get(0).values.get(0)).value.equals(testactivity.string2.value)) {
			return false;
		}
		return true;
	}
	
	private boolean checkStructuredActivityNode3Output(ParameterValueList outvalues, TestActivityFactory.StructuredActivityNodeTestActivity3 testactivity) {
		if(outvalues.size() != 1) {
			return false;
		}
		if(outvalues.get(0).values.size() != 2) {
			return false;
		}
		if(!(outvalues.get(0).values.get(0) instanceof Reference)) {
			return false;
		}
		if(!(outvalues.get(0).values.get(1) instanceof Reference)) {
			return false;
		}
		Object_ o1_out = ((Reference)outvalues.get(0).values.get(0)).referent;
		Object_ o2_out = ((Reference)outvalues.get(0).values.get(1)).referent;
		if(!((StringValue)o1_out.featureValues.get(0).values.get(0)).value.equals(testactivity.string1.value)) {
			return false;
		}
		if(!((StringValue)o2_out.featureValues.get(0).values.get(0)).value.equals(testactivity.string2.value)) {
			return false;
		}
		return true;
	}
	
	private boolean checkActivityExitEvent(ActivityExitEvent event,	Activity activity, ActivityEntryEvent parentevent) {
		if(!event.getActivity().equals(activity)) {
			return false;
		}
		if(!event.getParent().equals(parentevent)) {
			return false;
		}
		return true;
	}

	private boolean checkActivityNodeExitEvent(ActivityNodeExitEvent event,	ActivityNode node, Event parentevent) {
		if(!event.getNode().equals(node)) {
			return false;
		}
		if(!event.getParent().equals(parentevent)) {
			return false;
		}
		return true;
	}

	private boolean checkActivityNodeEntryEvent(ActivityNodeEntryEvent event, ActivityNode node, Event parentevent) {
		if(!event.getNode().equals(node)) {
			return false;
		}
		if(!event.getParent().equals(parentevent)) {
			return false;
		}
		return true;
	}

	private boolean checkActivityEntryEvent(ActivityEntryEvent event, Activity activity) {
		if(!event.getActivity().equals(activity)) {
			return false;
		}
		if(event.getParent() != null) {
			return false;
		}
		return true;
	}
	
	private boolean checkSuspendEvent(SuspendEvent event, Element location, Event parentevent, ActivityNode... enablednodes) {
		if(!event.getLocation().equals(location)) {
			return false;
		}
		if(!event.getParent().equals(parentevent)) {
			return false;
		}
		if(!event.getNewEnabledNodes().containsAll(Arrays.asList(enablednodes))) {
			return false;
		}
		return true;
	}

	@Override
	public void notify(Event event) {
		if(!(event instanceof ExtensionalValueEvent)) {
			eventlist.add(event);
			System.err.println(event);
		}		
	}
	
}