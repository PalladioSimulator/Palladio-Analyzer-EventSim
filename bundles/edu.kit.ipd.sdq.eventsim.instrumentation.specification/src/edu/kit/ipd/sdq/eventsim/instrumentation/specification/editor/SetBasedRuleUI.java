package edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.dialog.AddRestrictionDialog;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities.SelectableCalculator;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities.SelectableProbe;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities.TypedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.CalculatorRepository;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.ProbeRepository;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.IRestrictionUI;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction.RestrictionUIFactory;

public abstract class SetBasedRuleUI<P, I extends Instrumentable, R extends SetBasedInstrumentationRule<P, I>>
		extends Composite implements InstrumentationRuleUI {

	private Table probesTable;
	private Table calculatorsTable;

	private org.eclipse.swt.widgets.List list;

	private List<SelectableProbe> probes;
	private List<SelectableCalculator> calculators;
	private Map<String, SelectableProbe> probesPerItem;
	private Map<String, SelectableCalculator> calculatorsPerItem;

	private final R rule;

	private final List<DirtyListener> dirtyListeners = new ArrayList<>();

	public SetBasedRuleUI(R rule, Composite parent, int style) {
		super(parent, style);
		this.rule = rule;

		TypedInstrumentationRule<P, P, P> typedRule = TypedInstrumentationRule.fromSetBasedRule(rule);
		probes = ProbeRepository.getProbesFor(typedRule).stream().map(p -> new SelectableProbe(p))
				.collect(Collectors.toList());
		calculators = CalculatorRepository.getCalculatorsFor(typedRule).stream()
				.map(c -> new SelectableCalculator(c)).collect(Collectors.toList());

		createView();
	}

	private void createView() {
		setLayout(new FormLayout());

		Label label = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(0, 10);
		fd_label.bottom = new FormAttachment(100, -10);
		fd_label.left = new FormAttachment(50, 0);
		label.setLayoutData(fd_label);

		Label lblScope = new Label(this, SWT.NONE);
		lblScope.setFont(InstrumentationDescriptionEditor.getActive().getOrCreateFont(lblScope, SWT.BOLD));
		FormData fd_lblScope = new FormData();
		fd_lblScope.top = new FormAttachment(0, 10);
		fd_lblScope.left = new FormAttachment(0, 10);
		lblScope.setLayoutData(fd_lblScope);
		lblScope.setText("Scope:");

		Label lblAll = new Label(this, SWT.NONE);
		FormData fd_lblExternalcallactions = new FormData();
		fd_lblExternalcallactions.top = new FormAttachment(lblScope, 6);
		fd_lblExternalcallactions.left = new FormAttachment(0, 10);
		lblAll.setLayoutData(fd_lblExternalcallactions);
		lblAll.setText("All");

		Label lblSupertype = new Label(this, SWT.NONE);
		lblSupertype.setAlignment(SWT.CENTER);
		lblSupertype.setFont(InstrumentationDescriptionEditor.getActive().getOrCreateFont(lblSupertype, SWT.ITALIC));
		FormData fd_lblSupertype = new FormData();
		fd_lblSupertype.top = new FormAttachment(lblAll, 10, SWT.BOTTOM);
		fd_lblSupertype.left = new FormAttachment(0, 10);
		fd_lblSupertype.right = new FormAttachment(50, -10);
		lblSupertype.setLayoutData(fd_lblSupertype);
		lblSupertype.setText(getScopeTypeName());

		Label lblIncludedByall = new Label(this, SWT.WRAP);
		FormData fd_lblIncludedByall = new FormData();
		fd_lblIncludedByall.top = new FormAttachment(lblSupertype, 13, SWT.BOTTOM);
		fd_lblIncludedByall.left = new FormAttachment(0, 10);
		fd_lblIncludedByall.right = new FormAttachment(50, -10);
		lblIncludedByall.setLayoutData(fd_lblIncludedByall);
		lblIncludedByall.setText("for which all of the following restrictions hold:");

		Label lblRestrictions = new Label(this, SWT.NONE);
		lblRestrictions
				.setFont(InstrumentationDescriptionEditor.getActive().getOrCreateFont(lblRestrictions, SWT.BOLD));
		FormData fd_lblRestrictions = new FormData();
		fd_lblRestrictions.top = new FormAttachment(lblIncludedByall, 20, SWT.BOTTOM);
		fd_lblRestrictions.left = new FormAttachment(lblScope, 0, SWT.LEFT);
		lblRestrictions.setLayoutData(fd_lblRestrictions);
		lblRestrictions.setText("Restrictions:");

		Button btnAdd = new Button(this, SWT.NONE);
		FormData fd_btnAdd = new FormData();
		fd_btnAdd.top = new FormAttachment(lblRestrictions, 14);
		fd_btnAdd.right = new FormAttachment(label, -10);
		fd_btnAdd.left = new FormAttachment(label, -80);
		btnAdd.setLayoutData(fd_btnAdd);
		btnAdd.setText("Add");

		Button btnRemove = new Button(this, SWT.NONE);
		FormData fd_btnRemove = new FormData();
		fd_btnRemove.top = new FormAttachment(btnAdd, 31, SWT.TOP);
		fd_btnRemove.right = new FormAttachment(label, -10);
		fd_btnRemove.left = new FormAttachment(label, -80);
		btnRemove.setLayoutData(fd_btnRemove);
		btnRemove.setText("Remove");
		btnRemove.setEnabled(false);

		Button btnEdit = new Button(this, SWT.NONE);
		FormData fd_btnEdit = new FormData();
		fd_btnEdit.top = new FormAttachment(btnRemove, 31, SWT.TOP);
		fd_btnEdit.right = new FormAttachment(label, -10);
		fd_btnEdit.left = new FormAttachment(label, -80);
		btnEdit.setLayoutData(fd_btnEdit);
		btnEdit.setText("Edit");
		btnEdit.setEnabled(false);

		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editRestrictionIfPossible();
			}
		});

		list = new org.eclipse.swt.widgets.List(this, SWT.BORDER);
		FormData fd_list = new FormData();
		fd_list.top = new FormAttachment(btnAdd, 0, SWT.TOP);
		fd_list.left = new FormAttachment(lblScope, 0, SWT.LEFT);
		fd_list.right = new FormAttachment(btnAdd, -10, SWT.LEFT);
		fd_list.bottom = new FormAttachment(100, -10);
		list.setLayoutData(fd_list);

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (list.getSelectionCount() > 0) {
					btnEdit.setEnabled(true);
					btnRemove.setEnabled(true);
				} else {
					btnEdit.setEnabled(false);
					btnRemove.setEnabled(false);
				}
			}
		});

		populateRestrictionsList();

		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddRestrictionDialog<I> dialog = new AddRestrictionDialog<>(getShell(), rule.getInstrumentableType());
				dialog.create();

				if (dialog.open() == Window.OK) {
					IRestrictionUI<I> restrictionView = RestrictionUIFactory
							.getViewForRestrictionType(dialog.getSelectedRestrictionType());
					Dialog resDialog = restrictionView.getAsDialog(getShell());
					if (resDialog.open() == Window.OK) {
						if (restrictionView.restrictionChanged()) {
							InstrumentableRestriction<I> restriction = restrictionView.getRestriction();
							if (restriction != null) {
								list.add(restriction.getHint());
								rule.addRestriction(restriction);
							}

							notifyDirty();
						}
					}
				}
			}
		});

		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = list.getSelectionIndex();

				if (idx >= 0) {
					list.remove(idx);
					rule.getRestrictions().remove(idx);
					notifyDirty();
				}
			}
		});

		Label lblProbes = new Label(this, SWT.NONE);
		lblProbes.setFont(InstrumentationDescriptionEditor.getActive().getOrCreateFont(lblProbes, SWT.BOLD));
		FormData fd_lblProbes = new FormData();
		fd_lblProbes.top = new FormAttachment(0, 10);
		fd_lblProbes.left = new FormAttachment(label, 10);
		lblProbes.setLayoutData(fd_lblProbes);
		lblProbes.setText("Probes:");

		probesTable = new Table(this, SWT.BORDER | SWT.CHECK);
		FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(50, -10);
		fd_table.right = new FormAttachment(100, -10);
		fd_table.top = new FormAttachment(lblProbes, 14, SWT.BOTTOM);
		fd_table.left = new FormAttachment(lblProbes, 0, SWT.LEFT);
		probesTable.setLayoutData(fd_table);

		Label lblCalculators = new Label(this, SWT.NONE);
		lblCalculators.setFont(InstrumentationDescriptionEditor.getActive().getOrCreateFont(lblCalculators, SWT.BOLD));
		FormData fd_lblCalculators = new FormData();
		fd_lblCalculators.top = new FormAttachment(probesTable, 20);
		fd_lblCalculators.left = new FormAttachment(lblProbes, 0, SWT.LEFT);
		lblCalculators.setLayoutData(fd_lblCalculators);
		lblCalculators.setText("Calculators:");

		calculatorsTable = new Table(this, SWT.BORDER | SWT.CHECK);
		FormData fd_table_1 = new FormData();
		fd_table_1.top = new FormAttachment(lblCalculators, 14, SWT.BOTTOM);
		fd_table_1.left = new FormAttachment(lblProbes, 0, SWT.LEFT);
		fd_table_1.right = new FormAttachment(100, -10);
		fd_table_1.bottom = new FormAttachment(100, -10);
		calculatorsTable.setLayoutData(fd_table_1);
	}

	public void init() {
		populateProbesTable();
		probesTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail != SWT.CHECK) {
					return;
				}

				TableItem item = (TableItem) e.item;
				SelectableProbe sProbe = probesPerItem.get(item.getText());

				if (item.getChecked() && !sProbe.isSelected()) {
					rule.addProbe(sProbe.getProbe());
					sProbe.setSelected(true);
					notifyDirty();
				} else if (!item.getChecked() && sProbe.isSelected()) {
					ProbeRepresentative probeToBeRemoved = probesPerItem.get(item.getText()).getProbe();

					for (TableItem calItem : calculatorsTable.getItems()) {
						SelectableCalculator cal = calculatorsPerItem.get(calItem.getText());
						if (cal.isSelected() && cal.getCalculator().uses(probeToBeRemoved)) {
							MessageDialog dialog = new MessageDialog(getShell(), "Unselect Probe", null,
									"Unselecting probe \"" + probeToBeRemoved.getMeasuredProperty()
											+ "\" entails unselecting calculator \"" + cal.getCalculator().getMetric()
											+ "\". Continue anyway?",
									MessageDialog.WARNING, new String[] { "Yes", "No" }, 0);
							if (dialog.open() == 0) {
								rule.removeCalculator(cal.getCalculator());
								cal.setSelected(false);
								calItem.setChecked(false);
								notifyDirty();
							} else {
								item.setChecked(true);
								return;
							}
						}
					}

					rule.removeProbe(probeToBeRemoved);
					sProbe.setSelected(false);
					notifyDirty();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		populateCalculatorsTable();
		calculatorsTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.detail != SWT.CHECK) {
					return;
				}

				TableItem item = (TableItem) e.item;
				SelectableCalculator sCal = calculatorsPerItem.get(item.getText());

				if (item.getChecked() && !sCal.isSelected()) {
					rule.addCalculator(sCal.getCalculator());
					sCal.setSelected(true);
					SelectableProbe fromProbe = probesPerItem
							.get(sCal.getCalculator().getFromProbe().getMeasuredProperty());
					SelectableProbe toProbe = probesPerItem
							.get(sCal.getCalculator().getToProbe().getMeasuredProperty());

					if (!fromProbe.isSelected()) {
						rule.addProbe(fromProbe.getProbe());
						fromProbe.setSelected(true);
					}
					if (!toProbe.isSelected()) {
						rule.addProbe(toProbe.getProbe());
						toProbe.setSelected(true);
					}

					for (TableItem probeItem : probesTable.getItems()) {
						if (probeItem.getText().equals(fromProbe.getProbe().getMeasuredProperty())
								|| probeItem.getText().equals(toProbe.getProbe().getMeasuredProperty())) {
							if (!probeItem.getChecked())
								probeItem.setChecked(true);
						}
					}

					notifyDirty();
				} else if (!item.getChecked() && sCal.isSelected()) {
					rule.removeCalculator(sCal.getCalculator());
					sCal.setSelected(false);
					notifyDirty();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void editRestrictionIfPossible() {
		int idx = list.getSelectionIndex();

		if (idx >= 0) {
			IRestrictionUI<I> restrictionView = RestrictionUIFactory
					.getViewForRestriction(rule.getRestrictions().get(idx));
			Dialog dialog = restrictionView.getAsDialog(getShell());
			dialog.open();
			if (restrictionView.restrictionChanged()) {
				list.setItem(idx, restrictionView.getRestriction().getHint());
				notifyDirty();
			}
		}
	}

	protected abstract String getScopeTypeName();

	private Set<ProbeRepresentative> getInitiallySelectedProbes() {
		Set<ProbeRepresentative> probes = new HashSet<>();
		probes.addAll(rule.getProbes());
		return probes;
	}

	private Set<CalculatorRepresentative> getInitiallySelectedCalculators() {
		Set<CalculatorRepresentative> calculators = new HashSet<>();
		calculators.addAll(rule.getCalculators());
		return calculators;
	}

	private void populateRestrictionsList() {
		for (InstrumentableRestriction<I> res : rule.getRestrictions()) {
			list.add(res.getHint());
		}

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				editRestrictionIfPossible();
			}
		});
	}

	private void populateProbesTable() {
		Set<ProbeRepresentative> selectedProbes = getInitiallySelectedProbes();
		probesPerItem = new HashMap<>();

		for (SelectableProbe probe : probes) {
			TableItem item = new TableItem(probesTable, SWT.NONE);
			item.setText(probe.getProbe().getMeasuredProperty());
			probesPerItem.put(probe.getProbe().getMeasuredProperty(), probe);

			if (selectedProbes.contains(probe.getProbe())) {
				item.setChecked(true);
				probe.setSelected(true);
			}
		}
	}

	private void populateCalculatorsTable() {
		Set<CalculatorRepresentative> selectedCalculators = getInitiallySelectedCalculators();
		calculatorsPerItem = new HashMap<>();

		for (SelectableCalculator cal : calculators) {
			TableItem item = new TableItem(calculatorsTable, SWT.NONE);
			item.setText(cal.getCalculator().getMetric() + ": from <"
					+ cal.getCalculator().getFromProbe().getMeasuredProperty() + "> to <"
					+ cal.getCalculator().getToProbe().getMeasuredProperty() + ">");
			calculatorsPerItem.put(item.getText(), cal);

			if (selectedCalculators.contains(cal.getCalculator())) {
				item.setChecked(true);
				cal.setSelected(true);
			}
		}
	}

	public R getRule() {
		return rule;
	}

	protected void notifyDirty() {
		for (DirtyListener l : dirtyListeners) {
			l.onDirty();
		}
	}

	public void addDirtyListener(DirtyListener listener) {
		if (listener != null) {
			dirtyListeners.add(listener);
		}
	}
}
