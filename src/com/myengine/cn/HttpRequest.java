package com.myengine.cn;
/* HTTP request descriptor: */
public class HttpRequest
{
	private PROTO 	proto;			 /* Protocol (PROTO_*)           */
	private String  method;			 /* HTTP method (GET, POST, ...) */
	private String  host;			/* Host name                    */
	private String  addr;			/* Resolved IP address          */
	private int     port;		    /* Port number to connect to    */
	private String  orig_url;       /* Copy of the original URL     */
	private ParamArray par;		   /* Parameters, headers, cookies */
	private PivotDesc pivot;	   /* Pivot descriptor             */
	private long    user_val;	   /* Can be used freely           */
	private HttpSig same_sig;     /* Used by secondary ext fuzz.  */
	private Caller call;		  /* Callback to invoke when done */
	
	/* Used by directory brute-force: */
	private String trying_key;	 /* Current keyword ptr          */
	private String trying_spec; /* Keyword specificity info     */
	private String fuzz_par_enc;/* Fuzz target encoding         */

	public HttpRequest()
	{
		par=new ParamArray();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		HttpRequest req=(HttpRequest)super.clone();
		req.par=(ParamArray)par.clone();
		return req;
	}

	public PROTO getProto()
	{
		return proto;
	}
	public void setProto(PROTO proto)
	{
		this.proto = proto;
	}
	public String getMethod()
	{
		return method;
	}
	public void setMethod(String method)
	{
		this.method = method;
	}
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	public String getAddr()
	{
		return addr;
	}
	public void setAddr(String addr)
	{
		this.addr = addr;
	}
	public int getPort()
	{
		return port;
	}
	public void setPort(int port)
	{
		this.port = port;
	}
	public String getOrig_url()
	{
		return orig_url;
	}
	public void setOrig_url(String orig_url)
	{
		this.orig_url = orig_url;
	}
	public ParamArray getPar()
	{
		return par;
	}
	public void setPar(ParamArray par)
	{
		this.par = par;
	}
	public PivotDesc getPivot()
	{
		return pivot;
	}
	public void setPivot(PivotDesc pivot)
	{
		this.pivot = pivot;
	}
	public long getUser_val()
	{
		return user_val;
	}
	public void setUser_val(long user_val)
	{
		this.user_val = user_val;
	}
	public HttpSig getSame_sig()
	{
		return same_sig;
	}
	public void setSame_sig(HttpSig same_sig)
	{
		this.same_sig = same_sig;
	}
	public Caller getCall()
	{
		return call;
	}
	public void setCall(Caller call)
	{
		this.call = call;
	}
	public String getTrying_key()
	{
		return trying_key;
	}
	public void setTrying_key(String trying_key)
	{
		this.trying_key = trying_key;
	}
	public String getTrying_spec()
	{
		return trying_spec;
	}
	public void setTrying_spec(String trying_spec)
	{
		this.trying_spec = trying_spec;
	}
	public String getFuzz_par_enc()
	{
		return fuzz_par_enc;
	}
	public void setFuzz_par_enc(String fuzz_par_enc)
	{
		this.fuzz_par_enc = fuzz_par_enc;
	}
	
}
