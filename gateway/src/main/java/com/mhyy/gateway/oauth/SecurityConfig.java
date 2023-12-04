//package com.mhyy.gateway.oauth;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Autowired
//    private AccessManager accessManager;
//
//    @Bean
//    SecurityWebFilterChain WebFluxSecurityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
//        //AuthenticationWebFilter 过滤器 有一个必要的组件, ReactiveAuthenticationManager权限管理器
//        //权限管理器需要底层数据层的支持, 需要DB连接资源类
//        ReactiveJdbcAuthenticationManager reactiveJdbcAuthenticationManager =
//                new ReactiveJdbcAuthenticationManager(dataSource);
//        AuthenticationWebFilter authenticationWebFilter =
//                new AuthenticationWebFilter(reactiveJdbcAuthenticationManager);
//        authenticationWebFilter.
//                setServerAuthenticationConverter(new ServerBearerTokenAuthenticationConverter());
//        serverHttpSecurity.httpBasic().disable()
//                .csrf().disable()
//                .authorizeExchange()
//                .pathMatchers(HttpMethod.OPTIONS).permitAll()
//                .anyExchange().access(accessManager)
//                .and().addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);
//
//        return serverHttpSecurity.build();
//    }
//}
