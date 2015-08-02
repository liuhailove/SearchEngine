package tools;

import java.util.Random;

import com.myengine.cn.Global;
import com.myengine.cn.HttpRequest;
import com.myengine.cn.HttpResponse;
import com.myengine.cn.HttpSig;
import com.myengine.cn.ParamArray;
import com.myengine.cn.PivotDesc;

public class Common
{

	public static String newXssTag(String prefix)
	{
		String ret=null;
		if(Global.scan_id==0)
		{
			Random r=new Random();
			Global.scan_id=r.nextInt(999999)+1;
		}
		ret=prefix!=null?prefix:""+"-->\">'>'\"<sfi"+Global.cur_xss_id+"v"+Global.scan_id+">";
		return ret;
	}
	/* Registers last XSS tag along with a completed HttpRequest */
	public static  void registerXssTag(HttpRequest req)
	{
		Global.xss_req.set(Global.cur_xss_id, reqCopy(req,null,true));
		Global.cur_xss_id++;
	}
	/* Inserts or overwrites parameter value in param_array. If offset
	   == -1, will append parameter to list. Duplicates strings,
	   name and val can be NULL. */
	public static void setValue(int type,String name,String val,long offset,ParamArray par)
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
	/* Extracts parameter value from ParamArray. Name is matched if
	   non-NULL. Returns pointer to value data, not a duplicate string;
	   NULL if no match found. */
	public static String getValue(int type,String name,int offset,ParamArray par)
	{
		int i,coff=0;
		for(i=0;i<par.c;i++)
		{
			if(type!=par.t.get(i))
			{
				continue;
			}
			if(null!=name&&(null!=par.n.get(i))&&(name.equalsIgnoreCase(par.n.get(i))))
			{
				continue;
			}
			if(offset!=coff)
			{
				coff++;
				continue;
			}
			return par.v.get(i);
		}
		return null;
	}
	
	
	
	public static HttpRequest reqCopy(HttpRequest req, PivotDesc pv, boolean all)
	{
		HttpRequest ret=null;
		int i;
		if(null==req)
			return null;
		ret=new HttpRequest();
		
		ret.setProto(req.getProto());
		if(all)
			ret.setMethod(req.getMethod());
		else
			ret.setMethod("GET");
		ret.setHost(req.getHost());
		ret.setAddr(req.getAddr());
		ret.setPort(req.getPort());
		ret.setPivot(pv);
		ret.setUser_val(req.getUser_val());
		/* Copy all the requested data. */
		for(i=0;i<req.getPar().c;i++)
		{
			if(all||PARAMS.HEADER_SUBTYPE(req.getPar().t.get(i)))
			{
				setValue(req.getPar().t.get(i),req.getPar().n.get(i),req.getPar().v.get(i),-1,ret.getPar());
			}
		}
		try
		{
			ret.setSame_sig((HttpSig)req.getSame_sig().clone());
		} catch (CloneNotSupportedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	/* Gets the request that submitted the tag in the first place */
	public static HttpRequest getXssRequest(int xid,int sid)
	{
		if(sid!=Global.scan_id||xid>=Global.cur_xss_id)
			return null;
		return Global.xss_req.get(xid);
	}
	/* Creates a copy of a response. */
	public static HttpResponse resCopy(HttpResponse res)
	{
		HttpResponse ret=null;
		int i;
		if(null==res)
			return null;
		ret=new HttpResponse();
		ret.setState(res.getState());
		ret.setCode(res.getCode());
		ret.setMsg(res.getMsg());
		ret.setWarn(res.getWarn());
		for(i=0;i<res.getHdr().c;i++)
		{
			setValue(res.getHdr().t.get(i),res.getHdr().n.get(i),res.getHdr().v.get(i),-1,ret.getHdr());
		}
		ret.setPay_len(res.getPay_len());
		if(res.getPay_len()>0)
		{
			ret.setPayload(res.getPayload());
		}
		try
		{
			ret.setSig((HttpSig)res.getSig().clone());
		} catch (CloneNotSupportedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret.setSniff_mime_id(res.getSniff_mime_id());
		ret.setDecl_mime_id(res.getDecl_mime_id());
		ret.setDoc_type(res.getDoc_type());
		ret.setDoc_type(res.getDoc_type());
		ret.setCss_type(res.getCss_type());
		ret.setJs_type(res.getJs_type());
		ret.setJson_safe(res.getJson_safe());
		ret.setStuff_checked(res.getStuff_checked());
		ret.setScraped(res.getScraped());
		if(null!=res.getMeta_charset())
		{
			ret.setMeta_charset(res.getMeta_charset());
		}
		if(null!=res.getHeader_charset())
		{
			ret.setHeader_charset(res.getHeader_charset());
		}
		if(null!=res.getHeader_mime())
		{
			ret.setHeader_mime(res.getHeader_mime());
		}
		ret.setSniffed_mime(res.getSniffed_mime());
		
		return ret;
	}
	
}
