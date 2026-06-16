package com.payroll.service;

import com.payroll.dto.request.LoginRequestDTO;
import com.payroll.dto.request.RefreshRequestDTO;
import com.payroll.dto.response.LoginResponseDTO;
import com.payroll.entity.RefreshToken;
import com.payroll.entity.RevokedToken;
import com.payroll.entity.Usr;
import com.payroll.repository.RefreshTokenRepository;
import com.payroll.repository.RevokedTokenRepository;
import com.payroll.repository.UsrRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UsrRepository usrRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry-days:7}")
    private long refreshTokenExpiryDays;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usr user = usrRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword().toLowerCase(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is inactive");
        }

        // Rotate: delete old refresh tokens for this user
        refreshTokenRepository.deleteAllByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .userId(user.getId())
                .userName(user.getUsername())
                .name(user.getName())
                .companyId(user.getCompanyId())
                .build();
    }

    public LoginResponseDTO refresh(RefreshRequestDTO request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new BadCredentialsException("Refresh token expired — please log in again");
        }

        Usr user = stored.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is inactive");
        }

        // Rotate refresh token
        refreshTokenRepository.delete(stored);
        String newRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(newRefreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .userId(user.getId())
                .userName(user.getUsername())
                .name(user.getName())
                .companyId(user.getCompanyId())
                .build();
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.parseToken(token);
            RevokedToken revoked = RevokedToken.builder()
                    .tokenJti(jwtService.extractJti(claims))
                    .expiresAt(jwtService.extractExpiry(claims))
                    .revokedAt(LocalDateTime.now())
                    .build();
            revokedTokenRepository.save(revoked);

            // Extract userId from claims and delete all refresh tokens
            Long userId = claims.get("userId", Long.class);
            if (userId != null) {
                refreshTokenRepository.deleteAllByUserId(userId);
            }
        } catch (Exception ex) {
            log.debug("Logout called with unparseable token — ignoring: {}", ex.getMessage());
        }
    }
}
