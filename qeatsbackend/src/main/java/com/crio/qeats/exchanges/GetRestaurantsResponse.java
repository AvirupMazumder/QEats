/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import com.crio.qeats.dto.Restaurant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
public class GetRestaurantsResponse {
    
  private List<Restaurant> restaurants;

  @Autowired
  public GetRestaurantsResponse(List<Restaurant> restraurants) {
    this.restaurants = restraurants;
  }

  public void removeNonAsciiCharacters() {
    if (restaurants != null) {
      for (Restaurant restaurant : restaurants) {
        restaurant.removeNonAsciiCharacters();
      }
    }
  }
}

// }
