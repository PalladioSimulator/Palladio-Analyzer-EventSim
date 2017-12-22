package edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.palladiosimulator.analyzer.workflow.ConstantsContainer;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;

/**
 * A dialog for restrictions. The content of the dialog is retrieved from a
 * {@link IRestrictionUI}.
 * 
 * @author Henning Schulz
 *
 * @param <I>
 *            the type of {@link Instrumentable} the UI deals with
 */
public class RestrictionDialog<I extends Instrumentable> extends TitleAreaDialog {

	private final IRestrictionUI<I> ui;
	private final List<OnCloseListener> onCloseListeners = new ArrayList<>();
	private final List<OnCreatedListener> onCreatedListeners = new ArrayList<>();
	private List<OnNextListener> onNextListeners = new ArrayList<>();
	private final boolean pcmRequired;

	private boolean created = false;
	private boolean aborted = false;
	private boolean lastPage = true;

	private String initialMessage;

	public RestrictionDialog(Shell parentShell, IRestrictionUI<I> ui, boolean pcmRequired) {
		super(parentShell);
		this.ui = ui;
		this.pcmRequired = pcmRequired;
	}

	@Override
	public void create() {
		super.create();
		Restriction a = ui.getRestriction().getClass().getAnnotation(Restriction.class);
		setTitle(a.name());

		for (OnCreatedListener listener : onCreatedListeners) {
			listener.onCreated();
		}

		if (initialMessage != null) {
			final String m = getMessage();
			addOnNextListener(() -> setMessage(m, IMessageProvider.INFORMATION));
			setMessage(initialMessage, IMessageProvider.INFORMATION);
		}

		setLastPage(lastPage);
		created = true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		if (pcmRequired && InstrumentationDescriptionEditor.getActive().getPcm() == null) {
			initialMessage = "Please select a Palladio Component Model (PCM) before selecting PCM-specific entities.";
			final Composite container = askForModels(area);

			addOnNextListener(new OnNextListener() {
				@Override
				public void onNext() {
					container.dispose();
					enableFinish(false);
					ui.createUIArea(area);
					area.layout(true);
					setLastPage(true);
				}
			});
		} else {
			setLastPage(true);
			ui.createUIArea(area);
		}

		return area;
	}

	private Composite askForModels(Composite area) {
		setLastPage(false);

		if (created) {
			enableNext(false);
		} else {
			addOnCreatedListener(() -> enableNext(false));
		}

		final Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout());

		final UriListener allocListener = new UriListener();
		final UriListener usageListener = new UriListener();

		new CompoundListener(allocListener, usageListener) {
			@Override
			public void fire() {
				enableNext(true);
				InstrumentationDescription description = InstrumentationDescriptionEditor.getActive().getDescription();
				description.setAllocationUri(allocListener.uri);
				description.setUsagemodelUri(usageListener.uri);
				InstrumentationDescriptionEditor.getActive().reloadPcm();
			}
		};

		RestrictionUIHelper.createLoadModelSection(container, getParentShell(), "Allocation Model",
				ConstantsContainer.ALLOCATION_EXTENSION, allocListener);
		RestrictionUIHelper.createLoadModelSection(container, getParentShell(), "Usagemodel",
				ConstantsContainer.USAGEMODEL_EXTENSION, usageListener);

		return container;
	}

	private abstract class CompoundListener {

		private UriListener[] listeners;
		private boolean[] fired;

		@SafeVarargs
		public CompoundListener(UriListener... listeners) {
			this.listeners = listeners;
			this.fired = new boolean[listeners.length];

			for (UriListener l : listeners) {
				l.registerCompundListener(this);
			}
		}

		public void onTextChosen(TextChosenListener l) {
			boolean fire = true;

			int i = 0;
			for (TextChosenListener stored : listeners) {
				if (stored.equals(l)) {
					fired[i] = true;
				}

				fire = fire & fired[i];

				i++;
			}

			if (fire) {
				fire();
			}
		}

		public abstract void fire();

	}

	private class UriListener implements TextChosenListener {

		private String uri;
		private CompoundListener compoundListener;

		public void registerCompundListener(CompoundListener compoundListener) {
			this.compoundListener = compoundListener;
		}

		@Override
		public void textChosen(String text) {
			this.uri = text;
			if (compoundListener != null) {
				compoundListener.onTextChosen(this);
			}
		}

	}

	public void addOnCreatedListener(OnCreatedListener listener) {
		onCreatedListeners.add(listener);
	}

	public void addOnCloseListener(OnCloseListener listener) {
		onCloseListeners.add(listener);
	}

	public void addOnNextListener(OnNextListener listener) {
		onNextListeners.add(listener);
	}

	private void callOnCloseListeners() {
		onCloseListeners.forEach(l -> l.onClose());
	}

	private void callOnNextListeners(List<OnNextListener> onNextListeners) {
		onNextListeners.forEach(l -> l.onNext());
	}

	public void enableFinish(boolean enable) {
		if (lastPage)
			getButton(Window.OK).setEnabled(enable);
	}

	public void enableNext(boolean enable) {
		if (!lastPage)
			getButton(Window.OK).setEnabled(enable);
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setLastPage(boolean lastPage) {
		this.lastPage = lastPage;

		if (getButton(Window.OK) != null) {
			if (lastPage) {
				getButton(Window.OK).setText("Finish");
			} else {
				getButton(Window.OK).setText("Next >");
			}
		}
	}

	public void close(int buttonId) {
		buttonPressed(buttonId);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case Window.OK:
			if (lastPage) {
				callOnCloseListeners();
			} else {
				List<OnNextListener> onNextListenersCopy = onNextListeners;
				onNextListeners = new ArrayList<>();
				callOnNextListeners(onNextListenersCopy);
				return;
			}
			break;
		case Window.CANCEL:
			aborted = true;
			callOnCloseListeners();
			break;
		default:
			break;
		}

		super.buttonPressed(buttonId);
	}

}
