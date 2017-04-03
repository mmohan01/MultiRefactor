
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */



import java.lang.String;
import java.util.Calendar;
import java.lang.reflect.Array;
import java.io.*;


/** 
  * Cette classe utilise les fonctionnelites de Calendar mais avec certaines en plus.
  */
public class GanttCalendar implements Serializable
{
	/** L'objet Calendar de java. */
	private Calendar cal;

	/** Tableau pour associer un moi a son nombre de jours. */
	private static final int dayPerMonth[] = new int [12];

	/** Constructeur par defaut. On prend la date courante. */
	public GanttCalendar () 
	{
		cal = Calendar.getInstance(); 
		
		dayPerMonth[0] = 31;	dayPerMonth[1] = 28;
		dayPerMonth[2] = 31;	dayPerMonth[3] = 30;
		dayPerMonth[4] = 31;	dayPerMonth[5] = 30;
		dayPerMonth[6] = 31;	dayPerMonth[7] = 31;
		dayPerMonth[8] = 30;	dayPerMonth[9] = 31;
		dayPerMonth[10] = 30;	dayPerMonth[11] = 31;
	}
	
	/** Constructeur a partir d'une annee, d'un moi et d'un jour. */
	public GanttCalendar (int year, int month, int date)
	{
		cal = Calendar.getInstance();
		cal.set(year, month, date);
	}

	
	/** 	/** Constructeur a partir d'une String sous la forme ( JJ/MM/AAAA).*/
	public GanttCalendar (String s)
	{
		cal = Calendar.getInstance();
		
		//On recupere les caracteres "/"
		int fb=s.indexOf('/');
		int sb=s.indexOf('/',fb+1);
		//On recupere la valeur des differents champs.
		String d = s.substring(0,fb);
		String m = s.substring(fb+1,sb);
		String y = s.substring(sb+1);
		
		//On modifie la date.
		cal.set(new Integer(y).hashCode(), new Integer(m).hashCode()-1, new Integer(d).hashCode());
	}

	/** Constructeur par copie. */
	public GanttCalendar (GanttCalendar g)
	{
		cal = Calendar.getInstance();
		cal.set(g.getYear(), g.getMonth(), g.getDate());
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Cette fonction retourne un clone de l'objet GanttCalendar. */
	public GanttCalendar Clone()
	{
		GanttCalendar clone = new GanttCalendar(getYear(), getMonth(), getDay());
		return clone;
	}

	/**   Cette fontion retourne la date sous forme de String. */
	public String toString ()
	{
		return (getDate() + "/" + (getMonth()+1) + "/" + getYear());
	}


	/** Acceseur sur l'annee de la date. */
	public int getYear() { return cal.get(Calendar.YEAR); }

	/** Acceseur sur le moi de la date. */
	public int getMonth() { return cal.get(Calendar.MONTH); }

	/** Acceseur sur le jour de la date. */
	public int getDate() { return cal.get(Calendar.DATE); }

	/** Acceseur sur le jour de la date. */
	public int getDay() { return cal.get(Calendar.DAY_OF_MONTH ); }
	
	/** Accesseur sur le numero de jour dans la semaine */
	public int getDayWeek() { return cal.get(Calendar.DAY_OF_WEEK ); }
	
	/** Acceseur sur la semaine de la date */
	public int getWeek() { return cal.get(Calendar.WEEK_OF_YEAR ); }
	
	/** Retourne le tableau avec les noms des mois */
	public String [] getDayMonthLanguage(GanttLanguage l) 
	{
		String [] res = new String [12];
		for(int i=0;i<12;i++)
			res[i] = l.getMonth(i);
		return res; 
	}
	
	/** Retourne le tableau avec les noms des jours de la semaine */
	public String [] getDayWeekLanguage(GanttLanguage l) 
	{
		String [] res = new String [7];
		for(int i=0;i<7;i++)
			res[i] = l.getDay(i);
		return res; 
	}
	
	
	/** Cette fonction modifie l'annee de la date. */
	public void setYear(int y) { cal.set(Calendar.YEAR, y); }

	/** Cette fonction modifie le moi de la date. */
	public void setMonth(int m) { cal.set(Calendar.MONTH, m); }

	/** Cette fonction modifie le jour de la date. */
	public void setDay(int d) { cal.set(Calendar.DAY_OF_MONTH, d); }
	
	

	/** Cette fonction permet d'ajouter (ou de soustraire) un nombre de jour a la date. */
	public void add (int dayNumber)
	{
		cal.add(Calendar.DATE, dayNumber);
	}
	
	/** Cette fonction ajoute à nombre de jour à une nouvelle date pris a partir de notre date */
	public GanttCalendar newAdd (int dayNumber)
	{
		GanttCalendar gc = new GanttCalendar(getYear(), getMonth(), getDate());
		gc.cal.add(Calendar.DATE, dayNumber);
		return gc;
	}

	/** Fonction qui retourne la difference en nombre de jour entre 2 jours */
	public int diff( GanttCalendar d )
	{
		int res=0;
		GanttCalendar d1;
		GanttCalendar d2;
		
		if(this.compareTo(d)==0) return res;
		
		else if(compareTo(d)<0) {
			d1 = this.Clone();
			d2 = new GanttCalendar(d);
		}
		else {
			d1 = new GanttCalendar(d);
			d2 = this.Clone();
		}

		while(d1.compareTo(d2)!=0)
		{
			d1.add(1);
			res++;
		}
		return res;
	}
	
	/** Cette fontion retourne la String correspondant au moi. */
	String getdayMonth(GanttLanguage l) { return l.getMonth(getMonth()); }
	
	/** Cette fontion retourne la String correspondant au jour */
	String getdayWeek(GanttLanguage l) {return l.getDay(cal.get(Calendar.DAY_OF_WEEK)-1);}
	
	/** Cette fontion retourne le nombre de jour dans le moi. */
	public int getNumberOfDay() 
	{
		if(getMonth()==1)
			return (getYear()%4==0)?29:28; 	
		return dayPerMonth[getMonth()]; 
	}
	
	/** Cette fontion permet de comparer la date a la date when passe en parametre 
	  * @return 0 si les deux dates son egales.
	  * @return -1 si la date est avant when.
	  * @return 1 si la date est apres when.
	  */
	public int compareTo (GanttCalendar when)
	{
		if((getYear() == when.getYear()) &&
		   (getMonth() == when.getMonth()) && 
		   (getDate() == when.getDate())) return 0;
		
		if(getYear() < when.getYear()) return -1;
		if(getYear() > when.getYear()) return  1;
		
		if(getMonth() < when.getMonth()) return -1;
		if(getMonth() > when.getMonth()) return  1;
		
		if(getDate() < when.getDate()) return -1;
		if(getDate() > when.getDate()) return  1;
		
		System.out.println("Ce message ne doit pas apparaitre .....");
		return 0;
	}
	
	/** Modifie la date vers le moi suivant. */
	public void goNextMonth() { cal.add(Calendar.MONTH,1); }
	
	/** Modifie la date vers le moi precedent. */	
	public void goPrevMonth() { cal.add(Calendar.MONTH,-1); }
		
	/**Cette fonction sert dans l'affichage graphique lorsque on veut passer au champ suivant */	
	public void go (int field, int value) { cal.add(field,value);	}
	
	
	/* Retourne la date actuelle ainsi que l'heure */
	public static String getDateAndTime()
	{		
		GanttCalendar c= new GanttCalendar();		
		return c.toString() + " - " +c.cal.get(Calendar.HOUR_OF_DAY)
				+":"+c.cal.get(Calendar.MINUTE)+":"+c.cal.get(Calendar.SECOND);
	}
}



