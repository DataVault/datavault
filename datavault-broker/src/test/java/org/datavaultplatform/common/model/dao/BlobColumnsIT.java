package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class BlobColumnsIT extends BaseDatabaseTest {

  @Autowired
  EventDAO eventDAO;

  @PersistenceContext
  EntityManager em;

  final Random rand = new Random();

  final char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

  @Test
  void testComputedEnctryptionTarIVTinyBlobColOkayAt255() {
    checkComputedEncryptionTarIVColumn(255);
  }

  @Test
  void testComputedEnctryptionTarIVTinyBlobColFailsAt256() {
    assertThrows(Exception.class, ()-> checkComputedEncryptionTarIVColumn(256));
  }

  void checkComputedEncryptionTarIVColumn(int length){
    ComputedEncryption event = new ComputedEncryption();
    event.setTarIV(getRandomBytes(length));
    eventDAO.save(event);
    String id = event.getID();
    em.detach(event);

    ComputedEncryption event2 = (ComputedEncryption) eventDAO.findById(id).get();
    assertArrayEquals(event.getTarIV(), event2.getTarIV());
  }

  private byte[] getRandomBytes(int length){
    StringBuffer sb = new StringBuffer();
    for(int i=0;i<length;i++){
      sb.append(getRandomLetter());
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private char getRandomLetter(){
    return chars[rand.nextInt(26)];
  }


}
