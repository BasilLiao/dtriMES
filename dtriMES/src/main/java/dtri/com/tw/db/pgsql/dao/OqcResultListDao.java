package dtri.com.tw.db.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.OqcResultList;

public interface OqcResultListDao extends JpaRepository<OqcResultList, Long> {
	// 查詢 標籤
	List<OqcResultList> findAllByOrlid(Long id);

//	// 查詢SN重複
//	List<OqcInspectionItems> findAllByPbsn(String pbsn);
	
	//查詢 工單 
	List<OqcResultList> findByOrlow(String orlow);
	
//	 查詢 工單且只搜尋資料狀態為0或1  若 sys_status 不是 0、1 → 直接查不到資料
	//完全比對 =:
	@Query("SELECT d FROM OqcResultList d WHERE "//
			+ "(:orlow is null or d.orlow =:orlow) and "//
			+ "(:orlpsn is null or d.orlpsn =:orlpsn) and "//
			+ "d.sysstatus IN (0, 1)")
	List<OqcResultList> findByOrlowAndOrlpsn(String orlow,String orlpsn);
	
	//************************* 沒用到 *******************
	//LIKE比對 模式  "(:sysnote is null or c.sysnote LIKE %:sysnote% ) "//
	@Query("SELECT d FROM OqcResultList d WHERE "//
			+ "(:orlow is null or d.orlow LIKE %:orlow%) and "//
			+ "(:orlpsn is null or d.orlpsn LIKE %:orlpsn%) and "//
			+ "(:sysstatus is -1 or d.sysstatus =:sysstatus)")
	List<OqcResultList> findByLikeOrlowAndOrlpsn(String orlow,String orlpsn,int sysstatus);
	
	
	// （計算指定工單和資料狀態為0中，每個 SN 最後一筆紀錄為 PASS 的數量）
    @Query("SELECT COUNT(t1)  FROM OqcResultList t1   WHERE "
    		+ "t1.orlow = :orlow "
    		+ " AND t1.sysstatus = 0 "
    		+ "	AND t1.orlid = ( "
    		+ "		SELECT MAX(t2.orlid) "
            + " 	FROM OqcResultList t2   "
            + "		WHERE t2.orlpsn = t1.orlpsn "
            + " 	 AND t2.orlow = :orlow  AND t2.sysstatus = 0 )"
            + " AND t1.orltresults = 'PASS'")        
    long countLastPassByOrlow( String orlow);
    
			
	// （計算指定 工單 和 測試項目 和 資料狀態 為0中，每個 SN 最後一筆紀錄為 PASS 的數量）
    @Query("SELECT COUNT(t1)  FROM OqcResultList t1   WHERE "
    		+ "t1.orlow = :orlow "
    		+ " AND t1.sysstatus = 0 "
    		+ "	AND t1.orlid = ( "
    		+ "		SELECT MAX(t2.orlid) "
            + " 	FROM OqcResultList t2   "
            + "		WHERE t2.orlpsn = t1.orlpsn "
            + " 	AND t2.orlow = :orlow   AND t2.orltitem = :orltitem AND t2.sysstatus = 0 )"         
            + " AND t1.orltresults = 'PASS'")        
    long countLastPassByOrlowAndOrltitem( String orlow ,String orltitem);
    
    
    
    //用配對檢驗表的Key 刪除 資料
    Long deleteByOrloifid(Long id);

}