package dtri.com.tw.db.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.OqcInspectionForm;

public interface OqcInspectionFormDao extends JpaRepository<OqcInspectionForm, Long> {
	// 查詢 標籤
	List<OqcInspectionForm> findAllByOifid(Long id);

//	// 查詢SN重複
//	List<OqcInspectionItems> findAllByPbsn(String pbsn);
	
	//查詢 工單號
	List<OqcInspectionForm> findByOifow(String oifow);
	
//	 查詢 工單 客戶名稱 訂單號  "(:sysnote is null or c.sysnote LIKE %:sysnote% ) "//
	@Query("SELECT d FROM OqcInspectionForm d WHERE "//
			+ "(:oifow is null or d.oifow LIKE %:oifow%) and "//
			+ "(:oifcname is null or d.oifcname LIKE %:oifcname%) and "//	
			+ "(:oifonb is null or d.oifonb LIKE %:oifonb%) and "
			+ "(:sysstatus is -1 or d.sysstatus =:sysstatus)")
	List<OqcInspectionForm> findByoifowAndoifcnameAndoifonb(String oifow, String oifcname, String oifonb, int sysstatus);
	
//	// 取得下當前筆ID
//	@Query(value = "SELECT CURRVAL('production_body_seq')", nativeQuery = true)
//	Long getOqcInspectionItemsSeq();
//
//	// 取得下當前筆ID
//	@Query(value = "SELECT NEXTVAL('production_body_seq')", nativeQuery = true)
//	Long getOqcInspectionItemsSeqNext();
//
//	// 取得G_ID && 累加
//	@Query(value = "SELECT NEXTVAL('production_body_g_seq')", nativeQuery = true)
//	Long getOqcInspectionItemsGSeq();
//
//	// 查詢SN重複
//	List<OqcInspectionItems> findAllByPbsn(String pbsn);//
//
//	// 查詢燒錄 SN重複
//	List<OqcInspectionItems> findAllByPbbsn(String pbbsn);
//
//	// 查詢燒錄_Like+是舊的SN
//	List<OqcInspectionItems> findAllByPbbsnLike(String old_sn);
//
//	// 查詢燒錄_Like+不是舊的SN
//	List<OqcInspectionItems> findAllByPbbsnAndPbbsnNotLike(String pbbsn, String not_old_sn);
//
//	// 查詢該群組_Like+不是舊的SN
//	@Query(value = "SELECT b FROM OqcInspectionItems b WHERE "//
//			+ "( b.pbgid = :pbgid ) and "//
//			+ "(b.pbbsn LIKE %:pbbsn% or b.pboldsn LIKE %:pboldsn% ) "// coalesce 回傳非NULL值
//			+ " order by b.pbgid desc,b.pbid asc, b.sysmdate desc ")
//	List<OqcInspectionItems> findAllByPbgidAndPbbsnLikeOrPboldsnLike(Long pbgid, String pbbsn, String pboldsn);
//	


//	// 查詢SN重複+群組
//	List<OqcInspectionItems> findAllByPbsnAndPbgid(String pbsn, Long pbgid);
//
//	// 查詢燒錄 SN重複+群組
//	List<OqcInspectionItems> findAllByPbbsnAndPbgid(String pbbsn, Long pbgid);
//
//	// 查詢SN群組
//	List<OqcInspectionItems> findAllByPbgidOrderByPbsnAsc(Long pbgid);
//
//	// 查詢SN群組+非no_sn
//	List<OqcInspectionItems> findAllByPbgidAndPbbsnNotOrderByPbsnAsc(Long pbgid, String pbbsn);
//
//	// 查詢SN群組 已過站
//	@Query(value = "SELECT b.pbbsn FROM OqcInspectionItems b WHERE "//
//			+ "( b.pbgid = :pbgid ) and "//
//			+ "(b.pbschedule LIKE %:pbschedule% ) "// coalesce 回傳非NULL值
//			+ " order by b.pbsn asc")
//	List<String> findPbbsnPbscheduleList(Long pbgid, String pbschedule);
//
//	// 查詢SN群組 故障數
//	@Query(value = "SELECT b.pbbsn FROM OqcInspectionItems b WHERE "//
//			+ "( b.pbgid = :pbgid ) and "//
//			+ "(b.pbfvalue LIKE %:pbfvalue% ) and "//
//			+ "(b.pbfvalue !='' and b.pbfvalue is not null ) "// coalesce 回傳非NULL值
//			+ " order by b.pbsn asc")
//	List<String> findPbbsnPbscheduleFixList(Long pbgid, String pbfvalue);
//
//	// 查詢SN群組(此工單 ->產品完成[總數])
//	@Query(value = "SELECT b.pbcheck FROM OqcInspectionItems b WHERE "//
//			+ "( b.pbgid = :pbgid ) and (b.pbcheck = true) order by b.pbsn asc")
//	List<Boolean> findPbcheckList(Long pbgid);
//
//	// 查詢SN群組(此工單 ->產品 SN重複)
//	@Query(value = "SELECT b.pbbsn FROM OqcInspectionItems b WHERE "//
//			+ "( b.pbgid = :pbgid ) order by b.pbsn asc")
//	List<String> findPbbsnList(Long pbgid);
//
//	// 查詢一部分_Body
//	@Query(value = "SELECT b FROM OqcInspectionItems b WHERE "//
//			+ "( b.sysstatus = :sysstatus ) and "//
//			+ "(coalesce(:pbid, null) is null or b.pbid IN :pbid ) and "// coalesce 回傳非NULL值
//			+ "(b.pbid!=0 or b.pbid!=1) and (b.sysheader!=true) "//
//			+ " order by b.pbgid desc,b.pbid asc, b.sysmdate desc ")
//	List<OqcInspectionItems> findAllByOqcInspectionItems(@Param("sysstatus") Integer sys_status, @Param("pbid") List<Long> pb_id, Pageable pageable);
//
//	// 查詢一部分_Body By Check
//	@Query(value = "SELECT b FROM OqcInspectionItems b WHERE "//
//			+ "( b.sysstatus = :sysstatus ) and "//
//			+ "(coalesce(:pbid, null) is null or b.pbid IN :pbid ) and "// coalesce 回傳非NULL值
//			+ "(b.pbid!=0 or b.pbid!=1) and (b.sysheader!=true) and  (b.pbcheck=:pbcheck)"//
//			+ " order by b.pbgid desc,b.pbid asc ,b.sysheader desc")
//	List<OqcInspectionItems> findAllByOqcInspectionItems(@Param("sysstatus") Integer sys_status, @Param("pbid") List<Long> pb_id, @Param("pbcheck") Boolean pb_check,
//			Pageable pageable);
//
	// 移除單一SN
//	Long deleteByOiiid(Long id);
//
//	// 移除 群組
//	Long deleteByPbgid(Long id);

}