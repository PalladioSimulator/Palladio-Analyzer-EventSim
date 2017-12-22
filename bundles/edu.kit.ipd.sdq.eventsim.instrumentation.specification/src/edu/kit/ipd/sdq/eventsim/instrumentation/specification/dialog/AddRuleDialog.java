package edu.kit.ipd.sdq.eventsim.instrumentation.specification.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.LinkingResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.ActionTypeRepository;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.UserActionTypeRepository;

public class AddRuleDialog extends TitleAreaDialog {

    private static final String KEY_ACTION_RULE = "Actions";
    private static final String KEY_USER_ACTION_RULE = "User Actions";
    private static final String KEY_RESOURCE_RULE = "Resources";

    private final Map<String, java.util.List<Class<?>>> getDetailsOfType = new HashMap<>();

    private final java.util.List<Class<?>> actionTypes = ActionTypeRepository.getAllActionTypes();
    private final java.util.List<Class<?>> userActionTypes = UserActionTypeRepository.getAllUserActionTypes();
    private final java.util.List<Class<?>> resourceTypes;

    private List list;
    private List detailsList;

    private InstrumentationRule createdRule;

    public AddRuleDialog(Shell parentShell) {
        super(parentShell);

        // TODO read this information from a proper extension point; should not be hard-coded!
        resourceTypes = new ArrayList<>();
        resourceTypes.add(ActiveResourceRep.class);
        resourceTypes.add(PassiveResourceRep.class);
        resourceTypes.add(LinkingResourceRep.class);

        getDetailsOfType.put(KEY_ACTION_RULE, actionTypes);
        getDetailsOfType.put(KEY_USER_ACTION_RULE, userActionTypes);
        getDetailsOfType.put(KEY_RESOURCE_RULE, resourceTypes);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add Instrumentation Rule");
        setMessage("Please choose the entity type that should be instrumented.", IMessageProvider.INFORMATION);
        getButton(Window.OK).setText("Create");
        getButton(Window.OK).setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        container.setLayout(new FormLayout());

        Label lblTypeToBe = new Label(container, SWT.NONE);
        FormData fd_lblTypeToBe = new FormData();
        fd_lblTypeToBe.top = new FormAttachment(0, 10);
        fd_lblTypeToBe.left = new FormAttachment(0, 10);
        lblTypeToBe.setLayoutData(fd_lblTypeToBe);
        lblTypeToBe.setText("Pre-Selection");

        list = new List(container, SWT.BORDER | SWT.V_SCROLL);
        FormData fd_list = new FormData();
        fd_list.right = new FormAttachment(50, -10);
        fd_list.bottom = new FormAttachment(100, -10);
        fd_list.top = new FormAttachment(lblTypeToBe, 6);
        fd_list.left = new FormAttachment(0, 10);
        list.setLayoutData(fd_list);

        for (String type : getDetailsOfType.keySet()) {
            list.add(type);
        }

        Label lblDetails = new Label(container, SWT.NONE);
        FormData fd_lblDetails = new FormData();
        fd_lblDetails.top = new FormAttachment(0, 10);
        fd_lblDetails.left = new FormAttachment(50, 10);
        lblDetails.setLayoutData(fd_lblDetails);
        lblDetails.setText("(Super-)Type to be Instrumented");

        detailsList = new List(container, SWT.BORDER | SWT.V_SCROLL);
        FormData fd_list_1 = new FormData();
        fd_list_1.left = new FormAttachment(50, 10);
        fd_list_1.top = new FormAttachment(lblDetails, 6);
        fd_list_1.right = new FormAttachment(100, -10);
        fd_list_1.bottom = new FormAttachment(100, -10);
        detailsList.setLayoutData(fd_list_1);

        list.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (list.getSelectionCount() <= 0) {
                    return;
                }

                getButton(Window.OK).setEnabled(false);
                detailsList.removeAll();

                for (Class<?> type : getDetailsOfType.get(list.getSelection()[0])) {
                    detailsList.add(type.getSimpleName());
                }
            }
        });

        detailsList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getButton(Window.OK).setEnabled(true);
            }
        });

        return container;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == Window.OK) {
            switch (list.getSelection()[0]) {
            case KEY_ACTION_RULE:
                @SuppressWarnings("unchecked")
                Class<? extends AbstractAction> actionType = (Class<? extends AbstractAction>) actionTypes
                        .get(detailsList.getSelectionIndex());
                createdRule = new ActionRule(actionType);
                break;
            case KEY_USER_ACTION_RULE:
                @SuppressWarnings("unchecked")
                Class<? extends AbstractUserAction> userActionType = (Class<? extends AbstractUserAction>) userActionTypes
                        .get(detailsList.getSelectionIndex());
                createdRule = new UserActionRule(userActionType);
                break;
            case KEY_RESOURCE_RULE:
                @SuppressWarnings("unchecked")
                Class<? extends ResourceRepresentative> resourceType = (Class<? extends ResourceRepresentative>) resourceTypes
                        .get(detailsList.getSelectionIndex());
                createdRule = new ResourceRule<>(resourceType);
                break;
            default:
                break;
            }
        }

        super.buttonPressed(buttonId);
    }

    public InstrumentationRule getCreatedRule() {
        return createdRule;
    }

}
