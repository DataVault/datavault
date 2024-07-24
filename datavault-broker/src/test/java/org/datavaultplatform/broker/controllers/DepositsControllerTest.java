package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepositsControllerTest {

    static final String TEST_ARCHIVE_ID = "test-archive-id";
    static final String TEST_BAG_ID = "test-bag-id";
    static final String TEST_DEPOSIT_ID = "test-deposit-id";
    static final String TEST_RETRIEVE_ID = "test-retrieve-id";
    static final String TEST_RETRIEVE_PATH = "test-retrieve-path";
    static final String TEST_STORAGE_ID = "test-bag-id";
    static final String TEST_USER_ID = "test-user-id";
    static final String TEST_VAULT_ID = "test-vault-id";
    static final String TEST_EVENT_ID = "test-event-id";
    static final String NEW_JOB_ID = "new-job-id";
    static final String DUMMY = "dummy-value";
    static final String RETRIEVE_JSON_NO_RESTART = """
            {
              "taskClass" : "org.datavaultplatform.worker.tasks.Retrieve",
              "jobID" : "new-job-id",
              "properties" : {
                "retrieveId" : "test-retrieve-id",
                "depositId" : "test-deposit-id",
                "userFsRetryDelayMs2" : "234",
                "userFsRetryMaxAttempts" : "222",
                "userFsRetryDelayMs1" : "123",
                "depositCreationDate" : "20240617",
                "archiveSize" : "123456789",
                "userId" : "test-user-id",
                "archiveId" : "test-archive-id",
                "archiveDigestAlgorithm" : "SHA-1",
                "bagId" : "test-bag-id",
                "numOfChunks" : "3",
                "retrievePath" : "test-retrieve-path",
                "depositChunksRetrieved" : "null",
                "archiveDigest" : "tar-digest"
              },
              "fileStorePaths" : null,
              "fileUploadPaths" : null,
              "archiveFileStores" : [ {
                "id" : "archive-store-id",
                "storageClass" : "org.datavaultplatform.common.storage.impl.TivoliStorageManager",
                "label" : "archive-store-label",
                "retrieveEnabled" : true,
                "properties" : {
                  "arc-prop1" : "arc-value1",
                  "arc-prop2" : "arc-value2",
                  "optionsDir" : "test-options-dir",
                  "tempDir" : "test-temp-dir",
                  "tsmRetryTime" : "dummy-value",
                  "tsmMaxRetries" : "dummy-value",
                  "tsmReverse" : "dummy-value"
                }
              } ],
              "userFileStoreProperties" : {
                "test-bag-id" : {
                  "fs-prop1" : "fs-value1",
                  "fs-prop2" : "fs-value2"
                }
              },
              "userFileStoreClasses" : {
                "test-bag-id" : "org.datavaultplatform.common.storage.SFTPFileSystemDriver"
              },
              "chunkFilesDigest" : {
                "1" : "digest-1",
                "2" : "digest-2",
                "3" : "digest-3"
              },
              "tarIV" : "ZW5jLXRhci1pdg==",
              "chunksIVs" : {
                "1" : "b25l",
                "2" : "dHdv",
                "3" : "dGhyZWU="
              },
              "encTarDigest" : "enc-tar-digest",
              "encChunksDigest" : {
                "1" : "enc-digest-1",
                "2" : "enc-digest-2",
                "3" : "enc-digest-3"
              },
              "lastEvent" : null,
              "chunksToAudit" : null,
              "archiveIds" : null,
              "restartArchiveIds" : { },
              "redeliver" : false
            }
            """;
    static final String RETRIEVE_JSON_FOR_RESTART = """
               {
                   "taskClass": "org.datavaultplatform.worker.tasks.Retrieve",
                   "jobID": "new-job-id",
                   "properties": {
                       "retrieveId": "test-retrieve-id",
                       "depositId": "test-deposit-id",
                       "userFsRetryDelayMs2": "234",
                       "userFsRetryMaxAttempts": "222",
                       "userFsRetryDelayMs1": "123",
                       "depositCreationDate": "20240617",
                       "archiveSize": "123456789",
                       "userId": "test-user-id",
                       "archiveId": "test-archive-id",
                       "archiveDigestAlgorithm": "SHA-1",
                       "bagId": "test-bag-id",
                       "numOfChunks": "3",
                       "retrievePath": "test-retrieve-path",
                       "depositChunksRetrieved": "null",
                       "archiveDigest": "tar-digest"
                   },
                   "fileStorePaths": null,
                   "fileUploadPaths": null,
                   "archiveFileStores": [
                       {
                           "id": "archive-store-id",
                           "storageClass": "org.datavaultplatform.common.storage.impl.TivoliStorageManager",
                           "label": "archive-store-label",
                           "retrieveEnabled": true,
                           "properties": {
                               "arc-prop1": "arc-value1",
                               "arc-prop2": "arc-value2",
                               "optionsDir": "test-options-dir",
                               "tempDir": "test-temp-dir",
                               "tsmRetryTime": "dummy-value",
                               "tsmMaxRetries": "dummy-value",
                               "tsmReverse": "dummy-value"
                           }
                       }
                   ],
                   "userFileStoreProperties": {
                       "test-bag-id": {
                           "fs-prop1": "fs-value1",
                           "fs-prop2": "fs-value2"
                       }
                   },
                   "userFileStoreClasses": {
                       "test-bag-id": "org.datavaultplatform.common.storage.SFTPFileSystemDriver"
                   },
                   "chunkFilesDigest": {
                       "1": "digest-1",
                       "2": "digest-2",
                       "3": "digest-3"
                   },
                   "tarIV": "ZW5jLXRhci1pdg==",
                   "chunksIVs": {
                       "1": "b25l",
                       "2": "dHdv",
                       "3": "dGhyZWU="
                   },
                   "encTarDigest": "enc-tar-digest",
                   "encChunksDigest": {
                       "1": "enc-digest-1",
                       "2": "enc-digest-2",
                       "3": "enc-digest-3"
                   },
                   "lastEvent": {
                       "id": "test-event-id",
                       "message": "Archive Store Retrieved Chunk",
                       "retrieveId": "test-retrieve-id",
                       "eventClass": "org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk",
                       "sequence": 0,
                       "persistent": true,
                       "depositId": "test-deposit-id",
                       "jobId": "new-job-id",
                       "userId": "test-user-id"
                   },
                   "chunksToAudit": null,
                   "archiveIds": null,
                   "restartArchiveIds": {},
                   "redeliver": false
               }
       """;
    
    @Mock
    AdminService mAdminService;

    @Mock
    Archive mArchive;

    @Mock
    ArchiveStoreService mArchiveStoreService;

    @Mock
    CreateDeposit mCreateDeposit;

    @Mock
    Deposit mDeposit;

    @Mock
    DepositsService mDepositsService;

    @Mock
    ExternalMetadataService mExternalMetaDataService;

    @Mock
    FilesService mFilesService;

    @Mock
    Job mJob;

    @Mock
    JobsService mJobsService;

    @Mock
    MetadataService mMetaDataService;

    @Mock
    Event mLastEvent;

    @Mock
    Retrieve mRetrieve;

    @Mock
    RetrievesService mRetrievesService;

    @Mock
    Sender mSender;

    @Mock
    User mUser;

    @Mock
    UsersService mUsersService;

    @Mock
    Vault mVault;

    @Mock
    VaultsService mVaultsService;

    FileStore fileStore;

    @Captor
    ArgumentCaptor<Job> argJob;

    @Captor
    ArgumentCaptor<String> argRetrieveJson;

    @Captor
    ArgumentCaptor<Boolean> argIsRestart;

    @Captor
    ArgumentCaptor<String> argVaultId;


    Field jobField;

    ArchiveStore archiveStore;

    DepositChunk dc1;
    DepositChunk dc2;
    DepositChunk dc3;

    Date fixedDate;

    DepositsController controller;

    final String optionsDir = "test-options-dir";
    final String tempDir = "test-temp-dir";
    
    final String s3bucketName = DUMMY;
    final String s3region = DUMMY;
    final String s3accessKey=DUMMY;
    final String s3secretKey=DUMMY;
    final String tsmRetryTime = DUMMY;
    final String occRetryTime = DUMMY;
    final String tsmMaxRetries = DUMMY;
    final String occMaxRetries = DUMMY;
    final String ociNameSpace = DUMMY;
    final String tsmReverse = DUMMY;
    final String ociBucketName = DUMMY;
    final int maxRetryAttempts = 222;
    final long userFsRetryDelaySeconds1 = 123;
    final long userFsRetryDelaySeconds2 = 234;
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

    
    @Nested
    class RetrieveTests {
        @BeforeEach
        void setup() throws Exception {
            
            ZonedDateTime ltd = ZonedDateTime.of(LocalDateTime.of(2024,6,17, 20, 15, 0), ZoneOffset.UTC);
            fixedDate = Date.from(ltd.toInstant());
            
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
            archiveStore.setProperties(hashMapOf("arc-prop1","arc-value1","arc-prop2","arc-value2"));
            archiveStore.setRetrieveEnabled(true);
            archiveStore.setStorageClass(TivoliStorageManager.class.getName());
            lenient().when(mJob.isError()).thenReturn(true);
            
            fileStore = new FileStore(){
                @Override
                public String getID(){
                    return TEST_STORAGE_ID;
                }
            };
            fileStore.setProperties(hashMapOf("fs-prop1","fs-value1","fs-prop2","fs-value2"));
            fileStore.setLabel("file-store-label");
            fileStore.setStorageClass(SFTPFileSystemDriver.class.getName());
        }

        @SuppressWarnings("EqualsWithItself")
        @Test
        void testRetrieveDepositRestart1() throws Exception {
            assertThat(mDeposit).isEqualTo(mDeposit);
            when(mDeposit.getNonRestartJobId()).thenReturn(NEW_JOB_ID);
            lenient().when(mDeposit.getID()).thenReturn("deposit-id");
            //when(mUsersService.getUser(TEST_USER_ID)).thenReturn(mUser);
            when(mAdminService.ensureAdminUser(TEST_USER_ID)).thenReturn(mUser);
            when(mRetrieve.getDeposit()).thenReturn(mDeposit);
            when(mDepositsService.getUserDeposit(mUser, TEST_DEPOSIT_ID)).thenReturn(mDeposit);
            when(mRetrieve.getUser()).thenReturn(mUser);
            when(mRetrievesService.getRetrieve(TEST_RETRIEVE_ID)).thenReturn(mRetrieve);
            when(mDepositsService.getLastNotFailedRetrieveEvent(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID)).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                controller.retrieveDepositRestart(TEST_USER_ID,TEST_DEPOSIT_ID,TEST_RETRIEVE_ID);
            });
            assertThat(ex).hasMessage("There is no last event - so can't restart retrieve");

            verify(mDeposit).getID();
            verify(mDeposit).getNonRestartJobId();
            verify(controller).retrieveDepositRestart(TEST_USER_ID, TEST_DEPOSIT_ID, TEST_RETRIEVE_ID);
            verify(controller, never()).runRetrieveDeposit(mUser, mDeposit, mRetrieve, null);
        }
        
        @SuppressWarnings("EqualsWithItself")
        @Test
        void testRetrieveDepositRestart2() throws Exception {
            when(mDeposit.getNonRestartJobId()).thenReturn(NEW_JOB_ID);
            assertThat(mDeposit).isEqualTo(mDeposit);
            //when(mUsersService.getUser(TEST_USER_ID)).thenReturn(mUser);
            when(mAdminService.ensureAdminUser(TEST_USER_ID)).thenReturn(mUser);
            when(mRetrieve.getDeposit()).thenReturn(mDeposit);
            when(mDepositsService.getUserDeposit(mUser, TEST_DEPOSIT_ID)).thenReturn(mDeposit);
            when(mRetrieve.getUser()).thenReturn(mUser);
            when(mRetrievesService.getRetrieve(TEST_RETRIEVE_ID)).thenReturn(mRetrieve);
            when(mDepositsService.getLastNotFailedRetrieveEvent(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID)).thenReturn(mLastEvent);
            doReturn(true).when(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, mLastEvent);

            boolean result = controller.retrieveDepositRestart(TEST_USER_ID,TEST_DEPOSIT_ID,TEST_RETRIEVE_ID);
            assertThat(result).isTrue();
            verify(controller).runRetrieveDeposit(mUser, mDeposit, mRetrieve, mLastEvent);
            
            verify(mDeposit).getNonRestartJobId();
            verify(mRetrieve, times(2)).getDeposit();
            verify(mDeposit).getID();
            verify(mDepositsService).getLastNotFailedRetrieveEvent(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID);
        }

        @SuppressWarnings("EqualsWithItself")
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

        @SuppressWarnings("EqualsWithItself")
        @Test
        void testRetrieveDepositRestart() throws Exception {
            assertThat(mDeposit).isEqualTo(mDeposit);
            when(mRetrievesService.getRetrieve(TEST_RETRIEVE_ID)).thenReturn(mRetrieve);
            when(mRetrieve.getDeposit()).thenReturn(mDeposit);
            when(mRetrieve.getUser()).thenReturn(mUser);
            when(mUser.getID()).thenReturn(TEST_USER_ID);
            when(mDeposit.getID()).thenReturn(TEST_DEPOSIT_ID);
            doReturn(true).when(controller).retrieveDepositRestart(TEST_USER_ID, TEST_DEPOSIT_ID, TEST_RETRIEVE_ID);

            boolean result = controller.retrieveRestart(TEST_RETRIEVE_ID);
            assertThat(result).isTrue();

            verify(mRetrievesService).getRetrieve(TEST_RETRIEVE_ID);
            verify(mRetrieve).getDeposit();
            verify(mRetrieve).getUser();
            verify(mUser).getID();
            verify(mDeposit).getID();
            verify(controller).retrieveDepositRestart(TEST_USER_ID, TEST_DEPOSIT_ID, TEST_RETRIEVE_ID);
        }
        
        @SuppressWarnings("EqualsWithItself")
        @Test
        void testRetrieveRestartWhenRetrieveNotFound() throws Exception {
            assertThat(mDeposit).isEqualTo(mDeposit);
            when(mRetrievesService.getRetrieve(TEST_RETRIEVE_ID)).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () -> controller.retrieveRestart(TEST_RETRIEVE_ID));
            assertThat(ex).hasMessage("Retrieve 'test-retrieve-id' does not exist");
                    

            verify(mRetrievesService).getRetrieve(TEST_RETRIEVE_ID);
            verify(controller, never()).retrieveDepositRestart(TEST_USER_ID, TEST_DEPOSIT_ID, TEST_RETRIEVE_ID);
        }
        
        @Test
        void testRetrieveDepositRestartWhenRetrieveNotFound() throws Exception {
            when(mAdminService.ensureAdminUser(TEST_USER_ID)).thenReturn(mUser);
            when(mDepositsService.getUserDeposit(mUser, TEST_DEPOSIT_ID)).thenReturn(mDeposit);
            //when(mRetrieve.getUser()).thenReturn(mUser);
            when(mDeposit.getNonRestartJobId()).thenReturn(NEW_JOB_ID);
            Exception ex = assertThrows(Exception.class, () -> controller.retrieveDepositRestart(TEST_USER_ID, TEST_DEPOSIT_ID, TEST_RETRIEVE_ID));
            assertThat(ex).hasMessage("Retrieve 'test-retrieve-id' does not exist");

            verify(mDeposit).getNonRestartJobId();
            verify(mAdminService).ensureAdminUser(TEST_USER_ID);
            verify(mDepositsService).getUserDeposit(mUser, TEST_DEPOSIT_ID);
            verify(mRetrievesService).getRetrieve(TEST_RETRIEVE_ID);
        }

        @Test
        void testRunDepositRetrieveForNonRestart() throws Exception {
            checkRunDeposit(null, RETRIEVE_JSON_NO_RESTART);
        }
        
        @Test
        void testRunDepositRetrieveForRestart() throws Exception {
            ArchiveStoreRetrievedChunk lastEvent = new ArchiveStoreRetrievedChunk(){
                @Override
                public String getID() {
                    return TEST_EVENT_ID;
                }
            };
            lastEvent.setEventClass(ArchiveStoreRetrievedChunk.class.getCanonicalName());
            lastEvent.setJobId(NEW_JOB_ID);
            lastEvent.setChunkNumber(123);
            lastEvent.setDepositId(TEST_DEPOSIT_ID);
            lastEvent.setUserId(TEST_USER_ID);
            lastEvent.setRetrieveId(TEST_RETRIEVE_ID);
            checkRunDeposit(lastEvent, RETRIEVE_JSON_FOR_RESTART);
        }
        
        void checkRunDeposit(Event lastEvent, String expectedJson) throws Exception {
            when(mDeposit.getID()).thenReturn("test-deposit-id");
            when(mDeposit.getBagId()).thenReturn("test-bag-id");
            lenient().doReturn(NEW_JOB_ID).when(mDeposit).getNonRestartJobId();
            lenient().when(mRetrieve.getID()).thenReturn(TEST_RETRIEVE_ID);
            when(mDepositsService.getDepositForRetrieves("test-deposit-id")).thenReturn(mDeposit);
            when(mDepositsService.getChunksRetrieved(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID)).thenReturn(null);
            
            if (lastEvent == null) {
                doNothing().when(mDeposit).setNonRestartJobId(any(String.class));
                when(mDeposit.getJobs()).thenReturn(List.of(mJob));
            } else {
                doReturn(NEW_JOB_ID).when(mDeposit).getNonRestartJobId();
            }
            
            when(mArchiveStoreService.getForRetrieval()).thenReturn(archiveStore);

            when(mArchive.getArchiveStore()).thenReturn(archiveStore);
            when(mArchive.getArchiveId()).thenReturn(TEST_ARCHIVE_ID);
 
            when(mDeposit.getArchives()).thenReturn(List.of(mArchive));
            
            when(mDeposit.getArchiveSize()).thenReturn(123_456_789L);
            when(mDeposit.getDepositChunks()).thenReturn(List.of(dc1,dc2,dc3));
            when(mDeposit.getNumOfChunks()).thenReturn(3);
            when(mDeposit.getArchiveDigestAlgorithm()).thenReturn(Verify.SHA_1_ALGORITHM);
            when(mDeposit.getArchiveDigest()).thenReturn("tar-digest");
            when(mDeposit.getEncArchiveDigest()).thenReturn("enc-tar-digest");
            when(mDeposit.getEncIV()).thenReturn("enc-tar-iv".getBytes(StandardCharsets.UTF_8));
            when(mDeposit.getCreationTime()).thenReturn(fixedDate);

            when(mFilesService.validPath(TEST_RETRIEVE_PATH, fileStore)).thenReturn(true);

            // within doAnswer we can simulate the id being generated when the job is added
            doAnswer(invocation -> {
                        Job job = invocation.getArgument(1, Job.class);
                        jobField.set(job, NEW_JOB_ID);
                        return null;
                    }
            ).when(mJobsService).addJob(eq(mDeposit), argJob.capture());

            if (lastEvent == null) {
                doNothing().when(mRetrieve).setUser(mUser);
            }
            when(mRetrieve.getRetrievePath()).thenReturn(TEST_STORAGE_ID+"/" + TEST_RETRIEVE_PATH);

            if (lastEvent == null) {
                doNothing().when(mRetrievesService).addRetrieve(eq(mRetrieve), eq(mDeposit), any(String.class));
            }
            when(mSender.send(argRetrieveJson.capture(), argIsRestart.capture())).thenReturn("MESSAGE_ID_123");

            when(mUser.getFileStores()).thenReturn(List.of(fileStore));

            when(mVault.getID()).thenReturn(TEST_VAULT_ID);

            when(mVaultsService.checkRetentionPolicy(argVaultId.capture())).thenReturn(mVault);

            doReturn(mVault).when(mDeposit).getVault();

            boolean result = controller.runRetrieveDeposit(mUser, mDeposit, mRetrieve, lastEvent);
            assertThat(result).isTrue();

            System.out.println(argRetrieveJson.getValue());
            JSONAssert.assertEquals(expectedJson, argRetrieveJson.getValue(), false);

            verify(mDepositsService).getDepositForRetrieves("test-deposit-id");
            
            verify(mArchiveStoreService).getForRetrieval();

            verify(mFilesService).validPath(TEST_RETRIEVE_PATH, fileStore);

            Job addedJob = argJob.getValue();
            assertThat(addedJob.getID()).isEqualTo(NEW_JOB_ID);
            verify(mJobsService).addJob(any(Deposit.class),any(Job.class));

            if(lastEvent == null) {
                verify(mDepositsService).updateDeposit(mDeposit);
                verify(mDeposit).getJobs();
                verify(mDeposit).setNonRestartJobId(any(String.class));
            }
            verify(mDeposit, atLeast(1)).getArchives();
            verify(mArchive, atLeast(1)).getArchiveStore();
            verify(mArchive).getArchiveId();
            verify(mDeposit).getNonRestartJobId();
            verify(mDeposit).getArchiveSize();
            verify(mDeposit, times(3)).getDepositChunks();
            verify(mDeposit).getNumOfChunks();
            verify(mDeposit).getArchiveDigestAlgorithm();
            verify(mDeposit).getNonRestartJobId();
            verify(mDeposit).getArchiveDigest();
            verify(mDeposit).getEncArchiveDigest();
            verify(mDeposit, times(3)).getID();
            verify(mDeposit).getBagId();

            verify(mDepositsService).getChunksRetrieved(TEST_DEPOSIT_ID, TEST_RETRIEVE_ID);

            if(lastEvent == null) {
                verify(mJob).isError();
            }

            verify(mRetrieve,times(2)).getID();
            if(lastEvent == null) {
                verify(mRetrieve).setUser(mUser);
            }
            verify(mRetrieve, atLeast(1)).getRetrievePath();

            verify(mUser).getID();
            verify(mUser).getFileStores();

            verify(mVaultsService).checkRetentionPolicy(TEST_VAULT_ID);
        }
        
        private HashMap<String,String> hashMapOf(String k1, String p1, String k2, String p2){
            return new LinkedHashMap<>(new TreeMap<>(Map.of(k1,p1,k2,p2)));
        }
        
        @AfterEach
        void tearDown(){
            verifyNoMoreInteractions(
                    mAdminService, mArchive, mArchiveStoreService, mCreateDeposit,
                    mDeposit, mDepositsService, mExternalMetaDataService, mFilesService,
                    mJob, mJobsService, mMetaDataService,
                    mRetrieve, mRetrievesService, mSender,
                    mUser, mUsersService, mVault, mVaultsService);
        }
    }
}
