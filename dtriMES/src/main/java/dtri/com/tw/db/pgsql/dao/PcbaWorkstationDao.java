package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.db.entity.PcbaWorkstation;

public interface PcbaWorkstationDao extends JpaRepository<PcbaWorkstation, Long> {

	// 查詢全部
	ArrayList<PcbaWorkstation> findAll();

	// 查詢全部 指定工作站代表
	ArrayList<PcbaWorkstation> findAllByPwgidAndSysheaderOrderBySyssortAsc(Long pwgid, Boolean sysheader);

	ArrayList<PcbaWorkstation> findAllByPwgidOrderBySyssortAsc(Long pwgid);

	// 查詢工作站代表
	ArrayList<PcbaWorkstation> findAllBySysheaderOrderByPwcnameAsc(Boolean sysheader, Pageable pageable);

	// 查詢工作站代表
	ArrayList<PcbaWorkstation> findAllBySysheaderAndPwidNot(Boolean sysheader, Long pwid, Pageable pageable);

	// 查詢工作站代表
	ArrayList<PcbaWorkstation> findAllByPwid(Long pwid);

	// 查詢工作站碼
	ArrayList<PcbaWorkstation> findAllByPwcname(String pwcname, Pageable pageable);

	// 查詢工作站 欄位
	ArrayList<PcbaWorkstation> findAllByPwpbcell(String pwpbcell, Pageable pageable);

	// 查詢工作站碼+排除自己 欄位
	ArrayList<PcbaWorkstation> findAllByPwcnameAndPwcnameNot(String pwcname, String pwcname2, Pageable pageable);

	// 查詢工作站+排除自己 欄位
	ArrayList<PcbaWorkstation> findAllByPwpbcellAndPwpbcellNot(String pwpbcell, String pwpbcell2, Pageable pageable);

	// 查詢一部分
	@Query("SELECT c FROM PcbaWorkstation c "//
			+ "WHERE (:pwsgname is null or c.pwsgname LIKE %:pwsgname% ) and "//
			+ "(:pwpbname is null or c.pwpbname LIKE %:pwpbname% ) and "//
			+ "(coalesce(:pwgid, null) is null or c.pwgid IN :pwgid ) and "// coalesce 回傳非NULL值
			+ "( c.sysstatus = :sysstatus ) and "//
			+ "( c.sysheader = :sysheader ) and " //
			+ "( c.pwid != 0 )  "//
			+ "order by c.pwgid asc,c.sysheader desc,c.syssort asc")
	ArrayList<PcbaWorkstation> findAllByPcbaWorkstation(@Param("pwsgname") String pw_sg_name, //
			@Param("pwpbname") String pw_pb_name, @Param("sysstatus") Integer sysstatus, @Param("sysheader") Boolean sysheader, //
			@Param("pwgid") List<Long> pw_g_id, Pageable pageable);

	// 查詢一部分 關聯 工作站 與 工作項目
	@Query("SELECT wn FROM PcbaWorkstation wn join wn.pcbaWorkstationItem wi " + // join到PcbaWorkstation Enity裡面的 private PcbaWorkstationItem pcbaWorkstationItem;
			"where " + //
			"wn.sysheader = false and " + //
			"wn.pwcname = :pwcname and " + //
			"wi.pwipbcell = :pwipbcell ")
	ArrayList<PcbaWorkstation> findAllByPcbaWorkstation_item(@Param("pwcname") String pwcname, @Param("pwipbcell") String pwipbcell);

	// 取得最新G_ID   johnnny 以手動在postresql建立pcba_workstation_g_seq
	@Query(value = "SELECT NEXTVAL('pcba_workstation_g_seq')", nativeQuery = true)
	Long getPcba_workstation_g_seq();

	// 取得ID
	@Query(value = "SELECT CURRVAL('pcba_workstation_seq')", nativeQuery = true)
	Long getPcba_workstation_seq();

	// delete
	Long deleteByPwidAndSysheader(Long id, Boolean sysheader);

	// delete 群組移除
	Long deleteByPwgid(Long pwgid);
}