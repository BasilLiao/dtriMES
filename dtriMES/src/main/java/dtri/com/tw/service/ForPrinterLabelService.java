package dtri.com.tw.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class ForPrinterLabelService {

	/**
	 * label_3X1 = 3*1 套版
	 **/
	final String label_3X1 = "^FO20,2"//
			+ "^A0,20,19"//
			+ "^FB240,1,1,C,"//
			+ "^FD${part_no}\\&"//
			+ "^FS"//
			+ ""//
			+ "^FO${left},22"//
			+ "^BY1,2,30"//
			+ "^B3N,N,25,N,N"//
			+ "^FD${sn_1}"//
			+ "^FS"//
			+ ""//
			+ "^FO20,55"//
			+ "^A0,20,20"//
			+ "^FB240,1,5,C,"//
			+ "^FD${sn_2}\\&"//
			+ "^FS";//
	/**
	 * label_3X2 = 3*2 套版
	 **/
	final String label_3X2 = "" + //
			"" + //
			"^FO20,5" + //
			"^A0,20,19" + //
			"^FB240,1,1,C," + //
			"^FD${part_no}\\&" + //
			"^FS" + "" + //
			"^FO${left_1},30" + //
			"^BY1,2,30" + //
			"^B3N,N,25,N,N" + //
			"^FD${sn_1_1}" + //
			"^FS" + //
			"" + //
			"^FO20,65" + //
			"^A0,20,20" + //
			"^FB240,1,5,C," + //
			"^FD${sn_1_2}\\&" + //
			"^FS" + //
			"" + //
			"^FO20,80" + //
			"^A0,20,19" + //
			"^FB240,1,1,C," + //
			"^FD\\&" + //
			"^FS" + //
			"" + //
			"^FO${left_2},100" + //
			"^BY1,2,30" + //
			"^B3N,N,25,N,N" + //
			"^FD${sn_2_1}" + //
			"^FS" + //
			"" + //
			"^FO20,135" + //
			"^A0,20,20" + //
			"^FB240,1,5,C," + //
			"^FD${sn_2_2}\\&" + //
			"^FS" + //
			"";//

	/**
	 * 取得相對應->標籤機代碼
	 * 
	 * @param printName 服務名稱
	 **/
	public PrintService getPrinterService(String printName) {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		for (final PrintService service : services) {
			if (service.getName().equals(printName)) {
				System.out.println(service.getName());
				return service;
			}
		}
		return null;
	}

	/**
	 * 完成傳送指令
	 **/
	public boolean sendPrinter(String zpl, PrintService service) {
		// 用網路串流可能用到
		// byte[] buf = new byte[1024];
		// Socket socket = new Socket("127.0.0.1", 9100);
		// OutputStream out = socket.getOutputStream();

		// 紙張大小
		// DocFlavor flavor = INPUT_STREAM.AUTOSENSE;
		Thread thread = new Thread() {
			public void run() {
				try {
					byte[] zpl_by = zpl.getBytes();
					DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
					Doc doc = new SimpleDoc(zpl_by, flavor, null);
					DocPrintJob printJob = service.createPrintJob();
					printJob.print(doc, null);
					// (日後測試)打印狀態
//					if (isPrinterQueueInError(service.getName())) {
//						System.out.println("檢測到打印服務處於錯誤: " + service.getName());
//						// 清除打印列隊
//						clearPrintQueue(service.getName());
//						// 停止執行
//						return;
//					}
//					printJob.print(doc, null);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		return true;
	}

	// 檢查打印列隊中是否存在錯誤的任務
	private static boolean isPrinterQueueInError(String printerName) throws Exception {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			// 使用 Windows 命令檢查打印任務狀態
			Process process = new ProcessBuilder("cmd", "/c", "wmic printjob get Document,JobStatus,Status").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("Error") || line.contains("Paused")) {
					System.out.println("列隊狀態: " + line.trim());
					return true;
				}
			}
		} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
			// 使用 Linux/macOS 命令檢查打印任務狀態
			Process process = new ProcessBuilder("lpstat", "-o", printerName).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("stopped") || line.contains("error")) {
					System.out.println("列隊狀態: " + line.trim());
					return true;
				}
			}
		} else {
			System.out.println("不支持的操作系統: " + osName);
		}
		return false;
	}

	// 清除打印列隊的方法
	private void clearPrintQueue(String printerName) {
		String osName = System.getProperty("os.name").toLowerCase();
		try {
			if (osName.contains("win")) {
				// Windows 系統清除列隊
				System.out.println("清除 Windows 打印機列隊: " + printerName);
				runCommand("net stop spooler");
				runCommand("cmd /c del /Q /F \"%systemroot%\\System32\\spool\\PRINTERS\\*\"");
				runCommand("net start spooler");
			} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
				// Linux/macOS 系統清除列隊
				System.out.println("清除 Unix 打印機列隊: " + printerName);
				runCommand("cancel -a " + printerName);
			} else {
				System.out.println("不支持的操作系統: " + osName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 執行命令的方法
	private void runCommand(String command) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(command.split(" "));
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		}
		process.waitFor();
	}

	/**
	 * 3*1標籤設置 ^FX 說明:.<br>
	 * ^FX BY 宽度、宽度比率、高度<br>
	 * ^FX B3 code39 編碼<br>
	 * ^FX FB 文字設置<br>
	 * ^FX FO 座標位置<br>
	 * 
	 **/
	public String label_3X1(JSONArray sn_list, int nb) {
		String zpl = "", zpl_start = "^XA", zpl_end = "^XZ";
		for (int z = 1; z <= nb; z++) {
			String part_no = sn_list.getJSONObject(0).getString("part_no");
			for (int i = 0; i < sn_list.length(); i++) {
				// 15碼 靠左10(X) 每少一碼 多5
				zpl += zpl_start;
				String label_3X1_copy = label_3X1;
				JSONObject one = sn_list.getJSONObject(i);
				int left_size = 15 - (one.getString("sn_value").length());
				left_size = 30 + (left_size * 5);
				label_3X1_copy = label_3X1_copy.replace("${left}", left_size + "");
				label_3X1_copy = label_3X1_copy.replace("${sn_1}", one.getString("sn_value"));
				label_3X1_copy = label_3X1_copy.replace("${sn_2}",
						one.getString("sn_name") + " " + one.getString("sn_value"));
				label_3X1_copy = label_3X1_copy.replace("${part_no}", part_no);
				part_no = "";
				zpl += label_3X1_copy;
				zpl += zpl_end;
			}
		}

		// System.out.println(" ".getBytes().length+"|"+" ".getBytes().length+"|"+"
		// ".getBytes().length);
		zpl = zpl.replaceAll(" ", " ");// 取代特殊空白UTF8(輸入)->ASCII(系統) ,可能看不出來差異 就是不同
		return zpl;
	}

	/**
	 * 3*2標籤設置 ^FX 說明:.<br>
	 * ^FX BY 宽度、宽度比率、高度<br>
	 * ^FX B3 code39 編碼<br>
	 * ^FX FB 文字設置<br>
	 * ^FX FO 座標位置<br>
	 * 
	 **/
	public String label_3X2(JSONObject sn_list, int nb) {
		String zpl = "", zpl_start = "^XA", zpl_end = "^XZ";
		String part_no = sn_list.getString("part_no");
		for (int i = 0; i < nb; i++) {
			// 15碼 靠左10(X) 每少一碼 多5
			zpl += zpl_start;
			String label_3X2_copy = label_3X2;
			// 第一區塊
			int left_size_1 = 15 - sn_list.getString("sn_value_1").length();
			left_size_1 = 30 + (left_size_1 * 5);
			label_3X2_copy = label_3X2_copy.replace("${left_1}", left_size_1 + "");
			label_3X2_copy = label_3X2_copy.replace("${sn_1_1}", sn_list.getString("sn_value_1"));
			label_3X2_copy = label_3X2_copy.replace("${sn_1_2}",
					sn_list.getString("sn_name_1") + " " + sn_list.getString("sn_value_1"));
			label_3X2_copy = label_3X2_copy.replace("${part_no}", part_no);
			part_no = "";
			// 第二區塊
			int left_size_2 = 15 - sn_list.getString("sn_value_2").length();
			left_size_2 = 30 + (left_size_2 * 5);
			label_3X2_copy = label_3X2_copy.replace("${left_2}", left_size_2 + "");
			label_3X2_copy = label_3X2_copy.replace("${sn_2_1}", sn_list.getString("sn_value_2"));
			label_3X2_copy = label_3X2_copy.replace("${sn_2_2}",
					sn_list.getString("sn_name_2") + " " + sn_list.getString("sn_value_2"));
			label_3X2_copy = label_3X2_copy.replace("${part_no}", part_no);

			zpl += label_3X2_copy;
			zpl += zpl_end;
		}
		zpl = zpl.replaceAll(" ", " ");// 取代特殊空白UTF8(輸入)->ASCII(系統) ,可能看不出來差異 就是不同
		return zpl;
	}
}