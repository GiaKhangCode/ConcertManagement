package com.stellar.backend.repository;

import com.stellar.backend.entity.KhuVucSoDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhuVucSoDoRepository extends JpaRepository<KhuVucSoDo, Long> {
    List<KhuVucSoDo> findBySoDoSuKien_MaSoDo(Long maSoDo);
}
