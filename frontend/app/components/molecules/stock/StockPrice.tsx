import { useState, useEffect } from "react";
import styled from "styled-components";
import StockCurrentPrice from "../../atoms/stock/StockCurrentPrice";
import StockChange from "../../atoms/stock/StockChange";
import formatPrice from "@/app/utils/stock/formatPrice";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const StockPrice = ({ market }) => {
  useEffect(() => {
    const socket = new WebSocket("wss://api.upbit.com/websocket/v1");

    socket.onopen = () => {
      console.log(`Connected to ${market}`);
      socket.send(
        JSON.stringify([
          { ticket: "UNIQUE_TICKET" },
          {
            type: "ticker",
            codes: [market],
            isOnlySnapshot: true,
            isOnlyRealtime: true,
          },
          { format: "DEFAULT" },
        ])
      );
    };

    socket.onmessage = async (event) => {
      const data =
        event.data instanceof Blob ? await event.data.text() : event.data;
      const jsonData = JSON.parse(data);

      if (jsonData && jsonData.trade_price) {
        setPrice(jsonData.trade_price);
      }

      if (jsonData && jsonData.change_price && jsonData.change) {
        setChangePrice(
          jsonData.change === "FALL"
            ? -jsonData.change_price
            : jsonData.change_price
        );
      }

      if (jsonData && jsonData.change_rate && jsonData.change) {
        setChangeRate(jsonData.change_rate * 100);
      }
    };

    socket.onclose = () => {
      console.log(`Disconnected from ${market}`);
    };
  }, []);

  const [price, setPrice] = useState(0);
  const [changePrice, setChangePrice] = useState(0);
  const [changeRate, setChangeRate] = useState(0);

  return (
    <>
      <Container>
        <StockCurrentPrice currentPrice={formatPrice(price)} />
        <StockChange changePrice={changePrice} changeRate={changeRate} />
      </Container>
    </>
  );
};

export default StockPrice;
