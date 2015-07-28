package com.myengine.cn;

import java.util.ArrayList;
import java.util.List;

public class ParamArray implements Cloneable
{
	List<Byte> t=new ArrayList<Byte>();			/* Type  */
	List<String> n=new ArrayList<String>();		    /* Name  */
	List<String> v=new ArrayList<String>();			/* Value */			
	int   c=0;			/* Count */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
	
}
