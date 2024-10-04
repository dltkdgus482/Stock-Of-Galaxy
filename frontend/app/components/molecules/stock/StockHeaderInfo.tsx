"use client";
import React from "react";
import { useEffect } from "react";
import { useParams } from "next/navigation";
import styled from "@emotion/styled";
import StockHeaderInfoDetail from "../../atoms/stock/StockHeaderInfoDetail";
import { getHeaderStockData } from "../../../utils/apis/stock/getStockData";

interface StockHeaderInfoProps {
  // 필요한 경우 추가적인 props를 정의할 수 있습니다.
}

const Container = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: bold;
  color: #0e224d;
  flex-wrap: wrap;

  @media (max-width: 600px) {
    font-size: 14px;
    gap: 8px;
  }
`;

const Separator = styled.span`
  margin: 0 5px;
  color: #d9d9d9;

  @media (max-width: 600px) {
    margin: 0 3px;
  }
`;

const StockHeaderInfo: React.FC<StockHeaderInfoProps> = () => {
  const { stock, date } = useParams();
  const stock_code = Array.isArray(stock) ? stock[0] : stock ?? "005930";
  const current_date = date ?? "20241004";

  const dividends = [
    { target: "시가총액", targetPrice: 0 },
    { target: "1일 최저", targetPrice: 0 },
    { target: "1일 최고", targetPrice: 0 },
    { target: "1년 최저", targetPrice: 0 },
    { target: "1년 최고", targetPrice: 0 },
  ];

  useEffect(() => {
    getHeaderStockData(stock_code, "20241004").then((data) => {
      dividends[0].targetPrice = 0;
      dividends[1].targetPrice = 0;
      dividends[2].targetPrice = 0;
      dividends[3].targetPrice = 0;
      dividends[4].targetPrice = 0;
    });
  }, []);

  return (
    <Container>
      {dividends.map((dividend, index) => (
        <React.Fragment key={index}>
          <StockHeaderInfoDetail
            target={dividend.target}
            targetPrice={dividend.targetPrice}
          />
          {index < dividends.length - 1 && <Separator> | </Separator>}
        </React.Fragment>
      ))}
    </Container>
  );
};

export default StockHeaderInfo;
