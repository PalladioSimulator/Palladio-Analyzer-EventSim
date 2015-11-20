package edu.kit.ipd.sdq.eventsim.middleware.components;

public class RequiredRole<T> {

	private Class<T> type;

	private ProvidedRole<? extends T> providedRole;

	private WiringListener<T> bindingListener;

	public RequiredRole(Class<T> requiredType) {
		this(requiredType, null);
	}

	/**
	 * Requires the specified type. Once this required role is wired to a {@link ProvidedRole}, the specified
	 * {@code listener} is notified.
	 * 
	 * @param type
	 *            the type required by this role, usually an interface
	 * @param listener
	 *            the listener to be informed when this role is bound to a provided role
	 */
	public RequiredRole(Class<T> type, WiringListener<T> listener) {
		this.type = type;
		this.bindingListener = listener;
	}

	/**
	 * Wires this required role to the specified provided role.
	 * 
	 * @param providedRole
	 *            the provided role to be bound to this required role
	 */
	public void wire(ProvidedRole<? extends T> providedRole) {
		if (providedRole == null) {
			throw new IllegalArgumentException("Provided role may not be null");
		}
		this.providedRole = providedRole;

		// notify listener if any
		if (bindingListener != null) {
			bindingListener.notify(providedRole.getInstance());
		}
	}

	public T getService() throws UnboundRequiredRoleAccessException {
		if (providedRole == null) {
			throw new UnboundRequiredRoleAccessException(this);
		}
		return providedRole.getInstance();
	}

	/**
	 * @return the type required by this role.
	 */
	public Class<T> getRequiredType() {
		return type;
	}

}
