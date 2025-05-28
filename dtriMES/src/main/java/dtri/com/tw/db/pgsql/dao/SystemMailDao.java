package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemMail;

public interface SystemMailDao extends JpaRepository<SystemMail, Long> {

	// 帳號查詢
//	RmaMail findBySuaccount(String suaccount);

	// 查詢全部
	ArrayList<SystemMail> findAll();

	// 查詢ID
	ArrayList<SystemMail> findAllBySuid(Long id);

	// 查詢全部含-頁數
	@Query("SELECT c FROM SystemMail c "//
			+ "WHERE "
			+ "(:sureceived is null or c.sureceived  = :sureceived ) and "//
			+ "(:surepairdone is null or c.surepairdone  = :surepairdone ) and "//
			+ "(:suemail is null or c.suemail = :suemail )  " )//
	ArrayList<SystemMail> findByRmaMail( String sureceived, String surepairdone,String suemail );
	
	
    @Query("SELECT c FROM SystemMail c WHERE:suemail is null or c.suemail = :suemail")
    Set<SystemMail> findBysuemailContaining(String suemail);
	  
	// 查詢全部含-頁數
	@Query("SELECT c FROM SystemMail c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suename is null or c.suename LIKE %:suename% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//
			+ "( c.sysstatus = :sysstatus )  "//
			+ "order by c.suposition asc, c.suname asc")
	ArrayList<SystemMail> findAllBySystemMail( String suname, String suename, String suposition, Integer sysstatus, Pageable pageable);
	
	// 查詢全部含-頁數 不含ADMIN
	@Query("SELECT c FROM SystemMail c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suename is null or c.suename LIKE %:suename% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//	
			+ "( c.sysstatus = :sysstatus )  "//
			+ "order by c.suposition asc, c.suname asc")
	ArrayList<SystemMail> findAllBySystemMailNotAdmin( String suname, String suename, String suposition, Integer sysstatus, Pageable pageable);

	// 查詢全部含-頁數 不含ADMIN 排序名稱
	@Query("SELECT c FROM SystemMail c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "( c.sysstatus = 0 )  "//
			+ "order by c.suposition asc,c.suname asc")
	ArrayList<SystemMail> findAllBySystemMailNotAdmin(String suname);
	
	// @Query註解裡面寫JPQL語句,定義查詢
	@Query(nativeQuery = false, value = " SELECT i FROM SystemMail i WHERE su_id = ?1")
	SystemMail readId(Long id);

	Long deleteBySuid(Long suid);

}