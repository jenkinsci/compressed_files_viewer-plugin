package io.jenkins.plugins.compressedfilesviewer;

import java.io.IOException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;

public class ViewerAdder extends Notifier {

    private String name;

    @DataBoundConstructor
    public ViewerAdder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
	@Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}
	
	@Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
    	build.addAction(new ViewerAction());
		System.out.println("Build path: " + build.getRootDir().getAbsolutePath());
		
		return true;
    }
    
	@Symbol("test")
	@Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Add Compressed Files Viewer";
        }

    }
}
