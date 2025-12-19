package dtri.com.tw.service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;

import org.springframework.stereotype.Component;

@Component
public class LabelPrinterManagerService {

	// 打印序列
	private LabelPrinterManagerService printerManager;

	public void NewLabelListService() {
		printerManager = new LabelPrinterManagerService();
	}

	public LabelPrinterManagerService getPrinterManager() {
		return printerManager;
	}

	// 每台印表機一個單線程隊列（同台必定按順序）
	private final ConcurrentHashMap<String, ExecutorService> perPrinterExecutor = new ConcurrentHashMap<>();
	private final AtomicInteger threadSeq = new AtomicInteger(1);

	private ExecutorService executorOf(PrintService service) {
		// 用印表機名稱當 key（同一台一定同名）
		String key = (service != null && service.getName() != null) ? service.getName() : "UNKNOWN";

		return perPrinterExecutor.computeIfAbsent(key, k -> Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "printer-queue-" + k + "-" + threadSeq.getAndIncrement());
			t.setDaemon(true);
			return t;
		}));
	}

	/**
	 * 發送打印指令到指定打印服務（同一台印表機會排隊、按送入順序執行）
	 */
	public boolean sendPrinter(String zpl, PrintService service) {
		if (service == null)
			return false;

		// ✅ 重要：避免平台預設編碼不一致
		final byte[] zplBytes = (zpl == null) ? new byte[0] : zpl.getBytes(StandardCharsets.UTF_8);

		executorOf(service).submit(() -> {
			try {
				DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
				Doc doc = new SimpleDoc(zplBytes, flavor, null);

				DocPrintJob printJob = service.createPrintJob();

				// ✅ 建議：加上 JobName，方便你在列印佇列追蹤順序
				PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
				attrs.add(new JobName("ZPL-" + System.currentTimeMillis(), null));

				printJob.print(doc, attrs);
				// ✅ 強制間隔 0.5 秒（同一台印表機）
				Thread.sleep(300);

				System.out.println("已送出列印: " + service.getName() + ":");
			} catch (PrintException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// ✅ 重要：被中斷時要恢復中斷旗標，避免執行緒池狀態異常
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				// ✅ 保底：避免其他例外把這個佇列執行緒打掛
				e.printStackTrace();
			}
		});

		return true;
	}

	/**
	 * 關閉所有印表機佇列
	 */
	public void shutdown() {
		perPrinterExecutor.forEach((k, es) -> es.shutdown());
	}

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
}
