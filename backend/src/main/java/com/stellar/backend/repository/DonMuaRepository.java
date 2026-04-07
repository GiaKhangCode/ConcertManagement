package com.stellar.backend.repository;
import com.stellar.backend.entity.DonMua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonMuaRepository extends JpaRepository<DonMua, Long> {
    List<DonMua> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);
}
