package com.payroll.entity;

import com.payroll.converter.BooleanToZeroOneConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 70)
    private String name;

    @Column(name = "iso2", nullable = false, unique = true, length = 2)
    private String iso2;

    @Column(name = "iso3", nullable = false, length = 3)
    private String iso3;

    @Column(name = "phone_code", nullable = false)
    private Long phoneCode;

    @Convert(converter = BooleanToZeroOneConverter.class)
    @Column(name = "postcode_required", nullable = false, columnDefinition = "CHAR(1) DEFAULT '0'")
    private Boolean postcodeRequired;

    @Convert(converter = BooleanToZeroOneConverter.class)
    @Column(name = "is_eu", nullable = false, columnDefinition = "CHAR(1) DEFAULT '0'")
    private Boolean isEu;
}
