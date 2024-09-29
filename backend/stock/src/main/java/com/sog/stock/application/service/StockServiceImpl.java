package com.sog.stock.application.service;

import com.sog.stock.application.client.KisPresentPriceClient;
import com.sog.stock.domain.dto.FinancialDTO;
import com.sog.stock.domain.dto.FinancialListDTO;
import com.sog.stock.domain.dto.HolidayAddListRequestDTO;
import com.sog.stock.domain.dto.HolidayAddRequestDTO;
import com.sog.stock.domain.dto.QuarterStockPriceDTO;
import com.sog.stock.domain.dto.QuarterStockPriceListDTO;
import com.sog.stock.domain.dto.StockPresentPriceResponseDTO;
import com.sog.stock.domain.dto.kis.KisPresentPriceResponseDTO;
import com.sog.stock.domain.dto.rocket.RocketAddRequestDTO;
import com.sog.stock.domain.dto.StockAddListRequestDTO;
import com.sog.stock.domain.dto.StockDTO;
import com.sog.stock.domain.dto.DailyStockPriceListDTO;
import com.sog.stock.domain.dto.DailyStockPriceDTO;
import com.sog.stock.domain.dto.StockNameResponseDTO;
import com.sog.stock.domain.enums.QuarterType;
import com.sog.stock.domain.model.DailyStockHistory;
import com.sog.stock.domain.model.FinancialStatements;
import com.sog.stock.domain.model.QuarterStockHistory;
import com.sog.stock.domain.model.Rocket;
import com.sog.stock.domain.model.Stock;
import com.sog.stock.domain.model.StockHoliday;
import com.sog.stock.domain.repository.DailyStockHistoryRepository;
import com.sog.stock.domain.repository.FinancialStatementsRepository;
import com.sog.stock.domain.repository.QuarterStockHistoryRepository;
import com.sog.stock.domain.repository.RocketRepository;
import com.sog.stock.domain.repository.StockHolidayRepository;
import com.sog.stock.domain.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final DailyStockHistoryRepository dailyStockHistoryRepository;
    private final QuarterStockHistoryRepository quarterStockHistoryRepository;
    private final StockHolidayRepository stockHolidayRepository;
    private final StockRepository stockRepository;
    private final RocketRepository rocketRepository;
    private final FinancialStatementsRepository financialStatementsRepository;

    private final KisTokenService kisTokenService;
    private final KisPresentPriceClient kisPresentPriceClient;

    @Override
    public DailyStockPriceListDTO getDailyStockHistory(String stockCode) {

        // 종목번호로 모든 데이터 조회
        List<DailyStockHistory> historyList = dailyStockHistoryRepository.findByStock_StockCodeOrderByDailyStockHistoryDateDesc(
            stockCode);

        // entity 리스트 -> dto리스트 변환
        List<DailyStockPriceDTO> dtoList = historyList.stream()
            .map(DailyStockPriceDTO::fromEntity)
            .collect(Collectors.toList());

        // DTO 리스트를 감싸서 반환
        return new DailyStockPriceListDTO(dtoList);
    }

    @Override
    public void addDailyStockHistory(DailyStockPriceListDTO stockDailyPriceList) {
        List<DailyStockPriceDTO> dtoList = stockDailyPriceList.getStockDailyPriceList();

        // 각 DTO를 Entity로 변환하여 저장
        List<DailyStockHistory> entityList = dtoList.stream()
            .map(dto -> {
                // stockCode로 Stock 엔티티 조회
                Stock stock = stockRepository.findById(dto.getStockCode())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "해당 종목 코드가 존재하지 않습니다: " + dto.getStockCode()));

                // DTO를 Entity로 변환 (Stock 객체와 연결)
                return dto.toEntity(stock);
            })
            .collect(Collectors.toList());

        // 변환된 엔티티를 저장
        dailyStockHistoryRepository.saveAll(entityList);
    }

    @Override
    public void addQuarterStockHistory(QuarterStockPriceListDTO quarterStockPriceList) {
        // 각 QuarterStockPriceDTO를 entity로 변환 후 저장
        List<QuarterStockPriceDTO> quarterStocklist = quarterStockPriceList.getQuarterStockPriceList();

        List<QuarterStockHistory> entityList = quarterStocklist.stream()
            .map(dto -> {
                // stockCode로 Stock 엔티티 조회
                Stock stock = stockRepository.findById(dto.getStockCode())
                    .orElseThrow((() -> new IllegalArgumentException(
                        "해당 종목 코드가 존재하지 않습니다: " + dto.getStockCode())));
                return dto.toEntity(stock);

            })
            .collect(Collectors.toList());

        quarterStockHistoryRepository.saveAll(entityList);
    }

    @Override
    public QuarterStockPriceListDTO getQuarterStockHistory(String stockCode,
        QuarterType quarterType) {
        List<QuarterStockHistory> historyList;

        // quarterType에 따라 다르게 처리
        switch (quarterType) {
            case D:
                // "D" 타입일 경우 최근 90개의 데이터를 반환
                historyList = quarterStockHistoryRepository.findTop90ByStock_StockCodeAndQuarterTypeOrderByStock_EstDtDesc(
                    stockCode, quarterType);
                break;
            case M:
                // "M" 타입일 경우 최근 60개의 데이터를 반환
                historyList = quarterStockHistoryRepository.findTop60ByStock_StockCodeAndQuarterTypeOrderByStock_EstDtDesc(
                    stockCode, quarterType);
                break;
            case Y:
                // "Y" 타입일 경우 해당 종목코드의 모든 데이터를 반환
                historyList = quarterStockHistoryRepository.findByStock_StockCodeAndQuarterType(
                    stockCode, quarterType);
                break;
            default:
                throw new IllegalArgumentException("Invalid quarter type: " + quarterType);
        }

        // 데이터가 비어 있는 경우 처리
        if (historyList == null || historyList.isEmpty()) {
            throw new NoSuchElementException(
                "No stock history found for the given stock code and quarter type.");
        }

        // List<QuarterStockHistory> -> List<QuarterStockPriceDTO> 변환
        List<QuarterStockPriceDTO> stockPriceDTOList = historyList.stream()
            .map(QuarterStockPriceDTO::fromEntity) // fromEntity 메서드를 사용하여 변환
            .collect(Collectors.toList());

        // DTO 리스트를 담은 QuarterStockPriceListDTO 생성
        QuarterStockPriceListDTO stockPriceListDTO = new QuarterStockPriceListDTO();
        stockPriceListDTO.setQuarterStockPriceList(stockPriceDTOList); // 변환된 리스트 설정

        return stockPriceListDTO; // DTO 리스트 반환
    }


    @Override
    public void addStockList(StockAddListRequestDTO stockAddListRequestDTO) {
        // 각 stockDTO를 stock entity로 변환후 저장
        for (StockDTO addStock : stockAddListRequestDTO.getStocks()) {
            Stock stock = Stock.fromDTO(addStock);
            stockRepository.save(stock);
        }
    }

    @Override
    public void addStock(StockDTO stockAddRequest) {
        Stock stock = Stock.fromDTO(stockAddRequest);
        stockRepository.save(stock);
    }

    @Override
    public StockDTO searchStock(String stockCode) {
        Stock stock = stockRepository.findById(stockCode)
            .orElseThrow(() -> new RuntimeException("행성을 찾을 수 없습니다."));

        return stock.toDTO();
    }

    @Override
    public void addHolidayList(HolidayAddListRequestDTO holidayAddListRequestDTO) {
        // 각 holiday DTO를 StockHoliday 엔티티로 변환 후 저장
        for (HolidayAddRequestDTO holidayAddRequestDTO : holidayAddListRequestDTO.getHolidays()) {
            StockHoliday stockHoliday = StockHoliday.fromDTO(holidayAddRequestDTO);
            stockHolidayRepository.save(stockHoliday);
        }
    }

    @Override
    public boolean isHoliday(String holidayDate) {
        return stockHolidayRepository.existsByLocDate(holidayDate);
    }

    @Override
    public void addFinancialList(FinancialListDTO financialList) {
        // 각 financial dto를 entity로 변환 후 저장
        for (FinancialDTO financialDTO : financialList.getFinancialList()) {
            // stockCode가 null인지 확인
            System.out.println(financialDTO);
            if (financialDTO.getStock_code() == null || financialDTO.getStock_code().isEmpty()) {
                throw new IllegalArgumentException("종목 코드가 null이거나 비어있습니다.");
            }
            // Stock 조회
            Stock stock = stockRepository.findById(financialDTO.getStock_code())
                .orElseThrow(() -> new IllegalArgumentException(
                    "해당 종목 코드가 존재하지 않습니다: " + financialDTO.getStock_code()));

            // DTO TO ENTITY
            FinancialStatements financialStatements = FinancialStatements.builder()
                .stacyymm(financialDTO.getStac_yymm())
                .totalLiabilities(financialDTO.getTotal_liabilities())
                .totalEquity(financialDTO.getTotal_equity())
                .currentAssets(financialDTO.getCurrent_assets())
                .currentLiabilities(financialDTO.getCurrent_liabilities())
                .stock(stock)
                .build();

            financialStatementsRepository.save(financialStatements);
        }

    }

    @Override
    public FinancialListDTO searchFinancial(String stockCode) {
        // 종목번호별로 제무재표 list 조회
        // stockCode로 Stock 객체 조회
        Stock stock = stockRepository.findById(stockCode)
            .orElseThrow(() -> new IllegalArgumentException("해당 종목 코드가 존재하지 않습니다: " + stockCode));

        // Stock에 해당하는 FinancialStatements 리스트 조회
        List<FinancialStatements> financialStatementsList = financialStatementsRepository.findByStock(
            stock);

        // FinancialDTO 리스트로 변환
        List<FinancialDTO> financialDTOList = financialStatementsList.stream()
            .map(fs -> new FinancialDTO(
                fs.getStock().getStockCode(),
                fs.getStacyymm(),
                fs.getCurrentAssets(),
                fs.getCurrentLiabilities(),
                fs.getTotalLiabilities(),
                fs.getTotalEquity()
            ))
            .collect(Collectors.toList());

        // FinancialListDTO에 담아서 반환
        return new FinancialListDTO(financialDTOList);
    }

    @Override
    public StockNameResponseDTO searchStockName(String stockCode) {
        // stockCode로 db에서 검색하여 Stock 객체를 반환
        Stock stock = stockRepository.findById(stockCode)
            .orElseThrow(() -> new IllegalArgumentException("해당 종목 코드가 존재하지 않습니다: " + stockCode));

        // StockNameResponseDTO로 변환하여 반환
        return new StockNameResponseDTO(stock.getCorpName());
    }

    @Override
    public StockPresentPriceResponseDTO searchStockPresentPrice(String stockCode) {
        // kisToken redis에서
        String token = kisTokenService.getAccessToken().block(); // 동기처리
        if (token == null) {
            throw new RuntimeException("kis 토큰 접근 실패");
        }

        // 한국투자증권 API에 현재가 요청
        KisPresentPriceResponseDTO response = kisPresentPriceClient.requestStockPresentPrice(stockCode, token);

        // 응답값을 StockPresentPriceResponseDTO로 매핑하여 반환
        return StockPresentPriceResponseDTO.builder()
            .stockCode(stockCode)
            .stckPrpr(response.getOutput().getStckPrpr())         // 현재가
            .prdyVrss(response.getOutput().getPrdyVrss())         // 전일대비
            .prdyVrssSign(response.getOutput().getPrdyVrssSign()) // 전일대비 부호
            .prdyCtrt(response.getOutput().getPrdyCtrt())         // 전일대비율
            .build();
    }


    // 로켓 전체 조회 -> 같은 주식 코드로 연결된 로켓 리스트
//    @Override
//    public RocketResponseListDTO getAllRocketsByStockCode(String stockCode) {
//        Stock stock = stockRepository.findById(stockCode)
//            .orElseThrow(() -> new RuntimeException("Stock not found"));
//
//        // rocket에 memberId로 user서버에 사용자 정보 조회해와서 표시.
//        List<RocketResponseDTO> rocketList = rocketRepository.findByStock(stock).stream()
//            .map(rocket -> {
//                return RocketResponseDTO.builder()
//                    .nickname(rocket.getNickname())
//                    .characterType(rocket.getCharacterType())
//                    .createdAt(rocket.getRocketCreatedAt())
//                    .message(rocket.getContent())
//                    .price(rocket.getStockPrice())
//                    .build();
//            })
//            .collect(Collectors.toList());
//
//        return new RocketResponseListDTO(rocketList);
//    }
//
//    // 로켓 개별 조회
//    @Override
//    public RocketResponseDTO getRocketById(int rocketId) {
//        return rocketRepository.findById(rocketId)
//            .map(rocket -> RocketResponseDTO.builder()
//                .nickname(rocket.getNickname())
//                .characterType(rocket.getCharacterType())
//                .createdAt(rocket.getRocketCreatedAt())
//                .message(rocket.getContent())
//                .price(rocket.getStockPrice())
//                .build())
//            .orElseThrow(() -> new RuntimeException("Rocket not found"));
//    }
//
    @Override
    public boolean deleteRocket(int rocketId, Long memberId) {
        // 로켓 조회 후 memberId 확인
        return rocketRepository.findById(rocketId)
            .filter(rocket -> rocket.getMemberId().equals(memberId))  // memberId가 일치하는지 확인
            .map(rocket -> {
                rocket.markAsDeleted();  // 소프트 삭제 (isDeleted 플래그 변경)
                rocketRepository.save(rocket);  // 변경사항 저장
                return true;
            })
            .orElse(false);  // 로켓이 없거나 memberId가 일치하지 않으면 false 반환
    }


    @Override
    public void addRocket(RocketAddRequestDTO rocketAddRequestDTO) {
        // 주식 정보 조회
        Stock stock = stockRepository.findById(rocketAddRequestDTO.getStockCode())
            .orElseThrow(() -> new RuntimeException("해당 종목을 찾을 수 없습니다."));

        // Stock 엔티티와 DTO
        Rocket rocket = Rocket.builder()
            .memberId(rocketAddRequestDTO.getMemberId())
            .content(rocketAddRequestDTO.getMessage())
            .stockPrice(rocketAddRequestDTO.getPrice())
            .rocketCreatedAt(LocalDateTime.now())
            .isDeleted(false)
            .stock(stock)  // 조회한 Stock 엔티티 설정
            .build();

        // 엔티티 저장
        rocketRepository.save(rocket);
    }
}
