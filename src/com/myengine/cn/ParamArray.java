package com.myengine.cn;

import java.util.ArrayList;
import java.util.List;

public class ParamArray implements Cloneable
{
	public List<Integer> t=new ArrayList<Integer>();			/* Type  */
	public List<String> n=new ArrayList<String>();		    /* Name  */
	public List<String> v=new ArrayList<String>();			/* Value */			
	public int   c=0;			/* Count */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
	
}
