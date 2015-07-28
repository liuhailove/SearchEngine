package tools;

public class Tools
{
	public static void FATAL(String ...args)
	{
		System.out.println("-----------------FATAL----------------");
		System.out.println(System.err);
		for(int i=0;i<args.length;i++)
		{
			System.out.print(args[i]);
		}
		System.out.println();
		System.exit(1);
	}
	public static void DEBUG(String ...args)
	{
		System.out.println("-----------------DEBUG----------------");
		for(int i=0;i<args.length;i++)
		{
			System.out.print(args[i]);
		}
		System.out.println();
	}
	public static int strPos(String str,String search)
	{
		char[]sh=search.toCharArray();
		for(int i=0;i<sh.length;i++)
		{
			if(-1!=str.indexOf(sh[i]))
			{
				return str.indexOf(sh[i]);
			}
		}
		return -1;
	}
	/**
	 * 函数说明 strspn()从参数s 字符串的开头计算连续的字符，而这些字符都完全是accept 所指字符串中的字符。简单的说，若strspn()返回的数值为n，
	 * 则代表字符串s 开头连续有n 个字符都是属于字符串accept内的字符
	 * return:返回值 返回字符串s开头连续包含字符串accept内的字符数目
	 */
	public static int strSpn( String s, String accept)
	{
		char t[]=s.toCharArray();
		char a[]=accept.toCharArray();
		int count=0;
		for(int i=0;i<t.length;i++)
		{
			if(i>=a.length)
			{
				return count;
			}
			int j=0;
			for(;j<a.length;j++)
			{
				if(t[i]==a[j])
				{
					break;
				}
				
			}
			if(j==a.length)
			{
				return count;
			}
			++count;
			
		}
		return count;
	}
	/**
	 * 功能：查找字符串s中首次出现字符c的位置
	 * 说明：如果存在返回真，否则返回假。
	 * @param args
	 */
	public static boolean  strChr(String s,char c)
	{
		for(int i=0;i<s.length();i++)
		{
			if(s.charAt(i)==c)
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * 函数说明：strcspn()从参数s 字符串的开头计算连续的字符, 而这些字符都完全不在参数reject 所指的字符串中.
	 * 简单地说, 
	 * 若strcspn()返回的数值为n, 则代表字符串s 开头连续有n 个字符都不含字符串reject 内的字符.
	 * @param args
	 */
	public static int strCspn(String str,String reject)
	{
		char s[]=str.toCharArray();
		char r[]=reject.toCharArray();
		int i=0;
		for(;i<s.length;i++)
		{
			int j=0;
			for(;j<r.length&&s[i]!=r[j];j++)
			{
			}
			if(j!=r.length)
			{
				return i;
			}
		}
		return i;	
	}
	/**
	 * 
	 * @param str 操纵的字符串
	 * @param beginIndex 开始索引
	 * @param length 要得到字符串的长度
	 * @return 固定长度的字符串
	 */
	public static String getFixedLenghtStr(String str,int beginIndex,int length)
	{
		if(null==str)
			return null;
		if (beginIndex < 0) {
		    throw new StringIndexOutOfBoundsException(beginIndex);
		}
		if (length > str.length()) {
		    throw new StringIndexOutOfBoundsException(length);
		}
		if (beginIndex+length>str.length()) {
		    throw new StringIndexOutOfBoundsException(beginIndex+length);
		}

		char s[]=str.toCharArray();
		char  d[]=new char[length];
		System.arraycopy(s, beginIndex, d, 0, length);
		return  new String(d);
		   
	}
	public static void main(String[] args)
	{
		String str="Linux was first developed for 386/486-based pcs.";
		System.out.println(strCspn(str,"/-"));
		
		
	}
	
}
