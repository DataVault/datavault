package org.datavaultplatform.worker.tasks;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Had to create a comparator for Delete - want to sort delete message by file chunk numbers.
 * @see org.datavaultplatform.worker.tasks.Delete
 */
public class ChunkFileComparator implements Comparator<File> {

    static final Pattern PATTERN = Pattern.compile(".*\\.tar\\.(\\d+)");

    @Override
    public int compare(File file1, File file2) {
        Matcher m1 = PATTERN.matcher(file1.getName());
        Matcher m2 = PATTERN.matcher(file2.getName());
        if (m1.matches() && m2.matches()) {
            int num1 = m1.group(1) == null ? 0 : Integer.parseInt(m1.group(1));
            int num2 = m2.group(1) == null ? 0 : Integer.parseInt(m2.group(1));
            return Integer.compare(num1, num2);
        } else {
            return file1.getName().compareTo(file2.getName());
        }
    }
}
