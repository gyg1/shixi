package org.example.cityplan.config;

import org.example.cityplan.security.CustomUserDetailsService;
import org.example.cityplan.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 实现三级权限控制: USER < ADMIN < SUPER_ADMIN
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (使用 JWT 无状态认证)
                .csrf(AbstractHttpConfigurer::disable)

                // 启用 CORS
                .cors(cors -> {
                })

                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求授权
                .authorizeHttpRequests(authz -> authz
                        // 公开接口 - 无需认证
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/projects/files/*/download").permitAll() // 允许公开下载/预览图片
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 静态资源
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/static/**", "/assets/**").permitAll()

                        // 超级管理员接口
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")

                        // 管理员接口 (ADMIN 和 SUPER_ADMIN 都可访问)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 用户管理接口 - 仅超级管理员
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 项目管理接口 - 管理员及以上
                        .requestMatchers(HttpMethod.POST, "/api/projects/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 空间数据上传 - 管理员及以上
                        .requestMatchers(HttpMethod.POST, "/api/geodata/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 地图标绘接口
                        .requestMatchers(HttpMethod.GET, "/api/map-drawings/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/map-drawings/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/map-drawings/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/map-drawings/**").authenticated()

                        // 字典接口 - GET 所有认证用户, 修改需管理员
                        .requestMatchers(HttpMethod.GET, "/api/dicts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/dicts/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/dicts/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/dicts/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 表单模板接口
                        .requestMatchers(HttpMethod.GET, "/api/form-templates/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/form-templates/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/form-templates/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/form-templates/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 其他所有请求需要认证
                        .anyRequest().authenticated())

                // 添加 JWT 过滤器
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
