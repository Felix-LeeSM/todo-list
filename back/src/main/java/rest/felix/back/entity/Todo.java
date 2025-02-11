package rest.felix.back.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import rest.felix.back.entity.enumerated.TodoStatus;

import java.time.OffsetDateTime;



@Getter
@Setter
@ToString
@Entity
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne
    private User author;

    @ManyToOne
    private Group group;

    @Column
    @Enumerated(EnumType.STRING)
    private TodoStatus todoStatus = TodoStatus.PENDING;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
