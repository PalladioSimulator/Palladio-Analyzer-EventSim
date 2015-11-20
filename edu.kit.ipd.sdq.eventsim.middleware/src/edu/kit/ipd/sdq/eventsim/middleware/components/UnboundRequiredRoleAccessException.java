package edu.kit.ipd.sdq.eventsim.middleware.components;

public class UnboundRequiredRoleAccessException extends RuntimeException {

	private static final long serialVersionUID = 4167241608687312897L;

	public UnboundRequiredRoleAccessException(RequiredRole<?> role) {
		super("Tried to access required service " + role.getRequiredType()
				+ ", but this required role has not been wired to a provided role.");
	}

}
