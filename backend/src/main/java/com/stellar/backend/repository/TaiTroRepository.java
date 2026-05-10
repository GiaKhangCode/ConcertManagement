package com.stellar.backend.repository;

import com.stellar.backend.entity.TaiTro;
import com.stellar.backend.entity.TaiTroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaiTroRepository extends JpaRepository<TaiTro, TaiTroId> {
}
