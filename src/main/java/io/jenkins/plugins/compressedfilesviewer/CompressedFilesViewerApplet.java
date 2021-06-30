package io.jenkins.plugins.compressedfilesviewer;

import java.applet.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class CompressedFilesViewerApplet extends Applet {

	private Map<String, Map<ExtractedFile, List<ExtractedFile>>> allExtractedFiles = new HashMap<String, Map<ExtractedFile, List<ExtractedFile>>>();
	private Path destPath = Paths.get(System.getProperty("user.home"), "AppData", "Local", "Google", "Chrome", "User Data", "Default", "Cache");

	public void start() {
		File topDir = new File(destPath.toString(), "compressed-files-viewer");
		boolean isCreated = topDir.mkdir();
		if (isCreated) {
			System.out.println("Top dir is created");
		}
		destPath = topDir.toPath();

		File dir = new File(System.getProperty("user.home"), "Downloads");
		File lastModifiedFile = getLatestFileFromDir(dir);
		if (lastModifiedFile.isDirectory()) {
			lastModifiedFile = getLatestFileFromDir(lastModifiedFile);
		}
		System.out.println(lastModifiedFile.getName());

		File buildDir;
		File latestBuildDir = getLatestFileFromDir(destPath.toFile());
		if (latestBuildDir == null) {
			buildDir = new File(destPath.toString(), "1");
		} else {
			buildDir = new File(destPath.toString(), String.valueOf((Integer.parseInt(latestBuildDir.getName()) + 1)));
		}
		if (buildDir.mkdir()) {
			System.out.println(buildDir.getName() + " build directory is created!");
		}

		Map<ExtractedFile, List<ExtractedFile>> artifacts = null;
		try {
			artifacts = unzipOneLevel(lastModifiedFile.getAbsolutePath(), buildDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File compressedFilesDir = new File(buildDir.getAbsolutePath(), "compressed files");
		if (compressedFilesDir.mkdir()) {
			System.out.println("compressed files directory is created!");
		}
		assert artifacts != null;
		System.out.println(artifacts.keySet());

		for (ExtractedFile artifact : artifacts.keySet()) {
			System.out.println("Artifact name: " + artifact.getName());
			if (Util.isCompressed(artifact.getName())) {
				try {
					extract(artifact.getFile(), compressedFilesDir.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			System.out.println("Start connecting to the server");
			Socket s = new Socket("localhost", 6666);
			ObjectOutputStream dout = new ObjectOutputStream(s.getOutputStream());
			dout.writeObject(allExtractedFiles);
			dout.flush();
			dout.close();
			s.close();
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Desktop.getDesktop().open(buildDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extract(File file, String dirPath) throws IOException {
		String fileName = file.getName();
		File directory = Paths.get(dirPath, fileName.substring(0, fileName.indexOf("."))).toFile();
		if (directory.mkdir()) {
			System.out.println(directory.getName() + " directory is created");
		}

		Map<ExtractedFile, List<ExtractedFile>> extractedFilesMap = unzip(file.getAbsolutePath(), directory);

		allExtractedFiles.put(fileName, extractedFilesMap);
	}

	public Map<ExtractedFile, List<ExtractedFile>> unzip(String compressedFile, File destDir) throws IOException {
		Map<ExtractedFile, List<ExtractedFile>> extractedFilesMap = new LinkedHashMap<ExtractedFile, List<ExtractedFile>>();

		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(compressedFile));
		ZipEntry zipEntry = zis.getNextEntry();

		List<ExtractedFile> extractedFiles = null;
		boolean fillExtractedFiles = false;
		File directory = null;
		boolean waitToCheckIfTheDirectoryIsEmpty = false;
		File previousDir = null;

		while (zipEntry != null) {
			File newFile = new File(destDir, zipEntry.getName());
			System.out.println("File name: " + newFile.getName());

			if (directory != null && !newFile.getAbsolutePath().contains(directory.getAbsolutePath())
					&& waitToCheckIfTheDirectoryIsEmpty) {
				// The file that can enter here is the first one outside the directory
				System.out.println(
						"Is " + previousDir.getName() + " a directory: " + !previousDir.getName().contains("."));
				// This if is for the empty directories 
				if (previousDir.mkdir()) {
					System.out.println(previousDir.getName() + " directory is created");
				}
				System.out.println("(adding files to the suspected dir) List: " + extractedFiles);
				fillExtractedFiles = false;
				System.out.println("An entry is putted");
				extractedFilesMap.put(new ExtractedFile(previousDir), extractedFiles);
				extractedFiles = null;
				waitToCheckIfTheDirectoryIsEmpty = false;
			}

			if ((!newFile.getName().contains("."))) {
				if (extractedFiles == null) {
					/** The last created directory is either empty or not **/
					waitToCheckIfTheDirectoryIsEmpty = true;
					// Save the directory file to put it with its files later
					previousDir = newFile;
					System.out.println("Previous Dir: " + previousDir.getName());
				} else {
					fillExtractedFiles = false;
					System.out.println("An entry is putted");
					extractedFilesMap.put(new ExtractedFile(newFile), extractedFiles);
					extractedFiles = null;
				}
				System.out.println("List: " + extractedFiles);
				zipEntry = zis.getNextEntry();
				continue;
			}

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(newFile);
			} catch (FileNotFoundException e) {
				/**
				 * The file that you're trying to write on doesn't have its last directory so we
				 * create it and we prepare a list that will contains files in the created
				 * directory
				 */

				directory = new File(
						newFile.getAbsolutePath().substring(0, newFile.getAbsolutePath().lastIndexOf("\\")));
				if (directory.mkdir()) {
					fos = new FileOutputStream(newFile);

					System.out.println(directory.getName() + " is created!");
					fillExtractedFiles = true;
					extractedFiles = new ArrayList<ExtractedFile>();

				}
			}

			assert fos != null;

			try {
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fos.close();
			}

			if (fillExtractedFiles) {
				extractedFiles.add(new ExtractedFile(newFile));
			} else {
				// If the before last file is compressed we create a directory for it
				// and store its content in the extractedFilesMap
				if (Util.isCompressed(newFile.getName())) {
					File newZipDir = new File(newFile.getAbsolutePath().substring(0,
							newFile.getAbsolutePath().lastIndexOf(".")));
					System.out.println(newFile.getName() + " is compressed and not gonna hold it");
					Map<ExtractedFile, List<ExtractedFile>> zipDirMap = unzipOneLevel(newFile.getAbsolutePath(), newZipDir);
					
					System.out.println("An entry is putted");
					extractedFilesMap.put(new ExtractedFile(newFile), new ArrayList<ExtractedFile>(zipDirMap.keySet()));
					System.out.println("This will be deleted");
					if (newFile.delete()) {
						System.out.println(newFile.getName() + " is deleted");
					}
				} else {
					System.out.println("An entry is putted");
					extractedFilesMap.put(new ExtractedFile(newFile), extractedFiles);
				}
			}
			zipEntry = zis.getNextEntry();

		}
		zis.closeEntry();
		zis.close();
		return extractedFilesMap;
	}

	public Map<ExtractedFile, List<ExtractedFile>> unzipOneLevel(String compressedFile, File destDir)
			throws IOException {

		Map<ExtractedFile, List<ExtractedFile>> extractedFilesMap = new LinkedHashMap<ExtractedFile, List<ExtractedFile>>();

		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(compressedFile));
		ZipEntry zipEntry = zis.getNextEntry();
		
		while (zipEntry != null) {
			File newFile = new File(destDir, zipEntry.getName());
			System.out.println("(unzipOneLevel)File name: " + newFile.getName());

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(newFile);
			} catch (FileNotFoundException e) {

				File directory = new File(
						newFile.getAbsolutePath().substring(0, newFile.getAbsolutePath().lastIndexOf("\\")));
				if (directory.mkdir()) {
					fos = new FileOutputStream(newFile);
				}
			}

			assert fos != null;

			try {
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fos.close();
			}

			extractedFilesMap.put(new ExtractedFile(newFile), null);

			zipEntry = zis.getNextEntry();

		}
		zis.closeEntry();
		zis.close();
		return extractedFilesMap;
	}

	private File getLatestFileFromDir(File dir) {
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return null;
		}

		File lastModifiedFile = files[0];
		for (int i = 1; i < files.length; i++) {
			if (lastModifiedFile.lastModified() < files[i].lastModified()) {
				lastModifiedFile = files[i];
			}
		}
		return lastModifiedFile;
	}

	public void paint(Graphics g) {
		   Font f = new Font("TimesRoman", Font.ITALIC, 18);
	       g.setFont(f);
	       Color c = new Color(52, 152, 219);
	       g.setColor(c);
	       g.drawString("Done!", 0, 25);
	}

}