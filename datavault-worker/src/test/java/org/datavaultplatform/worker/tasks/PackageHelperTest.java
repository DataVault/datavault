package org.datavaultplatform.worker.tasks;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PackageHelperTest {
    
    @Nested
    class ValidateChunkMapTest {
     
        @Test
        void testNullChunkMap() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> {
                PackageHelper.validateChunkMap("test", 1, null);
            });
            assertThat(ex).hasMessage("The chunkMap cannot be null");
        }
        @Test
        void testNegativeChunkNumberChunkMap() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,() -> {
                PackageHelper.validateChunkMap("test", -1, Collections.emptyMap());
            });
            assertThat(ex).hasMessage("The number of chunks cannot be negative");
        }
        @Test
        void testChunkMapIsOkay(){
            Map<Integer,String> chunkMap = Map.of(1,"1",2,"2",3,"3");
            assertThat(PackageHelper.validateChunkMap("test", 3, chunkMap)).isEqualTo(true);
        }
        @Test
        void testChunkMapHasMissingKeys() {
            Map<Integer,String> chunkMap = Map.of(1,"1",3,"3");
            assertThat(PackageHelper.validateChunkMap("test", 3, chunkMap)).isEqualTo(false);
        }
        @Test
        void testChunkMapHasExtraKeys() {
            Map<Integer,String> chunkMap = Map.of(1,"1",2,"2",3,"3",4,"4");
            assertThat(PackageHelper.validateChunkMap("test", 3, chunkMap)).isEqualTo(false);
        }
        @Test
        void testChunkMapHasMissingAndExtraKeys() {
            Map<Integer,String> chunkMap = Map.of(1,"1",3,"3",4,"4");
            assertThat(PackageHelper.validateChunkMap("test", 3, chunkMap)).isEqualTo(false);
        }

        @Test
        void testZeroChunks() {
            Map<Integer,String> chunkMap = Collections.emptyMap();
            assertThat(PackageHelper.validateChunkMap("test", 0, chunkMap)).isEqualTo(true);
        }
    }
}