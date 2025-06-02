package com.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.server.utils.LocalDateTimeAdapter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Session {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Expose
    @Column(name = "start_time", nullable = false)
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime startTime;

    @Expose
    @Column(name = "end_time", nullable = false)
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime endTime;

    @Expose
    @Column(nullable = false, columnDefinition = "NUMERIC(10,2)")
    private float price;
}
