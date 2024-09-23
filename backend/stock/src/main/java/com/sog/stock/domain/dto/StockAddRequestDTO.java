package com.sog.stock.domain.dto;

import com.sog.stock.domain.model.Stock;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
public class StockAddRequestDTO {

    private String stockCode;
    private String companyName;
    private String companyDescription;
    private int establishedYear;
    private String ceo;
    private String webSite;
    private int fiscalMonth;
    private boolean isDelisted;
    private int dividendYear;
    private BigDecimal dividendAmount;
    private int dividendFrequency;
    private BigDecimal totalLiabilities;  // BigDecimal 타입 사용
    private BigDecimal totalEquity;  // BigDecimal 타입 사용
    private BigDecimal currentAssets;  // BigDecimal 타입 사용
    private BigDecimal currentLiabilities;


}
