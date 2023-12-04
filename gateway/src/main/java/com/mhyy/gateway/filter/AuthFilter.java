package com.mhyy.gateway.filter;

import com.mhyy.gateway.feignclient.Oauth2ServiceClient;
import io.netty.util.concurrent.CompleteFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Lazy
    @Autowired
    private Oauth2ServiceClient oauth2ServiceClient;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        log.info("path: {}, contains: {}", path, path.contains("/oauth"));
        if (path.contains("/oauth") || path.contains("/user/register")) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("Authorization");
//        Map<String, Object> result = oauth2ServiceClient.checkToken(token);
        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> oauth2ServiceClient.checkToken(token));
        Map<String, Object> result = future.get();
        boolean active = (boolean) result.get("active");
        if (!active) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        ServerHttpRequest httpRequest = request.mutate().headers(httpHeaders -> {
            httpHeaders.set("personId", request.getHeaders().getFirst("personId"));
//            httpHeaders.set("tracingId", "");
        }).build();
        exchange.mutate().request(httpRequest);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
