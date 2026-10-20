package org.qsp.airportmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "flight")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(
        name = "Flight.findActiveByOriginCode",
        query = "SELECT f FROM Flight f WHERE f.origin.code = :code AND f.active = true"
)
public class Flight extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_id", nullable = false)
    private Airport origin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Airport destination;

    @Column(nullable = false,length=30)
    private String departureTime;

    @Column(nullable = false,length = 30)
    private String arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;

    @Column(nullable = false)
    private boolean active = true;
}
