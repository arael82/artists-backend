package com.arael82.challenge.artists.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
