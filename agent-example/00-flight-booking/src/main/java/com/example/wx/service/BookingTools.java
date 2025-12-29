package com.example.wx.service;

import com.example.wx.domain.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;

import java.time.LocalDate;
import java.util.function.Function;

/**
 * @author wangx
 * @description
 * @create 2025/6/4 16:04
 */
@Configuration
public class BookingTools {

    private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

    @Autowired
    private FlightBookingService flightBookingService;

    public record BookingDetailsRequest(String bookingNumber, String name) {
    }

    public record ChangeBookingDatesRequest(String bookingNumber, String name, String date, String from, String to) {
    }

    public record CancelBookingRequest(String bookingNumber, String name) {
    }

    /**
     * 通过 @Bean + Function<...> 注册为可被模型调用的函数
     * 并与具体业务实现类进行解耦
     * @return
     */
    @Bean
    @Description("获取机票预定详细信息")
    public Function<BookingDetailsRequest, FlightBookingService.BookingDetails> getBookingDetails() {
        return request -> {
            try {
                return flightBookingService.getBookingDetails(request.bookingNumber(), request.name());
            }
            catch (Exception e) {
                logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
                return new FlightBookingService.BookingDetails(request.bookingNumber(), request.name(), null, null, null, null, null);
            }
        };
    }

    @Bean
    @Description("修改机票预定日期")
    public Function<ChangeBookingDatesRequest, String> changeBooking() {
        return request -> {
            flightBookingService.changeBooking(request.bookingNumber(), request.name(), request.date(), request.from(),
                    request.to());
            return "";
        };
    }

    @Bean
    @Description("取消机票预定")
    public Function<CancelBookingRequest, String> cancelBooking() {
        return request -> {
            flightBookingService.cancelBooking(request.bookingNumber(), request.name());
            return "";
        };
    }
}
