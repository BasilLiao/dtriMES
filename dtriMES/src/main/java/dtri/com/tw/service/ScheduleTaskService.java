package dtri.com.tw.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
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

	// 每日12/18:00分執行一次
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

		ProcessBuilder pb = new ProcessBuilder("" + db_pg_dump, "--dbname=" + db_name, "--port=" + db_port, "--no-password", "--verbose", "--format=c",
				"--blobs", "--encoding=UTF8", "--file=" + apache_path + db_folder_name + db_file_name + "_" + backupDay + ".sql");
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

	@Async
	@Scheduled(cron = "0 00 23 * * ? ")
	public void removeFTPFile() {
		System.out.println("每隔1天 晚上23點00 執行一次：" + new Date());
		// Step1. 備份位置 -> 需移除LOG資料
		ArrayList<SystemConfig> ftp_config = sysDao.findAllByConfig(null, "FTP_PLT", 0, PageRequest.of(0, 99));
		JSONObject c_json = new JSONObject();
		ftp_config.forEach(c -> {
			c_json.put(c.getScname(), c.getScvalue());
		});
		Integer year = Year.now().getValue();
		String ftpHost = c_json.getString("IP"), //
				ftpUserName = c_json.getString("ACCOUNT"), //
				ftpPassword = c_json.getString("PASSWORD"), //
				remotePath = c_json.getString("PATH") + year; //
				//remotePathBackup = c_json.getString("PATH_BACKUP"), //
				//localPath = "";//
		int ftpPort = c_json.getInt("PORT");

		FTPClient ftpClient = new FTPClient();
		// 登入 如果採用預設埠，可以使用ftp.connect(url)的方式直接連線FTP伺服器
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(ftpHost, ftpPort);// 連線FTP伺服器
			ftpClient.login(ftpUserName, ftpPassword);// 登陸FTP伺服器
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				System.out.println("未連線到FTP，使用者名稱或密碼錯誤。");
				ftpClient.disconnect();
			} else {
				System.out.println("FTP連線成功。");
				// 轉移到FTP伺服器目錄至指定的目錄下
				ftpClient.changeWorkingDirectory(new String(remotePath.getBytes("UTF-8"), "iso-8859-1"));
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				ftpClient.setControlEncoding("UTF-8");
				// 獲取ftp登入應答程式碼
				int reply = ftpClient.getReplyCode();
				// 驗證是否登陸成功
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					System.err.println("FTP server refused connection.");
				}
				// 獲取檔案列表(查詢)
				FTPFile[] fs = ftpClient.listFiles();
				for (FTPFile ff : fs) {
					// 排除TEST類型資料(其餘移除)
					if (ff.getName().indexOf("TEST") == -1) {
						String file_remove_name = ff.getName();
						String re_path = remotePath + "/" + file_remove_name;
						ftpClient.deleteFile(re_path);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("FTP的IP地址可能錯誤，請正確配置。");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("FTP的埠錯誤,請正確配置。");
		}
		// 設定檔案傳輸型別為二進位制+UTF-8 傳輸

	}
}