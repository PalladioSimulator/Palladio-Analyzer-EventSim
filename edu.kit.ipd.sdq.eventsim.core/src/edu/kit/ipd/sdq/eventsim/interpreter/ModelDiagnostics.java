package edu.kit.ipd.sdq.eventsim.interpreter;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.UnexpectedModelStructureException;
import edu.kit.ipd.sdq.eventsim.util.PCMEntityHelper;

public abstract class ModelDiagnostics {

	public enum DiagnosticsMode {

		/**
		 * Throw a {@link UnexpectedModelStructureException} when a {@link ITraversalStrategy} detects an error in the
		 * model under simulation.
		 */
		THROW_EXCEPTION,

		/**
		 * Log warning message and try continuing the traversal when a {@link ITraversalStrategy} detects an error in
		 * the model under simulation.
		 */
		LOG_WARNING_AND_CONTINUE
	}

	private static final Logger log = Logger.getLogger(ModelDiagnostics.class);

	private DiagnosticsMode mode;

	public ModelDiagnostics(DiagnosticsMode mode) {
		this.mode = mode;
	}

	protected void handle(String baseMessage, Entity entity) {
		String message = baseMessage + ": " + PCMEntityHelper.toString(entity);
		if (mode == DiagnosticsMode.THROW_EXCEPTION) {
			throw new UnexpectedModelStructureException(message);
		} else if (mode == DiagnosticsMode.LOG_WARNING_AND_CONTINUE) {
			message += ". Ignoring this " + entity.eClass().getName() + " and continuing with successor.";
			log.warn(message);
		}
	}

}
