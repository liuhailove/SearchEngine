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
	 * ����˵�� strspn()�Ӳ���s �ַ����Ŀ�ͷ�����������ַ�������Щ�ַ�����ȫ��accept ��ָ�ַ����е��ַ����򵥵�˵����strspn()���ص���ֵΪn��
	 * ������ַ���s ��ͷ������n ���ַ����������ַ���accept�ڵ��ַ�
	 * return:����ֵ �����ַ���s��ͷ���������ַ���accept�ڵ��ַ���Ŀ
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
	 * ���ܣ������ַ���s���״γ����ַ�c��λ��
	 * ˵����������ڷ����棬���򷵻ؼ١�
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
	 * ����˵����strcspn()�Ӳ���s �ַ����Ŀ�ͷ�����������ַ�, ����Щ�ַ�����ȫ���ڲ���reject ��ָ���ַ�����.
	 * �򵥵�˵, 
	 * ��strcspn()���ص���ֵΪn, ������ַ���s ��ͷ������n ���ַ��������ַ���reject �ڵ��ַ�.
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
	 * @param str ���ݵ��ַ���
	 * @param beginIndex ��ʼ����
	 * @param length Ҫ�õ��ַ����ĳ���
	 * @return �̶����ȵ��ַ���
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
