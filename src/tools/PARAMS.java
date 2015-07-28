package tools;
/* Flags for http_request parameter list entries: */
public class PARAMS
{
	public final static int PARAM_NONE=0;	/* Empty parameter slot         */
	public final static int PARAM_PATH=10;  /* Path or parametrized path    */
	public final static int PARAM_PATH_S=11; /* - Semicolon element          */
	public final static int PARAM_PATH_C=12; /* - Comma element              */
	public final static int PARAM_PATH_E=13;/* - Exclamation mark element   */
	public final static int PARAM_PATH_D=14; /* - Dollar sign element        */
	public final static int PARAM_QUERY=20;/* Query parameter              */
	public final static int PARAM_QUERY_S=21; /* - Semicolon element          */
	public final static int PARAM_QUERY_C=22;/* - Comma element              */
	public final static int PARAM_QUERY_E=23;  /* - Exclamation mark element   */
	public final static int PARAM_QUERY_D=24;/* - Dollar sign element        */
	public final static int PARAM_POST=50; /* Post parameter               */
	public final static int PARAM_POST_F=51; /* - File field                 */
	public final static int PARAM_POST_O=52; /* - Non-standard (e.g., JSON)  */
	public final static int PARAM_HEADER=100;/* Generic HTTP header          */
	public final static int PARAM_COOKIE=101; /* - HTTP cookie                */
	
	public static boolean HEADER_SUBTYPE(int _x)
	{
		return _x>=PARAM_HEADER;	
	}
	public static boolean POST_SUBTYPE(int _x)
	{
		return _x>PARAM_POST&&_x<PARAM_HEADER;
	}
	public static boolean QUERY_SUBTYPE(int _x)
	{
		return ((_x) >= PARAM_QUERY && (_x) < PARAM_POST);
	}
	public static boolean PATH_SUBTYPE(int _x)
	{
		return ((_x) >= PARAM_PATH && (_x) < PARAM_QUERY);
	}
}
