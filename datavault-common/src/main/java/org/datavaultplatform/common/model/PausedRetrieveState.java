package org.datavaultplatform.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "paused_retrieve_state")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class PausedRetrieveState {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Setter
    @Column(name="is_paused", columnDefinition = "TINYINT(1) NOT NULL")
    private boolean isPaused;

    @CreatedDate
    @Setter
    @Column(columnDefinition = "TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime created;
}

