package com.myengine.cn;

public class KwEntry
{
    private String word;                             /* Keyword itself                  */
    private int   hit_cnt;                          /* Number of confirmed sightings   */
    private boolean  is_ext;                           /* Is an extension?                */
    private boolean  hit_already;                      /* Had its hit count bumped up?    */
    private boolean  read_only;                        /* Read-only dictionary?           */
    private Type  tp;                            /* KW_*                            */
    private int total_age;                        /* Total age (in scan cycles)      */
    private int last_age;                         /* Age since last hit              */
	public String getWord()
	{
		return word;
	}
	public void setWord(String word)
	{
		this.word = word;
	}
	public int getHit_cnt()
	{
		return hit_cnt;
	}
	public void setHit_cnt(int hit_cnt)
	{
		this.hit_cnt = hit_cnt;
	}
	public boolean isIs_ext()
	{
		return is_ext;
	}
	public void setIs_ext(boolean is_ext)
	{
		this.is_ext = is_ext;
	}
	public boolean isHit_already()
	{
		return hit_already;
	}
	public void setHit_already(boolean hit_already)
	{
		this.hit_already = hit_already;
	}
	public boolean isRead_only()
	{
		return read_only;
	}
	public void setRead_only(boolean read_only)
	{
		this.read_only = read_only;
	}
	public Type getTp()
	{
		return tp;
	}
	public void setTp(Type tp)
	{
		this.tp = tp;
	}
	public int getTotal_age()
	{
		return total_age;
	}
	public void setTotal_age(int total_age)
	{
		this.total_age = total_age;
	}
	public int getLast_age()
	{
		return last_age;
	}
	public void setLast_age(int last_age)
	{
		this.last_age = last_age;
	}
	  
	  
}
