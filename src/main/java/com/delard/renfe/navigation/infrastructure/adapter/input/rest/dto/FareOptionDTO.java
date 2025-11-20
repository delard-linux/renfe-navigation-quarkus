/*
 * Copyright © ${YEAR} MCP Renfe Navigation Quarkus
 * All rights reserved.
 */

package com.delard.renfe.navigation.infrastructure.adapter.input.rest.dto;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for individual train fare
 */
public record FareOptionDTO(String name,Double price,String currency,String code,@JsonProperty("tp_enlace")String tpEnlace,String plan,List<String>features){
/**
 * Compact constructor for default values and defensive copying
 */
public FareOptionDTO{
// Default currency if null
if(currency==null){currency="EUR";}
// Defensive copy of features list
if(features==null){features=new ArrayList<>();}else{features=new ArrayList<>(features);}}

/**
 * Constructor with default values for convenience
 */
public FareOptionDTO(String name,Double price,String code,String tpEnlace){this(name,price,"EUR",code,tpEnlace,null,new ArrayList<>());}

}
