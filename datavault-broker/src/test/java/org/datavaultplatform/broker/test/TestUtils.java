package org.datavaultplatform.broker.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.springframework.util.DigestUtils;

public abstract class TestUtils {

  public static final Date NOW = new Date();
  public static final Date ONE_WEEK_AGO = datePlus(NOW, -7, ChronoUnit.DAYS);
  public static final Date TWO_WEEKS_AGO = datePlus(NOW, -14, ChronoUnit.DAYS);

  public static final Date ONE_YEAR_AGO = datePlus(NOW, -1, ChronoUnit.YEARS);

  public static final Date TWO_YEARS_AGO = datePlus(NOW, -2, ChronoUnit.YEARS);

  public static final Date THREE_YEARS_AGO = datePlus(NOW, -3, ChronoUnit.YEARS);

  public static Date datePlus(Date date, int amt, ChronoUnit unit) {
    Instant ins = Instant.ofEpochMilli(date.getTime());
    LocalDateTime local = LocalDateTime.ofInstant(ins, ZoneId.of("UTC"));
    LocalDateTime result = local.plus(amt, unit);
    return new Date(result.toInstant(ZoneOffset.UTC).toEpochMilli());
  }

  public static <T extends Exception> void checkException(Class<T> exceptionClass, String message, Executable executable) {
    T ex = assertThrows(exceptionClass, executable);
    assertEquals(message, ex.getMessage());
  }

  public static String useNewLines(String value) {
    return value.replace("\r\n","\n").replace("\r","\n");
  }

  public static ArrayList<String> getRandomList() {
    return getRandomList(100);
  }

  public static ArrayList<String> getRandomList(int size) {
    return Stream.generate(new Random()::nextInt).map(Object::toString)
        .limit(size).collect(Collectors.toCollection(ArrayList::new));
  }

  public static HashMap<String,String> getRandomMap(){
    return getRandomList().stream().collect(Collectors.toMap(
        Function.identity(),
        Function.identity(),
        (k1,k2)->k1,
        HashMap::new));
  }

  public static HashMap<String,String> getRandomMap(int size){
    return getRandomList(size).stream().collect(Collectors.toMap(
        Function.identity(),
        Function.identity(),
        (k1,k2)->k1,
        HashMap::new));
  }

  public static HashMap<Integer,String> getRandomMapIntegerKey() {
    return Stream.generate(new Random()::nextInt)
        .limit(100)
        .collect(Collectors.toMap(
            Function.identity(),
            (item) -> item.toString(), (
            k1,k2)->k2,
            HashMap::new));
  }

  public static HashMap<Integer,byte[]> getRandomMapIntegerKeyByteArrayValue() {
    return Stream.generate(new Random()::nextInt)
        .limit(100)
        .collect(Collectors.toMap(
            Function.identity(),
            (item) -> item.toString().getBytes(StandardCharsets.UTF_8),
            (k1,k2)->k1,
            HashMap::new));
  }

  @SneakyThrows
  public static String getMD5(String data) {
    return getMD5(data.getBytes(StandardCharsets.UTF_8));
  }

  public static  String getMD5(byte[] data) {
    byte[] digest = DigestUtils.md5Digest(data);
    String md5 = DatatypeConverter.printHexBinary(digest).toLowerCase();
    return md5;
  }

}
