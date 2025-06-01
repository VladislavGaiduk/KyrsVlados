package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "genres")
@Getter
@Setter
@ToString
public class Genre {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @Column(unique = true, nullable = false, length = 255)
    private String name;

    public Genre() {}

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
}