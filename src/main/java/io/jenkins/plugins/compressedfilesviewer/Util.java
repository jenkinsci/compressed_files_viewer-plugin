package io.jenkins.plugins.compressedfilesviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Util {
	public static boolean isCompressed(String s) {
		if (!s.contains(".")) return false;
    	s = s.substring(s.indexOf("."));
		List<String> extensionsList = new ArrayList<>(Arrays.asList(".zip", ".jar"));
		return extensionsList.contains(s.toLowerCase());
    }
}
