package org.datavaultplatform.broker.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/*
This code was used to generate the userpassword entries in
@see src/test/resources/ldap/testUsers.ldif
 */
@Slf4j
public class LdapUserPasswordHashTest {

  @ParameterizedTest
  @SneakyThrows
  @CsvSource({
      "hello123,{MD5}8wqnpmLHKLdAfFSua/0n0Q==",
      "jbond007,{MD5}SeUYrNJKW/y4Uc8zHgXitg==",
      "basilbrush,{MD5}6PLzKquTu7QLitZWPCZaOw==",
      "blogger,{MD5}nBJS+mDIR3g6UoEnPIpdDA=="})
  void testMd5PasswordHashes(String password, String expectedMd5Hash) {
    String md5hash = hashMD5Password(password);
    log.info("md5hash of[{}] is [{}]", password, md5hash);
    assertEquals(expectedMd5Hash, md5hash);
  }

  String hashMD5Password(String password) throws NoSuchAlgorithmException,
      UnsupportedEncodingException {
    MessageDigest digest = MessageDigest.getInstance("MD5");
    digest.update(password.getBytes(StandardCharsets.UTF_8));
    String md5Password = Base64.getEncoder().encodeToString(digest.digest());
    return "{MD5}" + md5Password;
  }

}