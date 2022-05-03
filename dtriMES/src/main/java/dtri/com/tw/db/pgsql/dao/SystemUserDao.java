package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemUser;

public interface SystemUserDao extends JpaRepository<SystemUser, Long> {

	// 帳號查詢
	SystemUser findBySuaccount(String suaccount);

	// 查詢全部
	ArrayList<SystemUser> findAll();

	// 查詢ID
	ArrayList<SystemUser> findAllBySuid(Long id);

	// 查詢全部含-頁數
	@Query("SELECT c FROM SystemUser c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suaccount is null or c.suaccount LIKE %:suaccount% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//
			+ "( c.sysstatus = :sysstatus )  "//
			+ "order by c.suposition asc, c.suname asc")
	ArrayList<SystemUser> findAllBySystemUser(String suname, String suaccount, String suposition, Integer sysstatus, Pageable pageable);

	// 查詢全部含-頁數 不含ADMIN
	@Query("SELECT c FROM SystemUser c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suaccount is null or c.suaccount LIKE %:suaccount% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//
			+ "( c.sysstatus = :sysstatus ) and (c.susggid != 1) "//
			+ "order by c.suposition asc, c.suname asc")
	ArrayList<SystemUser> findAllBySystemUserNotAdmin(String suname, String suaccount, String suposition, Integer sysstatus, Pageable pageable);

	// 查詢全部含-頁數 不含ADMIN 排序名稱
	@Query("SELECT c FROM SystemUser c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suaccount is null or c.suaccount LIKE %:suaccount% ) and "//
			+ "( c.sysstatus = 0 ) and (c.susggid != 1) "//
			+ "order by c.suposition asc,c.suname asc")
	ArrayList<SystemUser> findAllBySystemUserNotAdmin(String suname, String suaccount);

	// @Query註解裡面寫JPQL語句,定義查詢
	@Query(nativeQuery = false, value = " SELECT i FROM SystemUser i WHERE su_id = ?1")
	SystemUser readId(Long id);

	@Query(nativeQuery = false, value = " SELECT i.suname FROM SystemUser i WHERE "//
			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
	ArrayList<String> readAccounts(List<String> accounts);

	Long deleteBySuid(Long suid);

}