package edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Instrumentable;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.Restriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;

/**
 * A factory for {@link IRestrictionUI}s. The corresponding UI for a restriction
 * is found by the {@link RestrictionUI} annotation. If several UIs fit the
 * restriction, the most general is taken. If no UI can be found, one
 * representing an error dialog is returned.
 * 
 * @author Henning Schulz
 *
 */
public class RestrictionUIFactory {

	private static String[] restrictionPackages;
	private static String[] restrictionUiPackages;

	@SuppressWarnings("unchecked")
	public static <I extends Instrumentable> List<Class<? extends InstrumentableRestriction<I>>> getAllRestrictionTypesFor(
			Class<I> instrumentableType) {
		return ClassRepository
				.filterClassesInBundles(c -> isRestrictionTypeFor(c, instrumentableType), getRestrictionPackages())
				.stream().map(c -> (Class<? extends InstrumentableRestriction<I>>) c).collect(Collectors.toList());
	}

	private static String[] getRestrictionPackages() {
		if (restrictionPackages == null) {
			restrictionPackages = loadPackageExtensions(InstrumentableRestriction.EXTENSION_POINT_ID);
		}

		return restrictionPackages;
	}

	private static String[] getRestrictionUiPackages() {
		if (restrictionUiPackages == null) {
			restrictionUiPackages = loadPackageExtensions(IRestrictionUI.EXTENSION_POINT_ID);
		}

		return restrictionUiPackages;
	}

	private static String[] loadPackageExtensions(String id) {
		List<String> pckages = new ArrayList<>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(id);

		for (IExtension extension : point.getExtensions()) {
			pckages.add(extension.getNamespaceIdentifier());
		}

		return pckages.toArray(new String[0]);
	}

	private static boolean isRestrictionTypeFor(Class<?> restrictionType, Class<?> instrumentableType) {
		Restriction annotation = restrictionType.getAnnotation(Restriction.class);
		return InstrumentableRestriction.class.isAssignableFrom(restrictionType) && annotation != null
				&& annotation.instrumentableType().isAssignableFrom(instrumentableType);
	}

	public static <I extends Instrumentable> IRestrictionUI<I> getViewForRestriction(
			InstrumentableRestriction<I> restriction) {
		@SuppressWarnings("unchecked")
		IRestrictionUI<I> view = getView((Class<? extends InstrumentableRestriction<I>>) restriction.getClass());
		view.init(restriction);
		return view;
	}

	public static <I extends Instrumentable> IRestrictionUI<I> getViewForRestrictionType(
			Class<? extends InstrumentableRestriction<I>> type) {
		IRestrictionUI<I> view = getView(type);
		view.init();
		return view;
	}

	private static <I extends Instrumentable> IRestrictionUI<I> getView(
			Class<? extends InstrumentableRestriction<I>> type) {
		List<Class<?>> allViewTypes = ClassRepository
				.filterClassesInBundles(c -> viewTypeIsBuiltForRestrictionType(c, type), getRestrictionUiPackages());

		IRestrictionUI<I> currentView = null;
		for (Class<?> viewType : allViewTypes) {
			if (currentView != null) {
				RestrictionUI a = currentView.getClass().getAnnotation(RestrictionUI.class);
				RestrictionUI b = viewType.getAnnotation(RestrictionUI.class);

				if (b.restrictionType().isAssignableFrom(a.restrictionType())) {
					continue; // a is more specific than b
				}
			}

			@SuppressWarnings("unchecked")
			Class<? extends IRestrictionUI<I>> restrictionViewType = (Class<? extends IRestrictionUI<I>>) viewType;
			try {
				Constructor<? extends IRestrictionUI<I>> constructor = restrictionViewType.getConstructor();
				IRestrictionUI<I> view = constructor.newInstance();
				currentView = view;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				continue;
			}
		}

		if (currentView != null) {
			return currentView;
		} else {
			return errorView(type);
		}
	}

	private static boolean viewTypeIsBuiltForRestrictionType(Class<?> viewType, Class<?> restrictionType) {
		RestrictionUI annotation = viewType.getAnnotation(RestrictionUI.class);
		return annotation != null && annotation.restrictionType().isAssignableFrom(restrictionType);
	}

	private static <I extends Instrumentable> IRestrictionUI<I> errorView(
			Class<? extends InstrumentableRestriction<I>> type) {
		return new IRestrictionUI<I>() {
			@Override
			public void init(InstrumentableRestriction<I> restriction) {
			}

			@Override
			public void init() {
			}

			@Override
			public Dialog getAsDialog(Shell parentShell) {
				MessageDialog dialog = new MessageDialog(parentShell, "Error", null,
						"An error occured: No view found for " + type, MessageDialog.ERROR, new String[] { "OK" }, 0);
				return dialog;
			}

			@Override
			public InstrumentableRestriction<I> getRestriction() {
				return null;
			}

			@Override
			public boolean restrictionChanged() {
				return false;
			}

			@Override
			public Control createUIArea(Composite parent) {
				return parent;
			}
		};
	}

}
