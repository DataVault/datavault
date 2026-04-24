package org.datavaultplatform.worker.tasks;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

import static org.assertj.core.api.Assertions.assertThat;


class ChunkFileComparatorTest {

    final Comparator<File> chunkFileComparator = new ChunkFileComparator();
    
    @Test
    void testPatternWithChunkNumber(){
        String example = "/private/tmp/blah/temp/worker-store/d87ca007-9cee-4c49-8169-f74c2b90b773.tar.1234";
        Matcher m = ChunkFileComparator.PATTERN.matcher(example);
        assertThat(m.matches()).isTrue();
        assertThat(m.group(1)).isEqualTo("1234");
    }

    @Test
    void testPatternWithoutChunkNumber(){
        String example = "/private/tmp/blah/temp/worker-store/d87ca007-9cee-4c49-8169-f74c2b90b773.tar";
        Matcher m = ChunkFileComparator.PATTERN.matcher(example);
        assertThat(m.matches()).isFalse();
    }

    @Test
    void testRegexAndSort() {

        List<File> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int num = i + 1;
            items.add(new File("/private/tmp/blah/temp/worker-store/d87ca007-9cee-4c49-8169-f74c2b90b773.tar." + num));
        }
        Collections.shuffle(items);


        File[] shuffled = items.toArray(new File[0]);
        for (int i = 0; i < shuffled.length; i++) {
            int num = i + 1;
            File file = shuffled[i];
            System.out.printf("before %4d-%s%n", num, file.getName());
        }

        File[] sortedFiles = Arrays.stream(shuffled).sorted(chunkFileComparator).toArray(File[]::new);
        for (int i = 0; i < sortedFiles.length; i++) {
            int num = i + 1;
            File file = sortedFiles[i];
            assertThat(file.getName()).endsWith(".tar." + num);
            System.out.printf("after %4d-%s%n", num, file.getName());
        }
    }
}