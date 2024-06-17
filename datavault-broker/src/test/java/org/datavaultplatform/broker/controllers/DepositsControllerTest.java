package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepositsControllerTest {
    
    static String TEST_USER_ID = "test-user-id";
    static String TEST_DEPOSIT_ID = "test-deposit-id";
    static String TEST_RETRIEVE_ID = "test-retrieve-id";
    static String TEST_BAG_ID = "test-bag-id";
    
    @Mock
    VaultsService mVaultsService;

    @Mock
    RetrievesService mRetrievesService;

    @Mock
    DepositsService mDepositsService;

    @Mock
    MetadataService mMetaDataService;

    @Mock
    ExternalMetadataService mExternalMetaDataService;

    @Mock
    UsersService mUsersService;

    @Mock
    FilesService mFilesService;

    @Mock
    ArchiveStoreService mArchiveStoreService;

    @Mock
    JobsService mJobsService;

    @Mock
    AdminService mAdminService;

    @Mock
    Sender mSender;
    
    DepositsController controller;

    String optionsDir = "test-options-dir";
    String tempDir = "test-temp-dir";
    
    String s3bucketName = "";
    String s3region = "";
    String s3accessKey;
    String s3secretKey;
    String tsmRetryTime = "";
    String occRetryTime = "";
    String tsmMaxRetries = "";
    String occMaxRetries = "";
    String ociNameSpace;
    String tsmReverse;
    String ociBucketName;
    int maxRetryAttempts = 222;
    long userFsRetryDelaySeconds1 = 123;
    long userFsRetryDelaySeconds2 = 234;
    ObjectMapper mapper;
    
    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        controller = Mockito.spy(new DepositsController(
                mVaultsService, mDepositsService,
                mRetrievesService, mMetaDataService,
                
                mExternalMetaDataService, mFilesService,
                mUsersService, mArchiveStoreService, 
                
                mJobsService, mAdminService, mSender,

                optionsDir, tempDir,
                s3bucketName,  s3region, s3accessKey, s3secretKey, 
                
                tsmRetryTime, occRetryTime, tsmMaxRetries, occMaxRetries, ociNameSpace, 
                tsmReverse, ociBucketName, maxRetryAttempts, 
                userFsRetryDelaySeconds1,userFsRetryDelaySeconds2, mapper));
    }
    
    @Mock
    User mUser;

    @Mock
    Deposit mDeposit;

    @Mock
    Retrieve mRetrieve;
    
    @Mock
    Event mLastEvent;

    @Mock
    CreateDeposit mCreateDeposit;

    @Mock
    FileStore mFileStore;

    @Mock
    Archive mArchive;
    
    @Captor
    ArgumentCaptor<Job> argJob;

    @Captor
    ArgumentCaptor<String> argRetrieveJson;

    @Captor
    ArgumentCaptor<Boolean> argIsRestart;

    @Captor
    ArgumentCaptor<String> argVaultId;

    @Mock
    Vault mVault;

    @Mock
    Job mJob;

    Field jobField;

    ArchiveStore archiveStore;

    DepositChunk dc1;
    DepositChunk dc2;
    DepositChunk dc3;

    @Nested
    class RetrieveTests {
        @BeforeEach
        void setup() throws Exception {
            
            dc1 = new DepositChunk();
            dc1.setChunkNum(1);
            dc1.setArchiveDigest("digest-1");
            dc1.setEcnArchiveDigest("enc-digest-1");
            dc1.setEncIV("one".getBytes(StandardCharsets.UTF_8));

            dc2 = new DepositChunk();
            dc2.setChunkNum(2);
            dc2.setArchiveDigest("digest-2");
            dc2.setEcnArchiveDigest("enc-digest-2");
            dc2.setEncIV("two".getBytes(StandardCharsets.UTF_8));

            dc3 = new DepositChunk();
            dc3.setChunkNum(3);
            dc3.setArchiveDigest("digest-3");
            dc3.setEcnArchiveDigest("enc-digest-3");
            dc3.setEncIV("three".getBytes(StandardCharsets.UTF_8));
            
            lenient().when(mUser.getID()).thenReturn(TEST_USER_ID);
            
            jobField = Job.class.getDeclaredField("id");
            jobField.setAccessible(true);

            lenient().when(mDeposit.getBagId()).thenReturn(TEST_BAG_ID);
            lenient().when(mDeposit.getID()).thenReturn(TEST_DEPOSIT_ID);
            lenient().when(mRetrieve.getID()).thenReturn(TEST_RETRIEVE_ID);
            lenient().when(mRetrieve.getDeposit()).thenReturn(mDeposit);
            archiveStore = new ArchiveStore() {
                @Override
                public String getID() {
                    return "archive-store-id";
                }
            };
            archiveStore.setLabel("archive-store-label");
            archiveStore.setProperties(new HashMap<>(Map.of("prop1","value1","prop2","value2")));
            archiveStore.setRetrieveEnabled(true);
            archiveStore.setStorageClass(TivoliStorageManager.class.getName());
            //lenient().when(mJob.getID()).thenReturn("job-id-123");
            lenient().when(mJob.isError()).thenReturn(true);
            //lenient().when(mJob.getState()).thenReturn(0);
            //lenient().when(mJob.getStates()).thenReturn(new ArrayList(List.of("one-state")));
        }

        @Test
        void testRetrieveDepositRestart1() throws Exception {
            assertThat(mDeposit).isEqualTo(mDeposit);
            when(mUsersService.getUser(TEST_USER_ID)).thenReturn(mUser);
            when(mRetrieve.getDeposit()).thenReturn(mDeposit);
            when(mDepositsService.getUserDeposit(mUser, TEST_DEPOSIT_ID)).thenReturn(mDeposit);
            when(mRetrievesService.getRetrieve(TEST_RETRIEVE_ID)).thenReturn(mRetrieve);
            when(mDepositsService.getLastNotFailedRetrieveEvent(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID)).thenReturn(null);
            doReturn(true).when(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, null);
            
            boolean result = controller.retrieveDepositRestart(TEST_USER_ID,TEST_DEPOSIT_ID,TEST_RETRIEVE_ID);
            assertThat(result).isTrue();
            verify(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, null);
        }
        @Test
        void testRetrieveDepositRestart2() throws Exception {
            assertThat(mDeposit).isEqualTo(mDeposit);
            when(mUsersService.getUser(TEST_USER_ID)).thenReturn(mUser);
            when(mRetrieve.getDeposit()).thenReturn(mDeposit);
            when(mDepositsService.getUserDeposit(mUser, TEST_DEPOSIT_ID)).thenReturn(mDeposit);
            when(mRetrievesService.getRetrieve(TEST_RETRIEVE_ID)).thenReturn(mRetrieve);
            when(mDepositsService.getLastNotFailedRetrieveEvent(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID)).thenReturn(mLastEvent);
            doReturn(true).when(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, mLastEvent);

            boolean result = controller.retrieveDepositRestart(TEST_USER_ID,TEST_DEPOSIT_ID,TEST_RETRIEVE_ID);
            assertThat(result).isTrue();
            verify(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, mLastEvent);
        }

        @Test
        void testRetrieveDeposit() throws Exception {
            assertThat(mDeposit).isEqualTo(mDeposit);
            when(mUsersService.getUser(TEST_USER_ID)).thenReturn(mUser);
            
            when(mDepositsService.getUserDeposit(mUser, TEST_DEPOSIT_ID)).thenReturn(mDeposit);
            
            doReturn(true).when(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, null);

            boolean result = controller.retrieveDeposit(TEST_USER_ID,TEST_DEPOSIT_ID, mRetrieve);
            assertThat(result).isTrue();
            
            verify(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, null);
        }
        
        @Test
        void testRunDepositRetrieveForNonRestart() throws Exception {
            when(mFileStore.getStorageClass()).thenReturn("USER_STORE_CLASS");
            when(mArchiveStoreService.getForRetrieval()).thenReturn(archiveStore);
            when(mArchive.getArchiveId()).thenReturn("archive-id-1");
            when(mRetrieve.getRetrievePath()).thenReturn("abc");
            when(mDeposit.getJobs()).thenReturn(List.of(mJob));
            when(mDeposit.getArchives()).thenReturn(List.of(mArchive));
            when(mDeposit.getArchiveSize()).thenReturn(123_456_789L);
            when(mDeposit.getDepositChunks()).thenReturn(List.of(dc1,dc2,dc3));
            when(mDeposit.getNumOfChunks()).thenReturn(4);
            when(mDeposit.getArchiveDigestAlgorithm()).thenReturn(Verify.SHA_1_ALGORITHM);
            when(mArchive.getArchiveStore()).thenReturn(archiveStore);
            when(mDeposit.getArchiveDigest()).thenReturn("tar-digest");
            when(mDeposit.getEncArchiveDigest()).thenReturn("enc-tar-digest");
            when(mDeposit.getEncIV()).thenReturn("enc-tar-iv".getBytes(StandardCharsets.UTF_8));
            lenient().when(mArchiveStoreService.getArchiveStores()).thenReturn(List.of(archiveStore));
            doNothing().when(mRetrieve).setUser(mUser);
            
            when(mFileStore.getID()).thenReturn("abc");
            when(mUser.getFileStores()).thenReturn(List.of(mFileStore));
            
            when(mRetrieve.getRetrievePath()).thenReturn("abc/def");
            when(mDeposit.getCreationTime()).thenReturn(new Date());
            
            when(mFilesService.validPath("def", mFileStore)).thenReturn(true);

            doAnswer(invocation -> {
                        Job job = invocation.getArgument(1, Job.class);
                        jobField.set(job, "new-job-id");
                        return null;
                    }
            ).when(mJobsService).addJob(eq(mDeposit), argJob.capture());

            doNothing().when(mRetrievesService).addRetrieve(eq(mRetrieve), eq(mDeposit), any(String.class));
            
            when(mSender.send(argRetrieveJson.capture(), argIsRestart.capture())).thenReturn("MESSAGE_ID_123");
            
            when(mVaultsService.checkRetentionPolicy(argVaultId.capture())).thenReturn(mVault);
            
            doReturn(mVault).when(mDeposit).getVault();
            
            boolean result = controller.runRetrieveDeposit(mUser, mDeposit, mRetrieve, null);
            assertThat(result).isTrue();
            
            System.out.println(argRetrieveJson.getValue());
        }
    }
}
