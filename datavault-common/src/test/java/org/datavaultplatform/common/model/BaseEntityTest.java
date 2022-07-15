package org.datavaultplatform.common.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.SneakyThrows;

public abstract class BaseEntityTest<T, ID> {

  private void checkSame(T o1, T o2) {
    assertThat(o1).isEqualTo(o2);
    assertThat(o1.hashCode()).isEqualTo(o2.hashCode());
  }

  private void checkNotSame(T o1, Object o2) {
    assertThat(o1).isNotEqualTo(o2);
  }

  @SneakyThrows
  private void setID(T entity, ID id) {
    Field f = entity.getClass().getDeclaredField("id");
    f.setAccessible(true);
    f.set(entity, id);

    Method methodForGetID = getMethod(entity, "getID");
    if (methodForGetID == null) {
      methodForGetID = getMethod(entity, "getId");
    }
    assertNotNull(methodForGetID);
    assertEquals(id, methodForGetID.invoke(entity));
  }

  private Method getMethod(Object entity, String methodName) {
    try {
      return entity.getClass().getDeclaredMethod(methodName);
    } catch (Exception ex) {
      return null;
    }
  }

  String generateID() {
    return UUID.randomUUID().toString();
  }

  Long generateLongID() {
    return new Random().nextLong();
  }

  Integer generateIntID() {
    return new Random().nextInt();
  }

  private void checkClassIsEntityWithTableName(Class<T> clazz) {
    Entity entity = clazz.getDeclaredAnnotation(Entity.class);
    assertThat(entity).isNotNull();
    Table table = clazz.getDeclaredAnnotation(Table.class);
    assertThat(table).isNotNull();
    assertThat(table.name()).isNotBlank();
  }

  abstract void testEntity();

  private void checkNotSameWhenComparedToNonEntity(T entity) {
    checkNotSame(entity, "NOT_AN_ENTITY");
  }

    @SneakyThrows
  void checkEntity(Class<T> clazz, IDGenerator<ID> idGenerator) {
    checkClassIsEntityWithTableName(clazz);

    T entity1 = clazz.newInstance();
    T entity2 = clazz.newInstance();

    // two 'new' Entities of the same class - should be equal
    checkSame(entity1, entity1);

    // Entities should never be equal to null
    checkNotSame(entity1, null);
    checkNotSame(entity2, null);

    // Entities should not be equal to an instance of another class
    checkNotSameWhenComparedToNonEntity(entity1);
    checkNotSameWhenComparedToNonEntity(entity2);

    ID idOne = idGenerator.generateID();
    ID idTwo = idGenerator.generateID();

    // Entities with same id should be equal
    setID(entity1, idOne);
    setID(entity2, idOne);
    checkSame(entity1, entity2);

    // Entities with different id should be non-equal
    setID(entity2, idTwo);
    checkNotSame(entity1, entity2);

    // Entities with same id should be equal
    setID(entity1, idTwo);
    checkSame(entity1, entity2);
  }

  interface IDGenerator<ID> {

    ID generateID();
  }
}
