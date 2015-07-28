package com.myengine.cn;

/* HTTP response signature. */
public class HttpSig
{
	int code;			/* HTTP response code           */
	String data;		/* Response fingerprint data    */
	boolean has_text;	/* Does the page have text      */
}
