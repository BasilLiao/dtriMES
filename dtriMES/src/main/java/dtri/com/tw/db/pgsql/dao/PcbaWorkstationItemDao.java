package dtri.com.tw.db.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import dtri.com.tw.db.entity.PcbaWorkstationItem;

public interface PcbaWorkstationItemDao extends JpaRepository<PcbaWorkstationItem, Long> {

	// 查詢全部
	ArrayList<PcbaWorkstationItem> findAll();

	// 查詢一部分
	@Query("SELECT c FROM PcbaWorkstationItem c " //
			+ "WHERE (:pwipbcell is null or c.pwipbcell LIKE %:pwipbcell% ) and "//
			+ "(:pwipbvalue is null or c.pwipbvalue LIKE %:pwipbvalue% ) and "//
			+ "( c.pwiid != 0 ) "//
			+ "order by c.pwiid asc,c.sysmdate desc")
	ArrayList<PcbaWorkstationItem> findAllByPcbaWorkstationItem(@Param("pwipbcell") String pwipbcell, @Param("pwipbvalue") String pwipbvalue);

	ArrayList<PcbaWorkstationItem> findAllBySysheader(Boolean sysheader);

	// delete
	Long deleteByPwiidAndSysheader(Long id, Boolean sysheader);
}