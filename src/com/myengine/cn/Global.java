package com.myengine.cn;

import java.util.ArrayList;
import java.util.List;

public class Global
{
	
	public static BROWSER browserType=BROWSER.BROWSER_FAST;
	public static final String VERSION    = "2.07b";
	public static ParamArray global_http_par=new ParamArray();

	public static AUTH auth_type=AUTH.AUTH_NONE;
	/* Dummy file to upload to the server where possible. */
	public static final String DUMMY_EXT="gif";
	public static final String DUMMY_FILE="GIF89a,\\x01<html>";
	public static final String DUMMY_MIME="image/gif";
	public static  int cur_xss_id=0,scan_id=0;/* Stored XSS manager IDs          */
	public static List<HttpRequest>xss_req=new ArrayList<HttpRequest>();
	
	/* Page fingerprinting constants: */

	public static final int FP_SIZE  = 10 ;             /* Page fingerprint size           */
	public static final int FP_MAX_LEN  =15  ;            /* Maximum word length to count    */
	public static final int FP_T_REL   = 5  ;             /* Relative matching tolerance (%) */
	public static final int FP_T_ABS   = 6  ;             /* Absolute matching tolerance     */
	public static final int FP_B_FAIL   =3  ;             /* Max number of failed buckets    */

	public static final int BH_CHECKS   =15  ;            /* Page verification check count   */
	
	
	/* Various default settings for HTTP client (cmdline override): */

	public static final int MAX_CONNECTIONS        = 40;      /* Simultaneous connection cap     */
	public static final int MAX_CONN_HOST         =  10 ;     /* Per-host connction cap          */
	public static final double MAX_REQUESTS           = 1e8;     /* Total request count cap         */
	public static final double MAX_REQUESTS_SEC       = 0.0 ;    /* Max requests per second         */
	public static final int MAX_FAIL               = 100 ;    /* Max consecutive failed requests */
	public static final int RW_TMOUT               = 10 ;     /* Individual network R/W timeout  */
	public static final int RESP_TMOUT              =20 ;     /* Total request time limit        */
	public static final int IDLE_TMOUT              =10 ;     /* Connection tear down threshold  */
	public static final int MAX_GUESSES            = 256  ;   /* Guess-based wordlist size limit */
	public static final int SIZE_LIMIT=200000; /* Response size cap               */
	
	/* HTTP client constants: */

	public static final int MAX_URL_LEN             =1024;    /* Maximum length of an URL        */
	public static final int MAX_DNS_LEN             =255 ;    /* Maximum length of a host name   */
	public static final int READ_CHUNK              =4096;    /* Read buffer size                */
	
	
	
	/* Flags for http_response completion state: */

	public static final int STATE_NOTINIT  = 0;       /* Request not sent             */
	public static final int STATE_CONNECT   =1;       /* Connecting...                */
	public static final int STATE_SEND     = 2 ;      /* Sending request              */
	public static final int STATE_RECEIVE  = 3;       /* Waiting for response         */

	public static final int STATE_OK        =100 ;    /* Proper fetch                 */
	public static final int STATE_DNSERR   = 101 ;    /* DNS error                    */
	public static final int STATE_LOCALERR  =102 ;    /* Socket or routing error      */
	public static final int STATE_CONNERR  = 103;     /* Connection failed            */
	public static final int STATE_RESPERR   =104 ;    /* Response not valid           */
	public static final int STATE_SUPPRESS  =200 ;    /* Dropped (limits / errors)    */
	
	/* Internal globals for queue management: */
	public static QueueEntry queue;
	public static ConnEntry conn;
	public static DnsEntry dns;
	
	/* Counters: */
	public static int  req_errors_net;
	public static int	req_errors_http;
	public static int	req_errors_cur;
	public static int	req_count;
	public static int	req_dropped;
	public static int	queue_cur;
	public static int	conn_cur;
	public static int	conn_count;
	public static int	conn_idle_tmout;
	public static int	conn_busy_tmout;
	public static int	conn_failed;
	public static int	req_retried;
	public static int	url_scope;
	
	
	/* Flags for http_response warnings: */

	public static int WARN_NONE       =0;       /* No warnings                  */
	public static int WARN_PARTIAL    =1;       /* Incomplete read              */
	public static int WARN_TRAIL      =2;       /* Trailing request garbage     */
	public static int WARN_CFL_HDR    =4;       /* Conflicting headers          */
	
	
	public static PivotDesc root_pivot;
	
	
	
	
	
	
	/* Testing pivot points - used to organize the scan: */

	/* - Pivot types: */

	public static final int PIVOT_NONE              =0;               /* Invalid                   */
	public static final int PIVOT_ROOT              =1;               /* Root pivot                */

	public static final int PIVOT_SERV              =2;              /* Top-level host pivot      */
	public static final int PIVOT_DIR               =4;              /* Directory pivot           */
	public static final int PIVOT_FILE             = 8;              /* File pivot                */
	public static final int PIVOT_PATHINFO          =16;              /* PATH_INFO script          */

	public static final int PIVOT_UNKNOWN           =32;              /* (Currently) unknown type  */

	public static final int PIVOT_PARAM             =64;              /* Parameter fuzzing pivot   */
	public static final int PIVOT_VALUE             =128;             /* Parameter value pivot     */

	/* - Pivot states (initialized to PENDING or FETCH by database.c, then
	     advanced by crawler.c): */

	public static final int PSTATE_NONE            = 0;               /* Invalid                   */
	public static final int PSTATE_PENDING          =1;               /* Pending parent tests      */

	public static final int PSTATE_FETCH            =10;              /* Initial data fetch        */

	public static final int PSTATE_TYPE_CHECK       =20;              /* Type check (unknown only) */
	public static final int PSTATE_404_CHECK        =22;              /* 404 check (dir only)      */
	public static final int PSTATE_PARENT_CHECK     =24;              /* Parent check (dir only)   */
	public static final int PSTATE_IPS_CHECK        =26;              /* IPS filtering check       */

	/* For directories only (injecting children nodes): */

	public static final int PSTATE_CHILD_INJECT     =50;              /* Common security attacks   */
	public static final int PSTATE_CHILD_DICT       =55;              /* Dictionary brute-force    */

	/* For parametric nodes only (replacing parameter value): */

	public static final int PSTATE_PAR_CHECK        =60;              /* Parameter works at all?   */
	public static final int PSTATE_PAR_INJECT       =65;              /* Common security attacks   */
	public static final int PSTATE_PAR_NUMBER       =70 ;             /* Numeric ID traversal      */
	public static final int PSTATE_PAR_DICT         =75;              /* Dictionary brute-force    */
	public static final int PSTATE_PAR_TRYLIST      =99 ;             /* 'Try list' fetches        */

	public static final int PSTATE_DONE             =100;             /* Analysis done             */
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
