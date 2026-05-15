package com.stellar.backend.repository;

import com.stellar.backend.entity.ViCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ViCaNhanRepository extends JpaRepository<ViCaNhan, Long> {
    Optional<ViCaNhan> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);

    /**
     * Demo LOST UPDATE — gọi PROC_DEMO_WALLET_PAY.
     * Procedure: đọc SoDu → DBMS_SESSION.SLEEP(7) → UPDATE SoDu = cũ - amount.
     * Không COMMIT bên trong; Spring @Transactional quản lý commit.
     *
     * Cách demo:
     *   Tab A: mua vé / thanh toán
     *   Tab B: cùng hành động trên cùng tài khoản, chạy ngay sau Tab A
     *   → Cả 2 đọc SoDu = X trước khi bên kia commit → ghi đè lẫn nhau
     */
    @Modifying
    @Query(value = "CALL PROC_DEMO_WALLET_PAY(:maKH, :soTien)", nativeQuery = true)
    void demoPay(@Param("maKH") Long maTaiKhoan,
                 @Param("soTien") BigDecimal soTien);

    /**
     * Demo LOST UPDATE (nhận tiền) — gọi PROC_DEMO_WALLET_RECEIVE.
     * Procedure: đọc SoDu → DBMS_SESSION.SLEEP(7) → UPDATE SoDu = cũ + amount.
     */
    @Modifying
    @Query(value = "CALL PROC_DEMO_WALLET_RECEIVE(:maKH, :soTien)", nativeQuery = true)
    void demoReceive(@Param("maKH") Long maTaiKhoan,
                     @Param("soTien") BigDecimal soTien);
}
