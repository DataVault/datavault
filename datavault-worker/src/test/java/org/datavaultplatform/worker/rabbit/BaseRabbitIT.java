package org.datavaultplatform.worker.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.worker.config.RabbitConfig;
import org.datavaultplatform.worker.utils.SocketUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

@DirtiesContext

@Timeout(value = 5, unit = TimeUnit.MINUTES, threadMode  = Timeout.ThreadMode.SEPARATE_THREAD)
@TestPropertySource(properties = "logging.level.com.rabbitmq.client.ConnectionFactory=DEBUG")
public abstract class BaseRabbitIT {


    @Value("${spring.rabbitmq.host}")
    String rabbitMQhost;

    @Value("${spring.rabbitmq.port}")
    int rabbitMQport;

    @Value("${spring.rabbitmq.username}")
    String rabbitMQusername;

    @Value("${spring.rabbitmq.password}")
    String rabbitMQpassword;
    
    private static final Logger BASE_LOG = LoggerFactory.getLogger(BaseRabbitIT.class);

    public static final int HI_PRIORITY = 2;
    public static final int NORMAL_PRIORITY = 1;

    private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
            .withExposedPorts(5672, 15672)
            //.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(BaseRabbitIT.class)))
            .waitingFor(
                    Wait.forLogMessage(".*started TCP listener on.*\\n", 1)
                            .withStartupTimeout(Duration.ofMinutes(2)));
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    ApplicationContext ctx;
    @Autowired
    @Qualifier("monitorLogger")
    Logger log;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        BASE_LOG.info("spring.rabbitmq.host [{}]", RABBIT.getHost());
        BASE_LOG.info("spring.rabbitmq.port [{}]", RABBIT.getAmqpPort());
    }
    
    protected static final long classLoadedAt;
    
    static {
        classLoadedAt = System.currentTimeMillis();
    }

    @BeforeEach
    void setup(TestInfo info) {
        RabbitMessageSelectorScheduler scheduler = ctx.getBean(RabbitMessageSelectorScheduler.class);
        boolean realScheduler = scheduler != null && !mockingDetails(scheduler).isMock();
        if(realScheduler && log.getName().equals(RabbitConfig.class.getName())){
            Assertions.fail("log bean (monitorLogger) has not been overridden for tests");
        }
        log.info("info [{}]", info);
    }
    
    
    private boolean isServerListening2() {
        ConnectionFactory result = new ConnectionFactory();
        result.setUsername(this.rabbitMQusername);
        result.setPort(this.rabbitMQport);
        result.setHost(this.rabbitMQhost);
        result.setUsername(this.rabbitMQusername);
        result.setPassword(this.rabbitMQpassword);
        result.setAutomaticRecoveryEnabled(false);
        result.setConnectionTimeout(1_000);
        try {
            result.newConnection();
            return true;
        } catch(Exception ex) {
            return false;
        }
    }

    @BeforeEach
    void checkRabbitConnection() {
        assertThat(ctx.getStartupDate() > classLoadedAt).isTrue();

        assertThat(RABBIT.isCreated()).isTrue().withFailMessage(() -> "rabbit is NOT created");
        assertThat(RABBIT.isRunning()).isTrue().withFailMessage(() -> "rabbit is NOT running");
        log.info("RABBIT AMQP URL [ {} ]", RABBIT.getAmqpUrl());
        log.info("rabbit username [{}]", RABBIT.getAdminUsername());
        log.info("rabbit password [{}]", RABBIT.getAdminPassword());
        log.info("rabbit host [{}]", RABBIT.getHost());
        log.info("rabbit AMQP port [{}]", RABBIT.getAmqpPort());

        // double check that we can connect via socket to rabbit before proceeding with actual tests
        assertThat(SocketUtils.isServerListening(RABBIT.getHost(), RABBIT.getAmqpPort())).isTrue();
        assertThat(isServerListening2()).isTrue();
    }

    
    @BeforeAll
    public static void startContainer(){
        
        Assumptions.assumeTrue(BaseRabbitIT::isDockerAvailable);
        RABBIT.start();
    }

    public static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @AfterAll
    public static void tearDownContainer(){
        RABBIT.stop();
    }
}
