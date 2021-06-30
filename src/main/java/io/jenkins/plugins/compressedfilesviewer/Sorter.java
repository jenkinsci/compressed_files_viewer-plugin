package io.jenkins.plugins.compressedfilesviewer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sorter {
	
	private List<ExtractedFile> extractedFilesList;
    
	public Sorter(List<ExtractedFile> extractedFilesList) {
		this.extractedFilesList = extractedFilesList;
	}
	
    public List<ExtractedFile> sort() {
    	Collections.sort(extractedFilesList, new Comparator<ExtractedFile>() {
            private final Comparator<String> NATURAL_SORT = new ExplorerComparator();

            @Override
            public int compare(ExtractedFile o1, ExtractedFile o2) {
            	if (rankOfFile(o1) == rankOfFile(o2)) {
            		return NATURAL_SORT.compare(o1.getName(), o2.getName());
            	} else {
            		if (rankOfFile(o1) > rankOfFile(o2)) return -1;
            		else return 1;
            	}
                
            }
        });
    	return extractedFilesList;
    }
    
    private int rankOfFile(ExtractedFile ef) {
    	if (ef.isDirectory()) return 1;
    	else if (ef.isCompressed()) return 0;
    	else return -1;
    }

    public static class ExplorerComparator implements Comparator<String>, Serializable {

        private static final Pattern splitPattern = Pattern.compile("\\d+|\\.|\\s");

        @Override
        public int compare(String str1, String str2) {
            Iterator<String> i1 = splitStringPreserveDelimiter(str1).iterator();
            Iterator<String> i2 = splitStringPreserveDelimiter(str2).iterator();
            while (true) {
                //Til here all is equal.
                if (!i1.hasNext() && !i2.hasNext()) {
                    return 0;
                }
                //first has no more parts -> comes first
                if (!i1.hasNext() && i2.hasNext()) {
                    return -1;
                }
                //first has more parts than i2 -> comes after
                if (i1.hasNext() && !i2.hasNext()) {
                    return 1;
                }

                String data1 = i1.next();
                String data2 = i2.next();
                int result;
                try {
                    //If both data are numbers, then compare numbers
                    result = Long.compare(Long.parseLong(data1), Long.parseLong(data2));
                    //If numbers are equal than longer comes first
                    if (result == 0) {
                        result = Integer.compare(data1.length(), data2.length());
                        result = result * -1;
                    }
                } catch (NumberFormatException ex) {
                    //compare text case insensitive
                    result = data1.compareToIgnoreCase(data2);
                }

                if (result != 0) {
                    return result;
                }
            }
        }

        private List<String> splitStringPreserveDelimiter(String str) {
            Matcher matcher = splitPattern.matcher(str);
            List<String> list = new ArrayList<String>();
            int pos = 0;
            while (matcher.find()) {
                list.add(str.substring(pos, matcher.start()));
                list.add(matcher.group());
                pos = matcher.end();
            }
            list.add(str.substring(pos));
            return list;
        }
    }
}
