package ru.draftplace.santanizer.access.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "access_requests")
@AllArgsConstructor
@NoArgsConstructor
public class AccessRequest
{
    @Id
    @GeneratedValue
    @Getter
    @Setter
    private UUID id;

    @Column(nullable = false)
    @Getter
    @Setter
    private String email;

    @Column
    @Getter
    @Setter
    private String key;

    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private AccessRequestStatus status;

    @PrePersist
    public void generateId()
    {
        id = UUID.randomUUID();
        status = AccessRequestStatus.WAITING;
    }
}
