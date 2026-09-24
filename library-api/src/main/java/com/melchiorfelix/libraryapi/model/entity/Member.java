package com.melchiorfelix.libraryapi.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "library_member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String cardNumber;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
