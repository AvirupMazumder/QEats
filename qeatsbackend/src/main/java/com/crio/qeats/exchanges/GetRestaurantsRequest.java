/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class GetRestaurantsRequest {

  @NotNull
  @Min(-90)
  @Max(90)
  private Double latitude;
  @NotNull
  @Min(-180)
  @Max(180)
  private Double longitude;
    
   
  private String searchFor;

  public GetRestaurantsRequest(double latitude,double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
