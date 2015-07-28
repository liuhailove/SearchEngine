package com.myengine.cn;

public class Caller
{
	RCaller caller;
	public void setCalller(RCaller rc)
	{
		this.caller=rc;
	}
	public String call(HttpRequest req,HttpResponse res)
	{
		return this.caller.method(req, res);
	}
}
