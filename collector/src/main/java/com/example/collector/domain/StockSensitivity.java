package com.example.collector.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Table(name="stocksensitivity")
@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class StockSensitivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double score;

    @Column
    @Setter
    private Double performance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id")
    private Indicator indicator;

    @Setter
    private String unit;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
