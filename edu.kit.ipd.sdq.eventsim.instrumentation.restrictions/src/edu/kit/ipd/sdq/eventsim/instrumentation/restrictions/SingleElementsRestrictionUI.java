package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.IRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionDialog;

public abstract class SingleElementsRestrictionUI<I extends Instrumentable, R extends InstrumentableRestriction<I>, E extends Entity>
		implements IRestrictionUI<I> {

	private SetBasedInstrumentationRule<?, I> rule;
	private R restriction;
	private List<E> entities;

	private RestrictionDialog<I> dialog;

	private String selected;
	private boolean changed = false;

	@SuppressWarnings("unchecked")
	@Override
	public void init(InstrumentableRestriction<I> restriction) {
		setRule();
		this.restriction = (R) restriction;
		initialize(this.restriction);
		this.selected = getInitallySelectedEntityId();
	}

	@Override
	public void init() {
		setRule();
		this.restriction = createNewRestriction();
		initialize(this.restriction);
	}

	protected abstract R createNewRestriction();

	protected abstract String getInitallySelectedEntityId();

	protected abstract void initialize(R restriction);

	@SuppressWarnings("unchecked")
	private void setRule() {
		this.rule = (SetBasedInstrumentationRule<?, I>) InstrumentationDescriptionEditor.getActive().getActiveRule();
	}

	@Override
	public Dialog getAsDialog(Shell parentShell) {
		dialog = new RestrictionDialog<>(parentShell, this, true);
		dialog.addOnCloseListener(this::onDialogClose);

		dialog.addOnCreatedListener(() -> dialog.setMessage("Please select the entity you want to restrict to.",
				IMessageProvider.INFORMATION));

		if (selected == null) {
			dialog.addOnCreatedListener(() -> dialog.enableFinish(false));
		}

		return dialog;
	}

	@Override
	public InstrumentableRestriction<I> getRestriction() {
		return restriction;
	}

	@Override
	public boolean restrictionChanged() {
		return changed;
	}

	private void onDialogClose() {
		if (!dialog.isAborted() && selected != null) {
			if (!selected.equals(getInitallySelectedEntityId())) {
				setIdToRestriction(selected);
				changed = true;
			}
		}
	}

	protected abstract void setIdToRestriction(String id);

	protected abstract List<I> getInstrumentablesForEntity(E entity);

	private boolean includeEntity(E entity) {
		for (I instrumentable : getInstrumentablesForEntity(entity)) {
			if (includeInstrumentable(instrumentable)) {
				return true;
			}
		}

		return false;
	}

	private boolean includeInstrumentable(I instrumentable) {
		boolean included = true;
		for (InstrumentableRestriction<I> r : rule.getRestrictions()) {
			if (!restriction.equals(r)) {
				included = included && !r.exclude(instrumentable);
			}
		}

		return included;
	}

	@Override
	public Control createUIArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new FormLayout());

		entities = getAllEntities().stream().filter(this::includeEntity).collect(Collectors.toList());

		org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(container,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_list = new FormData();
		fd_list.bottom = new FormAttachment(100, -10);
		fd_list.right = new FormAttachment(100, -10);
		fd_list.top = new FormAttachment(0, 10);
		fd_list.left = new FormAttachment(0, 10);
		list.setLayoutData(fd_list);

		int i = 0;
		for (E entity : entities) {
			list.add(entity.getEntityName() + " (" + entity.getId() + ")");

			if (selected != null && selected.equals(entity.getId())) {
				list.setSelection(i);
			}

			i++;
		}

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.enableFinish(true);
				selected = entities.get(list.getSelectionIndex()).getId();
			}
		});

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (list.getSelectionCount() > 0)
					dialog.close(Window.OK);
			}
		});

		return container;
	}

	protected abstract List<E> getAllEntities();

}
