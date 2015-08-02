package com.myengine.cn;
import static tools.Tools.*;
import static tools.PARAMS.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tools.PARAMS;


public class Database
{

	public void loadKeywords(String fname,boolean read_only,long purge_age)
	{
		File file=new File(fname);
		BufferedReader reader=null;
		int maxSplit=5;
		List<KwEntry>kword=new ArrayList<KwEntry>();
		int lines=0;
		try
		{
			reader=new BufferedReader(new FileReader(file));
			String tmpStr=null;
			while((tmpStr=reader.readLine())!=null)
			{
				
				String []splitArr=tmpStr.split(" ",maxSplit);
				if(splitArr.length==1&&splitArr[0].equals("#ro"))
				{
					DEBUG("Found "+fname+" (readonly:"+read_only+")");
					if(!read_only)
					{
						 FATAL("Attempt to load read-only wordlist '"+fname+"' via -W (use -S instead).");
					}
					continue;
				}
				if(splitArr.length!=5)
				{
					 FATAL("Wordlist '"+fname+"': syntax error in line "+lines+".");
				}
				char type[]=splitArr[0].toCharArray();;
				int hits=Integer.parseInt(splitArr[1]);
				int total_age=Integer.parseInt(splitArr[2]);
				int last_age=Integer.parseInt(splitArr[3]);
				String word=splitArr[4];
				if(!(type[0]=='e')&&!(type[0]=='w'))
				{
					FATAL("Wordlist '"+fname+"': bad keyword type in line"+(lines+1)+".");
				}
				Type tp=Type.KW_GEN_AUTO;
				if(type[1]=='s')
				{
					tp=Type.KW_SPECIFIC;
				}
				else if(type[1]=='g')
				{
					tp=Type.KW_GENERIC;
				}
				
				if (last_age< purge_age)
				{
					boolean isContain=false;
					for(int i=0;i<kword.size();i++)
					{
						KwEntry entry=kword.get(i);
						 /* Check if this is a known keyword. */
						if(entry.getWord().equals(word))
						{
							isContain=true;
							if(!entry.isHit_already())
							{
								entry.setHit_cnt(entry.getHit_cnt()+hits);
								entry.setHit_already(true);
								entry.setLast_age(0);
								if(!entry.isRead_only()&&read_only)
								{
									entry.setRead_only(true);
								}
								
							}
							if(!entry.isIs_ext()&&type[0]=='e')
							{
								entry.setIs_ext(true);
								/**************
								 * 
								 */
								
							}
							kword.set(i, entry);	
						}
				
						
					}
					/* Word not known . Create a new wordlist entry. */
					if(!isContain)
					{
						KwEntry entry=new KwEntry();
						entry.setWord(word);
						entry.setIs_ext(type[0]=='e');
						entry.setTp(tp);
						entry.setRead_only(read_only);
						entry.setHit_cnt(hits);
						entry.setTotal_age(total_age);
						entry.setLast_age(last_age);
						/* If this is a new keyword (not loaded from file), mark it as hit to
					     avoid inflating hit_cnt. */
		
						if(type[0]=='e')
						{
							/****
							 * doSomething
							 */
						}
						kword.add(entry);
					}
					
				}
				
				lines++;
			}
			DEBUG("* Read "+lines+"lines from dictionary '"+fname+"' (read-only = "+read_only+").");
			reader.close();
			
			for(int i=0;i<kword.size();i++)
			{
				System.out.println(kword.get(i).getWord());
			}
			

		} catch (IOException e)
		{
			if(read_only)
			{
				FATAL("Unable to open read-only wordlist "+fname);
			}
			else
			{
				FATAL("Unable to open read-write wordlist "+fname+" (see dictionaries/README-FIRST).");
			}
		}
		finally
		{
			if(reader!=null)
			{
				try
				{
					reader.close();
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
	boolean urlAllowed(HttpRequest req)
	{
		return true;
	}
	/* Maps a parsed URL (in req) to the pivot tree, creating or modifying nodes
	   as necessary, and scheduling them for crawl. This should be called only
	   on requests that were *not* yet retrieved. */
	public void maybeAddPivot(HttpRequest req,HttpResponse res,boolean via_link)
	{
		PivotDesc cur=null;
		int i,par_cnt=0,path_cnt=0,last_val_cnt=0,pno;
		boolean ends_with_slash=false;
		String last_val=null;
		if(req==null)
		{
			FATAL("Invalid request data.");
		}
		HttpClient hc=new HttpClient();
		String url= hc.serializePath(req, true, true);
		DEBUG("--- New pivot requested: "+url+" ("+via_link+")");
		/* Initialize root pivot if not done already. */
		if(Global.root_pivot==null)
		{
			Global.root_pivot=new PivotDesc();
			Global.root_pivot.type=Global.PIVOT_ROOT;
			Global.root_pivot.state=Global.PSTATE_DONE;
			Global.root_pivot.linked=2;
			Global.root_pivot.fuzz_par=-1;
			Global.root_pivot.name="[root]";
		}
		if(false==urlAllowed(req))
		{
			Global.url_scope++;
			return;
		}
		/* Count the number of path and query parameters in the request. */
		for(i=0;i<req.getPar().c;i++)
		{
			if(PARAMS.QUERY_SUBTYPE(req.getPar().t.get(i))||PARAMS.POST_SUBTYPE(req.getPar().t.get(i)))
				par_cnt++;
			if(PARAMS.PATH_SUBTYPE(req.getPar().t.get(i)))
			{
				if(req.getPar().t.get(i)==PARAMS.PARAM_PATH
						&&req.getPar().n.size()>i
						&&req.getPar().v.size()>i
						&&Character.isWhitespace(req.getPar().v.get(i).charAt(0))==false)
				{
					ends_with_slash=true;
				}
				else
				{
					ends_with_slash=false;
				}
				if(req.getPar().v.get(i)!=null)
					last_val=req.getPar().v.get(i);
				path_cnt++;
				
			}
			 /* While we're at it, try to learn new keywords. */
			if(PARAMS.PATH_SUBTYPE(req.getPar().t.get(i))||PARAMS.QUERY_SUBTYPE(req.getPar().t.get(i)))
			{
				if(req.getPar().n.get(i)!=null)
				{
					wordlistConfirmWord(req.getPar().n.get(i));
				}
				wordlistConfirmWord(req.getPar().v.get(i));
			}
			 /* Try to find pivot point for the host. */
			for(i=0;i<Global.root_pivot.child_cnt;i++)
			{
				cur=Global.root_pivot.child.get(i);
				if(casePrefix(cur.req.getHost(),req.getHost()))
				{
					
				}
			}
			
			
		}
	

	}
	
	private void wordlistConfirmWord(String string)
	{
		// TODO Auto-generated method stub
		
	}


	public static void main(String[] args)
	{
		Database db=new Database();
		//db.loadKeywords("dictionaries/complete.wl", true, 2);
		System.out.println();
	}
	
}
