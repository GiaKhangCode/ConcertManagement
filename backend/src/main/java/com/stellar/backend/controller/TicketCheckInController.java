package com.stellar.backend.controller;

import com.stellar.backend.dto.CheckInRequest;
import com.stellar.backend.dto.CheckInResponse;
import com.stellar.backend.security.UserDetailsImpl;
import com.stellar.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
public class TicketCheckInController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('STAFF')")
    public ResponseEntity<CheckInResponse> checkIn(@RequestBody CheckInRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long staffId = userDetails.getId();
        
        CheckInResponse response = ticketService.checkIn(request, staffId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or hasRole('STAFF')")
    public ResponseEntity<List<CheckInResponse>> getHistory() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.getStaffScanHistory(userDetails.getId()));
    }
}
