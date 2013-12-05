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
import java.util.List;

import org.modeldriven.alf.fuml.impl.environment.AlfOpaqueBehaviorExecution;

import fUML.Semantics.Actions.BasicActions.ActionActivation;
import fUML.Semantics.Activities.CompleteStructuredActivities.StructuredActivityNodeActivation;
import fUML.Semantics.Activities.ExtraStructuredActivities.ExpansionActivationGroup;
import fUML.Semantics.Activities.IntermediateActivities.ActivityNodeActivation;
import fUML.Semantics.Activities.IntermediateActivities.TokenList;
import fUML.Semantics.Loci.LociL1.SemanticVisitor;
import fUML.Syntax.Actions.BasicActions.CallAction;
import fUML.Syntax.Actions.BasicActions.CallBehaviorAction;
import fUML.Syntax.Activities.IntermediateActivities.ActivityNode;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.Behavior;
import fUML.Syntax.CommonBehaviors.BasicBehaviors.OpaqueBehavior;

public class ActivityNodeExecutionStatus implements Comparable<ActivityNodeExecutionStatus>{

	protected ActivityExecutionStatus activityExecutionStatus = null;
	private int activationIndex = -1;
	private ActivityNodeActivation activityNodeActivation = null;
	private List<TokenList> waitingTokens = new ArrayList<TokenList>();

	public ActivityNodeExecutionStatus(ActivityExecutionStatus activityExecutionStatus, ActivityNodeActivation activityNodeActivation, int index) {
		this.activityExecutionStatus = activityExecutionStatus;
		this.activityNodeActivation = activityNodeActivation;
		this.activationIndex = index;
	}
		
	public int getIndex() {
		return activationIndex;
	}
	
	public void addWaitingTokens(TokenList tokens) {
		waitingTokens.add(tokens);
	}
	
	public TokenList removeWaitingTokens() {
		TokenList tokens = new TokenList();		
		if(waitingTokens.size() > 0) {
			tokens.addAll(waitingTokens.remove(0));
		}
		return tokens;
	}
	
	public ActivityNodeActivation getActivityNodeActivation() {			
		return this.activityNodeActivation;
	}
	
	public List<TokenList> getWaitingTokens() {
		return new ArrayList<TokenList>(waitingTokens);
	}

	public int compareTo(ActivityNodeExecutionStatus otherStatus) {
		if(activationIndex < otherStatus.getIndex()) {
			return -1;
		} else if(activationIndex > otherStatus.getIndex()) {
			return 1;
		}
		return 0;
	}
	
	public void handleEndOfExecution() {
		ExecutionContext.getInstance().eventHandler
				.handleActivityNodeExit(activityNodeActivation);

		// if(!(activityNodeActivation.node instanceof CallAction)) {
		// updateStatusOfContainingStructuredActivityNode();
		// }

		ActivityNode node = activityNodeActivation.node;

		if (updateStructuredNode(node)) {
			updateStatusOfContainingStructuredActivityNode();
		}

		activityExecutionStatus.removeExecutingActivation(node);
	}

	private boolean updateStructuredNode(ActivityNode node) {
		boolean updateStructuredNode = false;		
		if(node instanceof CallAction) {
			if (node instanceof CallBehaviorAction) {
				Behavior behavior = ((CallBehaviorAction)node).behavior;
				if(behavior instanceof OpaqueBehavior && !isAlfBehavior(behavior)) {
					updateStructuredNode = true;
				}
			}
		} else {
			updateStructuredNode = true;
		}
		return updateStructuredNode;
	}
	
	private boolean isAlfBehavior(Behavior behavior) {
		if(behavior instanceof OpaqueBehavior) {
			OpaqueBehavior opaqueBehavior = (OpaqueBehavior) behavior;
			if(opaqueBehavior.language.size() > 0) {
				if(opaqueBehavior.language.get(0).equals(AlfOpaqueBehaviorExecution.ALF_LANGUAGE_NAME)) {
					return true;
				}
			}			
		}
		return false;
	}

	protected void checkIfCanFireAgain() {
		if(!(activityNodeActivation instanceof ActionActivation)) {
			return;
		}
		ActionActivation actionActivation = (ActionActivation)activityNodeActivation;
		SemanticVisitor._beginIsolation();
		boolean fireAgain = false;
		actionActivation.firing = false;
		TokenList incomingTokens = new TokenList();
		if (actionActivation.isReady()) {
			incomingTokens = actionActivation.takeOfferedTokens();
			fireAgain = incomingTokens.size() > 0;
			actionActivation.firing = actionActivation.isFiring() & fireAgain;
		}
		SemanticVisitor._endIsolation();

		if (fireAgain) {
			activityExecutionStatus.addEnabledActivation(actionActivation, incomingTokens);
		}
	}
	
	protected void updateStatusOfContainingStructuredActivityNode() {
		ActionActivation containingStructuredActivityNodeActivation = getContainingStructuredActivityNodeActivation();
		if(containingStructuredActivityNodeActivation != null) {
			ActivityNodeExecutionStatus containingStructuredNodeExecutionStatus = activityExecutionStatus.getExecutingActivityNodeExecutionStatus(containingStructuredActivityNodeActivation.node);
			if(containingStructuredNodeExecutionStatus instanceof ExpansionRegionExecutionStatus) {
				ExpansionRegionExecutionStatus expansionRegionExecutionStatus = (ExpansionRegionExecutionStatus)containingStructuredNodeExecutionStatus;
				expansionRegionExecutionStatus.updateStatus();
			} else if(containingStructuredNodeExecutionStatus instanceof ConditionalNodeExecutionStatus) {
				ConditionalNodeExecutionStatus conditionalNodeExecutionStatus = (ConditionalNodeExecutionStatus)containingStructuredNodeExecutionStatus;
				conditionalNodeExecutionStatus.updateStatus();
			} else if(containingStructuredNodeExecutionStatus instanceof LoopNodeExecutionStatus) {
				LoopNodeExecutionStatus loopNodeExecutionStatus = (LoopNodeExecutionStatus)containingStructuredNodeExecutionStatus;
				loopNodeExecutionStatus.updateStatus();
			} else {
				if (containingStructuredNodeExecutionStatus != null) {
					StructuredActivityNodeExecutionStatus structuredActivityNodeExecutionStatus = (StructuredActivityNodeExecutionStatus)containingStructuredNodeExecutionStatus;
					structuredActivityNodeExecutionStatus.updateStatus();
				} else {
					if(containingStructuredActivityNodeActivation.node.inStructuredNode != null) {
						StructuredActivityNodeExecutionStatus structuredActivityNodeExecutionStatus = (StructuredActivityNodeExecutionStatus)activityExecutionStatus.getExecutingActivityNodeExecutionStatus(containingStructuredActivityNodeActivation.node.inStructuredNode);
						structuredActivityNodeExecutionStatus.updateStatus();
					}
				}
			}
		}
	}
	
	private ActionActivation getContainingStructuredActivityNodeActivation() { // return type is ActionActivation because of ExpansionRegion
		if(activityNodeActivation.group == null) {
			return null;
		}
		
		if (activityNodeActivation.group instanceof ExpansionActivationGroup) {
			return ((ExpansionActivationGroup)activityNodeActivation.group).regionActivation;
		}
		
		if(activityNodeActivation.group.containingNodeActivation instanceof StructuredActivityNodeActivation) {
			StructuredActivityNodeActivation containingStructuredActivation = (StructuredActivityNodeActivation)activityNodeActivation.group.containingNodeActivation;
			return containingStructuredActivation;
		}	
		
		return null;
	}
	
}
