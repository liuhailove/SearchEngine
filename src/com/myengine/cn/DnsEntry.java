package com.myengine.cn;
/* DNS cache item: */
public class DnsEntry
{
	String name;	/* Name requested               */
	String addr;		/* IP address (0 = bad host)    */
	DnsEntry next;	/* Next cache entry             */
}
