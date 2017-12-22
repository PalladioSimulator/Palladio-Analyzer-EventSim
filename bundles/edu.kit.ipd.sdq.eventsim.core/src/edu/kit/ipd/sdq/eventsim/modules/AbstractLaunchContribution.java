package edu.kit.ipd.sdq.eventsim.modules;

import java.util.List;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;

public abstract class AbstractLaunchContribution extends AbstractLaunchConfigurationTab implements ILaunchContribution {

    private List<Observer> dirtyListeners = new CopyOnWriteArrayList<>();

    @Override
    public void addDirtyListener(Observer listener) {
        dirtyListeners.add(listener);
    }

    @Override
    public void removeDirtyListener(Observer listener) {
        dirtyListeners.remove(listener);
    }

    @Override
    protected void setDirty(boolean dirty) {
        super.setDirty(dirty);
        dirtyListeners.forEach(l -> l.update(null, null));
    }

    @Override
    public String getName() {
        // no need to override by subclasses; won't be called anyway
        return null;
    }

    @Override
    public void dispose() {
        dirtyListeners.clear();
    }
    
    

}
