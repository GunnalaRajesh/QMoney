
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {
   private RestTemplate restTemplate;
    private StockQuotesService stockQuoteService;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuoteService) {
    this.stockQuoteService = stockQuoteService;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to){
        if(from.compareTo(to) >= 0){
          throw new RuntimeException("Sell date cannot be before purchase date");
        }
        String url = buildUri(symbol, from, to);
        Candle[] stocksStartToEndDate = restTemplate.getForObject(url, TiingoCandle[].class);
       
        return Arrays.asList(stocksStartToEndDate);
        
  }

  

  protected static String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
        String token="d93323cdfc99b8b622c280676435f5b8ed74c288";
        String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+token;
        // String url = uriTemplate.replace("$APIKEY",token).replace("$SYMBOL",symbol).replace("$STARTDATE",startDate.toString()).replace("$ENDDATE",endDate.toString());
        System.out.println(uriTemplate);
        return uriTemplate;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
        AnnualizedReturn annualizedReturn = null;
        List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
          for(int i=0;i<portfolioTrades.size();i++){
            try {
              annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i),endDate);
            } catch (JsonProcessingException | StockQuoteServiceException e) {
              e.printStackTrace();
            }
            annualizedReturns.add(annualizedReturn);
          }
          Comparator<AnnualizedReturn> SortByAnnReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
          Collections.sort(annualizedReturns,SortByAnnReturn);
        return annualizedReturns;
  }


  private AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endLocalDate) throws JsonProcessingException, StockQuoteServiceException{
    AnnualizedReturn annualizedReturn;
    String symbol = trade.getSymbol();
    LocalDate startLocalDate = trade.getPurchaseDate();
    List<Candle> stocksStartToEndDate;
    stocksStartToEndDate = stockQuoteService.getStockQuote(symbol, startLocalDate,endLocalDate);
    Candle stockStartDate = stocksStartToEndDate.get(0);
    Candle stockLatest = stocksStartToEndDate.get(stocksStartToEndDate.size()-1);

    Double buyPrice = stockStartDate.getOpen();
    Double sellPrice = stockLatest.getClose();

    // Total Returns 
    Double totalReturn = (sellPrice-buyPrice)/buyPrice;

    // caluclate years 
    Double numYears = (double)ChronoUnit.DAYS.between(startLocalDate,endLocalDate)/365;

    // annualized returns using formulae 
    Double annualizedReturns = Math.pow((1+totalReturn), (1/numYears))-1;
    annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, totalReturn);
    return annualizedReturn;
  }


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
