package com.water.water.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "water_purchases")
public class WaterPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"households", "buildings"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Apartment apartment;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double liters;

    @Column(nullable = false)
    private BigDecimal cost;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "block_name")
    private String blockName;

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
