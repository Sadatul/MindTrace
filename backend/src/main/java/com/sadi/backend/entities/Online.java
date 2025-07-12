package com.sadi.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * First cope this annotations (Look at Chat) @Entity @NoArgsConstructor @AllArgsConstructor @Getter @Setter @ToString @Table(name = "chats")
 * Change the Table name to online inside @Table annotation.
 * Create a primary key field. Usually we do it with UUID. Go to chat and copy the id field with annotations
 * To create ManyToOne relationship, you can copy the user field from chat and change the name to user.
 * Maybe to avoid issues you can remvoe fetch = FetchType.LAZY from the ManyToOne annotation.
 * @JoinColumn is actually used to define the name of the column. It is also NOT NULL.
 * @ToString.Exclude is is a must.
 * Next step create a V5__OnlineTable.sql file in src/main/resources/db/migration
 * Running the server automatically creates the online table. Do this only after you are sure about the table.
 */

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "online")
public class Online {
    /**
     * Here @Getter creates getter methods for all fields.
     * @Setter creates setter methods for all fields.
     * @Entity tells spring that this class defines a table.
     * @NoArgsConstructor creates a no-args constructor.
     * @AllArgsConstructor creates a constructor with all fields.
     * @ToString creates a toString method that includes all fields. Except for the ones marked with @ToString.Exclude.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Spring will automatically create a column named normal_field in the online table. This field is nullable
    private String normalField;

    // This will create a column named normal_field_not_null in the online table. This field is NOT NULL
    @Column(nullable = false)
    private String normalFieldNotNull;

    // This will create a column named normal_field_2 in the online table. This field is NOT NULL and has a size of 20 characters
    @Column(name = "normal_field_2", nullable = false, length = 20)
    private String normalFieldNotNullSize20;

    // You can also define custom Type with columnDefinition
}
