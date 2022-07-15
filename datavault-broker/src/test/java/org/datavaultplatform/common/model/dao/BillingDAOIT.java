package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.PendingVault.Billing_Type;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=false",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class BillingDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  BillingDAO dao;

  @Autowired
  VaultDAO vaultDAO;

  Vault v1;
  Vault v2;

  Vault v3;

  @Test
  void testWriteThenRead() {
    BillingInfo billingInfo1 = getBillingInfo1(v1);

    BillingInfo billingInfo2 = getBillingInfo2(v2);

    dao.save(billingInfo1);
    assertNotNull(billingInfo1.getID());
    assertEquals(1, count());

    dao.save(billingInfo2);
    assertNotNull(billingInfo2.getID());
    assertEquals(2, count());

    BillingInfo foundById1 = dao.findById(billingInfo1.getID()).get();
    assertEquals(billingInfo1.getID(), foundById1.getID());

    BillingInfo foundById2 = dao.findById(billingInfo2.getID()).get();
    assertEquals(billingInfo2.getID(), foundById2.getID());
  }

  @Test
  void testList() {
    BillingInfo billingInfo1 = getBillingInfo1(v1);

    BillingInfo billingInfo2 = getBillingInfo2(v2);

    dao.save(billingInfo1);
    assertNotNull(billingInfo1.getID());
    assertEquals(1, count());

    dao.save(billingInfo2);
    assertNotNull(billingInfo2.getID());
    assertEquals(2, count());

    List<BillingInfo> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(billingInfo1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(billingInfo2.getID())).count());

    assertEquals(2, dao.getTotalNumberOfVaults());
  }


  @Test
  void testUpdate() {
    BillingInfo billingInfo = getBillingInfo1(v1);

    dao.save(billingInfo);

    billingInfo.setSpecialComments("111-updated");

    dao.update(billingInfo);

    BillingInfo found = dao.findById(billingInfo.getID()).get();
    assertEquals(billingInfo.getSpecialComments(), found.getSpecialComments());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
    v1 = vaultDAO.save(VaultDAOIT.getVault1());
    v2 = vaultDAO.save(VaultDAOIT.getVault2());
    v3 = vaultDAO.save(VaultDAOIT.getVault3());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `BillingInfo`");
    template.execute("delete from `Vaults`");
    assertEquals(0, count());
  }

  private BillingInfo getBillingInfo1(Vault vault) {
    BillingInfo billingInfo = new BillingInfo();
    billingInfo.setSpecialComments("111");
    billingInfo.setBillingType(Billing_Type.BUDGET_CODE);

    billingInfo.setVault(vault);
    vault.setBillinginfo(billingInfo);
    return billingInfo;
  }

  private BillingInfo getBillingInfo2(Vault vault) {
    BillingInfo billingInfo = new BillingInfo();
    billingInfo.setSpecialComments("222");
    billingInfo.setBillingType(Billing_Type.GRANT_FUNDING);

    billingInfo.setVault(vault);
    vault.setBillinginfo(billingInfo);
    return billingInfo;
  }

  long count() {
    return dao.count();
  }

  @Test
  void testSearch() {

    BillingInfo billingInfo1 = getBillingInfo1(v1);
    billingInfo1.setContactName("contactName1");
    billingInfo1.setProjectTitle("AAA");
    BillingInfo billingInfo2 = getBillingInfo2(v2);
    billingInfo2.setContactName("contactName12");
    billingInfo2.setProjectTitle("BBB");
    BillingInfo billingInfo3 = getBillingInfo2(v3);
    billingInfo3.setProjectTitle("CCC");
    billingInfo3.setContactName("contactName123");

    dao.save(billingInfo1);
    dao.save(billingInfo2);
    dao.save(billingInfo3);

    {
      List<BillingInfo> items1 = dao.search("contactName1","projectTitle","asc",null, null);
      assertEquals(3, items1.size());
      assertEquals(billingInfo1, items1.get(0));
      assertEquals(billingInfo2, items1.get(1));
      assertEquals(billingInfo3, items1.get(2));
    }

    {
      List<BillingInfo> items2 = dao.search("contactName1", "projectTitle", "desc", null, null);
      assertEquals(3, items2.size());
      assertEquals(billingInfo3, items2.get(0));
      assertEquals(billingInfo2, items2.get(1));
      assertEquals(billingInfo1, items2.get(2));
    }

    {
      List<BillingInfo> items3 = dao.search("contactName12", "projectTitle", "asc", null, null);
      assertEquals(2, items3.size());
      assertEquals(billingInfo2, items3.get(0));
      assertEquals(billingInfo3, items3.get(1));
    }
    {
      List<BillingInfo> items4 = dao.search("contactName12", "projectTitle", "asc", null, null);
      assertEquals(2, items4.size());
      assertEquals(billingInfo2, items4.get(0));
      assertEquals(billingInfo3, items4.get(1));
    }

    {
      List<BillingInfo> items5 = dao.search("contactName1", "projectTitle", "asc", "0", "3");
      assertEquals(3, items5.size());
      assertEquals(billingInfo1, items5.get(0));
      assertEquals(billingInfo2, items5.get(1));
      assertEquals(billingInfo3, items5.get(2));
    }

    {
      List<BillingInfo> items6 = dao.search("contactName1", "projectTitle", "asc", "1", "2");
      assertEquals(2, items6.size());
      assertEquals(billingInfo2, items6.get(0));
      assertEquals(billingInfo3, items6.get(1));
    }

    {
      List<BillingInfo> items7 = dao.search("contactName1", "projectTitle", "asc", "1", "1");
      assertEquals(1, items7.size());
      assertEquals(billingInfo2, items7.get(0));
    }

    {
      List<BillingInfo> items8 = dao.search("contactName1","projectTitle","desc","1", "1");
      assertEquals(1, items8.size());
      assertEquals(billingInfo2, items8.get(0));
    }

    {
      List<BillingInfo> items9 = dao.search(billingInfo1.getID(),"projectTitle","desc",null, null);
      assertEquals(1, items9.size());
      assertEquals(billingInfo1, items9.get(0));
    }

    {
      List<BillingInfo> items10 = dao.search(billingInfo2.getID(),"projectTitle","desc",null, null);
      assertEquals(1, items10.size());
      assertEquals(billingInfo2, items10.get(0));
    }

    {
      List<BillingInfo> itemsLower = dao.search(billingInfo2.getID().toLowerCase(),"projectTitle","desc",null, null);
      assertEquals(1, itemsLower.size());
      assertEquals(billingInfo2, itemsLower.get(0));
    }

    {
      List<BillingInfo> itemsUpper = dao.search(billingInfo2.getID().toUpperCase(),"projectTitle","desc",null, null);
      assertEquals(1, itemsUpper.size());
      assertEquals(billingInfo2, itemsUpper.get(0));
    }

    {
      List<BillingInfo> itemsAll = dao.search(null,"projectTitle","desc",null, null);
      assertEquals(3, itemsAll.size());
      assertEquals(billingInfo1, itemsAll.get(2));
      assertEquals(billingInfo2, itemsAll.get(1));
      assertEquals(billingInfo3, itemsAll.get(0));
    }
  }

  @Test
  void testListWithParams() {
    BillingInfo billingInfo1 = getBillingInfo1(v1);
    billingInfo1.setContactName("contactName1");
    billingInfo1.setProjectTitle("AAA");
    BillingInfo billingInfo2 = getBillingInfo2(v2);
    billingInfo2.setContactName("contactName12");
    billingInfo2.setProjectTitle("BBB");
    BillingInfo billingInfo3 = getBillingInfo2(v3);
    billingInfo3.setProjectTitle("CCC");
    billingInfo3.setContactName("contactName123");

    dao.save(billingInfo1);
    dao.save(billingInfo2);
    dao.save(billingInfo3);

    {
      List<BillingInfo> items1 = dao.list("projectTitle","asc",null, null);
      assertEquals(3, items1.size());
      assertEquals(billingInfo1, items1.get(0));
      assertEquals(billingInfo2, items1.get(1));
      assertEquals(billingInfo3, items1.get(2));
    }

    {
      List<BillingInfo> items2 = dao.list("projectTitle","desc",null, null);
      assertEquals(3, items2.size());
      assertEquals(billingInfo1, items2.get(2));
      assertEquals(billingInfo2, items2.get(1));
      assertEquals(billingInfo3, items2.get(0));
    }

    {
      //by default, sort is descending
      List<BillingInfo> items3 = dao.list("projectTitle",null,null, null);
      assertEquals(3, items3.size());
      assertEquals(billingInfo1, items3.get(2));
      assertEquals(billingInfo2, items3.get(1));
      assertEquals(billingInfo3, items3.get(0));
    }

    {
      //this should be same as NO offset and maxResult params
      List<BillingInfo> items4 = dao.list("projectTitle","asc","0", String.valueOf(Integer.MAX_VALUE));
      assertEquals(3, items4.size());
      assertEquals(billingInfo1, items4.get(0));
      assertEquals(billingInfo2, items4.get(1));
      assertEquals(billingInfo3, items4.get(2));
    }

    {
      //we should skip first result
      List<BillingInfo> items5 = dao.list("projectTitle","asc","1", String.valueOf(Integer.MAX_VALUE));
      assertEquals(2, items5.size());
      assertEquals(billingInfo2, items5.get(0));
      assertEquals(billingInfo3, items5.get(1));
    }

    {
      //we should skip first and last result
      List<BillingInfo> items6 = dao.list("projectTitle","asc","1", "1");
      assertEquals(1, items6.size());
      assertEquals(billingInfo2, items6.get(0));
    }
  }


  @Test
  void testCountByQuery() {
    BillingInfo billingInfo1 = getBillingInfo1(v1);
    billingInfo1.setContactName("contactName1");
    billingInfo1.setProjectTitle("AAA");

    BillingInfo billingInfo2 = getBillingInfo2(v2);
    billingInfo2.setContactName("contactName12");
    billingInfo2.setProjectTitle("BBB");

    BillingInfo billingInfo3 = getBillingInfo2(v3);
    billingInfo3.setProjectTitle("CCC");
    billingInfo3.setContactName("contactName123");

    dao.save(billingInfo1);
    dao.save(billingInfo2);
    dao.save(billingInfo3);

    assertEquals(3, dao.countByQuery(null));
    assertEquals(3, dao.countByQuery(""));
    assertEquals(0, dao.countByQuery("XXX"));

    assertEquals(1, dao.countByQuery(billingInfo1.getID()));
    assertEquals(billingInfo1, dao.search(billingInfo1.getID(),"projectTitle","asc",null, null).get(0));

    assertEquals(2, dao.countByQuery("contactName12"));
    assertEquals(billingInfo3, dao.search("contactName12","projectTitle","desc",null, null).get(0));
    assertEquals(billingInfo2, dao.search("contactName12","projectTitle","desc",null, null).get(1));

    assertEquals(2, dao.countByQuery("contactName12".toLowerCase()));
    assertEquals(2, dao.countByQuery("contactName12".toUpperCase()));
  }

  @Test
  void testCountWhenNoRowsInTable() {
    assertEquals(0, dao.count());
    assertEquals(0, dao.countByQuery(null));
  }

  @Test
  void testNumber() {
      assertTrue(Number.class.isAssignableFrom(Long.class));
      assertFalse(Number.class.isAssignableFrom(String.class));
      assertTrue(Number.class.isAssignableFrom(Integer.class));
  }
}
