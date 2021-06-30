package io.jenkins.plugins.compressedfilesviewer;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;

public class ExtractedFile implements Serializable {
	
	private static final long serialVersionUID = 6666L;
	
	private File file;
	
	public ExtractedFile(File file) {
		this.file = file;
	}
	
	public long getSize() {
		return file.length();
	}
	public String getName() {
		return file.getName();
	}
	public File getFile() {
		return file;
	}

	public boolean isCompressed() {
		return  Util.isCompressed(getName());
	}
	
	public boolean isDirectory() {
    	return !getName().contains(".");
    }

	public String getUrl() throws MalformedURLException {
		return file.toURI().toURL().toExternalForm();
	}
}
