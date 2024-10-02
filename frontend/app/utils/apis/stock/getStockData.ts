import { defaultRequest } from "../request";
import axios from "axios";

interface StockDailyPriceProps {
  stockDate: string;
  lowPrice: number;
  highPrice: number;
  startPrice: number;
  endPrice: number;
  prdyVrss: string;
  prdyVrssSign: string;
  prdyCtrt: string;
}

function toTimestamp(dateString) {
  // yyyymmddhhmmss 형식의 문자열을 분리
  const year = dateString.substring(0, 4);
  const month = dateString.substring(4, 6) - 1; // 월은 0부터 시작 (0 = 1월)
  const day = dateString.substring(6, 8);
  const hour = dateString.substring(8, 10);
  const minute = dateString.substring(10, 12);
  const second = dateString.substring(12, 14);

  // Date 객체 생성
  const date = new Date(year, month, day, hour, minute, second);

  // 타임스탬프 반환 (밀리초 단위이므로 1000으로 나눠서 초 단위로 반환)
  return date.getTime();
}

export const getDailyStockData = async (
  stockCode: string
): Promise<StockDailyPriceProps | void> => {
  try {
    const res = await defaultRequest.get(`/stock/${stockCode}/dailyhistory`);
    console.log("여기");
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const getMinuteStockData = async (stock_code: string) => {
  const now = new Date();

  const hours = String(now.getHours()).padStart(2, "0"); // HH
  const minutes = String(now.getMinutes()).padStart(2, "0"); // MM
  const seconds = String(now.getSeconds()).padStart(2, "0"); // SS

  const time = `${hours}${minutes}${seconds}`;

  try {
    const res = await axios({
      method: "GET",
      url: `${process.env.NEXT_PUBLIC_API_BASE_URL}/stock/${stock_code}/minute-chart/${time}`,
    });

    const formattedData = res.data.minuteStockPrices.map((data) => {
      return {
        timestamp: toTimestamp(data.stckBsopDate.concat(data.stckCntgHour)),
        open: parseInt(data.stckOprc),
        high: parseInt(data.stckHgpr),
        low: parseInt(data.stckLwpr),
        close: parseInt(data.stckPrpr),
        volume: 0,
        turnover: 0,
        // volume: data.cntgVol,
        // turnover: data.acmlTrPbmn,
      };
    });

    formattedData.sort((a, b) => a.timestamp - b.timestamp);

    return formattedData;
  } catch (error) {
    console.log(error);
  }
};

export const getPastStockData = async (stock_code: string, type: string) => {
  try {
    const res = await axios({
      method: "GET",
      url: `${process.env.NEXT_PUBLIC_API_BASE_URL}/stock`,
    });

    console.log(res.data);
  } catch (error) {
    console.log(error);
  }
};

export const getCurrentPrice = async (stock_code: string) => {
  try {
    const res = await axios({
      method: "GET",
      url: `${process.env.NEXT_PUBLIC_API_BASE_URL}/stock/${stock_code}/current`,
    });

    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const getHeaderStockData = async (
  stock_code: string,
  loc_date: string
) => {
  try {
    const res = await axios({
      method: "GET",
      url: `${process.env.NEXT_PUBLIC_API_BASE_URL}/stock/${stock_code}/${loc_date}`,
    });

    console.log(res.data);
    return res.data;
  } catch (error) {
    console.log(error);
  }
};
