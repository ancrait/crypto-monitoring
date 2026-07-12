package com.sorokaandriy.price_alert.service;

import com.sorokaandriy.price_alert.dto.UserAlertRequest;
import com.sorokaandriy.price_alert.dto.UserAlertResponse;
import com.sorokaandriy.price_alert.entity.UserAlert;
import com.sorokaandriy.price_alert.entity.enumeration.Direction;
import com.sorokaandriy.price_alert.exception.UserAlertNotFoundException;
import com.sorokaandriy.price_alert.repository.UserAlertRepository;
import com.sorokaandriy.price_alert.service.mapper.UserAlertMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAlertServiceTest {

    @Mock
    private UserAlertRepository repository;

    @Mock
    private UserAlertMapper mapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserAlertService service;


    @Test
    void shouldCreateAlertAndCacheInRedis() {
        UserAlertRequest request = new UserAlertRequest(
                132117L, "BTC", new BigDecimal("50000.00"), Direction.ABOVE);

        UserAlert entity = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("50000.00"), Direction.ABOVE, true, Instant.now());

        UserAlertResponse response = new UserAlertResponse(
                12L, "BTC", new BigDecimal("50000.00"), Direction.ABOVE, true, Instant.now());


        when(repository.existsByChatIdAndSymbolAndTargetPrice(
                request.getChatId(), request.getSymbol(), request.getTargetPrice()))
                .thenReturn(false);
        when(mapper.fromUserAlertRequestToUserAlert(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.fromUserAlertToUserAlertResponse(entity)).thenReturn(response);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);


        UserAlertResponse result = service.createUserAlert(request);

        assertEquals(response, result);
        verify(repository).save(entity);
        ArgumentCaptor<UserAlertResponse> captor = ArgumentCaptor.forClass(UserAlertResponse.class);
        verify(valueOperations).set(eq("alerts:132117:BTC"), captor.capture(), eq(Duration.ofMinutes(3)));
        assertEquals(response, captor.getValue());
    }

    @Test
    void shouldThrowExceptionWhenDuplicateAlert() {
        UserAlertRequest request = new UserAlertRequest(
                132117L, "BTC", new BigDecimal("50000.00"), Direction.ABOVE);

        when(repository.existsByChatIdAndSymbolAndTargetPrice(
                request.getChatId(), request.getSymbol(), request.getTargetPrice()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.createUserAlert(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldUpdateAlertAndDeleteFromCache(){
        UserAlertRequest request = new UserAlertRequest(
                132117L, "BTC", new BigDecimal("53000.00"), Direction.BELOW);

        UserAlert entity = new UserAlert(12L, 132117L, "BTC",

                new BigDecimal("50000.00"), Direction.ABOVE, true, Instant.now());

        UserAlert updatedEntity = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("53000.00"), Direction.BELOW, true, Instant.now());

        UserAlertResponse response = new UserAlertResponse(
                12L, "BTC", new BigDecimal("53000.00"), Direction.BELOW, true, Instant.now());

        when(repository.findByChatIdAndSymbolAndTargetPrice(request.getChatId(), request.getSymbol(),
                request.getTargetPrice())).thenReturn(Optional.of(entity));
        when(repository.save(updatedEntity))
                .thenReturn(updatedEntity);
        when(mapper.fromUserAlertToUserAlertResponse(updatedEntity))
                .thenReturn(response);


        UserAlertResponse result = service.updateUserAlert(request, request.getChatId(),
                request.getSymbol(), request.getTargetPrice());

        assertEquals(response, result);
        assertEquals(new BigDecimal("53000.00"), entity.getTargetPrice());
        assertEquals(Direction.BELOW, entity.getDirection());
        verify(repository).save(updatedEntity);
        verify(redisTemplate).delete("alerts:132117:BTC");

    }


    @Test
    void shouldNotUpdateAlert(){
        UserAlertRequest request = new UserAlertRequest(
                132117L, "BTC", new BigDecimal("53000.00"), Direction.BELOW);

        BigDecimal existPrice = new BigDecimal("50000.00");

        when(repository.findByChatIdAndSymbolAndTargetPrice(request.getChatId(),
                request.getSymbol(), existPrice))
                .thenThrow(new UserAlertNotFoundException("alert not found"));

        assertThrows(UserAlertNotFoundException.class, () -> service.updateUserAlert(request,request.getChatId(),
                request.getSymbol(), existPrice));

        verify(repository, never()).save(any());
        verifyNoInteractions(redisTemplate);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldDeleteAlert(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        UserAlert entity = new UserAlert(12L, 132117L, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        when(repository.findByChatIdAndSymbolAndTargetPrice(chatId,symbol,targetPrice))
                .thenReturn(Optional.of(entity));

        service.deleteUserAlert(chatId, symbol, targetPrice);

        verify(repository).delete(entity);
        verify(redisTemplate).delete("alerts:132117:BTC");

    }

    //todo
    @Test
    void shouldReturnUserAlertFromCache(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        UserAlertResponse response = new UserAlertResponse(12L,"BTC",
                new BigDecimal("53000"),Direction.ABOVE, true, Instant.now());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("alerts:132117:BTC"))
                .thenReturn(response);

        UserAlertResponse userAlertResponse = service
                .getUserAlert(chatId, symbol, targetPrice);


      //  assertEquals(userAlertResponse, response);
        verify(redisTemplate,times(1)).opsForValue();
        verify(valueOperations,times(1)).get(any());
        verifyNoInteractions(repository);

    }


    @Test
    void shouldReturnUserAlertFromDb(){

        Long chatId = 132117L;
        String symbol = "BTC";
        BigDecimal targetPrice = new BigDecimal("53000.00");

        UserAlertResponse response = new UserAlertResponse(12L,"BTC",
                new BigDecimal("53000"),Direction.ABOVE, true, Instant.now());

        UserAlert entity = new UserAlert(12L,chatId,"BTC",
                new BigDecimal("53000"),Direction.ABOVE, true, Instant.now());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("alerts:132117:BTC"))
                .thenReturn(null);
        when(repository.findByChatIdAndSymbolAndTargetPrice(chatId, symbol, targetPrice))
                .thenReturn(Optional.of(entity));
        when(mapper.fromUserAlertToUserAlertResponse(entity))
                .thenReturn(response);

        UserAlertResponse userAlertResponse = service
                .getUserAlert(chatId, symbol, targetPrice);


        assertEquals(userAlertResponse, response);
        verify(repository).findByChatIdAndSymbolAndTargetPrice(chatId, symbol, targetPrice);
        verify(mapper).fromUserAlertToUserAlertResponse(entity);
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations).get(eq("alerts:132117:BTC"));
        verify(valueOperations).set(eq("alerts:132117:BTC"), eq(response), eq(Duration.ofMinutes(3)));

    }

    @Test
    void shouldReturnAllAlertsByChatId(){

        Long chatId = 21332L;

        UserAlert entity = new UserAlert(12L, chatId, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        UserAlert entity2 = new UserAlert(13L, chatId, "ETH",
                new BigDecimal("54000.00"), Direction.BELOW, true, Instant.now());

        UserAlertResponse response1 = new UserAlertResponse(12L, "BTC",
                new BigDecimal("53000.00"), Direction.ABOVE, true, Instant.now());

        UserAlertResponse response2 = new UserAlertResponse(13L, "ETH",
                new BigDecimal("54000.00"), Direction.BELOW, true, Instant.now());

        when(repository.findByChatId(chatId))
                .thenReturn(List.of(entity, entity2));
        when(mapper.fromUserAlertToUserAlertResponse(entity)).thenReturn(response1);
        when(mapper.fromUserAlertToUserAlertResponse(entity2)).thenReturn(response2);

        List<UserAlertResponse> result = service.getUserAlerts(chatId);

        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
        verify(repository).findByChatId(chatId);
        verify(mapper).fromUserAlertToUserAlertResponse(entity);
        verify(mapper).fromUserAlertToUserAlertResponse(entity2);
    }

    @Test
    void shouldChangeEnabled(){

        Long chatId = 122310L;
        String symbol = "SOL";
        BigDecimal targetPrice = new BigDecimal("22000.00");

        UserAlert userAlert = new UserAlert(16L, chatId, symbol, targetPrice,
                Direction.ABOVE, true, Instant.now());


        when(repository.findByChatIdAndSymbolAndTargetPrice(chatId, symbol, targetPrice))
                .thenReturn(Optional.of(userAlert));


        Boolean enabled = service.changeEnabled(chatId, symbol, targetPrice);

        assertEquals(false, enabled);
        assertEquals(false, userAlert.getEnabled());
        verify(repository).findByChatIdAndSymbolAndTargetPrice(chatId,symbol,targetPrice);
        verify(repository).save(userAlert);
        verify(redisTemplate).delete("alerts:122310:SOL");


    }







}
