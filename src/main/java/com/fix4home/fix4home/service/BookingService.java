package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BookingNotFoundException;
import com.fix4home.fix4home.model.dto.booking.*;
import com.fix4home.fix4home.model.entity.Booking;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.BookingStatus;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService extends BaseService implements DTOConverter<Booking, BookingDTO> {

    private final BookingRepository bookingRepository;
    private final NotificationHelperService notificationHelperService;

    // ==================== CUSTOMER OPERATIONS ====================

    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        logBusinessOperation("CREATE_BOOKING", "title=" + request.getTitle());

        validateRequired(request, "request");
        User user = getCurrentUser();
        requireRole(Role.CUSTOMER);

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .title(request.getTitle())
                .address(request.getAddress())
                .date(request.getDate())
                .notes(request.getNotes())
                .phone(request.getPhone())
                .name(request.getName())
                .wardCode(request.getWardCode())
                .needsSurvey(request.getNeedsSurvey() != null ? request.getNeedsSurvey() : false)
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        
        // Create notification for booking creation
        notificationHelperService.createBookingNotification(user, savedBooking);
        
        return convertToDTO(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingListResponseDTO getBookings(BookingStatus status, Integer page, Integer limit) {
        logBusinessOperation("GET_BOOKINGS", "status=" + status, "page=" + page, "limit=" + limit);

        User user = getCurrentUser();
        requireRole(Role.CUSTOMER);

        // Set defaults for pagination
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());

        Page<Booking> bookingPage;
        if (status != null) {
            bookingPage = bookingRepository.findByUserAndStatus(user, status, pageable);
        } else {
            bookingPage = bookingRepository.findByUser(user, pageable);
        }

        List<BookingDTO> bookingDTOs = bookingPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return BookingListResponseDTO.builder()
                .bookings(bookingDTOs)
                .total(bookingPage.getTotalElements())
                .page(pageNumber)
                .limit(pageSize)
                .build();
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        logBusinessOperation("GET_BOOKING_BY_ID", "id=" + id);

        validatePositiveId(id, "id");
        User user = getCurrentUser();
        requireRole(Role.CUSTOMER);

        Booking booking = findBookingByIdAndUser(id, user);
        return convertToDTO(booking);
    }

    @Transactional
    public BookingDTO updateBooking(Long id, UpdateBookingRequest request) {
        logBusinessOperation("UPDATE_BOOKING", "id=" + id);

        validatePositiveId(id, "id");
        validateRequired(request, "request");
        User user = getCurrentUser();
        requireRole(Role.CUSTOMER);

        Booking booking = findBookingByIdAndUser(id, user);

        // Only allow updates to PENDING bookings
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot update booking with status: " + booking.getStatus());
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            booking.setTitle(request.getTitle());
        }
        if (request.getAddress() != null) {
            booking.setAddress(request.getAddress());
        }
        if (request.getDate() != null) {
            booking.setDate(request.getDate());
        }
        if (request.getNotes() != null) {
            booking.setNotes(request.getNotes());
        }
        if (request.getPhone() != null) {
            booking.setPhone(request.getPhone());
        }
        if (request.getName() != null) {
            booking.setName(request.getName());
        }
        if (request.getWardCode() != null) {
            booking.setWardCode(request.getWardCode());
        }
        if (request.getNeedsSurvey() != null) {
            booking.setNeedsSurvey(request.getNeedsSurvey());
        }

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }

    @Transactional
    public BookingDTO cancelBooking(Long id) {
        logBusinessOperation("CANCEL_BOOKING", "id=" + id);

        validatePositiveId(id, "id");
        User user = getCurrentUser();
        requireRole(Role.CUSTOMER);

        Booking booking = findBookingByIdAndUser(id, user);

        // Only allow cancellation of PENDING bookings
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel booking with status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        // Create notification for booking cancellation
        notificationHelperService.createCancelBookingNotification(user, savedBooking);

        return convertToDTO(savedBooking);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Booking findBookingByIdAndUser(Long id, User user) {
        return bookingRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BookingNotFoundException(id, user.getId()));
    }

    // ==================== DTO CONVERSION METHODS ====================

    @Override
    public BookingDTO convertToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .title(booking.getTitle())
                .address(booking.getAddress())
                .date(booking.getDate())
                .notes(booking.getNotes())
                .phone(booking.getPhone())
                .name(booking.getName())
                .wardCode(booking.getWardCode())
                .needsSurvey(booking.getNeedsSurvey())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}

