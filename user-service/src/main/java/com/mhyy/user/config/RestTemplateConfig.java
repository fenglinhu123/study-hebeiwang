package com.mhyy.user.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private CloseableHttpClient httpClient;

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        return httpComponentsClientHttpRequestFactory;
    }

    @Bean(name = "innerRestTemplate")
    @LoadBalanced
    public RestTemplate getInnerRestTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }

        return restTemplate;
    }

    @Bean(name = "outerRestTemplate")
    public RestTemplate getOuterRestTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> converter : messageConverters) {
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
            }
        }

        return restTemplate;
    }
}
