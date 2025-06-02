package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "Users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Expose
    @Column(nullable = false)
    private String password;

    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Expose
    @OneToOne
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;
}
