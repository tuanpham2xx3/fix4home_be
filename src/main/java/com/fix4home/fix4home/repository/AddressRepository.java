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
    
    // Vietnam Administrative API integration queries
    @Query("SELECT a FROM Address a WHERE a.provinceCode = :provinceCode")
    List<Address> findByProvinceCode(@Param("provinceCode") String provinceCode);
    
    @Query("SELECT a FROM Address a WHERE a.wardCode = :wardCode")
    List<Address> findByWardCode(@Param("wardCode") String wardCode);
    
    @Query("SELECT a FROM Address a WHERE a.provinceCode = :provinceCode AND a.wardCode = :wardCode")
    List<Address> findByProvinceCodeAndWardCode(@Param("provinceCode") String provinceCode, 
                                               @Param("wardCode") String wardCode);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.provinceCode = :provinceCode")
    List<Address> findByUserIdAndProvinceCode(@Param("userId") Long userId, 
                                            @Param("provinceCode") String provinceCode);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.wardCode = :wardCode")
    List<Address> findByUserIdAndWardCode(@Param("userId") Long userId, 
                                        @Param("wardCode") String wardCode);
    
    // Enhanced location-based search with province/ward filtering
    @Query("SELECT a FROM Address a WHERE " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:provinceCode IS NULL OR a.provinceCode = :provinceCode) AND " +
           "(:wardCode IS NULL OR a.wardCode = :wardCode)")
    List<Address> findByLocationCriteria(@Param("city") String city,
                                       @Param("provinceCode") String provinceCode,
                                       @Param("wardCode") String wardCode);
    
    boolean existsByUserIdAndId(Long userId, Long addressId);
} 