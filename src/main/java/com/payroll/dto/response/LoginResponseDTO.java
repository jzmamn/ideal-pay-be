package com.payroll.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String userName;
    private String name;
    private Long companyId;
}
