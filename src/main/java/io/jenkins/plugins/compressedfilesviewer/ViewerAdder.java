package io.jenkins.plugins.compressedfilesviewer;

import java.io.IOException;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

public class ViewerAdder extends Notifier {

    private final String name;

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
