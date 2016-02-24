package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.dialog.AddRuleDialog;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.DescriptionToXmlParser;

/**
 * An editor for {@link InstrumentationDescription}s. For editing specific
 * {@link InstrumentationRule}s, corresponding {@link InstrumentationRuleUI}s
 * are used.
 * 
 * @author Henning Schulz
 *
 */
public class InstrumentationDescriptionEditor extends EditorPart {
	public InstrumentationDescriptionEditor() {
	}

	private static InstrumentationDescriptionEditor active;

	public static final String ID = "edu.kit.ipd.sdq.eventsim.instrumentation.specification.ideditor";

	private InstrumentationDescription description;
	private FileEditorInput input;
	private DescriptionToXmlParser parser = new DescriptionToXmlParser();

	private PCMModel pcm;

	private boolean dirty = false;
	private Text ruleNameField;

	private Composite ruleContainer;
	private InstrumentationRuleUI currentRuleView;
	private List ruleList;

	private Map<Integer, Font> createdFonts = new HashMap<>();

	public static InstrumentationDescriptionEditor getActive() {
		return active;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof FileEditorInput)) {
			throw new RuntimeException("Illegal input: " + input.getClass());
		}

		this.input = (FileEditorInput) input;
		setSite(site);
		setInput(input);
		setPartName(this.input.getPath().lastSegment());

		try {
			this.input.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
			description = parser.readFromFile(this.input.getPath().toString());
		} catch (JAXBException e) {
			throw new PartInitException("Could not read " + this.input.getPath());
		} catch (CoreException e) {
			throw new PartInitException("Could not refresh input file.");
		}

		loadPcm(false);
	}

	public Font getOrCreateFont(Label label, int style) {
		Font font = createdFonts.get(style);

		if (font == null) {
			FontDescriptor descriptor = FontDescriptor.createFrom(label.getFont()).setStyle(style);
			font = descriptor.createFont(label.getDisplay());
			createdFonts.put(style, font);
		}

		return font;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FormLayout());
		Color white = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		parent.setBackground(white);

		Label lblInstrumentationRules = new Label(parent, SWT.NONE);
		lblInstrumentationRules.setFont(getOrCreateFont(lblInstrumentationRules, SWT.BOLD));
		FormData fd_lblInstrumentationRules = new FormData();
		fd_lblInstrumentationRules.right = new FormAttachment(0, 163);
		fd_lblInstrumentationRules.top = new FormAttachment(0, 10);
		fd_lblInstrumentationRules.left = new FormAttachment(0, 10);
		lblInstrumentationRules.setLayoutData(fd_lblInstrumentationRules);
		lblInstrumentationRules.setText("Instrumentation Rules");

		ruleList = new List(parent, SWT.BORDER | SWT.V_SCROLL);
		FormData fd_list = new FormData();
		fd_list.bottom = new FormAttachment(100, -10);
		fd_list.right = new FormAttachment(28, -9);
		fd_list.top = new FormAttachment(0, 31);
		fd_list.left = new FormAttachment(0, 10);
		ruleList.setLayoutData(fd_list);

		Button btnRemove = new Button(parent, SWT.NONE);
		FormData fd_btnRemove = new FormData();
		fd_btnRemove.right = new FormAttachment(ruleList, 86, SWT.RIGHT);
		fd_btnRemove.top = new FormAttachment(0, 62);
		fd_btnRemove.left = new FormAttachment(ruleList, 16, SWT.RIGHT);
		btnRemove.setLayoutData(fd_btnRemove);
		btnRemove.setText("Remove");
		btnRemove.setEnabled(false);
		final InstrumentationDescriptionEditor outer = this;
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = ruleList.getSelectionIndex();
				if (MessageDialog.openQuestion(outer.getSite().getWorkbenchWindow().getShell(), "Remove",
						"Are you sure to delete the instrumentation rule \""
								+ description.getRules().get(index).getName() + "\"?")) {
					description.removeRule(index);
					ruleList.remove(index);
					currentRuleView.dispose();
					currentRuleView = null;
					ruleNameField.setText("");
					ruleNameField.setEnabled(false);
					setDirty(true);
				}
			}
		});

		Button btnAdd = new Button(parent, SWT.NONE);
		FormData fd_btnAdd = new FormData();
		fd_btnAdd.right = new FormAttachment(ruleList, 86, SWT.RIGHT);
		fd_btnAdd.top = new FormAttachment(0, 31);
		fd_btnAdd.left = new FormAttachment(ruleList, 16, SWT.RIGHT);
		btnAdd.setLayoutData(fd_btnAdd);
		btnAdd.setText("Add");
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddRuleDialog dialog = new AddRuleDialog(outer.getSite().getWorkbenchWindow().getShell());
				dialog.create();
				if (dialog.open() == Window.OK) {
					InstrumentationRule created = dialog.getCreatedRule();
					description.addRule(created);
					ruleList.add(created.getName());
					ruleList.setSelection(ruleList.getItemCount() - 1);
					onRuleSelected(created);
					setDirty(true);
					btnRemove.setEnabled(true);
				}
			}
		});

		ruleList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ruleList.getSelectionCount() > 0) {
					btnRemove.setEnabled(true);
				} else {
					btnRemove.setEnabled(false);
				}
			}
		});

		Label lblDetails = new Label(parent, SWT.NONE);
		lblDetails.setFont(getOrCreateFont(lblDetails, SWT.BOLD));
		FormData fd_lblDetails = new FormData();
		fd_lblDetails.right = new FormAttachment(btnAdd, 157);
		fd_lblDetails.left = new FormAttachment(btnAdd, 30);
		fd_lblDetails.top = new FormAttachment(lblInstrumentationRules, 0, SWT.TOP);
		lblDetails.setLayoutData(fd_lblDetails);
		lblDetails.setText("Details of: ");

		ruleNameField = new Text(parent, SWT.BORDER);
		ruleNameField.setText("");
		FormData fd_txtAname = new FormData();
		fd_txtAname.left = new FormAttachment(lblDetails, 5, SWT.RIGHT);
		fd_txtAname.right = new FormAttachment(100, -10);
		fd_txtAname.top = new FormAttachment(lblInstrumentationRules, 0, SWT.CENTER);
		ruleNameField.setLayoutData(fd_txtAname);
		ruleNameField.setEnabled(false);

		ruleNameField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (currentRuleView != null && !ruleNameField.getText().equals(currentRuleView.getRule().getName()))
					onRuleNameChanged(currentRuleView.getRule(), ruleNameField.getText());
			}
		});

		ruleContainer = new Composite(parent, SWT.BORDER);
		ruleContainer.setLayout(new FillLayout());
		FormData fd_composite = new FormData();
		fd_composite.left = new FormAttachment(lblDetails, 0, SWT.LEFT);
		fd_composite.right = new FormAttachment(100, -10);
		fd_composite.bottom = new FormAttachment(100, -10);
		fd_composite.top = new FormAttachment(0, 31);
		ruleContainer.setLayoutData(fd_composite);

		populateRuleList();
	}

	private void populateRuleList() {
		for (InstrumentationRule rule : description.getRules()) {
			ruleList.add(rule.getName());
		}

		ruleList.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = ruleList.getSelectionIndex();
				if (idx >= 0)
					onRuleSelected(description.getRules().get(idx));
			}
		});
	}

	private void onRuleSelected(InstrumentationRule rule) {
		ruleNameField.setEnabled(true);

		if (currentRuleView != null) {
			currentRuleView.dispose();
		}

		currentRuleView = InstRuleUIFactory.getInstance().createView(rule, ruleContainer, SWT.NONE);
		currentRuleView.init();
		currentRuleView.addDirtyListener(new DirtyListener() {
			@Override
			public void onDirty() {
				setDirty(true);
			}
		});
		ruleContainer.layout(true);
		ruleNameField.setText(rule.getName());
		ruleNameField.getParent().layout(true);
	}

	private void onRuleNameChanged(InstrumentationRule rule, String newName) {
		setDirty(true);

		// TODO: Handling, if newName is ""
		if (newName != null && !newName.equals("")) {
			rule.setName(newName);

			for (int i = 0; i < description.getRules().size(); i++) {
				if (rule.equals(description.getRules().get(i))) {
					ruleList.setItem(i, newName);
					break;
				}
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			parser.saveToFile(description, input.getPath().toString());
			setDirty(false);
			this.input.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (JAXBException e) {
			monitor.setCanceled(true);
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	private void setDirty(boolean dirty) {
		if (this.dirty != dirty) {
			this.dirty = dirty;
			firePropertyChange(PROP_DIRTY);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFocus() {
		synchronized (ID) {
			active = this;
		}
	}

	@Override
	public void dispose() {
		for (Entry<Integer, Font> entry : createdFonts.entrySet()) {
			entry.getValue().dispose();
		}

		super.dispose();
	}

	public InstrumentationDescription getDescription() {
		return description;
	}

	public InstrumentationRule getActiveRule() {
		return currentRuleView.getRule();
	}

	public PCMModel getPcm() {
		return pcm;
	}

	public void reloadPcm() {
		loadPcm(true);
	}

	private void loadPcm(boolean setDirty) {
		if (!description.getAllocationUri().equals(InstrumentationDescription.UNDEFINED)
				&& !description.getUsagemodelUri().equals(InstrumentationDescription.UNDEFINED)) {
			pcm = PCMModel.loadFromUri(URI.createURI(description.getUsagemodelUri()),
					URI.createURI(description.getAllocationUri()));
			setDirty(setDirty);
		}
	}

}
