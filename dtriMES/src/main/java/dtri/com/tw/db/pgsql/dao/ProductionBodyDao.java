package dtri.com.tw.db.pgsql.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.ProductionBody;

public interface ProductionBodyDao extends JpaRepository<ProductionBody, Long> {
	// 查詢 標籤
	List<ProductionBody> findAllByPbid(Long id);

	// 取得下當前筆ID
	@Query(value = "SELECT CURRVAL('production_body_seq')", nativeQuery = true)
	Long getProductionBodySeq();

	// 取得下當前筆ID
	@Query(value = "SELECT NEXTVAL('production_body_seq')", nativeQuery = true)
	Long getProductionBodySeqNext();

	// 取得G_ID && 累加
	@Query(value = "SELECT NEXTVAL('production_body_g_seq')", nativeQuery = true)
	Long getProductionBodyGSeq();

	// 查詢SN重複
	List<ProductionBody> findAllByPbsn(String pbsn);

	// 查詢燒錄 SN重複
	List<ProductionBody> findAllByPbbsn(String pbbsn);
	
	
	// 查詢燒錄_Like+是舊的SN
	List<ProductionBody> findAllByPbbsnLike(String old_sn);

	// 查詢燒錄_Like+不是舊的SN
	List<ProductionBody> findAllByPbbsnAndPbbsnNotLike(String pbbsn, String not_old_sn);

	// 查詢該群組_Like+不是舊的SN
	@Query(value = "SELECT b FROM ProductionBody b WHERE "//
			+ "( b.pbgid = :pbgid ) and "//
			+ "(b.pbbsn LIKE %:pbbsn% or b.pboldsn LIKE %:pboldsn% ) "// coalesce 回傳非NULL值
			+ " order by b.pbgid desc,b.pbid asc, b.sysmdate desc ")
	List<ProductionBody> findAllByPbgidAndPbbsnLikeOrPboldsnLike(Long pbgid, String pbbsn, String pboldsn);

	// 查詢SN重複+群組
	List<ProductionBody> findAllByPbsnAndPbgid(String pbsn, Long pbgid);

	// 查詢燒錄 SN重複+群組
	List<ProductionBody> findAllByPbbsnAndPbgid(String pbbsn, Long pbgid);

	// 查詢SN群組
	List<ProductionBody> findAllByPbgidOrderByPbsnAsc(Long pbgid);

	// 查詢SN群組+非no_sn
	List<ProductionBody> findAllByPbgidAndPbbsnNotOrderByPbsnAsc(Long pbgid, String pbbsn);

	// 查詢SN群組 已過站
	@Query(value = "SELECT b.pbbsn FROM ProductionBody b WHERE "//
			+ "( b.pbgid = :pbgid ) and "//
			+ "(b.pbschedule LIKE %:pbschedule% ) "// coalesce 回傳非NULL值
			+ " order by b.pbsn asc")
	List<String> findPbbsnPbscheduleList(Long pbgid, String pbschedule);

	// 查詢SN群組(此工單 ->產品完成[總數])
	@Query(value = "SELECT b.pbcheck FROM ProductionBody b WHERE "//
			+ "( b.pbgid = :pbgid ) and (b.pbcheck = true) order by b.pbsn asc")
	List<Boolean> findPbcheckList(Long pbgid);

	// 查詢SN群組(此工單 ->產品 SN重複)
	@Query(value = "SELECT b.pbbsn FROM ProductionBody b WHERE "//
			+ "( b.pbgid = :pbgid ) order by b.pbsn asc")
	List<String> findPbbsnList(Long pbgid);

	// 查詢一部分_Body
	@Query(value = "SELECT b FROM ProductionBody b WHERE "//
			+ "( b.sysstatus = :sysstatus ) and "//
			+ "(coalesce(:pbid, null) is null or b.pbid IN :pbid ) and "// coalesce 回傳非NULL值
			+ "(b.pbid!=0 or b.pbid!=1) and (b.sysheader!=true) "//
			+ " order by b.pbgid desc,b.pbid asc, b.sysmdate desc ")
	List<ProductionBody> findAllByProductionBody(@Param("sysstatus") Integer sys_status, @Param("pbid") List<Long> pb_id, Pageable pageable);

	// 查詢一部分_Body By Check
	@Query(value = "SELECT b FROM ProductionBody b WHERE "//
			+ "( b.sysstatus = :sysstatus ) and "//
			+ "(coalesce(:pbid, null) is null or b.pbid IN :pbid ) and "// coalesce 回傳非NULL值
			+ "(b.pbid!=0 or b.pbid!=1) and (b.sysheader!=true) and  (b.pbcheck=:pbcheck)"//
			+ " order by b.pbgid desc,b.pbid asc ,b.sysheader desc")
	List<ProductionBody> findAllByProductionBody(@Param("sysstatus") Integer sys_status, @Param("pbid") List<Long> pb_id, @Param("pbcheck") Boolean pb_check,
			Pageable pageable);

	// 移除單一SN
	Long deleteByPbid(Long id);

	// 移除 群組
	Long deleteByPbgid(Long id);

}