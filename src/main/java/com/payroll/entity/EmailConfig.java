package com.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.payroll.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_config")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host", nullable = false, length = 255)
    private String host;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    /** Stored encrypted/plain — integrate vault or jasypt for production */
    @Column(name = "password", nullable = false, length = 500)
    private String password;

    @Column(name = "from_name", nullable = false, length = 150)
    private String fromName;

    @Column(name = "from_address", nullable = false, length = 255)
    private String fromAddress;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "use_tls", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean useTls;

    @Convert(converter = BooleanToYNConverter.class)
    @Column(name = "is_active", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr createdBy;

    @Column(name = "created_date", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    @JsonIgnoreProperties({"role", "createdBy", "modifiedBy", "hibernateLazyInitializer", "handler"})
    private Usr modifiedBy;

    @Column(name = "modified_date", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime modifiedDate;
}
