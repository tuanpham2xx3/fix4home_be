package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Booking;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUser(User user);
    
    Page<Booking> findByUser(User user, Pageable pageable);
    
    List<Booking> findByStatus(BookingStatus status);
    
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
    
    List<Booking> findByUserAndStatus(User user, BookingStatus status);
    
    Page<Booking> findByUserAndStatus(User user, BookingStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    Page<Booking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status, Pageable pageable);
    
    Optional<Booking> findByIdAndUser(Long id, User user);
}

