package com.sog.stock.domain.model;

import com.sog.stock.domain.dto.FinancialDTO;
import com.sog.stock.domain.dto.FinancialListDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "financial_statements")
public class FinancialStatements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer financialStatementsId;

    @Column(nullable = false)
    private String stacyymm;

    @Column(nullable = true)
    private BigDecimal totalLiabilites;

    @Column(nullable = false)
    private BigDecimal totalEquity;

    @Column(nullable = false)
    private BigDecimal currentAssets;

    @Column(nullable = true)
    private BigDecimal currentLiabilites;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_code", nullable = false)
    private Stock stock;

}
