package com.myengine.cn;
import static tools.Tools.strChr;
import static tools.Tools.strCspn;
import static tools.Tools.strPos;
import static tools.Tools.strSpn;
import static tools.Tools.getFixedLenghtStr;
import java.net.URLDecoder;

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
						req.getPar().t.set(i, (byte)PARAMS.PARAM_NONE);
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

		      case ';': setValue((byte)PARAMS.PARAM_QUERY_S, name, value, -1, req.getPar()); break;
		      case ',': setValue((byte)PARAMS.PARAM_QUERY_C, name, value, -1, req.getPar()); break;
		      case '!': setValue((byte)PARAMS.PARAM_QUERY_E, name, value, -1, req.getPar()); break;
		      case '$': setValue((byte)PARAMS.PARAM_QUERY_D, name, value, -1, req.getPar()); break;
		      default: setValue((byte)PARAMS.PARAM_QUERY, name, value, -1, req.getPar());

		    }
		    if(cur.length()>next_seg)
		    	cur=cur.substring(next_seg);
		    else
		    	break;
		}
		
		
	}
	/* Inserts or overwrites parameter value in param_array. If offset
	   == -1, will append parameter to list. Duplicates strings,
	   name and val can be NULL. */
	void setValue(byte type,String name,String val,long offset,ParamArray par)
	{
		int i,coff=0,matched=-1;
		/* If offset specified, try to find an entry to replace. */
		if(offset>=0)
		{
			for(i=0;i<par.c;i++)
			{
				if(type!=par.t.get(i))
					continue;
				if(null!=name&&(null!=par.n||!name.equalsIgnoreCase(par.n.get(i))))
					continue;
				if(offset!=coff)
				{
					coff++;
					continue;
				}
			    matched = i;
				break;
			}
		}
		if(matched==-1)
		{
			 /* No offset or no match - append to the end of list. */
			par.t.add( par.c, type);
			par.n.add(par.c, name);
			par.v.add(par.c, val);
			par.c++;
			
		}
		else
		{
			 /* Matched - replace name & value. */
			par.n.set(matched, name);
			par.v.set(matched, val);
			
			
		}
	}
	/* Reconstructs URI from httpRequest data. Includes protocol and host
	   if with_host is non-zero. */
	
	
	public static void main(String[] args)
	{
		String url="http://localhost:8080/T1/index.jsp?name=liuhg&password=liuhg";
		HttpRequest req=new HttpRequest();
		HttpRequest ref=new HttpRequest();
		HttpClient hc=new HttpClient();
		//System.out.println(hc.getFixedLenghtStr(url, 1, 4));
		hc.parseUrl(url,req,ref);
		//System.out.println(url.substring(0,4));

	}
	
	
}
