/**
 */
package org.modelexecution.fuml.Syntax.Actions.IntermediateActions.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.modelexecution.fuml.Syntax.Actions.BasicActions.InputPin;

import org.modelexecution.fuml.Syntax.Actions.BasicActions.impl.ActionImpl;

import org.modelexecution.fuml.Syntax.Actions.IntermediateActions.DestroyObjectAction;
import org.modelexecution.fuml.Syntax.Actions.IntermediateActions.IntermediateActionsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Destroy Object Action</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.modelexecution.fuml.Syntax.Actions.IntermediateActions.impl.DestroyObjectActionImpl#getIsDestroyLinks <em>Is Destroy Links</em>}</li>
 *   <li>{@link org.modelexecution.fuml.Syntax.Actions.IntermediateActions.impl.DestroyObjectActionImpl#getIsDestroyOwnedObjects <em>Is Destroy Owned Objects</em>}</li>
 *   <li>{@link org.modelexecution.fuml.Syntax.Actions.IntermediateActions.impl.DestroyObjectActionImpl#getTarget <em>Target</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DestroyObjectActionImpl extends ActionImpl implements DestroyObjectAction {
	/**
	 * The default value of the '{@link #getIsDestroyLinks() <em>Is Destroy Links</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIsDestroyLinks()
	 * @generated
	 * @ordered
	 */
	protected static final Object IS_DESTROY_LINKS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIsDestroyLinks() <em>Is Destroy Links</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIsDestroyLinks()
	 * @generated
	 * @ordered
	 */
	protected Object isDestroyLinks = IS_DESTROY_LINKS_EDEFAULT;

	/**
	 * The default value of the '{@link #getIsDestroyOwnedObjects() <em>Is Destroy Owned Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIsDestroyOwnedObjects()
	 * @generated
	 * @ordered
	 */
	protected static final Object IS_DESTROY_OWNED_OBJECTS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIsDestroyOwnedObjects() <em>Is Destroy Owned Objects</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIsDestroyOwnedObjects()
	 * @generated
	 * @ordered
	 */
	protected Object isDestroyOwnedObjects = IS_DESTROY_OWNED_OBJECTS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTarget() <em>Target</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTarget()
	 * @generated
	 * @ordered
	 */
	protected InputPin target;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DestroyObjectActionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IntermediateActionsPackage.Literals.DESTROY_OBJECT_ACTION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getIsDestroyLinks() {
		return isDestroyLinks;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsDestroyLinks(Object newIsDestroyLinks) {
		Object oldIsDestroyLinks = isDestroyLinks;
		isDestroyLinks = newIsDestroyLinks;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_LINKS, oldIsDestroyLinks, isDestroyLinks));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getIsDestroyOwnedObjects() {
		return isDestroyOwnedObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsDestroyOwnedObjects(Object newIsDestroyOwnedObjects) {
		Object oldIsDestroyOwnedObjects = isDestroyOwnedObjects;
		isDestroyOwnedObjects = newIsDestroyOwnedObjects;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_OWNED_OBJECTS, oldIsDestroyOwnedObjects, isDestroyOwnedObjects));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public InputPin getTarget() {
		return target;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTarget(InputPin newTarget, NotificationChain msgs) {
		InputPin oldTarget = target;
		target = newTarget;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET, oldTarget, newTarget);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTarget(InputPin newTarget) {
		if (newTarget != target) {
			NotificationChain msgs = null;
			if (target != null)
				msgs = ((InternalEObject)target).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET, null, msgs);
			if (newTarget != null)
				msgs = ((InternalEObject)newTarget).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET, null, msgs);
			msgs = basicSetTarget(newTarget, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET, newTarget, newTarget));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET:
				return basicSetTarget(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_LINKS:
				return getIsDestroyLinks();
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_OWNED_OBJECTS:
				return getIsDestroyOwnedObjects();
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET:
				return getTarget();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_LINKS:
				setIsDestroyLinks(newValue);
				return;
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_OWNED_OBJECTS:
				setIsDestroyOwnedObjects(newValue);
				return;
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET:
				setTarget((InputPin)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_LINKS:
				setIsDestroyLinks(IS_DESTROY_LINKS_EDEFAULT);
				return;
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_OWNED_OBJECTS:
				setIsDestroyOwnedObjects(IS_DESTROY_OWNED_OBJECTS_EDEFAULT);
				return;
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET:
				setTarget((InputPin)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_LINKS:
				return IS_DESTROY_LINKS_EDEFAULT == null ? isDestroyLinks != null : !IS_DESTROY_LINKS_EDEFAULT.equals(isDestroyLinks);
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__IS_DESTROY_OWNED_OBJECTS:
				return IS_DESTROY_OWNED_OBJECTS_EDEFAULT == null ? isDestroyOwnedObjects != null : !IS_DESTROY_OWNED_OBJECTS_EDEFAULT.equals(isDestroyOwnedObjects);
			case IntermediateActionsPackage.DESTROY_OBJECT_ACTION__TARGET:
				return target != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (isDestroyLinks: ");
		result.append(isDestroyLinks);
		result.append(", isDestroyOwnedObjects: ");
		result.append(isDestroyOwnedObjects);
		result.append(')');
		return result.toString();
	}

} //DestroyObjectActionImpl