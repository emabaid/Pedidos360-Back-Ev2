package abaid.pedidos360.rabbitadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeInfo(String name, String vhost, String type, Boolean durable) {}
