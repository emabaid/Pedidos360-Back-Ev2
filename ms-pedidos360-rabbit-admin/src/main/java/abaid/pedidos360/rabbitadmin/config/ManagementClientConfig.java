package abaid.pedidos360.rabbitadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Cliente HTTP de la API de gestion de RabbitMQ (puerto 15672). Se usa solo para LISTAR;
// crear y borrar se hace por AMQP con RabbitAdmin.
@Configuration
public class ManagementClientConfig {

    @Bean
    public RestClient rabbitManagementClient(
            @Value("${pedidos360.rabbit.management-url}") String url,
            @Value("${spring.rabbitmq.username}") String user,
            @Value("${spring.rabbitmq.password}") String password) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);

        String basic = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        return RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }
}
