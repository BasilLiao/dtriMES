package dtri.com.tw.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.db.entity.WorkstationProgram;
import dtri.com.tw.db.pgsql.dao.WorkstationProgramDao;

@Service
public class SystemApiService {
	@Autowired
	private WorkstationProgramDao programDao;

	// 取得當前 工作站 資料清單
	public JSONArray getWorkstationProgramList() {

		ArrayList<WorkstationProgram> list = programDao.findAllBySysheader(true);
		JSONArray array = new JSONArray();
		list.forEach(s -> {
			array.put(new JSONObject().put("value", s.getWpname()).put("key", s.getWpgid()));
		});

		return array;
	}
}
