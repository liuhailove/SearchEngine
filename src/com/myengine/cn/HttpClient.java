package com.myengine.cn;
import static tools.Common.getValue;
import static tools.Common.newXssTag;
import static tools.Common.registerXssTag;
import static tools.Common.setValue;
import static tools.Tools.DEBUG;
import static tools.Tools.FATAL;
import static tools.Tools.getFixedLenghtStr;
import static tools.Tools.strChr;
import static tools.Tools.strCspn;
import static tools.Tools.strPos;
import static tools.Tools.strSpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import tools.PARAMS;
public class HttpClient
{

	/* Convert a fully-qualified or relative URL string to a proper HttpRequest
	   representation. Returns true on success, false on format error. */
	public boolean parseUrl(String url,HttpRequest req,HttpRequest ref)
	{
		String cur=url;
		boolean has_host=false,add_slash=true;
		req.setOrig_url(url);
		if(-1!=cur.indexOf(':'))
		{
			if(-1!=cur.indexOf("http:"))
			{
				req.setProto(PROTO.PROTO_HTTP);
				cur=cur.substring(5);
			}
			else if(-1!=cur.indexOf("https:"))
			{
				req.setProto(PROTO.PROTO_HTTPS);
				cur=cur.substring(6);
			}
			else
			{
				return false;
			}
			
		}
		else
		{
			if((ref==null)||(ref.getProto()==null))
			{
				return false;
			}
			req.setProto(ref.getProto());
		}
		 /* Interpret, skip //[login[:pass@](\[ipv4\]|\[ipv6\]|host)[:port] part of the
	     URL, if present. Note that "http:blarg" is a valid relative URL to most
	     browsers, and "//example.com/blarg" is a valid non-FQ absolute one.
	     We need to mimick this, which complicates the code a bit.

	     We only accept /, ?, #, and : to mark the end of a host name. Some browsers
	     also allow \ or ;, but it's unlikely that we need to obey this. */
		if(cur.charAt(0)=='/'&&cur.charAt(1)=='/')
		{
			int path_st;
			String at_sign,host,x;
			boolean has_utf=false;
			cur=cur.substring(2);
			path_st=strPos(cur,"/?#");
			if(-1!=cur.indexOf('@'))
			{
				at_sign=cur.substring(cur.indexOf('@'));
				if(at_sign!=null&&path_st>cur.indexOf('@'))
				{
					cur=at_sign.substring(1);
					if(req.getPivot()==null)
					{
						return false;
					}
					/******
					 * problem(PROB_URL_AUTH, ref, 0, url, req->pivot, 0);
					 * 
					 */
				}
				
			}
			path_st = strPos(cur, ":/?#");
			/* No support for IPv6 or [ip] notation for now, so let's just refuse to
		       parse the URL. Also, refuse excessively long domain names for sanity. */
			if(cur.charAt(0)=='[')
				return false;
			x=host=cur.substring(0,path_st);
			host=host.toLowerCase();
			for(int i=0;i<x.length();i++)
			{
				char t=x.charAt(i);
				if(t>='a'&&t<='z')
				{
					continue;
				}
				if(t>=0x80||t<=0xff)
				{
					has_utf=true;
				}
				
			}
			
			
			 /* Host names that contained high bits need to be converted to Punycode
		       in order to resolve properly. */
			if(has_utf)
			{
				/*dosomthing*/
			}
			req.setHost(host);
			cur=cur.substring(path_st);
		    /* All right, moving on: if host name is followed by :, let's try to
		       parse and validate port number; otherwise, assume 80 / 443, depending
		       on protocol. */
			if(cur.charAt(0)==':')
			{
				cur=cur.substring(1);
				int digit_cnt=strSpn(cur,"0123456789");
				int port=Integer.parseInt(cur.substring(0,digit_cnt));
				if(0==digit_cnt||(!strChr("/?#",cur.charAt(digit_cnt))))
				{
					return false;
				}
				req.setPort(port);
				cur=cur.substring(digit_cnt);
			}
			else
			{
				if(req.getProto().equals(PROTO.PROTO_HTTPS))
					req.setPort(443);
				else
					req.setPort(80);
			}
			has_host=true;
			
			
			
		}
		else
		{
			 /* No host name found - copy from referring request instead. */
			if(null==req||(null==ref.getHost())||(ref.getHost().equals("")))
			{
				return false;
			}
			req.setHost(ref.getHost());
			req.setAddr(ref.getAddr());
			req.setPort(ref.getPort());
		}
		if(cur.charAt(0)=='#')
		{
			int i;
		    /* No-op path. If the URL does not specify host (e.g., #foo), copy
		       everything from referring request, call it a day. Otherwise
		       (e.g., http://example.com#foo), let tokenize_path() run to
		       add NULL-"" entry to the list. */
			if(!has_host)
			{
				for(i=0;i<ref.getPar().c;i++)
				{
					if(PARAMS.PATH_SUBTYPE(ref.getPar().t.get(i))||PARAMS.QUERY_SUBTYPE(ref.getPar().t.get(i)))
					{
						setValue(ref.getPar().t.get(i),ref.getPar().n.get(i),ref.getPar().v.get(i),-1,req.getPar());
						return false;
					}
				}
				
			}
			
		}
		if(!has_host&&cur.charAt(0)=='?')
		{
			int i;
		    /* URL begins with ? and does not specify host (e.g., ?foo=bar). Copy all
		       path segments, but no query, then fall through to parse the query
		       string. */
			for(i=0;i<ref.getPar().c;i++)
			{
				if(PARAMS.PATH_SUBTYPE(ref.getPar().t.get(i)))
				{
					setValue(ref.getPar().t.get(i), ref.getPar().n.get(i), ref.getPar().v.get(i), -1, req.getPar());
				}
			}
		    /* In this case, we do not want tokenize_path() to tinker with the path
		       in any way. */

		    add_slash = false;
		}
		else if(!has_host&&cur.charAt(0)!='/')
		{
		    /* The URL does not begin with / or ?, and does not specify host (e.g.,
		       foo/bar?baz). Copy path from referrer, but drop the last "proper"
		       path segment and everything that follows it. This mimicks browser
		       behavior (for URLs ending with /, it just drops the final NULL-""
		       pair). */
			int i;
			int path_cnt=0,path_cur=0;
			for(i=0;i<ref.getPar().c;i++)
			{
				if(ref.getPar().t.get(i)==PARAMS.PARAM_PATH)
					path_cnt++;
			}
			for(i=0;i<ref.getPar().c;i++)
			{
				if(ref.getPar().t.get(i)==PARAMS.PARAM_PATH)
					path_cur++;
				if(path_cur<path_cnt&&PARAMS.PATH_SUBTYPE(ref.getPar().t.get(i)))
					setValue(ref.getPar().t.get(i), ref.getPar().n.get(i), ref.getPar().v.get(i), -1, req.getPar());	
			}
			
		}
		  /* Tokenize the remaining path on top of what we parsed / copied over. */
		tokenizePath(cur, req, add_slash);
		return true;
	}

	/* Split path at known "special" character boundaries, URL decode values,
	   then put them in the provided http_request struct. */
	private void tokenizePath(String str, HttpRequest req, boolean add_slash)
	{
		String cur;
		boolean know_dir=false;
		while(str.charAt(0)=='/')str=str.substring(1);
		cur=str;
		/* Parse path elements first. */
		while(null!=cur&&!strChr("?#",cur.charAt(0)))
		{
			int next_seg,next_eq;
			String name=null,value=null;
			boolean first_el=(str.equals(cur));
			int add;
			if(first_el)
			{
				add=0;
			}
			else
			{
				add=1;
			}
			if(first_el||cur.charAt(0)=='/')
			{
			   /* Optimize out //, /\0, /./, and /.\0. They do indicate
		         we are looking at a directory, so mark this. */
				if(!first_el&&(cur.length()>=1&&cur.charAt(1)=='/'))
				{
					cur=cur.substring(1);
					know_dir=true;
					continue;
				}
				if(cur.charAt(0+add)=='.'&&(cur.length()>=(1+add)&&cur.charAt(1+add)=='/'))
				{
					cur=cur.substring(1+add);
					know_dir=true;
					continue;
				}
			   /* Also optimize out our own \.\ prefix injected in directory
		         probes. This is to avoid recursion if it actually worked in some
		         way. */
				if(cur.startsWith("/\\.\\")&&(cur.length()>=4&&cur.charAt(4)=='/'))
				{
					cur=cur.substring(4);
					continue;
				}
				if(cur.toLowerCase().startsWith("/%5c.%5c")&&(cur.length()>=8&&cur.charAt(8)=='/'))
				{
					cur=cur.substring(8);
					continue;
				}

			    /* If we encountered /../ or /..\0, remove everything up to and
			         including the last "true" path element. It's also indicative
			         of a directory, by the way. */
				
				if(cur.charAt(0+add)=='.'&&cur.charAt(1+add)=='.'&&((cur.length()>=2+add)&&cur.charAt(2+add)=='/'))
				{
					int i,last_p=req.getPar().c;
					for(i=0;i<req.getPar().c;i++)
					{
						if(req.getPar().t.get(i)==PARAMS.PARAM_PATH)
							last_p=i;
					}
					for(i=last_p;i<req.getPar().c;i++)
					{
						req.getPar().t.set(i, PARAMS.PARAM_NONE);
					}
					cur=cur.substring(2+add);
					know_dir=true;
					continue;
				}
			}
		    /* If we're here, we have an actual item to add; cur points to
		       the string if it's the first element, or to field separator
		       if one of the subsequent ones. */
			next_seg=strCspn(cur.substring(1),"/;,!$?#")+1;
			next_eq =strCspn(cur.substring(1),"=/;,!$?#")+1;
			know_dir=false;
			if(next_eq<next_seg)
			{
				name=URLDecoder.decode(getFixedLenghtStr(cur, add, next_eq-add));
				value=URLDecoder.decode(getFixedLenghtStr(cur, next_eq+1, next_seg-next_eq-1));
			}
			else
			{
				value=URLDecoder.decode(getFixedLenghtStr(cur,add,next_seg-add));
			}

		    /* If the extracted segment is just '.' or '..', but is followed by
		       something else than '/', skip one separator. */
			if(null==name&&cur.length()>=next_seg&&cur.charAt(next_seg)!='/'&&(value.equals(".")||value.equals("..")))
			{
				next_seg=strCspn(cur.substring(next_seg+1),"/;,!$?#")+next_seg+1;
				value=URLDecoder.decode(getFixedLenghtStr(cur,add,next_seg-add));
			}
			switch(first_el==true?'/':cur.charAt(0))
			{
				case ';':setValue((byte)PARAMS.PARAM_PATH_S, name, value, -1, req.getPar()); break;
				case ',':setValue((byte)PARAMS.PARAM_PATH_C, name, value, -1, req.getPar()); break;
				case '!':setValue((byte)PARAMS.PARAM_PATH_E, name, value, -1, req.getPar()); break;
				case '$':setValue((byte)PARAMS.PARAM_PATH_D, name, value, -1, req.getPar()); break;
				default:setValue((byte)PARAMS.PARAM_PATH, name, value, -1, req.getPar()); break;
					
			}
			cur=cur.substring(next_seg);
			
		}
		/* If the last segment was /, /./, or /../, *or* if we never added
	     anything to the path to begin with, we want to store a NULL-""
	     entry to denote it's a directory. */
		if(know_dir||(add_slash&&(str==null||strChr("?#",cur.charAt(0)))))
		{
			setValue((byte)PARAMS.PARAM_PATH,null,"",-1,req.getPar());
		}
		/* Deal with regular query parameters now. This is much simpler,
	     obviously. */
		while(null!=cur&&!strChr("#",cur.charAt(0)))
		{
			int next_seg=strCspn(cur.substring(1),"#&;,!$")+1;
			int next_eq=strCspn(cur.substring(1),"=#&;,!$")+1;
			String name=null,value=null;
			/* foo=bar syntax... */
			if(next_eq<next_seg)
			{
				name=URLDecoder.decode(getFixedLenghtStr(cur,1,next_eq-1));
				value=URLDecoder.decode(getFixedLenghtStr(cur,next_eq+1,next_seg-next_eq-1));
			}
			else
			{
				value=URLDecoder.decode(getFixedLenghtStr(cur,1,next_seg-1));
			}
		    switch (cur.charAt(0)) {

		      case ';': setValue(PARAMS.PARAM_QUERY_S, name, value, -1, req.getPar()); break;
		      case ',': setValue(PARAMS.PARAM_QUERY_C, name, value, -1, req.getPar()); break;
		      case '!': setValue(PARAMS.PARAM_QUERY_E, name, value, -1, req.getPar()); break;
		      case '$': setValue(PARAMS.PARAM_QUERY_D, name, value, -1, req.getPar()); break;
		      default: setValue(PARAMS.PARAM_QUERY, name, value, -1, req.getPar());

		    }
		    if(cur.length()>next_seg)
		    	cur=cur.substring(next_seg);
		    else
		    	break;
		}
		
		
	}

	/* Reconstructs URI from httpRequest data. Includes protocol and host
	   if with_host is non-zero. */
	String serializePath(HttpRequest req,boolean with_host,boolean with_post)
	{
		int i,cur_pos;
		boolean got_search=false;
		StringBuilder ret=new StringBuilder();
		if(with_host)
		{
			ret.append("http");
			if(req.getProto()==PROTO.PROTO_HTTPS)
				ret.append("s");
			ret.append("://");
			ret.append(req.getHost());
			if((req.getProto()==PROTO.PROTO_HTTP&&req.getPort()!=80)||
				(req.getProto()==PROTO.PROTO_HTTPS&&req.getPort()!=443))
			{
				ret.append(":"+req.getPort());
			}
		}
		
		/* First print path... */
		for(i=0;i<req.getPar().c;i++)
		{
			String enc=ENC.ENC_PATH;
			if(null!=req.getPivot()&&null!=req.getFuzz_par_enc()&&i==req.getPivot().fuzz_par)
			{
				enc=req.getFuzz_par_enc();
			}
			if(PARAMS.PATH_SUBTYPE(req.getPar().t.get(i)))
			{
				switch(req.getPar().t.get(i))
				{
			       case PARAMS.PARAM_PATH_S: ret.append(";"); break;
			       case PARAMS.PARAM_PATH_C: ret.append(","); break;
			       case PARAMS.PARAM_PATH_E: ret.append("!"); break;
			       case PARAMS.PARAM_PATH_D: ret.append("$"); break;
			       default: ret.append("/");
				}
				if(req.getPar().n.get(i)!=null)
				{
					String str=URLEncoder.encode(req.getPar().n.get(i));
					ret.append(str);
					ret.append("=");
				}
				if(req.getPar().v.get(i)!=null)
				{
					String str=URLEncoder.encode(req.getPar().v.get(i));
					ret.append(str);
				}
				
			}

		}
		/* Then actual parameters. */
		for(i=0;i<req.getPar().c;i++)
		{
			String enc=ENC.ENC_DEFAULT;
			if(null!=req.getPivot()&&null!=req.getFuzz_par_enc()&&i==req.getPivot().fuzz_par)
				enc=req.getFuzz_par_enc();
			if(PARAMS.QUERY_SUBTYPE(req.getPar().t.get(i)))
			{
				if(!got_search)
				{
					ret.append("?");
					got_search=true;
				}
				else
				{
					switch(req.getPar().t.get(i))
					{
					    case PARAMS.PARAM_QUERY_S: ret.append(";"); break;
				        case PARAMS.PARAM_QUERY_C: ret.append(","); break;
				        case PARAMS.PARAM_QUERY_E: ret.append("!"); break;
				        case PARAMS.PARAM_QUERY_D: ret.append("$"); break;
				        default: ret.append("&");
					}
				}
				if(req.getPar().n.get(i)!=null)
				{
					String str=URLEncoder.encode(req.getPar().n.get(i));
					ret.append(str);
					ret.append("=");
				}
				if(req.getPar().v.get(i)!=null)
				{
					String str=URLEncoder.encode(req.getPar().v.get(i));
					ret.append(str);
				}
				
			}
				
				
		}
		got_search=false;
		if(with_post)
		{
			for(i=0;i<req.getPar().c;i++)
			{
				String enc=ENC.ENC_DEFAULT;
				if(req.getPivot()!=null&&req.getFuzz_par_enc()!=null&&i==req.getPivot().fuzz_par)
					enc=req.getFuzz_par_enc();
				if(PARAMS.POST_SUBTYPE(req.getPar().t.get(i)))
				{
					if(!got_search)
					{
						ret.append(" DATA:");
						got_search=true;
						
					}
					else
						ret.append("&");
					if(null!=req.getPar().n.get(i))
					{
						String str=URLEncoder.encode(req.getPar().n.get(i));
						ret.append(str);
						ret.append("=");
					}
					if(null!=req.getPar().v.get(i))
					{
						String str=URLEncoder.encode(req.getPar().v.get(i));
						ret.append(str);
					}
				}
			}
		}
		String result=ret.toString();
		result=result.trim();
		return result;
	}
	/**
	 * 
	 * @param str IP
	 * @return true if IP is legal or false if IP illeagal
	 */
    private boolean checkIP(String str) 
    {
        Pattern pattern = Pattern
                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }

	/* Looks up IP for a particular host, returns data in network order.
	   Uses standard resolver, so it is slow and blocking, but we only
	   expect to call it a couple of times during a typical assessment.
	   There are some good async DNS libraries to consider in the long run. */
	private String mayLookupHost(String host)
	{
		if(null==host||host.length()==0)
			return null;
		DnsEntry d=Global.dns,prev=null;
		String ret_addr=null;
		while(null!=d)
		{
			if(d.name.equalsIgnoreCase(host))
				return d.addr;
			prev=d;
			d=d.next;
		}
		try
		{
			
			if(host.equals("localhost"))
			{
				 ret_addr= InetAddress.getLocalHost().getHostAddress();
			}
			else if(checkIP(host))
			{
				/*if host is a standard ip,return directly*/
				ret_addr= host;
			}
			else 
			{
				InetAddress[] hosts=InetAddress.getAllByName(host);
				boolean search=false;
				for(int i=0;i<hosts.length;i++)
				{
					d=Global.dns;
					while(null!=d)
					{
						if(hosts[i].getHostAddress().equals(d.addr))
						{
							search=true;
							ret_addr=d.addr;
							
						}
						d=d.next;
							
					}
					//System.out.println(hosts[i].getHostAddress());
				}
				if(search!=true)
				{
					ret_addr= hosts[0].getHostAddress();
				}
				if(null==prev)
					d=Global.dns=new DnsEntry();
				else
					d=prev.next=new DnsEntry();
				d.name=host;
				d.addr=ret_addr;
					
			}

		} catch (UnknownHostException e)
		{
			
			e.printStackTrace();
			ret_addr= null;
		}
		return ret_addr;

		
	}
	/* Creates an ad hoc DNS cache entry, to override NS lookups. */
	void fakeHost(String name,String addr)
	{
		DnsEntry d=Global.dns,prev=Global.dns;
		while((null!=d)&&(null!=d.next))
		{
			prev=d;
			d=d.next;
		}
		if(null==Global.dns)
		{
			d=Global.dns=new DnsEntry();
		}
		else
		{
			d=prev.next=new DnsEntry();
		}
		d.name=name;
		d.addr=addr;
	}
	

	String GET_CK(String name,ParamArray par)
	{
		return getValue(PARAMS.PARAM_COOKIE,name,0,par);
	}
	void SET_CK(String name,String val,ParamArray par)
	{
		setValue(PARAMS.PARAM_COOKIE,name,val,0,par);
		
	}
	String GET_PAR(String name,ParamArray par)
	{
		return getValue(PARAMS.PARAM_QUERY,name,0,par);
	}
	void SET_PAR(String name,String val,ParamArray par)
	{
		setValue(PARAMS.PARAM_QUERY,name,val,-1,par);
		
	}
	String GET_HDR(String name,ParamArray par)
	{
		return getValue(PARAMS.PARAM_HEADER,name,0,par);
	}
	void SET_HDR(String name,String val,ParamArray par)
	{
		setValue(PARAMS.PARAM_HEADER,name,val,0,par);
		
	}
	String GET_HDR_OFF(String name,ParamArray par,int offset)
	{
		return getValue(PARAMS.PARAM_HEADER,name,offset,par);
	}
	/* Prepares a serialized HTTP buffer to be sent over the network. */
	String buildRequestData(HttpRequest req)
	{
		if(null==req)
		{
			FATAL("HttpRequest is NULL");
		}
		StringBuilder ret_buf,ck_buf,pay_buf,path;
		int req_type=PARAMS.PARAM_NONE;
		if(req.getProto()==PROTO.PROTO_NONE)
		{
			FATAL("uninitialized HttpRequest");
		}
		path=new StringBuilder(serializePath(req,false,false));
		ret_buf=new StringBuilder();
		if(req.getMethod()!=null)
		{
			ret_buf.append(req.getMethod());
		}
		else
		{
			ret_buf.append("GET");
		}
		ret_buf.append(" ");
		ret_buf.append(path);
		ret_buf.append(" HTTP/1.1\r\n");
		ret_buf.append("Host: ");
		ret_buf.append(req.getHost());
		if((req.getProto()==PROTO.PROTO_HTTP&&req.getPort()!=80)||
			(req.getProto()==PROTO.PROTO_HTTPS&&req.getPort()!=443))
		{
			ret_buf.append(":").append(req.getPort());
		}
		ret_buf.append("\r\n");
		/* Insert generic browser headers first. */
		if(Global.browserType==BROWSER.BROWSER_FAST)
		{
			ret_buf.append("Accept-Encoding: gzip\r\n");
			ret_buf.append("Connection: keep-alive\r\n");
			if(null!=GET_HDR("User-Agent",req.getPar()))
			{
				ret_buf.append("User-Agent: Mozilla/5.0 SF/").append(Global.VERSION).append("\r\n");
		
			}
		    /* Some servers will reject to gzip responses unless "Mozilla/..."
		       is seen in User-Agent. Bleh. */	
		}
		else if(Global.browserType==BROWSER.BROWSER_FFOX)
		{
			if(null!=GET_HDR("User-Agent",req.getPar()))
			{
				ret_buf.append("User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; ")
				.append("rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 SF/")
				.append(Global.VERSION)
				.append("\r\n");
				ret_buf.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;")
				.append("q=0.8\r\n");
				
			}
			if(null!=GET_HDR("Accept-Language",req.getPar()))
			{
				ret_buf.append("Accept-Language: en-us,en\r\n");
			}
			ret_buf.append("Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n");
			ret_buf.append("Keep-Alive: 300\r\n");
			ret_buf.append("Connection: keep-alive\r\n");
			
		}
		else if(Global.browserType==BROWSER.BROWSER_MSIE)
		{
			ret_buf.append("Accept: */*\r\n");
			if(null!=GET_HDR("Accept-Language",req.getPar()))
			{
				ret_buf.append("Accept-Language: en,en-US;q=0.5\r\n");
				
			}
			if(null!=GET_HDR("User-Agent",req.getPar()))
			{
				ret_buf.append("User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; ")
				.append("Trident/4.0; .NET CLR 1.1.4322; InfoPath.1; .NET CLR ").
				append("2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; SF/")
				.append(Global.VERSION)
				.append(")\r\n");
			}
			ret_buf.append("Accept-Encoding: gzip, deflate\r\n");
			ret_buf.append("Connection: Keep-Alive\r\n");
		}
		else /* iPhone */
		{
			if(null!=GET_HDR("User-Agent",req.getPar()))
			{
				ret_buf.append("User-Agent: Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_1 like Mac OS ")
				.append("X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 ")
				.append("Mobile/8B117 Safari/6531.22.7 SF/")
				.append(Global.VERSION)
				.append("\r\n");
				
			}
			ret_buf.append("Accept: application/xml,application/xhtml+xml,text/html;q=0.9,")
			.append("text/plain;q=0.8,image/png,*/*;q=0.5\r\n");
			if(null!=GET_HDR("Accept-Language",req.getPar()))
			{
				ret_buf.append("Accept-Language: en-us\r\n");
			}
			ret_buf.append("Accept-Encoding: gzip, deflate\r\n");
			ret_buf.append("Connection: keep-alive\r\n");
		}
		 /* Request a limited range up front to minimize unwanted traffic.
	     Note that some Oracle servers apparently fail on certain ranged
	     requests, so allowing -H override seems like a good idea. */
		if(null!=GET_HDR("Range",Global.global_http_par))
		{
			ret_buf.append("Range: bytes=0-")
			.append(Global.SIZE_LIMIT-1)
			.append("\r\n");
		}
		/* Include a dummy "Referer" header, to avoid certain XSRF checks. */
		if(null!=GET_HDR("Referer",req.getPar()))
		{
			ret_buf.append("Referer: http");
			if(req.getProto()==PROTO.PROTO_HTTPS)
				ret_buf.append("s");
			ret_buf.append("://");
			ret_buf.append(req.getHost());
			ret_buf.append("\r\n");
		}
		/* Take care of HTTP authentication next. */
		if(Global.auth_type==AUTH.AUTH_BASIC)
		{
			//暂时未处理
		}
		/* Append any other requested headers and cookies. */
		boolean ck_pos=false;
		ck_buf=new StringBuilder();
		for(int i=0;i<req.getPar().c;i++)
		{
			if(req.getPar().t.get(i)==PARAMS.PARAM_HEADER)
			{
				ck_buf.append(req.getPar().n.get(i));
				ck_buf.append(": ");
				ck_buf.append(req.getPar().v.get(i));
				ck_buf.append("\r\n");
			}
			else if(req.getPar().t.get(i)==PARAMS.PARAM_COOKIE)
			{
				if(ck_pos)
				{
					ck_buf.append(";");
				}
				ck_pos=true;
				ck_buf.append(req.getPar().n.get(i));
				ck_buf.append("=");
				ck_buf.append(req.getPar().v.get(i));
				
			}
		}
		/* Also include extra globals, if any (but avoid dupes). */
		for(int i=0;i<req.getPar().c&&i<Global.global_http_par.c;i++)
		{
		
				if(Global.global_http_par.t.get(i)==PARAMS.PARAM_HEADER&&
						null==GET_HDR(Global.global_http_par.n.get(i),req.getPar()))
				{
					ck_buf.append(Global.global_http_par.n.get(i));
					ck_buf.append(": ");
					ck_buf.append(Global.global_http_par.v.get(i));
					ck_buf.append("\r\n");
				}
				else if(Global.global_http_par.t.get(i)==PARAMS.PARAM_COOKIE&&
						null==GET_CK(Global.global_http_par.n.get(i),req.getPar()))
				{
					if(ck_pos)
					{
						ck_buf.append(";");
					}
					ck_pos=true;
					ck_buf.append(Global.global_http_par.n.get(i));
					ck_buf.append("=");
					ck_buf.append(Global.global_http_par.v.get(i));
				}
			
		}
		if(ck_pos)
		{
			ret_buf.append("Cookie: ");
			ret_buf.append(ck_buf);
			ret_buf.append("\r\n");
		}
		/* Now, let's serialize the payload, if necessary. */
		for(int i=0;i<req.getPar().c;i++)
		{
			switch(req.getPar().t.get(i))
			{
				case PARAMS.PARAM_POST_F:
				case PARAMS.PARAM_POST_O:
					req_type=req.getPar().t.get(i);
					break;
				case PARAMS.PARAM_POST:
					if(req_type==PARAMS.PARAM_NONE)
						req_type=PARAMS.PARAM_POST;
					break;	
			}
		}
		pay_buf=new StringBuilder();
		if(req_type==PARAMS.PARAM_POST)
		{
			/* The default case: application/x-www-form-urlencoded. */
			for(int i=0;i<req.getPar().c;i++)
			{
				String enc=ENC.ENC_DEFAULT;
				if(null!=req.getPivot()&&null!=req.getFuzz_par_enc()&&i==req.getPivot().fuzz_par)
					enc=req.getFuzz_par_enc();
				if(req.getPar().t.get(i)==PARAMS.PARAM_POST)
				{
					if(pay_buf.length()>0)
					{
						pay_buf.append("&");
					}
					
					if(null!=req.getPar().n.get(i))
					{
						pay_buf.append(URLEncoder.encode(req.getPar().n.get(i)));
						pay_buf.append("=");
					}
					if(null!=req.getPar().v.get(i))
					{
						pay_buf.append(URLEncoder.encode(req.getPar().v.get(i)));
						
					}
				}
			}
			ret_buf.append("Content-Type: application/x-www-form-urlencoded\r\n");
		}
		else if(req_type==PARAMS.PARAM_POST_O)
		{
			/* Opaque, non-escaped data of some sort. */
			for(int i=0;i<req.getPar().c;i++)
			{
				if(req.getPar().t.get(i)==PARAMS.PARAM_POST_O&&null!=req.getPar().v.get(i))
				{
					pay_buf.append(req.getPar().v.get(i));
				}
			}
			ret_buf.append("Content-Type: text/plain\r\n");
		}
		else if(req_type==PARAMS.PARAM_POST_F)
		{
			/* MIME envelopes: multipart/form-data */
			Random r=new Random();
			String bound="sf"+r.nextInt(10000)+1;
			for(int i=0;i<req.getPar().c;i++)
			{
				if(req.getPar().t.get(i)==PARAMS.PARAM_POST||req.getPar().t.get(i)==PARAMS.PARAM_POST_F)
				{
					pay_buf.append("--");
					pay_buf.append(bound);
					pay_buf.append("\r\n");
					pay_buf.append("Content-Disposition: form-data; name=\"");
					if(null!=req.getPar().n.get(i))
					{
						pay_buf.append(req.getPar().n.get(i));
					}
					if(req.getPar().t.get(i)==PARAMS.PARAM_POST_F)
					{
						String tmp="\"; filename=\"sfish"+r.nextInt(16)+1+Global.DUMMY_EXT+"\"\r\n"
								+"Content-Type: "+Global.DUMMY_MIME+"\r\n\r\n";
						pay_buf.append(pay_buf);
						pay_buf.append(newXssTag(Global.DUMMY_FILE));
						registerXssTag(req);
						
						
					}
					else
					{
						pay_buf.append("\"\r\n\r\n");
						if(null!=req.getPar().v.get(i))
						{
							pay_buf.append(req.getPar().v.get(i));
						}
					}
					pay_buf.append("--\r\n");
				}
			}
			pay_buf.append("--");
			pay_buf.append(bound);
			pay_buf.append("\r\n");
			ret_buf.append("Content-Type: multipart/form-data; boundary=");
			ret_buf.append(bound);
			ret_buf.append("\r\n");
			
		}
		else if(req_type==0)
			ret_buf.append("\r\n");
		/* Finalize HTTP payload... */
		for(int i=0;i<pay_buf.length();i++)
		{
			if(pay_buf.charAt(i)==0xff)
				pay_buf.setCharAt(i, (char) 0x00);
		}
		String pay=pay_buf.toString().trim();
		if(pay.length()>0)
		{
			ret_buf.append("Content-Length: "+pay.length()+"\r\n\r\n");
			ret_buf.append(pay);
		}
		/* Phew! */
		String ret=ret_buf.toString().trim();
		
		return ret;
	}
	
	/* Builds response fingerprint data. These fingerprints are used to
	   find "roughly comparable" pages based on their word length
	   distributions (divided into FP_SIZE buckets). */
	void fprintResponse(HttpResponse res)
	{
		int i,c_len=0;
		boolean in_space=false;
		res.getSig().code=res.getCode();
		for(i=0;i<res.getPay_len();i++)
		{
			if(res.getPayload().charAt(i)<=0x20||strChr("<>\"'&:\\",res.getPayload().charAt(i)))
			{
				if(in_space==false)
				{
					in_space=true;
					if(c_len>0&&++c_len<=Global.FP_MAX_LEN)
					{
						int val=res.getSig().data.charAt(c_len%Global.FP_MAX_LEN);
						val++;
						res.getSig().data.replace(res.getSig().data.charAt(c_len%Global.FP_MAX_LEN), (char) val);
						
					}
					c_len=0;
					
				}
				else
					c_len=0;
				if(res.getPayload().charAt(i)=='&')
				{
					do{i++;}
					while(i<res.getPay_len()&&(Character.isDigit(res.getPayload().charAt(i))||strChr("#;",res.getPayload().charAt(i))));
				}
			}
			else
			{
				if(in_space)
				{
					in_space=false;
					if(c_len>0&&++c_len<=Global.FP_MAX_LEN)
					{
						int val=res.getSig().data.charAt(c_len%Global.FP_MAX_LEN);
						val++;
						res.getSig().data.replace(res.getSig().data.charAt(c_len%Global.FP_MAX_LEN), (char) val);
						
					}
					c_len=0;
				}
				else
				{
					res.getSig().has_text=true;
					c_len++;
				}
			}
		}
		if(c_len>0)
		{
			int val=res.getSig().data.charAt(c_len%Global.FP_MAX_LEN);
			val++;
			res.getSig().data.replace(res.getSig().data.charAt(c_len%Global.FP_MAX_LEN), (char) val);
		}
		
	}
	/* Parses a network buffer containing raw HTTP response received over the
	   network ('more' == the socket is still available for reading). Returns 0
	   if response parses OK, 1 if more data should be read from the socket,
	   2 if the response seems invalid, 3 if response OK but connection must be
	   closed. */
	int parseResponse(HttpRequest req,HttpResponse res,LinkedList<String> read_buf,int data_len,boolean more)
	{
		String cur_line=null;
		int pay_len=-1;
		int cur_data_off=0,
			total_chunk=0,
			http_ver;
		boolean chunked=false,compressed=false,must_close=false;
		if(0!=res.getCode())
		{
			FATAL("struct HttpResponse reused! Original code "+res.getCode()+"'.");
		}
		/* First, let's do a superficial request completeness check. Be
	     prepared for a premature end at any point. */
		String line=read_buf.pop();
		
		
		
		
		return -1;
	}
	
	
	public void asyncRequest(HttpRequest req)
	{
		QueueEntry qe=null;
		HttpResponse res=null;
		if(req.getProto()==PROTO.PROTO_NONE||(null==req.getCall()))
			FATAL("uninitialized HttpRequest");
		res=new HttpResponse();
		req.setAddr(mayLookupHost(req.getHost()));
	    /* Don't try to issue extra requests if max_fail
	     consecutive failures exceeded; but still try to
	     wrap up the (partial) scan. */
		if(Global.req_errors_cur>Global.MAX_FAIL)
		{
			DEBUG("!!! Too many subsequent request failures!\n");
			res.setState(Global.STATE_SUPPRESS);
			req.getCall().call(req, res);
			/*
			 * 没处理
			 * 
			 * */
			
			
		}
		
		
		/* DNS errors mean instant fail. */
		if(null==req.getAddr())
		{
			/*
			 * 没处理
			 * 
			 * */
		}
		/* Enforce user limits. */
		/**
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		/* OK, looks like we're good to go. Insert the request
	     into the the queue. */
		
		qe=Global.queue;
		Global.queue=new QueueEntry();
		Global.queue.req=req;
		Global.queue.res=res;
		Global.queue.next=qe;
		if(Global.queue.next!=null)
			Global.queue.next.prev=Global.queue;
		
		Global.queue_cur++;
		Global.req_count++;

		
	}
	

	/* Associates a queue entry with an existing connection (if 'use_c' is
	   non-NULL), or creates a new connection to host (if 'use_c' NULL). */
	private void connAssociate(ConnEntry use_c,QueueEntry q)
	{
		ConnEntry c;
		if(use_c!=null)
		{
			c=use_c;
			c.reused=true;
		}
		else
		{
		    /* OK, we need to create a new connection list entry and connect
		       it to a target host. */
			c=new ConnEntry();
			c.proto=q.req.getProto();
			c.addr=q.req.getAddr();
			c.port=q.req.getPort();
			try
			{
				c.socket=new Socket(c.addr,c.port);
			} catch (UnknownHostException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			/* Make it official. */
			c.next=Global.conn;
			Global.conn=c;
			if(c.next!=null)
				c.next.prev=c;
			Global.conn_count++;
		}
		c.q=q;
		q.c=c;
		q.res.setState(Global.STATE_CONNECT);
		c.req_start=c.last_rw=System.currentTimeMillis();
		c.write_buf=buildRequestData(q.req);
		c.write_len=c.write_buf.length();
	}
	/* Processes the queue. Returns the number of queue entries remaining,
	   0 if none. Will do a blocking select() to wait for socket state changes
	   (or timeouts) if no data available to process. This is the main
	   routine for the scanning loop. */
	public int nextFromQueue()
	{
		long cur_time=System.currentTimeMillis();
		if(Global.conn_cur>0)
		{
			ConnEntry c=Global.conn;
			for(int i=0;i<Global.conn_count;i++)
			{
				ConnEntry next=c.next;
				
				try
				{
					//得到读写流
					OutputStream os=c.socket.getOutputStream();
					PrintWriter pw=new PrintWriter(os);
					pw.write(c.write_buf);
					pw.flush();
					c.q.res.setState(Global.STATE_SEND);
					c.last_rw=System.currentTimeMillis();
					c.socket.shutdownOutput();
					//输入流
					InputStream is=c.socket.getInputStream();
					//接收服务器的相应  
					BufferedReader br=new BufferedReader(new InputStreamReader(is));
					
					String line=null;
					int resLenth=0;
					while((line=br.readLine())!=null)
					{
						c.read_buf.add(line);
						resLenth+=line.length();
					}
					c.read_len=resLenth;
					
					
					int P_ret=parseResponse(c.q.req,c.q.res,c.read_buf,c.read_len,(c.read_len>(Global.SIZE_LIMIT+Global.READ_CHUNK)?false:true));
					
					
					
					
					
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	
	
	
	

	public static void main(String[] args)
	{
		String url="http://localhost:8080/T1/index.jsp?name=liuhg&password=liuhg";
		HttpRequest req=new HttpRequest();
		HttpRequest ref=new HttpRequest();
		HttpClient hc=new HttpClient();
		//System.out.println(hc.getFixedLenghtStr(url, 1, 4));
		hc.parseUrl(url,req,ref);
		hc.buildRequestData(req);
		//System.out.println(hc.serializePath(req, true, false));
		//System.out.println(hc.checkIP("127.0.0.100"));
//		System.out.println(hc.mayLookupHost("www.baidu.com"));
//		System.out.println(hc.mayLookupHost("www.baidu.com"));
		//System.out.println(url.substring(0,4));
//		StringBuilder sb=new StringBuilder();
//		System.out.println(sb.length());

	}
	
	
}
