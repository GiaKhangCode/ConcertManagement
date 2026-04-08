package com.stellar.backend.repository;
import com.stellar.backend.entity.SuKien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuKienRepository extends JpaRepository<SuKien, Long> {
    // Lấy danh sách sự kiện theo trạng thái (VD: "Chờ phê duyệt", "Sắp diễn ra")
    List<SuKien> findByTrangThai(String trangThai);

    // Lọc theo danh sách trạng thái — dùng cho public API (chỉ hiện sự kiện đã duyệt)
    List<SuKien> findByTrangThaiIn(List<String> trangThaiList);

    // Lấy danh sách sự kiện của một nhà tổ chức theo mã tài khoản
    List<SuKien> findByNguoiTao_MaTaiKhoan(Long maTaiKhoan);

    // Tìm kiếm sự kiện theo tên hoặc địa điểm, chỉ lấy các trạng thái được phép hiển thị
    @Query("SELECT s FROM SuKien s WHERE s.trangThai IN :trangThaiList AND " +
           "(LOWER(s.tenSuKien) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(s.diaDiem IS NOT NULL AND LOWER(s.diaDiem.tenDiaDiem) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<SuKien> searchByKeywordAndTrangThaiIn(@Param("keyword") String keyword,
                                               @Param("trangThaiList") List<String> trangThaiList);

    // Tìm kiếm sự kiện theo tên hoặc địa điểm (mở rộng thêm nghệ sĩ sau nếu cần)
    List<SuKien> findByTenSuKienContainingIgnoreCaseOrDiaDiem_TenDiaDiemContainingIgnoreCase(String keyword1, String keyword2);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE SuKien s SET s.laSuKienNoiBat = 0")
    void resetTatCaSuKienNoiBat();
}
