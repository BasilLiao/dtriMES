package dtri.com.tw.bean;

import java.util.ArrayList;

/**
 * @author Basil
 * @see 系統設定<br>
 *      Ex:<br>
 *      ^XA --開始<br>
 * 
 *      ^LH25,15 --紙張上->標籤位置座標<br>
 *      ^LL100 --標籤長度<br>
 *      ^PW300 --標籤寬度<br>
 *      ^CI28 --文字編碼UTF8<br>
 *      ^MD100 --暗度<br>
 *      ^PR3 --速度<br>
 * 
 *      ^FO0,2 --標籤上->打印位置座標-開始<br>
 *      ^ADN,20 --字型<br>
 *      ^FB240,1,1,C,0 --段落設定<br>
 *      ^FDPART NO:AA\& --文字內容<br>
 *      ^FS --標籤上->打印位置-結束<br>
 * 
 *      ^FO25,22 --標籤上->打印位置座標-開始<br>
 *      ^BY1,2,30 --條碼長寬設定<br>
 *      ^B3N,N,25,N,N --條碼類型設定<br>
 *      ^FDQQQWWWDDERRR --條碼-文字內容<br>
 *      ^FS<br>
 * 
 *      ^FO100,100<br>
 *      ^BQN,2,10<br>
 *      ^FDLA,http://0123456789ABCD___2D code<br>
 *      ^FS<br>
 * 
 *      ^XZ --結束<br>
 */

public class LabelListBean {

	// ===基本(設定)============================
	// 功能項目

	private Long llid;

	private String llname;// 標籤名稱

	private String llgname;// 標籤群名稱

	private String llxa;// 固定-開頭:^XA ... 固定-結尾:^XZ

	// ===紙張與標籤機(設定)============================

	private String llci;// 固定-設定-編碼: ^CI28=UTF8

	private String llll;// 動態-設定-標籤紙張-長度(1cm=200點) ^LL100

	private String llpw;// 動態-設定-標籤紙張-寬度(1cm=200點) ^PW300

	private String lllh;// 動態-設定-[標籤紙張]上->[標籤]位置座標(x,y) ^LH25,15

	private String llmd;// 動態-設定-暗度(+-30) ^MD30

	private String llpr;// 動態-設定-速度(1-7) ^PR

	// ===區域位置(設計)============================

	private String llfo;// 動態-設計-[標籤]上->[打印]位置座標(x,y) ^FO0,2 ... [打印]位置結尾:^FS

	// ===一般文字(設計)============================
	private String llfd;// 動態-設計-[文字]開始:^FD ... [文字]結尾: \&
	private String lltestfd;// 動態-設計-[測試文字]開始:^FD ... [測試文字]結尾: \&
	/**
	 * https://minisoft.com/support/index.php/zebra-font-support/
	 * https://docsplayer.com/139398159-Zpl-ii-%E7%B0%A1%E6%98%93%E6%8C%87%E4%BB%A4%E9%9B%86.html
	 * T 字型:A,B,C,D,E,F,G,H,O,GS,P,Q,R,S,T,U,V<br>
	 * c 角度:N=旋轉0度列印R=旋轉90度列印I=旋轉180度列印B=旋轉270度列印<br>
	 * h 高度:20<br>
	 * w 寬度:(不填寫 則預設等比例)<br>
	 */
	private String lla;// _動態-設計-字型(Tc,h,w) ^AAN,20
	/**
	 * a 文字區段:寬度(點) <br>
	 * b 文字區段:行數 <br>
	 * c 文字區段:行間高度 <br>
	 * d 文字區段:L（左）， C（中心）， J（邊到邊）和R（右）<br>
	 * e 文字區段:縮排 <br>
	 **/
	private String llfb;// 動態-設計-文字區段(a,b,c,d,e) ^FB240,1,5,C,0

	// ===Barcode(一維碼設計)============================
	/**
	 * m：條碼窄線的點數<br>
	 * w：條碼寬窄比例值(1：2 ~ 1：3)<br>
	 * h：條碼高度<br>
	 */
	private String llby;// 動態-設計-條碼比例(m,w,h) ^BY1,2,30

	/**
	 * x 編碼類型:1=Code11,3=Code39,C=Code128<br>
	 * c 編碼角度:N=旋轉0度列印R=旋轉90度列印I=旋轉180度列印B=旋轉270度列印<br>
	 * d 附加檢-編碼查碼:Y/N(會增長條碼)<br>
	 * e 條碼列印:(文字)高度(DEFAULT=10)<br>
	 * (f,g)下方?:Y,N / 上方?:Y,Y<br>
	 * 會自動抓 ^FD 內的數字<br>
	 */
	private String llb;// 動態-設計-條碼類型(xc,d,e,f,g) ^B3N,N,25,N,N

	private String llbfd;// 動態-設計-條碼文字:^FD ...
	private String lltestbfd;// 動態-設計-條碼測試文字:^FD ...

	// ===Image(圖片設計)============================
	private String llgfa;// 動態-設計-圖片 請使用ZPLConveterImg 轉換
	private String llpgfa;// 動態-設計-圖片 倍率(只能小)

	// ===QR code(二維碼設計)============================
	/**
	 * c 編碼角度:N=旋轉0度列印R=旋轉90度列印I=旋轉180度列印B=旋轉270度列印<br>
	 * d 類型:請固定2<br>
	 * e 大小:等比例縮放<br>
	 * 會自動抓 ^FD 內的數字<br>
	 */
	private String llbq;// 動態-設計-條碼比例(c,d,e)^BQN,2,10
	private String llbqfd;// 動態-設計-條碼文字:^FDLA, ...
	private String testbqfd;// 動態-設計-條碼測試文字:^FDLA, ...
	// ===彙整標籤(內容)============================
	private ArrayList<String> folist;

	public LabelListBean() {
		// 標籤紙
		this.llxa = "^XA{ZPL打印內容}^XZ";
		this.llci = "^CI28";
		this.lllh = "^LH{x,y起始打印座標(點)}";
		this.llll = "^LL{標籤長度(點)}";
		this.llpw = "^PW{標籤寬度(點)}";
		this.llmd = "^MD{打印暗度}";
		this.llpr = "^PR{打印速度}";

		// 區域位置(通用)
		this.llfo = "^FO{x,y區域位置座標(點)}^FS";

		// 一般文字(字體)
		this.llfd = "^FD{一般文字}\\&";
		// 一般字形(字體)
		this.lla = "^A{字型與角度,高,寬}";
		// 一般字段(字體)
		this.llfb = "^FB{寬度(點),行數,行間高度,靠左右中,縮排}";

		// 一維碼設計
		this.llbfd = "^FD{條碼文字}";
		this.llby = "^BY{條碼窄線(點),條碼寬比,條碼高度(點)}";
		this.llb = "^B3N,N,{高度},N,N";

		// 二維碼設計
		this.llbqfd = "^FDLA,{條碼文字}";
		this.llbq = "^BQ{角度,2,大小}";

		// 準備要的資料
		this.folist = new ArrayList<String>();

	}

	public String getLlxa() {
		return llxa;
	}

	public void setLlxa(String llxa) {
		this.llxa = llxa;
	}

	public String getLlci() {
		return llci;
	}

	public void setLlci(String llci) {
		this.llci = llci;
	}

	public String getLlll() {
		return llll;
	}

	public void setLlll(String llll) {
		this.llll = llll;
	}

	public String getLlpw() {
		return llpw;
	}

	public void setLlpw(String llpw) {
		this.llpw = llpw;
	}

	public String getLllh() {
		return lllh;
	}

	public void setLllh(String lllh) {
		this.lllh = lllh;
	}

	public String getLlmd() {
		return llmd;
	}

	public void setLlmd(String llmd) {
		this.llmd = llmd;
	}

	public String getLlpr() {
		return llpr;
	}

	public void setLlpr(String llpr) {
		this.llpr = llpr;
	}

	public String getLlfo() {
		return llfo;
	}

	public void setLlfo(String llfo) {
		this.llfo = llfo;
	}

	public String getLlfd() {
		return llfd;
	}

	public void setLlfd(String llfd) {
		this.llfd = llfd;
	}

	public String getLltestfd() {
		return lltestfd;
	}

	public void setLltestfd(String lltestfd) {
		this.lltestfd = lltestfd;
	}

	public String getLla() {
		return lla;
	}

	public void setLla(String lla) {
		this.lla = lla;
	}

	public String getLlfb() {
		return llfb;
	}

	public void setLlfb(String llfb) {
		this.llfb = llfb;
	}

	public String getLlby() {
		return llby;
	}

	public void setLlby(String llby) {
		this.llby = llby;
	}

	public String getLlb() {
		return llb;
	}

	public void setLlb(String llb) {
		this.llb = llb;
	}

	public String getLlbfd() {
		return llbfd;
	}

	public void setLlbfd(String llbfd) {
		this.llbfd = llbfd;
	}

	public String getLltestbfd() {
		return lltestbfd;
	}

	public void setLltestbfd(String lltestbfd) {
		this.lltestbfd = lltestbfd;
	}

	public String getLlgfa() {
		return llgfa;
	}

	public void setLlgfa(String llgfa) {
		this.llgfa = llgfa;
	}

	public String getLlbq() {
		return llbq;
	}

	public void setLlbq(String llbq) {
		this.llbq = llbq;
	}

	public String getLlbqfd() {
		return llbqfd;
	}

	public void setLlbqfd(String llbqfd) {
		this.llbqfd = llbqfd;
	}

	public String getTestbqfd() {
		return testbqfd;
	}

	public void setTestbqfd(String testbqfd) {
		this.testbqfd = testbqfd;
	}

	public ArrayList<String> getFolist() {
		return folist;
	}

	public void setFolist(ArrayList<String> folist) {
		this.folist = folist;
	}

	public Long getLlid() {
		return llid;
	}

	public void setLlid(Long llid) {
		this.llid = llid;
	}

	public String getLlname() {
		return llname;
	}

	public void setLlname(String llname) {
		this.llname = llname;
	}

	public String getLlgname() {
		return llgname;
	}

	public void setLlgname(String llgname) {
		this.llgname = llgname;
	}

}