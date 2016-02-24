package edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;

/**
 * A UI for {@link InstrumentableRestriction}s. Either the
 * {@link IRestrictionUI#init() init()} method (for a new restriction) or the
 * {@link IRestrictionUI#init(InstrumentableRestriction)
 * init(InstrumentableRestriction)} (for an existing restriction) has to be
 * called after the UI is instantiated.
 * 
 * @author Henning Schulz
 *
 * @param <I>
 *            the type of {@link Instrumentable} the UI deals with
 */
public interface IRestrictionUI<I extends Instrumentable> {

	public static final String EXTENSION_POINT_ID = "edu.kit.ipd.sdq.eventsim.instrumentation.specification.restrictionUIs";

	void init(InstrumentableRestriction<I> restriction);

	void init();

	Dialog getAsDialog(Shell parentShell);

	InstrumentableRestriction<I> getRestriction();

	boolean restrictionChanged();

	Control createUIArea(Composite parent);

}
