package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "Persons")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Person {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Expose
    @Column(name = "email", length = 50)
    private String email;

    @Expose
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Expose
    @Column(name = "patronomic", length = 50)
    private String patronomic;

}
