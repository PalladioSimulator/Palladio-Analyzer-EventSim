package edu.kit.ipd.sdq.eventsim.modules;

import java.util.Observer;

import org.eclipse.debug.ui.ILaunchConfigurationTab2;

public interface ILaunchContribution extends ILaunchConfigurationTab2 {

    void addDirtyListener(Observer observer);
    
    void removeDirtyListener(Observer observer);
    
}
