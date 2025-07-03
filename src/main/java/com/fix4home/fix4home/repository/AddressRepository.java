package com.fix4home.fix4home.repository;

import com.fix4home.fix4home.model.entity.Address;
import com.fix4home.fix4home.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUser(User user);
    
    List<Address> findByUserId(Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.id = :addressId")
    Optional<Address> findByUserIdAndAddressId(@Param("userId") Long userId, @Param("addressId") Long addressId);
    
    @Query("SELECT a FROM Address a WHERE a.city = :city")
    List<Address> findByCity(@Param("city") String city);
    
    @Query("SELECT a FROM Address a WHERE a.district = :district")
    List<Address> findByDistrict(@Param("district") String district);
    
    boolean existsByUserIdAndId(Long userId, Long addressId);
} 