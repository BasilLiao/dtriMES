package dtri.com.tw.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.LabelListDao;
import dtri.com.tw.db.pgsql.dao.WorkstationClassDao;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;

@Service
public class SystemApiService {
	@Autowired
	private WorkstationProgramDao programDao;

	@Autowired
	private WorkstationClassDao classDao;

	@Autowired
	private LabelListDao labelsDao;

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
}
