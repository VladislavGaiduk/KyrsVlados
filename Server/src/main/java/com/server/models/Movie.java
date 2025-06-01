package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "movies")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Movie {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @Column(nullable = false, length = 255)
    private String title;

    @Expose
    @Column(nullable = false)
    private float rating;

    @Expose
    @Column(name = "year")
    private int year;

    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}