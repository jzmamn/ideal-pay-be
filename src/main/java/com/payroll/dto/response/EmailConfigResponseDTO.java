package com.payroll.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmailConfigResponseDTO {
    private Long    id;
    private String  host;
    private Integer port;
    private String  username;
    private String  fromName;
    private String  fromAddress;
    private Boolean useTls;
    private Boolean isActive;
}
