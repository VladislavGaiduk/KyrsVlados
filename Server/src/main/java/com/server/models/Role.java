package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Roles")
@Getter
@Setter
@ToString
public class Role {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @Column(unique = true, nullable = false)
    private String name;

    public Role() {}

    public Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

}
