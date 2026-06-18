package com.fincore.common.security.controller;

import com.fincore.common.security.jwt.JwtTokenProvider;
import com.fincore.common.security.jwt.JwtUserDetails;
import com.fincore.common.base.dto.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        var details = (JwtUserDetails) auth.getPrincipal();
        String accessToken = jwtTokenProvider.createAccessToken(
                details.getUserId(), details.getUsername(), details.getRoles(), details.getPermissions());
        String refreshToken = jwtTokenProvider.createRefreshToken(details.getUserId());

        return Result.ok(new LoginResponse(accessToken, refreshToken, 7200L,
                new UserInfo(details.getUserId(), details.getUsername(), details.getRoles(), details.getPermissions())));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private UserInfo userInfo;
    }

    @Data @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private List<String> roles;
        private List<String> permissions;
    }
}
