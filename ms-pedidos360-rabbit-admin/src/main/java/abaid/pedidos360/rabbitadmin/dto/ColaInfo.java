package abaid.pedidos360.rabbitadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ColaInfo(String name, String vhost, Boolean durable, Long messages, Integer consumers, String state) {}
