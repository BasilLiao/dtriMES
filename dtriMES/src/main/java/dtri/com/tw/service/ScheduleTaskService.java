package dtri.com.tw.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dtri.com.tw.bean.FtpUtilBean;
import dtri.com.tw.db.entity.SystemConfig;
import dtri.com.tw.db.pgsql.dao.SystemConfigDao;

@Component
public class ScheduleTaskService {
	@Autowired
	private SystemConfigDao sysDao;
	@Value("${catalina.home}")
	private String apache_path;

	// log 訊息
	private static Logger logger = LogManager.getLogger();

	// 每日18:00分執行一次
	// 系統 備份(pgsql+ftp)
	@Async
	@Scheduled(cron = "0 30 12,18 * * ? ")
	public void backupDataBase() {
		System.out.println("每隔1天 早上12點30分/晚上18點30 執行一次：" + new Date());
		logger.info("Database backup night 18.30  執行一次：" + new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String backupDay = sdf.format(new Date());
		System.out.println("備份資料庫:" + new Date());
		logger.info("備份資料庫：" + new Date());

		// Step1. 備份位置
		ArrayList<SystemConfig> ftp_config = sysDao.findAllByConfig(null, "FTP_DATA_BKUP", 0, PageRequest.of(0, 99));
		JSONObject c_json = new JSONObject();
		ftp_config.forEach(c -> {
			c_json.put(c.getScname(), c.getScvalue());
		});
		String ftp_host = c_json.getString("IP"), //
				ftp_user_name = c_json.getString("ACCOUNT"), //
				ftp_password = c_json.getString("PASSWORD"), //
				ftp_remote_path = c_json.getString("PATH");//
		int ftp_port = c_json.getInt("FTP_PORT");

		// Step2. 資料庫設定
		ArrayList<SystemConfig> data_config = sysDao.findAllByConfig(null, "DATA_BKUP", 0, PageRequest.of(0, 99));
		JSONObject d_json = new JSONObject();
		data_config.forEach(d -> {
			d_json.put(d.getScname(), d.getScvalue());
		});
		String db_folder_name = d_json.getString("FOLDER_NAME"), //
				db_file_name = d_json.getString("FILE_NAME"), //
				db_pg_dump = d_json.getString("PG_DUMP"), //
				db_name = d_json.getString("DB_NAME");//
		int db_port = d_json.getInt("DB_PORT");

		// Runtime rt = Runtime.getRuntime();
		// rt = Runtime.getRuntime();
		// Step3. 備份指令-postgres
		Process p;
		/**
		 * Apache C:\Users\Basil\AppData\Local\Temp\tomcat
		 * 
		 * C:\Program Files\PostgreSQL\10\bin\pg_dump.exe --file
		 * "C:\\Users\\Basil\\Desktop\\DTRIME~1.SQL" --host "localhost" --port "5432"
		 * --username "postgres" --no-password --verbose --format=c --blobs --encoding
		 * "UTF8" "dtrimes"
		 */

		ProcessBuilder pb = new ProcessBuilder("" + db_pg_dump, "--dbname=" + db_name, "--port=" + db_port, "--no-password", "--verbose",
				"--format=c", "--blobs", "--encoding=UTF8", "--file=" + apache_path + db_folder_name + db_file_name + "_" + backupDay + ".sql");
		try {
			// Step3-1.查資料夾
			File directory = new File(apache_path + db_folder_name);
			if (!directory.exists()) {
				directory.mkdir();
			}

			p = pb.start();
			final BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = r.readLine();
			while (line != null) {
				System.err.println(line);
				logger.info(line);
				line = r.readLine();
			}
			r.close();
			p.waitFor();
			System.out.println(p.exitValue());
			logger.info(p.exitValue());
		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
		// Step4. 上傳-FTP
		try {
			File initialFile = new File(apache_path + db_folder_name + db_file_name + "_" + backupDay + ".sql");
			InputStream input = new FileInputStream(initialFile);
			FtpUtilBean f_Bean = new FtpUtilBean(ftp_host, ftp_user_name, ftp_password, ftp_port);
			f_Bean.setInput(input);
			f_Bean.setFtpPath(ftp_remote_path);
			f_Bean.setFileName(db_file_name + "_" + backupDay + ".sql");
			FtpService fts = new FtpService();
			fts.uploadFile(f_Bean);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}