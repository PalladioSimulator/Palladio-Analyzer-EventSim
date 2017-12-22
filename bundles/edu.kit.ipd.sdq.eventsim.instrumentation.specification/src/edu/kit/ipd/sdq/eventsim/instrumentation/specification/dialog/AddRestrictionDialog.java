package edu.kit.ipd.sdq.eventsim.instrumentation.specification.dialog;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUIFactory;

public class AddRestrictionDialog<I extends Instrumentable> extends TitleAreaDialog {

	private final Class<I> instrumentableType;
	private List list;

	private java.util.List<Class<? extends InstrumentableRestriction<I>>> restrictionTypes = new ArrayList<>();
	private Class<? extends InstrumentableRestriction<I>> selectedRestrictionType;

	public AddRestrictionDialog(Shell parentShell, Class<I> instrumentableType) {
		super(parentShell);
		this.instrumentableType = instrumentableType;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Add Restriction");
		setMessage("Please choose the type of restriction.", IMessageProvider.INFORMATION);
		getButton(Window.OK).setText("Create");
		getButton(Window.OK).setEnabled(false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new FormLayout());

		list = new List(container, SWT.BORDER);
		FormData fd_list = new FormData();
		fd_list.bottom = new FormAttachment(100, -10);
		fd_list.top = new FormAttachment(0, 10);
		fd_list.left = new FormAttachment(0, 10);
		fd_list.right = new FormAttachment(100, -10);
		list.setLayoutData(fd_list);

		for (Class<? extends InstrumentableRestriction<I>> restrictionType : RestrictionUIFactory
				.getAllRestrictionTypesFor(instrumentableType)) {
			Restriction annotation = restrictionType.getAnnotation(Restriction.class);
			list.add(annotation.name());
			restrictionTypes.add(restrictionType);
		}

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (list.getSelectionCount() > 0) {
					getButton(Window.OK).setEnabled(true);
				} else {
					getButton(Window.OK).setEnabled(false);
				}
			}
		});

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				buttonPressed(Window.OK);
			}
		});

		return container;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == Window.OK) {
			selectedRestrictionType = restrictionTypes.get(list.getSelectionIndex());
		}

		super.buttonPressed(buttonId);
	}

	public Class<? extends InstrumentableRestriction<I>> getSelectedRestrictionType() {
		return selectedRestrictionType;
	}

}
