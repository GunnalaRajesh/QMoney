
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
  RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException, RuntimeException{
        List<Candle> stocksStartToEndDate = new ArrayList<>();
        if(from.compareTo(to) >= 0){
          throw new RuntimeException();
        }
        String url = buildUri(symbol, from, to);

        try {
            String stocks = restTemplate.getForObject(url, String.class);
            ObjectMapper objectMapper = getObjectMapper();
            TiingoCandle[] stocksStartToEndDateArray = objectMapper.readValue(stocks, TiingoCandle[].class);

            if(stocksStartToEndDateArray != null){
              stocksStartToEndDate = Arrays.asList(stocksStartToEndDateArray);
            }else{
              stocksStartToEndDate = Arrays.asList(new TiingoCandle[0]);
            }
        } catch (NullPointerException e) {
          throw new StockQuoteServiceException("Error occured when requesting responce from Tiingo API", e.getCause());
        }
        
        return stocksStartToEndDate;
  }
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    final String token="d93323cdfc99b8b622c280676435f5b8ed74c288";
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+token;
    // System.out.println(uriTemplate);
    return uriTemplate;
  }
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
