package com.stellar.backend.repository;

import com.stellar.backend.entity.ApDung;
import com.stellar.backend.entity.ApDungId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApDungRepository extends JpaRepository<ApDung, ApDungId> {
}
