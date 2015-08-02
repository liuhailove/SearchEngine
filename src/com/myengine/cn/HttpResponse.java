package com.myengine.cn;

public class HttpResponse
{
	private int state;			/* HTTP convo state (STATE_*)   */
	private int code;			/* HTTP response code           */
	private String msg;			/* HTTP response message        */
	private int warn;			 /* Warning flags                */
	private boolean cookies_set;/* Sets cookies?                */
	private ParamArray hdr;		/* Server header, cookie list   */
	private long pay_len;		/* Response payload length      */
	private String payload;	   /* Response payload data        */
	private HttpSig sig;	    /* Response signature data      */
	
	/* Various information populated by content checks: */
	private int sniff_mime_id;	 /* Sniffed MIME (MIME_*)        */
	private int decl_mime_id;	 /* Declared MIME (MIME_*)       */
	private String meta_charset; /* META tag charset value       */
	private String header_charset;/* Content-Type charset value   */
	private String header_mime; /* Content-Type MIME type       */
	private String sniffed_mime;/* Detected MIME type (ref)     */
	/* Everything below is of interest to scrape_response() only: */
	
	byte doc_type;				 /* 0 - tbd, 1 - bin, 2 - ascii  */
	byte css_type;				 /* 0 - tbd, 1 - other, 2 - css  */
	byte js_type;				/* 0 - tbd, 1 - other, 2 - js   */
	byte json_safe;				/* 0 - no, 1 - yes              */
	byte stuff_checked;			/* check_stuff() called?        */
	byte scraped;				/* scrape_response() called?    */
	public int getState()
	{
		return state;
	}
	public void setState(int state)
	{
		this.state = state;
	}
	public int getCode()
	{
		return code;
	}
	public void setCode(int code)
	{
		this.code = code;
	}
	public String getMsg()
	{
		return msg;
	}
	public void setMsg(String msg)
	{
		this.msg = msg;
	}
	public int getWarn()
	{
		return warn;
	}
	public void setWarn(int warn)
	{
		this.warn = warn;
	}
	public boolean isCookies_set()
	{
		return cookies_set;
	}
	public void setCookies_set(boolean cookies_set)
	{
		this.cookies_set = cookies_set;
	}
	public ParamArray getHdr()
	{
		return hdr;
	}
	public void setHdr(ParamArray hdr)
	{
		this.hdr = hdr;
	}
	public long getPay_len()
	{
		return pay_len;
	}
	public void setPay_len(long pay_len)
	{
		this.pay_len = pay_len;
	}
	public String getPayload()
	{
		return payload;
	}
	public void setPayload(String payload)
	{
		this.payload = payload;
	}
	public HttpSig getSig()
	{
		return sig;
	}
	public void setSig(HttpSig sig)
	{
		this.sig = sig;
	}
	public int getSniff_mime_id()
	{
		return sniff_mime_id;
	}
	public void setSniff_mime_id(int sniff_mime_id)
	{
		this.sniff_mime_id = sniff_mime_id;
	}
	public int getDecl_mime_id()
	{
		return decl_mime_id;
	}
	public void setDecl_mime_id(int decl_mime_id)
	{
		this.decl_mime_id = decl_mime_id;
	}
	public String getMeta_charset()
	{
		return meta_charset;
	}
	public void setMeta_charset(String meta_charset)
	{
		this.meta_charset = meta_charset;
	}
	public String getHeader_charset()
	{
		return header_charset;
	}
	public void setHeader_charset(String header_charset)
	{
		this.header_charset = header_charset;
	}
	public String getHeader_mime()
	{
		return header_mime;
	}
	public void setHeader_mime(String header_mime)
	{
		this.header_mime = header_mime;
	}
	public String getSniffed_mime()
	{
		return sniffed_mime;
	}
	public void setSniffed_mime(String sniffed_mime)
	{
		this.sniffed_mime = sniffed_mime;
	}
	public byte getDoc_type()
	{
		return doc_type;
	}
	public void setDoc_type(byte doc_type)
	{
		this.doc_type = doc_type;
	}
	public byte getCss_type()
	{
		return css_type;
	}
	public void setCss_type(byte css_type)
	{
		this.css_type = css_type;
	}
	public byte getJs_type()
	{
		return js_type;
	}
	public void setJs_type(byte js_type)
	{
		this.js_type = js_type;
	}
	public byte getJson_safe()
	{
		return json_safe;
	}
	public void setJson_safe(byte json_safe)
	{
		this.json_safe = json_safe;
	}
	public byte getStuff_checked()
	{
		return stuff_checked;
	}
	public void setStuff_checked(byte stuff_checked)
	{
		this.stuff_checked = stuff_checked;
	}
	public byte getScraped()
	{
		return scraped;
	}
	public void setScraped(byte scraped)
	{
		this.scraped = scraped;
	}
	
	
	
}
