package edu.kit.ipd.sdq.eventsim.instrumentation.restrictions;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.IRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionDialog;

/**
 * 
 * @author Henning Schulz
 *
 * @param <I>
 *            the handled type of {@link Instrumentable}
 * @param <R>
 *            the restriction type
 * @param <E>
 *            the type of element to be selecteds
 */
public abstract class SingleElementsRestrictionUI<I extends Instrumentable, R extends InstrumentableRestriction<I>, E>
		implements IRestrictionUI<I> {

	private SetBasedInstrumentationRule<?, I> rule;
	private R restriction;
	private List<E> entities;

	private RestrictionDialog<I> dialog;

	private E selected;
	private boolean changed = false;

	@SuppressWarnings("unchecked")
	@Override
	public void init(InstrumentableRestriction<I> restriction) {
		setRule();
		this.restriction = (R) restriction;
		initialize(this.restriction);
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

		dialog.addOnCreatedListener(() -> dialog.setMessage(getDescriptionMessage(), IMessageProvider.INFORMATION));

		if (selected == null) {
			dialog.addOnCreatedListener(() -> dialog.enableFinish(false));
		}

		return dialog;
	}

	protected abstract String getDescriptionMessage();

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
			if (!elementToID(selected).equals(getInitallySelectedEntityId())) {
				setIdToRestriction(elementToID(selected));
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

		String initSelectedId = getInitallySelectedEntityId();
		if (initSelectedId != null) {
			for (E element : entities) {
				if (initSelectedId.equals(elementToID(element))) {
					selected = element;
				}
			}
		}

		Text filterText = new Text(container, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 10);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(100, -10);
		filterText.setLayoutData(fd_text);

		ListViewer viewer = new ListViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_list = new FormData();
		fd_list.bottom = new FormAttachment(100, -10);
		fd_list.right = new FormAttachment(100, -10);
		fd_list.top = new FormAttachment(filterText, 10, SWT.BOTTOM);
		fd_list.left = new FormAttachment(0, 10);
		viewer.getList().setLayoutData(fd_list);

		viewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<E>) inputElement).toArray();
			}
		});

		viewer.setInput(entities);

		viewer.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return elementToName((E) element);
			}
		});

		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				viewer.resetFilters();
				viewer.addFilter(new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						@SuppressWarnings("unchecked")
						String name = elementToName((E) element).toLowerCase();
						return name.contains(filterText.getText().toLowerCase());
					}
				});

				if (viewer.getStructuredSelection().isEmpty() && selected != null) {
					viewer.setSelection(new StructuredSelection(selected), true);
				}
			}
		});

		if (selected != null) {
			viewer.setSelection(new StructuredSelection(selected), true);
			dialog.addOnCreatedListener(() -> dialog.enableFinish(true));
		}

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!viewer.getStructuredSelection().isEmpty()) {
					dialog.enableFinish(true);
					selected = (E) viewer.getStructuredSelection().getFirstElement();
				}
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (!viewer.getStructuredSelection().isEmpty()) {
					dialog.close(Window.OK);
				}
			}
		});

		return container;
	}

	protected abstract String elementToName(E element);

	protected abstract String elementToID(E element);

	protected abstract List<E> getAllEntities();

}
