package rest.felix.back.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import rest.felix.back.entity.enumerated.GroupRole;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@Entity
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne
    @JoinColumn
    private User user;

    @ManyToOne
    @JoinColumn
    private Group group;

    @Enumerated(EnumType.STRING)
    private GroupRole groupRole;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

}
