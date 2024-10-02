import styled from "@emotion/styled";
import CompanyInfo from "@/app/components/organisms/stock/CompanyInfoContainer";
import DividendInfo from "@/app/components/molecules/stock/DividendInfo";
import ValuationMetrics from "@/app/components/molecules/stock/ValuationMetrics";
import FinancialMetricsContainer from "@/app/components/organisms/stock/FinancialMetricsContainer";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
`;

const SubContainer = styled.div`
  display: flex;
  flex-direction: column;
  min-width: 100%;
  height: auto;
`;

const StockInfoTemplate = () => {
  return (
    <>
      <Container>
        <SubContainer>
          <CompanyInfo />
          <ValuationMetrics />
        </SubContainer>
        <SubContainer>
          <FinancialMetricsContainer />
        </SubContainer>
      </Container>
    </>
  );
};

export default StockInfoTemplate;
