package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.RmaMail;

public interface RmaMailListDao extends JpaRepository<RmaMail, Long> {

	// 帳號查詢
//	RmaMail findBySuaccount(String suaccount);

	// 查詢全部
	ArrayList<RmaMail> findAll();

	// 查詢ID
	ArrayList<RmaMail> findAllBySuid(Long id);

	// 查詢全部含-頁數
	@Query("SELECT c FROM RmaMail c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suename is null or c.suename LIKE %:suename% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//
			+ "(:susggid=0L or c.susggid = :susggid ) and "//
			+ "( c.sysstatus = :sysstatus )  "//
			+ "order by c.suposition asc, c.suname asc")
	ArrayList<RmaMail> findAllByRmaMail(Long susggid, String suname, String suename, String suposition, Integer sysstatus, Pageable pageable);
	
	// 查詢全部含-頁數 不含ADMIN
	@Query("SELECT c FROM RmaMail c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suename is null or c.suename LIKE %:suename% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//
			+ "(:susggid=0L or c.susggid = :susggid ) and "//
			+ "( c.sysstatus = :sysstatus ) and (c.susggid != 1) "//
			+ "order by c.suposition asc, c.suname asc")
	ArrayList<RmaMail> findAllByRmaMailNotAdmin(Long susggid, String suname, String suename, String suposition, Integer sysstatus, Pageable pageable);

	// 查詢全部含-頁數 不含ADMIN 排序名稱
	@Query("SELECT c FROM RmaMail c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
//			+ "(:suaccount is null or c.suaccount LIKE %:suaccount% ) and "//
			+ "( c.sysstatus = 0 ) and (c.susggid != 1) "//
			+ "order by c.suposition asc,c.suname asc")
//	ArrayList<RmaMail> findAllByRmaMailNotAdmin(String suname, String suaccount);
	ArrayList<RmaMail> findAllByRmaMailNotAdmin(String suname);
	
	// @Query註解裡面寫JPQL語句,定義查詢
	@Query(nativeQuery = false, value = " SELECT i FROM RmaMail i WHERE su_id = ?1")
	RmaMail readId(Long id);

//	@Query(nativeQuery = false, value = " SELECT i.suname FROM RmaMail i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

	Long deleteBySuid(Long suid);

}