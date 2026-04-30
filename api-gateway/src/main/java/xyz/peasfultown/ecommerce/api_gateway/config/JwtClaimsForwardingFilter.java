package xyz.peasfultown.ecommerce.api_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtClaimsForwardingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .flatMap(jwt -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", jwt.getSubject())
                            .header("X-User-Role", jwt.getClaimAsString("role"))
                            .header("X-User-Email", jwt.getClaimAsString("email"))
                            // remove the Authorization header
                            // since downstream doesn't need to validate jwt anymore
                            .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                // if no JWT present (public route), just forward as-is
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;  // run before other filters
    }
}
