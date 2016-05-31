package edu.kit.ipd.sdq.eventsim.command;

import com.google.inject.Inject;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;

/**
 * Executes {@link ICommand}s operating on PCM models.
 * 
 * @author Philipp Merkle
 * 
 * @see ICommandExecutor
 */
public class PCMModelCommandExecutor implements ICommandExecutor<PCMModel> {

    @Inject
    private final PCMModel pcm;

    /**
     * Constructs an executor that is capable of executing {@link ICommand}s operating on PCM
     * models.
     * 
     * @param pcm
     *            the PCM model on which executed commands operate
     * 
     */
    @Inject
    public PCMModelCommandExecutor(final PCMModel pcm) {
        this.pcm = pcm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T execute(final ICommand<T, PCMModel> command) {
        return command.execute(this.pcm, this);
    }

}
