package com.careerflow.common.client;

import com.careerflow.common.security.InternalAuthSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ServiceWebClientFactory {

    private final InternalClientHeaders internalClientHeaders;

    public ServiceWebClientFactory(InternalClientHeaders internalClientHeaders) {
        this.internalClientHeaders = internalClientHeaders;
    }

    public WebClient create(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(serviceAuthFilter())
                .build();
    }

    public ExchangeFilterFunction serviceAuthFilter() {
        return (request, next) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                ClientRequest authorized = ClientRequest.from(request)
                        .headers(headers -> {
                            headers.remove(InternalClientHeaders.HEADER);
                            headers.set(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + jwtAuthenticationToken.getToken().getTokenValue());
                        })
                        .build();
                return next.exchange(authorized);
            }
            if (InternalAuthSupport.isInternalCall()) {
                ClientRequest internal = ClientRequest.from(request)
                        .header(InternalClientHeaders.HEADER, internalClientHeaders.apiKey())
                        .build();
                return next.exchange(internal);
            }
            return next.exchange(request);
        };
    }
}
