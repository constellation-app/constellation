package au.gov.asd.tac.constellation.webserver.api.swagger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author spica
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExampleResponse (@JsonProperty("code") String code, @JsonProperty("description") String description, @JsonProperty("content") String content) {
}

