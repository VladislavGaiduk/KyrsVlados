package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "halls")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Hall {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Expose
    @Column(nullable = false)
    private Integer capacity;

    @Expose
    @Column(columnDefinition = "TEXT")
    private String description;
}
