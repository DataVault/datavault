package org.datavaultplatform.common.model;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Message")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Message {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  @Column(name = "id", unique = true, length = 36)
  private String id;

  private String message;


  private Date timestamp;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Message message = (Message) o;
    return id != null && Objects.equals(id, message.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
