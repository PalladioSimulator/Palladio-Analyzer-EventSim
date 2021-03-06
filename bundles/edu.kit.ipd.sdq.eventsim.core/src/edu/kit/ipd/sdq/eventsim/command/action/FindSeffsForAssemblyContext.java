package edu.kit.ipd.sdq.eventsim.command.action;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffPackage;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;

/**
 * This command returns all {@link ResourceDemandingSEFF}s provided by the component that is
 * encapsulated in a specified {@link AssemblyContext}.
 * 
 * @author Philipp Merkle
 * 
 */
public class FindSeffsForAssemblyContext implements IPCMCommand<List<ResourceDemandingSEFF>> {

    private AssemblyContext assemblyCtx;

    /**
     * Constructs a new command to find all SEFFs in the specified AssemblyContext.
     * 
     * @param assemblyCtx
     *            the AssemblyContext
     */
    public FindSeffsForAssemblyContext(AssemblyContext assemblyCtx) {
        this.assemblyCtx = assemblyCtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResourceDemandingSEFF> execute(PCMModel pcm, ICommandExecutor<PCMModel> executor) {
        // find basic component
        // TODO: support composite components etc.
        BasicComponent basicComponent = findBasicComponent(assemblyCtx);

        // collect all SEFFs of the component
        List<ResourceDemandingSEFF> seffs = new ArrayList<ResourceDemandingSEFF>();
        for (ServiceEffectSpecification s : basicComponent.getServiceEffectSpecifications__BasicComponent()) {
            if (SeffPackage.eINSTANCE.getResourceDemandingSEFF().isInstance(s)) {
                seffs.add((ResourceDemandingSEFF) s);
            } else {
                throw new EventSimException("Currently, only resource demanding SEFFs are supported.");
            }
        }

        return seffs;
    }

    /**
     * Returns the component that is encapsulated in the given assembly context. If this component
     * is not a BasicComponent, an exception is thrown.
     * 
     * @param assemblyCtx
     *            the assembly context
     * @return the component, if the encapsulated component is a BasicComponent
     */
    private BasicComponent findBasicComponent(AssemblyContext assemblyCtx) {
        RepositoryComponent component = assemblyCtx.getEncapsulatedComponent__AssemblyContext();
        if (RepositoryPackage.eINSTANCE.getBasicComponent().isInstance(component)) {
            return (BasicComponent) component;
        } else {
            throw new EventSimException("Currently only BasicComponents are supported.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cachable() {
        return false;
    }

}
