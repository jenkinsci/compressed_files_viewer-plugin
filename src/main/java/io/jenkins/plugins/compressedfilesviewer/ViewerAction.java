package io.jenkins.plugins.compressedfilesviewer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import hudson.model.Action;
import hudson.model.Build;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import jenkins.model.ArtifactManager;
import jenkins.model.RunAction2;

public class ViewerAction implements RunAction2 {

	private transient Run run;
	private Map<String, Map<ExtractedFile, List<ExtractedFile>>> allExtractedFiles = new HashMap<String, Map<ExtractedFile, List<ExtractedFile>>>();

	public ViewerAction() {
	}

	class ConnectionThread implements Runnable {

		@Override
		public void run() {
			System.out.println();
			System.out.println("Connection is started!");
			try {
				ServerSocket ss = new ServerSocket(6666);
				Socket s = ss.accept();
				ObjectInputStream dis = new ObjectInputStream(s.getInputStream());
				try {
					allExtractedFiles = (Map<String, Map<ExtractedFile, List<ExtractedFile>>>) dis.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				System.out.println("SUCCESS!");
				
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void startConnection() throws IOException {
		if (allExtractedFiles.isEmpty()) {
			ConnectionThread ct = new ConnectionThread();
			Thread t = new Thread(ct);
			t.start();
		}
	}

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

	public Map<String, Map<ExtractedFile, List<ExtractedFile>>> getAllExtractedFiles() {
		return allExtractedFiles;
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
