package dtri.com.tw.db.pgsql.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.OqcInspectionForm;

public interface OqcInspectionFormDao extends JpaRepository<OqcInspectionForm, Long> {
	// 查詢 標籤
	List<OqcInspectionForm> findAllByOifid(Long id);

	//查詢 工單號
	List<OqcInspectionForm> findByOifow(String oifow);
	
//	 查詢 工單 客戶名稱 訂單號  "(:sysnote is null or c.sysnote LIKE %:sysnote% ) "//
	@Query("SELECT d FROM OqcInspectionForm d WHERE "//
			+ "(:oifow is null or d.oifow LIKE %:oifow%) and "//
			+ "(:oifcname is null or d.oifcname LIKE %:oifcname%) and "//	
			+ "(:oifonb is null or d.oifonb LIKE %:oifonb%) and "
			+ "(:sysstatus is -1 or d.sysstatus =:sysstatus)")
	List<OqcInspectionForm> findByoifowAndoifcnameAndoifonb(String oifow, String oifcname, String oifonb, int sysstatus,Pageable pageable);

}