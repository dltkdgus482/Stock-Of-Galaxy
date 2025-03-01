package com.sog.stock.domain.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockFrequencyByDateListDTO {

    private List<StockFrequencyByDateDTO> stockFrequencyByDateList;

}
