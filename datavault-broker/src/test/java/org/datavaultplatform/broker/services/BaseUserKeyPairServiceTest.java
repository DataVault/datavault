package org.datavaultplatform.broker.services;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.UsesTestContainers;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

@Slf4j
@UsesTestContainers
public abstract class BaseUserKeyPairServiceTest {
  
  public static final String NO = "no";
  public static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

  public static final String TEST_PASSPHRASE = "tenet";
  
  @SneakyThrows
  static void execInContainer(GenericContainer<?> container, String label, String command) {
    execInContainer(container, label, command.split(" "));
  }

  static void execInContainer(GenericContainer<?> container, String label, String... commands) {
    try {
      Container.ExecResult result = container.execInContainer(commands);
      if (result.getExitCode() != 0) {
        log.info("[{}] std err   [{}]", label, result.getStderr().trim());
        log.info("[{}] std out   [{}]", label, result.getStdout().trim());
        log.info("[{}] exit code [{}]", label, result.getExitCode());
        fail(String.format("exit status for [%s] was [%d] not [0]", label, result.getExitCode()));
      } else {
        log.info("[{}] std err   [{}]", label, result.getStderr().trim());
        log.info("[{}] std out   [{}]", label, result.getStdout().trim());
        log.info("[{}] exit code [{}]", label, result.getExitCode());
      }
    } catch (Exception ex) {
      fail(String.format("problem running command [%s]", label), ex);
    }
  }

  static void copyScriptToContainer(GenericContainer<?> container, String contents,
      String path) {
    container.copyFileToContainer(Transferable.of(contents), path);
    execInContainer(container, "chmod for " + path, "chmod +x " + path);
  }

  static String readFileFromContainer(GenericContainer<?> container, String path) {
    return container.copyFileFromContainer(path, is -> StreamUtils.copyToString(is, UTF_8));
  }

  abstract void testKeyPair(UserKeyPairService service);

  @SneakyThrows
  final String getStringFromResource(Resource res) {
    return StreamUtils.copyToString(res.getInputStream(), UTF_8);
  }

  static Stream<UserKeyPairService> provideUserKeyPairService() {
    return Stream.of (
        new UserKeyPairServiceJSchImpl(TEST_PASSPHRASE),
        new UserKeyPairServiceImpl(TEST_PASSPHRASE)
    );
  }

}
