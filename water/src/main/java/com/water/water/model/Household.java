package com.water.water.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"households", "buildings"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Apartment apartment;

    // Direct relationship to Building block
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id")
    @JsonIgnoreProperties({"colony"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Building building;

    @Column(nullable = false)
    private String block;

    @Column(name = "flat_number", nullable = false)
    private String flatNumber;

    @Column(name = "has_meter", nullable = false)
    private boolean hasMeter = true;

    @Column(name = "area_sqm", nullable = false)
    private double areaSqm = 100.0;
}
