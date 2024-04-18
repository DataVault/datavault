package org.datavaultplatform.broker.services;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.FileInfo;
import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.storage.UserStore;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class FilesServiceTest {

    public static final long TIMEOUT_MS = 100;
    @Mock
    StorageClassNameResolver mStorageClassNameResolver;

    @Mock
    UserStore mUserStore;

    @Mock
    FileStore mFileStore;

    @Mock
    FileInfo mFileInfo1;

    @Mock
    FileInfo mFileInfo2;

    FilesService sut;

    @BeforeEach
    void setup() {
        sut = new FilesService(mStorageClassNameResolver);
        sut.setTimeout(Duration.ofMillis(TIMEOUT_MS));
    }

    @Nested
    class GetFilesListing {
        
        @Test
        void testUserStoreExists() {

            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {

                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(mUserStore);

                when(mUserStore.list("testFilePath")).thenReturn(Arrays.asList(mFileInfo1, mFileInfo2));

                List<FileInfo> result = sut.getFilesListing("testFilePath", mFileStore);

                assertThat(result).containsExactly(mFileInfo1, mFileInfo2);
            }
        }

        @Test
        void testUserStoreDoesNotExists() {

            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {

                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(null);

                List<FileInfo> result = sut.getFilesListing("testFilePath", mFileStore);

                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    class GetFilesizeTests {

        @Test
        void testFileSizeNoUserStore() {
            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {
                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(null);
                Long result = sut.getFilesize("testFilePath", mFileStore);
                assertThat(result).isNull();
            }
        }

        @Test
        void testFileSizeSuccess() throws Exception {
            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {
                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(mUserStore);

                when(mUserStore.getSize("testFilePath")).thenReturn(1234L);

                long start = System.currentTimeMillis();
                Long result = sut.getFilesize("testFilePath", mFileStore);
                assertThat(result).isEqualTo(1234L);
                long diff = System.currentTimeMillis() - start;
                assertThat(diff).isLessThan(TIMEOUT_MS);
            }
        }

        @Test
        void testFileSizeSuccessTimedOut() throws Exception {
            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {

                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(mUserStore);

                when(mUserStore.getSize("testFilePath")).thenAnswer(invocationOnMock -> {
                    TimeUnit.MILLISECONDS.sleep(TIMEOUT_MS * 2);
                    return 9999L;
                });

                long start = System.currentTimeMillis();
                Long result = sut.getFilesize("testFilePath", mFileStore);
                long diff = System.currentTimeMillis() - start;
                assertThat(result).isNull();
                assertThat(diff).isBetween(TIMEOUT_MS, TIMEOUT_MS * 2);
            }
        }
        
        @Test
        void testUserStoreThrowsException() throws Exception {
            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {
                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(mUserStore);

                when(mUserStore.getSize("testFilePath")).thenThrow(new Exception("oops"));

                long start = System.currentTimeMillis();
                Long result = sut.getFilesize("testFilePath", mFileStore);
                assertThat(result).isNull();
                
                long diff = System.currentTimeMillis() - start;
                assertThat(diff).isLessThan(TIMEOUT_MS);
            }
        }
    }

    @Nested
    class ValidPathTests {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void testUserStoreExists(boolean isValid) {

            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {

                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(mUserStore);

                when(mUserStore.valid("testFilePath")).thenReturn(isValid);

                boolean result = sut.validPath("testFilePath", mFileStore);

                assertThat(result).isEqualTo(isValid);
            }
        }

        @Test
        void testUserStoreDoesNotExists() {

            try (MockedStatic<UserStore> mockedUserStore = Mockito.mockStatic(UserStore.class)) {

                mockedUserStore.when(() -> UserStore.fromFileStore(mFileStore, mStorageClassNameResolver)).thenReturn(null);

                boolean result = sut.validPath("testFilePath", mFileStore);

                assertThat(result).isFalse();
            }
        }


    }


    @Nested
    class TimeoutTests {

        @Test
        void testResultReturned() throws Exception {
            Callable<Long> callable = () -> {
                Thread.sleep(300);
                return 1234L;
            };
            long start = System.currentTimeMillis();
            Long result = FilesService.executeWithTimeout(callable, Duration.ofMillis(400));
            long diff = System.currentTimeMillis() - start;
            assertThat(diff).isBetween(300L, 400L);
            assertThat(result).isEqualTo(1234);
        }

        @SuppressWarnings("CodeBlock2Expr")
        @Test
        void testExceptionThrown() {
            Callable<Long> callable = () -> {
                throw new Exception("oops");
            };
            long start = System.currentTimeMillis();
            Exception ex = assertThrows(Exception.class, () -> {
                FilesService.executeWithTimeout(callable, Duration.ofMillis(400));
            });
            assertThat(ex).isInstanceOf(Exception.class).hasMessage("oops");
            long diff = System.currentTimeMillis() - start;
            assertThat(diff).isLessThan(400L);
        }
        @SuppressWarnings("CodeBlock2Expr")
        @Test
        void testErrorThrown() {
            Callable<Long> callable = () -> {
                throw new Error("oops");
            };
            long start = System.currentTimeMillis();
            Error ex = assertThrows(Error.class, () -> {
                FilesService.executeWithTimeout(callable, Duration.ofMillis(400));
            });
            assertThat(ex).isInstanceOf(Error.class).hasMessage("oops");
            long diff = System.currentTimeMillis() - start;
            assertThat(diff).isLessThan(400L);
        }

        @Test
        void testTimedOut() throws Exception {
            Callable<Long> callable = () -> {
                Thread.sleep(300);
                return 1234L;
            };
            long start = System.currentTimeMillis();
            Long result = FilesService.executeWithTimeout(callable, Duration.ofMillis(200));
            long diff = System.currentTimeMillis() - start;
            assertThat(diff).isBetween(200L, 300L);
            assertThat(result).isNull();
        }

    }

}