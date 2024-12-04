package com.arael82.challenge.artists.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractEntity {

    @PrePersist
    public void prePersist() {

        if(createdOn == null) //We set default value in case if the value is not set yet.
            createdOn = LocalDateTime.now();

        if(active == null)
            active = true;
    }

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    @Column(nullable = false)
    private Boolean active;

    @Override
    public String toString() {
        return ", createdOn=" + createdOn +
                ", modifiedOn=" + modifiedOn +
                ", active=" + active;
    }
}
