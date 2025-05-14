package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//import dtri.com.tw.db.entity.RepairRmaDetail;
import dtri.com.tw.db.entity.RmaList;

public interface RmaListDao extends JpaRepository<RmaList, Long> {
	
	//完全比對
	// List<Rmalist> findByRmaNumber(String rmaNumber);

	//模糊比對  使用 JPQL 或原生 SQL 查詢
	//    @Query("SELECT r FROM Rmalist r WHERE r.rmaNumber LIKE %:rmaNumber%")
	//    ArrayList<RmaList> findByRmaNumberLike(@Param("rmaNumber") String rmaNumber);	
	    
	  //@Param("rmaNumber")  綁定查詢方法中的參數。
	    
	    @Query("SELECT r FROM RmaList r WHERE r.rmaNumber LIKE %:rmaNumber%")
	    List<RmaList> findByRmaNumberContaining(@Param("rmaNumber") String rmaNumber);
		    
	    @Query("SELECT r FROM RmaList r WHERE r.rmaNumber LIKE %:rmaNumber%")
	    Set<RmaList> findByRmaNoContaining(String rmaNumber);
	  		
	    // 方法中的參數名稱和**@Query 中的佔位符的命名參數**需要對應，這樣 Spring Data JPA 才能正確地將方法中的參數值傳遞到 JPQL 查詢中。
		@Query("SELECT d FROM RmaList d WHERE "
				   + "(:id is null or d.id = :id) and "
			       + "(:rmaNumber is null or d.rmaNumber LIKE %:rmaNumber%) and "
			       + "(:serialNumber is null or d.serialNumber LIKE %:serialNumber%) and "
			       + "(:mbNumber is null or d.mbNumber LIKE %:mbNumber%) and "
			       + "(:customer is null or d.customer LIKE %:customer%) and " 		       
			       + "(:issue is null or d.issue LIKE %:issue%) and "
			       + "(:state is null or d.state LIKE %:state%) and "
			       + "(:stateCheck is null or d.stateCheck =:stateCheck) "		       
				   + "order by d.rmaNumber , d.stateCheck , d.serialNumber , d.sysmdate asc ")		//sysmdate
		ArrayList<RmaList> findAllByRdidAndRdruidBat1(Long id,String rmaNumber, String serialNumber, String mbNumber,String customer, String issue,String state,Integer stateCheck);
				
		//維修頁面搜尋
		@Query("SELECT d FROM RmaList d WHERE "
				   + "(:id is null or d.id =:id) and "
				   + "(:rmaNumber is null or d.rmaNumber  LIKE %:rmaNumber%) and "
				   + "(:serialNumber is null or d.serialNumber =:serialNumber) and "
			       + "(:mbNumber is null or d.mbNumber =:mbNumber) and "
			       + "(COALESCE(:excludedStates, NULL) IS NULL OR d.stateCheck NOT IN (:excludedStates))")
		List<RmaList> findAllBysnAndmb(Long id,String rmaNumber, String serialNumber, String mbNumber, List<Integer> excludedStates);
		
		// 移除資料
//		ArrayList<RmaList> deleteByRmaid(Long id);
		// 移除資料  deleteBy 方法回傳值應為 Long 或 void
		Long deleteByid(Long id);
		Long deleteByrmaNumber(String rmaNumber);
		//Spring Data JPA 需要 And 來區分不同欄位。
		Long deleteByRmaNumberAndSerialNumber(String rmaNumber, String serialNumber);
		
		//方法會回傳該 RMA 編號的筆數，如果大於 0，表示存在。
		 long countByRmaNumber(String rmaNumber);
		// 檢查 rma_number 是否存在
		 boolean existsByRmaNumber(String rmaNumber);  
				
		ArrayList<RmaList> findAll();
		
		@Query("SELECT d FROM RmaList d WHERE "
				+ "(d.id = :id)  "			    
				+ "order by d.rmaNumber asc ")		
		ArrayList<RmaList> findByid(Long id);
		
		// findAllBy後面加上要查詢的欄位名稱,不能隨便亂編繪錯誤
		ArrayList<RmaList> findAllByrmaNumber(String rmaNumber);
		
		// findAllBy後面加上要查詢的欄位名稱,不能隨便亂編繪錯誤
		ArrayList<RmaList> findAllByserialNumber(String serialNumber);
		//需要 And 來區分不同欄位。
		ArrayList<RmaList> findAllByRmaNumberAndSerialNumber(String rmaNumber,String serialNumber);
}
