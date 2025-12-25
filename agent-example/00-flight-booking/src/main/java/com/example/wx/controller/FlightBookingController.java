package com.example.wx.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.wx.service.FlightBookingService;
import com.example.wx.service.FlightBookingService.BookingDetails;

@RestController
@RequestMapping
public class FlightBookingController {

    private final FlightBookingService flightBookingService;

    public FlightBookingController(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }

    @RequestMapping("/api/bookings")
	@ResponseBody
	public List<BookingDetails> getBookings() {
		return flightBookingService.getBookings();
	}
    
}
