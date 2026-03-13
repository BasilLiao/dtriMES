package dtri.com.tw.db.pgsql.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.OqcInspectionItems;

public interface OqcInspectionItemsDao extends JpaRepository<OqcInspectionItems, Long> {
	// 查詢 標籤
	List<OqcInspectionItems> findAllByOiiid(Long id);
	//oii_title_val:標題值 給OQCFormServic用
	List<OqcInspectionItems> findAllByOiititleval(String oiititleval ,Sort sort);
	
	List<OqcInspectionItems> findAllByOiititleval(String oiititleval);
	
	//查每個標題值唯一
	@Query("SELECT t1 FROM OqcInspectionItems t1 WHERE t1.id = (" +
		       "SELECT MIN(t2.id) FROM OqcInspectionItems t2 WHERE t2.oiititleval = t1.oiititleval)")
	List<OqcInspectionItems> findMinIdPerTitle();
	

	// 查詢 oiitestnb oiitestmodel oiitestclient
	@Query("SELECT b FROM OqcInspectionItems b WHERE "
			   + "(:oiicheckname is null or b.oiicheckname =:oiicheckname) and " //檢查項目名稱
			   + "(:oiicheckval  is null or b.oiicheckval LIKE %:oiicheckval%) and " //檢查內容值 (如果是下拉式/勾選 請用,區隔 Ex:[key_val,key_val]
			   + "(:oiichecktype  is null or b.oiichecktype LIKE %:oiichecktype%) and " //檢查輸入類型 1.一般入 2.下拉式選單 3.勾選式
			   + "(:oiicheckoptions is null or b.oiicheckoptions LIKE %:oiicheckoptions%) and "    //"可自訂值"    			
			   + "(:oiititleval  is null or b.oiititleval LIKE %:oiititleval%) and "  //標題值			   
			   + "(:sysnote is null or b.sysnote LIKE %:sysnote%)  "
			   + " order by b.oiititleval asc ,b.syssort asc ,b.oiicheckname asc")
	List<OqcInspectionItems> findAllByOqcItems(String oiicheckname	,String oiicheckval,String oiichecktype,String oiicheckoptions,String oiititleval,String sysnote,Pageable pageable);

	//計算有無重複short
	@Query("SELECT COUNT(b) FROM OqcInspectionItems b WHERE "
			 + "(:oiititleval  is null or b.oiititleval =:oiititleval) and "  //標題值
			 + "(:syssort  is null or b.syssort =:syssort) " ) //順序	
	long countSyssort(String oiititleval, int syssort );
	

	// 移除單一SN
	Long deleteByOiiid(Long id);

//	// 移除 群組
//	Long deleteByPbgid(Long id);

}