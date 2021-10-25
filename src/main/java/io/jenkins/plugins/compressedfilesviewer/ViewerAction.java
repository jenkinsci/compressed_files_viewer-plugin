package io.jenkins.plugins.compressedfilesviewer;

import java.io.IOException;
import hudson.model.Run;
import jenkins.model.RunAction2;

public class ViewerAction implements RunAction2 {

	private transient Run run;

	public ViewerAction() { }

	public String getJobName() throws IOException, InterruptedException {
		String[] list = run.getAbsoluteUrl().split("/");
		return list[list.length - 2];
	}

	public boolean isCompressed(String s) {
		return Util.isCompressed(s);
	}

	public String getCustomBaseURL() {
		return run.getAbsoluteUrl();
	}

	public Run getRun() {
		return run;
	}

	@Override
	public void onAttached(Run<?, ?> run) {
		this.run = run;
	}

	@Override
	public void onLoad(Run<?, ?> run) {
		this.run = run;
	}

	@Override
	public String getIconFileName() {
		return "/plugin/compressed_files_viewer/zip.png";
	}

	@Override
	public String getDisplayName() {
		return "Compressed Files Viewer";
	}

	@Override
	public String getUrlName() {
		return "compressed-files-viewer";
	}
}
