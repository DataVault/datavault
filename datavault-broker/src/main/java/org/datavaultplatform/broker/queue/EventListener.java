package org.datavaultplatform.broker.queue;

import static org.datavaultplatform.broker.config.QueueConfig.BROKER_QUEUE_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.ArchivesService;
import org.datavaultplatform.broker.services.AuditsService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.audit.AuditComplete;
import org.datavaultplatform.common.event.audit.AuditError;
import org.datavaultplatform.common.event.audit.AuditStart;
import org.datavaultplatform.common.event.audit.ChunkAuditComplete;
import org.datavaultplatform.common.event.audit.ChunkAuditStarted;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.event.deposit.ChunksDigestEvent;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.event.deposit.PackageComplete;
import org.datavaultplatform.common.event.deposit.Start;
import org.datavaultplatform.common.event.deposit.StartCopyUpload;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.event.deposit.ValidationComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveError;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Component
@Transactional
public class EventListener implements MessageListener {

  public static final int MAX_ATTEMPTS = 5;
  public static final String TYPE_COMPLETE = "complete";
  public static final String TYPE_ERROR = "error";
  public static final String TYPE_RETRIEVE_START = "retrievestart";
  public static final String TYPE_RETRIEVE_COMPLETE = "retrievecomplete";
  public static final String TYPE_START = "start";
  public static final String AUDIT_CHUNK_PREFIX = "audit-chunk-";
  public static final String EMAIL_KEY_ARCHIVE_ID = "archive-id";
  public static final String EMAIL_KEY_AUDIT_ID = "audit-id";
  public static final String EMAIL_KEY_CHUNK_ID = "chunk-id";
  public static final String EMAIL_KEY_DEPOSIT_ID = "deposit-id";
  public static final String EMAIL_KEY_DEPOSIT_NAME = "deposit-name";
  public static final String EMAIL_KEY_GROUP_NAME = "group-name";
  public static final String EMAIL_KEY_HAS_PERSONAL_DATA = "hasPersonalData";
  public static final String EMAIL_KEY_HELP_MAIL = "help-mail";
  public static final String EMAIL_KEY_HELP_PAGE = "help-page";
  public static final String EMAIL_KEY_HOME_PAGE = "home-page";
  public static final String EMAIL_KEY_LOCATION = "location";
  public static final String EMAIL_KEY_RETRIEVER_FIRSTNAME = "retriever-firstname";
  public static final String EMAIL_KEY_RETRIEVER_ID = "retriever-id";
  public static final String EMAIL_KEY_RETRIEVER_LASTNAME = "retriever-lastname";
  public static final String EMAIL_KEY_SIZE_BYTES = "size-bytes";
  public static final String EMAIL_KEY_TIMESTAMP = "timestamp";
  public static final String EMAIL_KEY_USER_FIRSTNAME = "user-firstname";
  public static final String EMAIL_KEY_USER_ID = "user-id";
  public static final String EMAIL_KEY_USER_LASTNAME = "user-lastname";
  public static final String EMAIL_KEY_VAULT_ID = "vault-id";
  public static final String EMAIL_KEY_VAULT_NAME = "vault-name";
  public static final String EMAIL_KEY_VAULT_REVIEW_DATE = "vault-review-date";
  public static final String EMAIL_KEY_ERROR_MESSAGE = "error-message";
  public static final String SUBJECT_START = "Confirmation - a new DataVault deposit is starting.";
  public static final String SUBJECT_COMPLETE = "Confirmation - a new DataVault deposit is complete.";
  public static final String SUBJECT_RETRIEVE_START = "Confirmation - a new DataVault retrieval is starting.";
  public static final String SUBJECT_RETRIEVE_COMPLETE = "Confirmation - a new DataVault retrieval is complete.";
  public static final String SUBJECT_ERROR = "DataVault ERROR - attempted deposit or retrieval of a deposit failed";
  public static final String SUBJECT_AUDIT_ERROR = "DataVault ERROR - Chunk failed checksum during audit.";
  private static final Map<String, String> EMAIL_SUBJECTS;
  private static final String ADMIN_DEPOSIT_PREFIX = "admin-deposit-";
  private static final String USER_DEPOSIT_PREFIX = "user-deposit-";

  static {
    EMAIL_SUBJECTS = new HashMap<>();

    EMAIL_SUBJECTS.put(USER_DEPOSIT_PREFIX + TYPE_START,
        SUBJECT_START);
    EMAIL_SUBJECTS.put(USER_DEPOSIT_PREFIX + TYPE_COMPLETE,
        SUBJECT_COMPLETE);
    EMAIL_SUBJECTS.put(USER_DEPOSIT_PREFIX + TYPE_ERROR,
        SUBJECT_ERROR);
    EMAIL_SUBJECTS.put(USER_DEPOSIT_PREFIX + TYPE_RETRIEVE_START,
        SUBJECT_RETRIEVE_START);
    EMAIL_SUBJECTS.put(USER_DEPOSIT_PREFIX + TYPE_RETRIEVE_COMPLETE,
        SUBJECT_RETRIEVE_COMPLETE);

    EMAIL_SUBJECTS.put(ADMIN_DEPOSIT_PREFIX + TYPE_START,
        SUBJECT_START);
    EMAIL_SUBJECTS.put(ADMIN_DEPOSIT_PREFIX + TYPE_COMPLETE,
        SUBJECT_COMPLETE);
    EMAIL_SUBJECTS.put(ADMIN_DEPOSIT_PREFIX + TYPE_ERROR,
        SUBJECT_ERROR);
    EMAIL_SUBJECTS.put(ADMIN_DEPOSIT_PREFIX + TYPE_RETRIEVE_START,
        SUBJECT_RETRIEVE_START);
    EMAIL_SUBJECTS.put(ADMIN_DEPOSIT_PREFIX + TYPE_RETRIEVE_COMPLETE,
        SUBJECT_RETRIEVE_COMPLETE);

    EMAIL_SUBJECTS.put(AUDIT_CHUNK_PREFIX + TYPE_ERROR,
        SUBJECT_AUDIT_ERROR);
  }

  private final Clock clock;
  private final JobsService jobsService;
  private final EventService eventService;
  private final VaultsService vaultsService;
  private final DepositsService depositsService;
  private final ArchiveStoreService archiveStoreService;
  private final ArchivesService archivesService;
  private final RetrievesService retrievesService;
  private final UsersService usersService;
  private final EmailService emailService;
  private final AuditsService auditsService;
  private final String homeUrl;
  private final String helpUrl;
  private final String helpMail;
  private final String auditAdminEmail;

  // preDeleteStatus is an instance variable - NOT thread safe
  // it should at least be in a map keyed by job id ????
  Deposit.Status preDeletionStatus;

  @Autowired
  public EventListener(
      Clock clock,
      JobsService jobsService,
      VaultsService vaultsService,
      DepositsService depositsService,
      ArchiveStoreService archiveStoreService,
      ArchivesService archivesService,
      RetrievesService retrievesService,
      EventService eventService,
      UsersService usersService,
      EmailService emailService,
      AuditsService auditsService,
      @Value("${home.page}") String homeUrl,
      @Value("${help.page}") String helpUrl,
      @Value("${help.mail}") String helpMail,
      @Value("${audit.adminEmail}") String auditAdminEmail) {
    this.clock = clock;
    this.jobsService = jobsService;
    this.vaultsService = vaultsService;
    this.depositsService = depositsService;
    this.archiveStoreService = archiveStoreService;
    this.archivesService = archivesService;
    this.retrievesService = retrievesService;
    this.eventService = eventService;
    this.usersService = usersService;
    this.emailService = emailService;
    this.auditsService = auditsService;
    this.homeUrl = homeUrl;
    this.helpUrl = helpUrl;
    this.helpMail = helpMail;
    this.auditAdminEmail = auditAdminEmail;

    Assert.isTrue(StringUtils.isNotBlank(this.homeUrl), () -> "homeUrl is blank");
    Assert.isTrue(StringUtils.isNotBlank(this.helpMail), () -> "helpMail is blank");
    Assert.isTrue(StringUtils.isNotBlank(this.helpUrl), () -> "helpUrl is blank");
    Assert.isTrue(StringUtils.isNotBlank(this.auditAdminEmail), () -> "auditAdminEmail is blank");
  }


  @Override
  @RabbitListener(queues = BROKER_QUEUE_NAME)
  public void onMessage(Message msg) {

    String messageBody = new String(msg.getBody(), StandardCharsets.UTF_8);
    log.info("Received [{}]", messageBody);

    try {
      onMessageInternal(messageBody);
    } catch (Exception ex) {
      log.error("unexpected exception processing message [{}]", messageBody, ex);
    }
  }

  public Event onMessageInternal(String messageBody) throws Exception {

    // Decode the event
    Event event = getConcreteEvent(messageBody);

    // Get the related deposit
    Deposit deposit = null;
    String depositId = event.getDepositId();

    if (depositId != null) {
      deposit = getDeposit(event.getDepositId());
      event.setDeposit(deposit);
    }

    // Get the related job
    Job job = getJob(event.getJobId());
    event.setJob(job);

    // Get the related User
    String userId = event.getUserId();
    if (userId != null) {
      User initUser = getUser(event.getUserId());
      event.setUser(initUser);
    }

    if (event.getPersistent()) {
      // Persist the event properties in the database ...
      eventService.addEvent(event);
    }

    // Update with next state information (if present)
    updateJobToNextState(event, job);

    processEvent(messageBody, event, deposit, job);
    return event;
  }

  void processEvent(String messageBody, Event event, Deposit deposit, Job job)
      throws Exception {

    log.info("[{}] JobId[{}]", event.getClass().getSimpleName(), event.getJobId());

    // perform an action based on the event type ...
    if (event instanceof InitStates) {
      process01InitStates((InitStates) event, job);

    } else if (event instanceof UpdateProgress) {
      process02UpdateProcess((UpdateProgress) event, job);

    } else if (event instanceof Start) {
      process03Start((Start) event, deposit);

    } else if (event instanceof ComputedSize) {
      process04ComputedSize((ComputedSize) event, deposit);

    } else if (event instanceof ComputedChunks) {
      process05ComputedChunks((ComputedChunks) event, deposit);

    } else if (event instanceof ComputedEncryption) {
      process06ComputedEncryption((ComputedEncryption) event, deposit);

    } else if (event instanceof ComputedDigest) {
      process07ComputedDigest((ComputedDigest) event, deposit);

    } else if (event instanceof UploadComplete) {
      process08UploadComplete((UploadComplete) event, deposit);

    } else if (event instanceof Complete) {
      process09Complete((Complete) event, deposit);

    } else if (event instanceof Error) {
      process10Error((Error) event, deposit, job);

    } else if (event instanceof RetrieveStart) {
      process11RetrieveStart((RetrieveStart) event, deposit);

    } else if (event instanceof RetrieveComplete) {
      process12RetrieveComplete((RetrieveComplete) event, deposit);

    } else if (event instanceof DeleteStart) {
      process13DeleteStart((DeleteStart) event, deposit);

    } else if (event instanceof DeleteComplete) {
      process14DeleteComplete((DeleteComplete) event, deposit);

    } else if (event instanceof AuditStart) {
      process15AuditStart((AuditStart) event);

    } else if (event instanceof ChunkAuditStarted) {
      process16ChunkAuditStarted((ChunkAuditStarted) event);

    } else if (event instanceof ChunkAuditComplete) {
      process17ChunkAuditComplete((ChunkAuditComplete) event);

    } else if (event instanceof AuditComplete) {
      process18AuditComplete((AuditComplete) event);

    } else if (event instanceof AuditError) {
      process19AuditError((AuditError) event);

    } else if (event instanceof TransferComplete) {
      process20TransferComplete((TransferComplete) event);

    } else if (event instanceof PackageComplete) {
      process21PackageComplete((PackageComplete) event);

    } else if (event instanceof StartCopyUpload) {
      process22StartCopyUpload((StartCopyUpload) event);

    } else if (event instanceof CompleteCopyUpload) {
      process23CompleteCopyUpload((CompleteCopyUpload) event, deposit);

    } else if (event instanceof ValidationComplete) {
      process24ValidationComplete((ValidationComplete) event);

    } else if (event instanceof RetrieveError) {
      process25RetrieveError((RetrieveError) event);

    } else {
      throw new Exception(
          String.format("Failed to process unknown Event class[%s]message[%s]", event.getClass(),
              messageBody));
    }
  }

  @SneakyThrows
  Event getConcreteEvent(String messageBody) {
    ObjectMapper mapper = new ObjectMapper();

    //Event Mapper using wrong Event constructor for AuditComplete? 4 param v 6 v message?  hopefully using message instead of 6 for audit
    Event commonEvent = mapper.readValue(messageBody, Event.class);

    @SuppressWarnings("unchecked")
    Class<? extends Event> clazz = Class.forName(commonEvent.getEventClass()).asSubclass(Event.class);
    Event concreteEvent = mapper.readValue(messageBody, clazz);
    return concreteEvent;
  }

  void updateJobToNextState(Event event, Job job) {
    Assert.isTrue(event != null, () -> "The event cannot be null");
    Assert.isTrue(job != null, () -> "The job cannot be null");
    Integer nextState = event.nextState;
    if (nextState == null) {
      log.warn("No Event Next State for JobId[{}]", job.getID());
      return;
    }
    processJob(job, $job -> {
      if ($job.getState() == null || ($job.getState() < nextState)) {
        $job.setState(nextState);
        jobsService.updateJob($job);
      }
    });
  }

  void validateType(String type) {
    List<String> validTypes = List.of(
        TYPE_START,
        TYPE_COMPLETE,
        TYPE_RETRIEVE_START,
        TYPE_RETRIEVE_COMPLETE,
        TYPE_ERROR);
    if (type == null || validTypes.contains(type) == false) {
      throw new IllegalArgumentException("Invalid Type " + type);
    }
  }

  void sendEmails(Deposit deposit, Event event, String type,
      String userTemplate, String adminTemplate) {

    validateType(type);

    // Get related information for emails
    Vault vault = deposit.getVault();
    Group group = vault.getGroup();
    User depositUser = deposit.getUser();
    User retrieveUser = usersService.getUser(event.getUserId());

    Map<String, Object> model = getModel(deposit, depositUser, event, retrieveUser, vault, group);

    String userSubject = getUserSubject(type);
    String adminSubject = getAdminSubject(type);

    // Send email to group owners
    for (User groupAdmin : group.getOwners()) {
      String adminEmail = groupAdmin.getEmail();
      log.info("GroupAdmin email is " + adminEmail);
      sendTemplateEmail(adminEmail,
          adminSubject,
          adminTemplate,
          model);
    }

    // Send email to the deposit user
    String depositUserEmail = depositUser.getEmail();
    log.info("DepositUser email is " + depositUserEmail);
    sendTemplateEmail(depositUserEmail,
        userSubject,
        userTemplate,
        model);
  }

  Map<String, Object> getModel(Deposit deposit, User depositUser, Event event,
      User retrieveUser, Vault vault, Group group) {
    Map<String, Object> model = new HashMap<>();
    model.put(EMAIL_KEY_GROUP_NAME, group.getName());
    model.put(EMAIL_KEY_DEPOSIT_NAME, deposit.getName());
    model.put(EMAIL_KEY_DEPOSIT_ID, deposit.getID());
    model.put(EMAIL_KEY_VAULT_NAME, vault.getName());
    model.put(EMAIL_KEY_VAULT_ID, vault.getID());
//        model.put("vault-retention-expiry", vault.getRetentionPolicyExpiry());
    model.put(EMAIL_KEY_VAULT_REVIEW_DATE, vault.getReviewDate());
    model.put(EMAIL_KEY_USER_ID, depositUser.getID());
    model.put(EMAIL_KEY_USER_FIRSTNAME, depositUser.getFirstname());
    model.put(EMAIL_KEY_USER_LASTNAME, depositUser.getLastname());
    model.put(EMAIL_KEY_SIZE_BYTES, deposit.getArchiveSize());
    model.put(EMAIL_KEY_TIMESTAMP, event.getTimestamp());
    model.put(EMAIL_KEY_HAS_PERSONAL_DATA, deposit.getHasPersonalData());
    if (event instanceof Error) {
      model.put(EMAIL_KEY_ERROR_MESSAGE, event.getMessage());
    }
    if (event instanceof Error ||
        event instanceof RetrieveStart ||
        event instanceof RetrieveComplete ||
        event instanceof RetrieveError) {
      model.put(EMAIL_KEY_RETRIEVER_FIRSTNAME, retrieveUser.getFirstname());
      model.put(EMAIL_KEY_RETRIEVER_LASTNAME, retrieveUser.getLastname());
      model.put(EMAIL_KEY_RETRIEVER_ID, retrieveUser.getID());
    }
    model.put(EMAIL_KEY_HOME_PAGE, this.getHomeUrl());
    model.put(EMAIL_KEY_HELP_PAGE, this.getHelpUrl());
    model.put(EMAIL_KEY_HELP_MAIL, this.getHelpMail());
    return model;
  }

  <T> boolean processWithRefresh(
      final T initialValue,
      final Supplier<T> supplier,
      final Consumer<T> processor) {
    Assert.isTrue(initialValue != null, () -> "The initialValue cannot be null");
    T value = initialValue;
    String label = value.getClass().getName();
    boolean success = false;
    int attempts = 0;
    long start = System.currentTimeMillis();
    while (!success && attempts < MAX_ATTEMPTS) {
      try {
        attempts++;
        processor.accept(value);
        success = true;
      } catch (org.hibernate.StaleObjectStateException e) {
        log.warn("Stale[{}]attempts[{}]message[{}]", label, attempts, e.getMessage());
        // Refresh from database and retry
        value = supplier.get();
      }
    }
    long diff = System.currentTimeMillis() - start;
    log.info("Processed[{}]attempts[{}]timeMs[{}]success[{}]", label, attempts, diff, success);
    return success;
  }

  void processDeposit(Deposit deposit, Consumer<Deposit> consumer) {
    processWithRefresh(deposit,
        () -> getDeposit(deposit.getID()),
        consumer);
  }

  void processJob(Job job, Consumer<Job> consumer) {
    processWithRefresh(job,
        () -> getJob(job.getID()),
        consumer);
  }

  void processVault(Vault vault, Consumer<Vault> consumer) {
    processWithRefresh(vault,
        () -> getVault(vault.getID()),
        consumer);
  }


  void process01InitStates(InitStates initStatesEvent, Job job) {
    // Update the Job state information

    processJob(job, $job -> {
      $job.setStates(initStatesEvent.getStates());
      jobsService.updateJob($job);
    });
  }

  void process02UpdateProcess(UpdateProgress updateProgressEvent, Job job) {
    // Update the Job state

    processJob(job, $job -> {
      $job.setProgress(updateProgressEvent.getProgress());
      $job.setProgressMax(updateProgressEvent.getProgressMax());
      $job.setProgressMessage(updateProgressEvent.getProgressMessage());
      jobsService.updateJob($job);
    });
  }

  void process03Start(Start event, Deposit deposit) {
    // Update the deposit status

    processDeposit(deposit, $deposit -> {
      if ($deposit.getStatus() == null || ($deposit.getStatus() != Deposit.Status.COMPLETE)) {
        $deposit.setStatus(Deposit.Status.IN_PROGRESS);
        depositsService.updateDeposit($deposit);
      }
    });
//              // Get related information for emails
    this.sendEmails(deposit, event, TYPE_START,
        EmailTemplate.USER_DEPOSIT_START,
        EmailTemplate.GROUP_ADMIN_DEPOSIT_START);
  }

  void process04ComputedSize(ComputedSize computedSizeEvent, Deposit deposit) {
    // Update the deposit with the computed size

    long sizeInBytes = computedSizeEvent.getBytes();
    processDeposit(deposit, $deposit -> {
      log.info("Setting Deposit Size from computedSizeEvent to: " + sizeInBytes);
      $deposit.setSize(sizeInBytes);
      depositsService.updateDeposit($deposit);
    });
  }

  void process05ComputedChunks(ComputedChunks computedChunksEvent, Deposit deposit) {
    // Update the deposit with the computed digest

    processDeposit(deposit, $deposit -> {
      updateDepositWithChunks($deposit, computedChunksEvent);
      depositsService.updateDeposit($deposit);
    });
  }

  void process06ComputedEncryption(
      ComputedEncryption computedEncryptionEvent, Deposit deposit) {
    // Update the deposit with the computed digest

    processDeposit(deposit, $deposit -> {
      // If went through chunking will also get the chunking information
      updateDepositWithChunks($deposit, computedEncryptionEvent);

      Map<Integer, byte[]> chunksIVs = computedEncryptionEvent.getChunkIVs();
      Map<Integer, String> encChunksDigests = computedEncryptionEvent.getEncChunkDigests();
      byte[] tarIV = computedEncryptionEvent.getTarIV();
      String encTarDigest = computedEncryptionEvent.getEncTarDigest();

      List<DepositChunk> chunks = $deposit.getDepositChunks();
      for (DepositChunk chunk : chunks) {
        Integer num = chunk.getChunkNum();

        chunk.setEncIV(chunksIVs.get(num));
        chunk.setEcnArchiveDigest(encChunksDigests.get(num));
      }

      // TODO: we might want to only update this if it's needed
      $deposit.setEncIV(tarIV);
      $deposit.setEncArchiveDigest(encTarDigest);

      depositsService.updateDeposit($deposit);
    });
  }

  void process07ComputedDigest(ComputedDigest event, Deposit deposit) {
    // Update the deposit with the computed digest and digest algorithm
    processDeposit(deposit, $deposit -> {
      $deposit.setArchiveDigest(event.getDigest());
      $deposit.setArchiveDigestAlgorithm(event.getDigestAlgorithm());
      depositsService.updateDeposit($deposit);
    });
  }

  void process08UploadComplete(UploadComplete uploadCompleteEvent, Deposit deposit) {
    // Update the deposit status and add archives

    processDeposit(deposit, $deposit -> {
      // Add the archive objects, one for each archiveStore
      for (Map.Entry<String, String> keyPair : uploadCompleteEvent.getArchiveIds().entrySet()) {
        String archiveStoreId = keyPair.getKey();
        String archiveId = keyPair.getValue();
        ArchiveStore archiveStore = archiveStoreService.getArchiveStore(archiveStoreId);
        archivesService.saveOrUpdateArchive($deposit, archiveStore, archiveId);
      }
    });
  }

  void process09Complete(Complete completeEvent, Deposit deposit) {
    // Update the deposit status and add archives

    processDeposit(deposit, $deposit -> {

      $deposit.setNonRestartJobId(null);
      $deposit.setStatus(Deposit.Status.COMPLETE);
      $deposit.setArchiveSize(completeEvent.getArchiveSize());
      depositsService.updateDeposit($deposit);

      // Add to the cumulative vault size
      Vault initVault = $deposit.getVault();

      processVault(initVault, $vault -> {
        long vaultSize = $vault.getSize();
        $vault.setSize(vaultSize + $deposit.getSize());
        vaultsService.updateVault($vault);
      });
    });

//                // Get related information for emails
    this.sendEmails(deposit, completeEvent, TYPE_COMPLETE,
        EmailTemplate.USER_DEPOSIT_COMPLETE,
        EmailTemplate.GROUP_ADMIN_DEPOSIT_COMPLETE);
  }

  void process10Error(Error errorEvent, Deposit deposit, Job job) {
    // Update the deposit status and job properties

    if (deposit != null && deposit.getStatus() != Deposit.Status.COMPLETE) {
      processDeposit(deposit, $deposit -> {
        if ($deposit.getStatus() == Deposit.Status.DELETE_IN_PROGRESS) {
          $deposit.setStatus(Deposit.Status.DELETE_FAILED);
        } else {
          $deposit.setStatus(Deposit.Status.FAILED);
        }
        depositsService.updateDeposit($deposit);
      });
    }

    processJob(job, $job -> {
      $job.setError(true);
      $job.setErrorMessage(errorEvent.getMessage());
      jobsService.updateJob($job);
    });

    if (deposit != null) {
      //TODO - send email by using correct template when deposit delete fails
      // Get related information for emails
      this.sendEmails(deposit, errorEvent, TYPE_ERROR,
          EmailTemplate.USER_DEPOSIT_ERROR,
          EmailTemplate.GROUP_ADMIN_DEPOSIT_ERROR);
    } else if (errorEvent.getAuditId() != null) {
      // TODO: what to do with audit error
    }
  }

  void process11RetrieveStart(RetrieveStart startEvent, Deposit deposit) {
    // Update the Retrieve status
    Retrieve retrieve = getRetrieve(startEvent.getRetrieveId());
    retrieve.setStatus(Retrieve.Status.IN_PROGRESS);
    retrievesService.updateRetrieve(retrieve);

    this.sendEmails(deposit, startEvent, TYPE_RETRIEVE_START, EmailTemplate.USER_RETRIEVE_START,
        EmailTemplate.GROUP_ADMIN_RETRIEVE_START);
  }

  void process12RetrieveComplete(RetrieveComplete completeEvent, Deposit deposit) {
    // Update the Retrieve status
    Retrieve retrieve = getRetrieve(completeEvent.getRetrieveId());
    retrieve.setStatus(Retrieve.Status.COMPLETE);
    retrievesService.updateRetrieve(retrieve);

    this.sendEmails(deposit, completeEvent, TYPE_RETRIEVE_COMPLETE,
        EmailTemplate.USER_RETRIEVE_COMPLETE,
        EmailTemplate.GROUP_ADMIN_RETRIEVE_COMPLETE);
  }

  void process13DeleteStart(DeleteStart event, Deposit deposit) {
    preDeletionStatus = deposit.getStatus();
    log.info("Pre Deletion Status:" + preDeletionStatus);
    deposit.setStatus(Deposit.Status.DELETE_IN_PROGRESS);
    depositsService.updateDeposit(deposit);
  }

  void process14DeleteComplete(DeleteComplete event, Deposit deposit) {
    long depositSizeBeforeDelete = deposit.getSize();
    deposit.setStatus(Deposit.Status.DELETED);
    deposit.setSize(0);
    depositsService.updateDeposit(deposit);

    // if this wasn't an Error deposit remove from the vault size
    if (preDeletionStatus != Deposit.Status.FAILED) {
      Vault vault = deposit.getVault();

      processVault(vault, $vault -> {
        long vaultSize = $vault.getSize();
        $vault.setSize(vaultSize - depositSizeBeforeDelete);
        vaultsService.updateVault($vault);
      });
    } else {
      log.warn("DeleteComplete : preDeletionStatus is FAILED for jobId [{}]", event.getJobId());
    }
  }

  void process15AuditStart(AuditStart event) {
    // Update the Audit
    Audit audit = getAudit(event.getAuditId());
    audit.setStatus(Audit.Status.IN_PROGRESS);
    auditsService.updateAudit(audit);
  }

  void process16ChunkAuditStarted(ChunkAuditStarted event) {
    // Update the Audit status
    Audit audit = getAudit(event.getAuditId());
    DepositChunk chunk = getDepositChunk(event.getChunkId());
    String archiveId = event.getArchiveId();
    String location = event.getLocation();

    auditsService.addAuditStatus(audit, chunk, archiveId, location);
  }

  void process17ChunkAuditComplete(ChunkAuditComplete event) {
    AuditChunkStatus auditInfo = processAuditChunkStatus(event);
    auditInfo.complete();
    auditsService.updateAuditChunkStatus(auditInfo);
  }

  void process18AuditComplete(AuditComplete event) {
    Audit audit = getAudit(event.getAuditId());

    // If one chunk audit failed set status as failed
    List<AuditChunkStatus> auditChunkStatus =
        auditsService.getAuditChunkStatusFromAudit(audit);

    Audit.Status auditStatus = auditChunkStatus.stream()
        .filter(c -> AuditChunkStatus.Status.ERROR.equals(c.getStatus()))
        .findFirst()
        .map(err -> Audit.Status.FAILED)
        .orElse(Audit.Status.COMPLETE);

    audit.setStatus(auditStatus);
    auditsService.updateAudit(audit);
  }

  void process19AuditError(AuditError event) {
    AuditChunkStatus auditInfo = processAuditChunkStatus(event);
    auditInfo.failed(event.getMessage());
    auditsService.updateAuditChunkStatus(auditInfo);

    // At the moment just email
    log.info("Sending Error email to Admin");
    sendAuditEmails(event, EmailTemplate.AUDIT_CHUNK_ERROR);

    // TODO: try to fix chunk
  }

  private void ignore(Event event) {
    log.info("ignoring [{}] JobId[{}]", event.getClass().getSimpleName(), event.getJobId());
  }
  void process20TransferComplete(TransferComplete event) {
    ignore(event);
  }

  void process21PackageComplete(PackageComplete event) {
    ignore(event);
  }

  void process22StartCopyUpload(StartCopyUpload event) {
    ignore(event);
  }

  void process23CompleteCopyUpload(CompleteCopyUpload event, Deposit deposit) {
    
    processDeposit(deposit, $deposit -> {
      //make sure the event is correctly linked to the deposit - depositId is not enough
      event.setDeposit($deposit);

      eventService.addEvent(event);

      String archiveStoreId = event.getArchiveStoreId();
        String archiveId = event.getArchiveId();
        ArchiveStore archiveStore = archiveStoreService.getArchiveStore(archiveStoreId);
        archivesService.saveOrUpdateArchive($deposit, archiveStore, archiveId);
    });
  }

  void process24ValidationComplete(ValidationComplete event) {
    ignore(event);
  }

  void process25RetrieveError(RetrieveError event) {
    Job job = getJob(event.getJobId());
    Retrieve retrieve = getRetrieve(event.getRetrieveId());
    retrieve.setStatus(Retrieve.Status.FAILED);
    retrievesService.updateRetrieve(retrieve);

    processJob(job, $job -> {
      $job.setError(true);
      $job.setErrorMessage(event.getMessage());
      jobsService.updateJob($job);
    });

    this.sendEmails(getDeposit(event.getDepositId()), event, TYPE_ERROR,
            EmailTemplate.USER_DEPOSIT_ERROR,
            EmailTemplate.GROUP_ADMIN_DEPOSIT_ERROR);
  }

  String getUserSubject(String type) {
    String userSubjectKey = USER_DEPOSIT_PREFIX + type;
    log.info("User Subject key: " + userSubjectKey);
    if (EMAIL_SUBJECTS.containsKey(userSubjectKey) == false) {
      throw new IllegalArgumentException("User Subject key not found: " + userSubjectKey);
    }
    String userSubject = EMAIL_SUBJECTS.get(userSubjectKey);
    return userSubject;
  }

  String getAdminSubject(String type) {
    String adminSubjectKey = ADMIN_DEPOSIT_PREFIX + type;
    log.info("Admin Subject key: " + adminSubjectKey);
    if (EMAIL_SUBJECTS.containsKey(adminSubjectKey) == false) {
      throw new IllegalArgumentException("Admin Subject key not found: " + adminSubjectKey);
    }
    String adminSubject = EMAIL_SUBJECTS.get(adminSubjectKey);
    return adminSubject;
  }

  String getAuditErrorSubject(String type) {
    String auditSubjectKey = AUDIT_CHUNK_PREFIX + type;
    log.info("Audit Error Subject key: " + auditSubjectKey);
    if (EMAIL_SUBJECTS.containsKey(auditSubjectKey) == false) {
      throw new IllegalArgumentException("Audit Error Subject key not found: " + auditSubjectKey);
    }
    String auditSubject = EMAIL_SUBJECTS.get(auditSubjectKey);
    return auditSubject;
  }

  void sendAuditEmails(AuditError event, String template) {

    String auditSubject = getAuditErrorSubject(TYPE_ERROR);

    Map<String, Object> model = getErrorModel(event);

    log.info("Audit admin email is " + auditAdminEmail);
    sendTemplateEmail(auditAdminEmail, auditSubject, template, model);
  }

  void sendTemplateEmail(String to, String subject, String template,
      Map<String, Object> model) {
    if (to != null) {
      emailService.sendTemplateMail(to, subject, template, model);
    } else {
      log.warn("sendTemplateEmail : null 'to' address for subject[{}]  and template[{}]", subject, template);
    }
  }

  Map<String, Object> getErrorModel(AuditError event) {
    Map<String, Object> model = new HashMap<>();

    model.put(EMAIL_KEY_AUDIT_ID, event.getAuditId());
    model.put(EMAIL_KEY_CHUNK_ID, event.getChunkId());
    model.put(EMAIL_KEY_ARCHIVE_ID, event.getArchiveId());
    model.put(EMAIL_KEY_LOCATION, event.getLocation());
    model.put(EMAIL_KEY_TIMESTAMP, event.getTimestamp());
    return model;
  }

  Audit getAudit(String auditId) {
    Audit audit = auditsService.getAudit(auditId);
    if (audit == null) {
      throw new IllegalArgumentException("Audit not found for id " + auditId);
    }
    return audit;
  }

  DepositChunk getDepositChunk(String chunkId) {
    DepositChunk chunk = depositsService.getDepositChunkById(chunkId);
    if (chunk == null) {
      throw new IllegalArgumentException("Chunk not found for id " + chunkId);
    }
    return chunk;
  }

  Retrieve getRetrieve(String retrieveId) {
    Retrieve retrieve = retrievesService.getRetrieve(retrieveId);
    if (retrieve == null) {
      throw new IllegalArgumentException("Retrieve not found for id " + retrieveId);
    }
    return retrieve;
  }

  Deposit getDeposit(String depositId) {
    Deposit deposit = depositsService.getDeposit(depositId);
    if (deposit == null) {
      throw new IllegalArgumentException("Deposit not found for id " + depositId);
    }
    return deposit;
  }

  Job getJob(String jobId) {
    Job job = jobsService.getJob(jobId);
    if (job == null) {
      throw new IllegalArgumentException(("Job not found for id " + jobId));
    }
    return job;
  }

  Vault getVault(String vaultId) {
    Vault vault = vaultsService.getVault(vaultId);
    if (vault == null) {
      throw new IllegalArgumentException("Vault not found for id " + vaultId);
    }
    return vault;
  }

  User getUser(String userId) {
    User user = usersService.getUser(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found for id " + userId);
    }
    return user;
  }

  public String getHomeUrl() {
    return this.homeUrl;
  }

  public String getHelpUrl() {
    return this.helpUrl;
  }

  public String getHelpMail() {
    return this.helpMail;
  }

  void updateDepositWithChunks(Deposit deposit, ChunksDigestEvent event) {
    Map<Integer, String> chunksDigest = event.getChunksDigest();
    String algorithm = event.getDigestAlgorithm();
    deposit.setNumOfChunks(chunksDigest.size());
    depositsService.updateDeposit(deposit);

    List<DepositChunk> chunks = new ArrayList<>();

    //by using keySet - we always iterate in chunkNum order
    for (Integer chunkNum : new TreeSet<>(chunksDigest.keySet())) {
      String hash = chunksDigest.get(chunkNum);
      // Create new Deposit chunk
      DepositChunk depositChunk = new DepositChunk(deposit, chunkNum, hash, algorithm);
      chunks.add(depositChunk);
    }

    deposit.setDepositChunks(chunks);
  }

  AuditChunkStatus processAuditChunkStatus(Event event) {
    Audit audit = getAudit(event.getAuditId());
    DepositChunk chunk = getDepositChunk(event.getChunkId());
    String archiveId = event.getArchiveId();
    String location = event.getLocation();

    List<AuditChunkStatus> auditChunkStatus =
        auditsService.getRunningAuditChunkStatus(audit, chunk, archiveId, location);

    if (auditChunkStatus.size() != 1) {
      // TODO: Make sure it never happen
      log.error("Unexpected number of running audit chunks (should be 1) it is : " + auditChunkStatus.size());
    }
    AuditChunkStatus auditInfo = auditChunkStatus.get(0);
    auditInfo.setCompleteTime(new Date(clock.millis()));
    return auditInfo;
  }

}
