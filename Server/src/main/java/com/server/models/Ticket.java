package com.server.models;

import com.google.gson.annotations.Expose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"session_id", "seat_number"}
    )
)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Ticket {
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Expose
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Expose
    @Column(name = "purchase_time", nullable = false, updatable = false)
    private LocalDateTime purchaseTime = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (purchaseTime == null) {
            purchaseTime = LocalDateTime.now();
        }
    }
}
