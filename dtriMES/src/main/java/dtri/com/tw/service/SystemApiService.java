package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.db.entity.ProductionHeader;
import dtri.com.tw.db.entity.Workstation;
import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.LabelListDao;
import dtri.com.tw.db.pgsql.dao.ProductionHeaderDao;
import dtri.com.tw.db.pgsql.dao.WorkstationClassDao;
import dtri.com.tw.db.pgsql.dao.WorkstationDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;

@Service
public class SystemApiService {
	@Autowired
	private WorkstationProgramDao programDao;

	@Autowired
	private WorkstationClassDao classDao;

	@Autowired
	private LabelListDao labelsDao;

	@Autowired
	private ProductionHeaderDao phDao;
	@Autowired
	private WorkstationDao workstationDao;

	// 取得當前 工作站 資料清單
	public JSONArray getWorkstationProgramList() {

		ArrayList<WorkstationProgram> list = programDao.findAllBySysheader(true);
		JSONArray array = new JSONArray();
		list.forEach(s -> {
			array.put(new JSONObject().put("value", s.getWpname()).put("key", s.getWpgid()));
		});

		return array;
	}

	// 取得當前 產線線別 資料清單
	public JSONArray getWorkstationLineList() {

		ArrayList<String> list = classDao.getWcLineDistinct();
		JSONArray array = new JSONArray();
		list.forEach(s -> {
			array.put(s);
		});

		return array;
	}

	// 取得當前 標籤組 資料清單
	public JSONArray getLabelGroup() {

		ArrayList<String> list = labelsDao.getLabelGroupDistinct();
		JSONArray array = new JSONArray();
		list.forEach(s -> {
			array.put(s);
		});
		return array;
	}

	// 取得當前 有效工單 之進度
	public JSONObject getWorkOrderList() {
		List<Integer> sysstatus = new ArrayList<Integer>();
		// 狀態非(暫停/終止/完成)的資料
		sysstatus.add(2);
		sysstatus.add(8);
		sysstatus.add(9);
		
		List<ProductionHeader> ph_all = phDao.findAllBySysstatusNotIn(sysstatus);
		// 取得各站名稱
		ArrayList<Workstation> wheader = workstationDao.findAllBySysheaderOrderByWcnameAsc(true, null);
		HashMap<String, String> wMap = new HashMap<String, String>();
		wheader.forEach(w -> {
			if (!w.getWcname().equals("")) {
				wMap.put(w.getWcname(), w.getWpbname());
			}
		});

		JSONObject jsonAll = new JSONObject();
		ph_all.forEach(s -> {
			String phpbschedule = s.getPhpbschedule();
			if(phpbschedule!=null) {
				// 取代工作站名稱
				for (HashMap.Entry<String, String> entry : wMap.entrySet()) {
					String k = entry.getKey();
					String v = entry.getValue();
					phpbschedule = phpbschedule.replaceAll(k, v);
				}
				//串起資料
				jsonAll.put(s.getProductionRecords().getPrid(),
						s.getPhwcline() + "_" + s.getPhschedule() + "_" + phpbschedule);// 製造線_總進度_各站進度
			}
		});				

		return jsonAll;
	}
}
