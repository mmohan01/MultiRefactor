/***************************************************************************
                           GanttLanguageSpanish.java  -  description
                             -------------------
    begin                : jan 2003
    copyright            : (C) 2003 by Thomas Alexandre
    email                : alex.thomas@netcourrier.com
	help by              : Juan Rey juanrey@inicia.es
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/


import java.lang.String;

/**
  * Spanish class Language
  */
public class GanttLanguageSpanish extends GanttLanguage
{

	public String getLanguage() { return "Español"; }

	public String appliTitle() { return "Diagrama de Gantt"; }

	public String getMonth (int m)
	{
		switch(m)
		{
			case 0: return "Enero";
			case 1: return "Febrero";
			case 2: return "Marzo";
			case 3: return "Abril";
			case 4: return "Mayo";
			case 5: return "Junio";
			case 6: return "Julio";
			case 7: return "Augosto";
			case 8: return "Septiembre";
			case 9: return "Octubre";
			case 10:return "Noviembre";
		}
		return "Diciembre";
	}

	public String getDay (int d)
	{
		switch(d)
		{
			case 1: return "Lunes";
			case 2: return "Martes";
			case 3: return "Miercoles";
			case 4: return "Jueves";
			case 5: return "Viernes";
			case 6: return "Sábado";
		}
		return "Domingo";
	}

	public String week () { return "semana "; }

	public String getOk() { return "Ok"; }

	public String getCancel() { return "Cancelar"; }
	
	public String getYes() { return "Sí"; }
	
	public String getNo() { return "No"; }

	public String error() { return "Error"; }

	public String warning() { return "Advertencia"; }

//////////////////////////////////////////////////////////////////////////

	public String project () { return "Proyecto"; }

	public String newProject () { return "Nuevo"; }

	public String openProject () { return "Abrir"; }

	public String saveProject () { return "Guardar"; }

	public String saveAsProject () { return "Guardar como"; }
	
	public String export () { return "Export..."; }

	public String printProject () { return "Imprimir"; }
	
	public String ProjectProperties () { return "Proyecto Propiedades"; }	

	public String quit () { return "Salir"; }

//////////////////////////////////////////////////////////////////////////

	public String task () { return "Tarea"; }

	public String createTask () { return "Nueva tarea"; }

	public String deleteTask () { return "Borrar"; }

	public String propertiesTask () { return "Propiedades"; }

	public String notesTask () { return "Notas"; }

	public String upTask () { return "Arriba"; }

	public String downTask () { return "Abajo"; }

//////////////////////////////////////////////////////////////////////////

	public String human () { return "Humano"; }
	
	public String newHuman () { return "Nuevo Humano"; }

	public String deleteHuman () { return "Borrar Humano"; }

	public String propertiesHuman () { return "Humano Propiedades"; }

//////////////////////////////////////////////////////////////////////////

	public String language () { return "Idioma"; }

//////////////////////////////////////////////////////////////////////////

	public String help () { return "Ayuda"; }

	public String manual () { return "Manual"; }

	public String webPage () { return "Página Web"; }

	public String javadoc () { return "Javadoc"; }

	public String about () { return "Acerca de"; }

//////////////////////////////////////////////////////////////////////////

	public String backDate () { return "Atrás"; }

	public String forwardDate () { return "Adelante"; }

	public String zoomIn () { return "Acercar"; }

	public String zoomOut () { return "Alejar"; }

//////////////////////////////////////////////////////////////////////////

	public String propertiesFor () { return "Propiedades de "; }

	public String newTask () { return "Nueva tarea"; }

	public String notesFor () { return "Notas de "; }

	public String chooseDate () { return "seleccione una fecha"; }

	public String name () { return "Nombre"; }

	public String motherTask () { return "Subtarea de "; }

	public String none () { return "Ninguna"; }

	public String dateOfBegining () { return "Fecha de inicio"; }

	public String length () { return "Duración"; }

	public String meetingPoint () { return "Punto de sincronización"; }

	public String depends () { return "Dependencias"; }

	public String advancement () { return "Progreso"; }

	public String putDate () { return "Ponga la fecha y hora"; }

	public String propertiesMsg (String taskName) { return "Ya existe una tarea con el mismo nombre ("+taskName+")"; }
	
//////////////////////////////////////////////////////////////////////////

	public String [] getPersonFunction()
	{
		String [] res = {
			"Indefinido",
			"Encargado De Proyecto",
			"Revelador",
			"Escritor de la Doc",	 
			"Probador",
			"Diseñador Gráfico",
			"Traductor",
			"Embalador (.rpm, .tgz ...)",
			"Análisis",
			"Diseñador De la Tela",
			"Ningún Papel Específico"
		};
		return res;
	}
	
	public String [] getColsName()
	{
		String[] columnNames = {"Nombre", "Función", "Contacto" };
		return columnNames;
	}

//////////////////////////////////////////////////////////////////////////
	
	public String chef() { return "Chef"; }

	public String organization() { return "Organización"; }
	
	public String shortDescription() { return "Descripción"; }

//////////////////////////////////////////////////////////////////////////

	public String msg1() { return "¿¿ Quiere grabar el proyecto antes ??"; }

	public String msg2() { return "No se pudo abrir el fichero "; }

	public String msg3() { return "¿ Esta seguro de que quiere borrar la tarea "; }

	public String msg4() { return "No puede funcionar el comando de Netscape\n Funcione su browser en http://ganttproject.sourceforge.net"; }
	
	public String msg5() { return "No puede funcionar el comando de Netscape\n Funcione su browser en ../doc/index.html"; }
	
	public String msg6() { return "¿ Esta seguro de que quiere borrar "; }
	
}



