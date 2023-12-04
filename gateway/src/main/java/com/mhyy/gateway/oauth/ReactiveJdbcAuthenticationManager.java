//package com.mhyy.gateway.oauth;
//
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
//import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import javax.sql.DataSource;
//
//@Component
//public class ReactiveJdbcAuthenticationManager implements ReactiveAuthenticationManager {
//
//    //通过datasource创建的一个东西
//    private TokenStore tokenStore;
//
//    public ReactiveJdbcAuthenticationManager(DataSource dataSource) {
//        this.tokenStore = new JdbcTokenStore(dataSource);
//    }
//
//    @Override
//    public Mono<Authentication> authenticate(Authentication authentication) {
//
//        return Mono.justOrEmpty(authentication)
//                .filter(a -> a instanceof BearerTokenAuthenticationToken)
//                .cast(BearerTokenAuthenticationToken.class)
//                .map(BearerTokenAuthenticationToken::getToken)
//                .flatMap(accessToken -> {
//                    OAuth2AccessToken oAuth2AccessToken = this.tokenStore.readAccessToken(accessToken);
//                    if (oAuth2AccessToken == null) {
//                        return Mono.error(new InvalidTokenException("InvalidTokenException!"));
//                    } else if (oAuth2AccessToken.isExpired()) {
//                        return Mono.error(new InvalidTokenException("InvalidTokenException! is Expired."));
//                    }
//                    OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
//                    if (oAuth2Authentication == null) {
//                        return Mono.error(new InvalidTokenException("Fake token!"));
//                    }
//                    return Mono.justOrEmpty(oAuth2Authentication);
//                }).cast(Authentication.class);
//    }
//}
