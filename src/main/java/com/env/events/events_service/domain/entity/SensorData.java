package com.env.events.events_service.domain.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Table("sensor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    private Long id;

    @Column("device_id")
    private String deviceId;

    @Column("temperature")
    private BigDecimal temperature;

    @Column("humidity")
    private BigDecimal humidity;

    @Column("pressure")
    private BigDecimal pressure;

    @Column("aqi")
    private Short aqi;

    @Column("tvoc_ppb")
    private Integer tvocPpb;

    @Column("eco2_ppm")
    private Integer eco2Ppm;

    @Column("created_at")
    private OffsetDateTime createdAt;
}