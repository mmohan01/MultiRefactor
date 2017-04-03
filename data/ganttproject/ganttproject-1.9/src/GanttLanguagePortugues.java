/***************************************************************************
                           GanttLanguagePortugues.java  -  description
                             -------------------
    begin                : dec 2002
    copyright            : (C) 2002 by Thomas Alexandre
    email                : alex.thomas@netcourrier.com
	help by              : Nelson Ferraz  nferraz@phperl.com
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
  * Portugues class Language
  */
public class GanttLanguagePortugues extends GanttLanguage
{

	public String getLanguage() { return "Portugues"; }

	public String appliTitle() { return "GanttProject"; }

	public String getMonth (int m)
	{
		switch(m)
		{
			case 0: return "Janeiro";
			case 1: return "Fevereiro";
			case 2: return "Março";
			case 3: return "Abril";
			case 4: return "Maio";
			case 5: return "Junho";
			case 6: return "Julho";
			case 7: return "Agosto";
			case 8: return "Setembro";
			case 9: return "Outubro";
			case 10:return "Novembro";
		}
		return "Dezembro";
	}

	public String getDay (int d)
	{
		switch(d)
		{
			case 1: return "Segunda";
			case 2: return "Terça";
			case 3: return "Quarta";
			case 4: return "Quinta";
			case 5: return "Sexta";
			case 6: return "Sábado";
		}
		return "Domingo";

	}

	public String week () { return "Semana"; }

	public String getOk() { return "Ok"; }

	public String getCancel() { return "Cancelar"; }
	
	public String getYes() { return "Sim"; }
	
	public String getNo() { return "Não"; }

	public String error() { return "Erro"; }

	public String warning() { return "Atenção"; }

//////////////////////////////////////////////////////////////////////////

	public String project () { return "Projeto"; }

	public String newProject () { return "Novo"; }

	public String openProject () { return "Abrir"; }

	public String saveProject () { return "Salvar"; }

	public String saveAsProject () { return "Salvar como"; }
	
	public String export () { return "Exportar..."; }

	public String printProject () { return "Imprimir..."; }

	public String ProjectProperties () { return "Propriedades..."; }

	public String quit () { return "Sair"; }

//////////////////////////////////////////////////////////////////////////

	public String task () { return "Tarefa"; }

	public String createTask () { return "Nova tarefa"; }

	public String deleteTask () { return "Deletar"; }

	public String propertiesTask () { return "Propriedades..."; }

	public String notesTask () { return "Notas"; }

	public String upTask () { return "Acima"; }

	public String downTask () { return "Abaixo"; }

//////////////////////////////////////////////////////////////////////////
	
	public String human () { return "Pessoa"; }
	
	public String newHuman () { return "Nova pessoa"; }

	public String deleteHuman () { return "Deletar pessoa"; }

	public String propertiesHuman () { return "Propriedades..."; }

//////////////////////////////////////////////////////////////////////////

	public String language () { return "Linguagem"; }

//////////////////////////////////////////////////////////////////////////

	public String help () { return "Ajuda"; }

	public String manual () { return "Manual"; }

	public String webPage () { return "Página na web"; }

	public String javadoc () { return "Javadoc"; }

	public String about () { return "Sobre..."; }

//////////////////////////////////////////////////////////////////////////

	public String backDate () { return "Anterior"; }

	public String forwardDate () { return "Próximo"; }

	public String zoomIn () { return "Aumentar zoom"; }

	public String zoomOut () { return "Reduzir zoom"; }

//////////////////////////////////////////////////////////////////////////

	public String propertiesFor () { return "Propriedades para "; }

	public String newTask () { return "Nova tarefa"; }

	public String notesFor () { return "Notas para "; }

	public String chooseDate () { return "Escolha uma data"; }

	public String name () { return "Nome"; }

	public String motherTask () { return "Tarefa-mãe"; }

	public String none () { return "Nenhuma"; }

	public String dateOfBegining () { return "Data de início"; }

	public String length () { return "Duração"; }

	public String meetingPoint () { return "Ponto de encontro"; }

	public String depends () { return "Dependências"; }

	public String advancement () { return "Avanço"; }

	public String putDate () { return "Coloque a data e hora"; }

	public String propertiesMsg (String taskName) { return "Uma tarefa já possui esse nome ("+taskName+")"; }

//////////////////////////////////////////////////////////////////////////

	public String [] getPersonFunction()
	{
		String [] res = {
			"Indefinido",
			"Gerente de Projeto",
			"Desenvolvedor",
			"Escritor de documentação",	 
			"Testador",
			"Designer Gráfico",
			"Tradutor de documentação",
			"Empacotador (.rpm, .tgz ...)",
			"Analista",
			"Web Designer",
			"Sem papel específico"
		};
		return res;
	}
	
	public String [] getColsName()
	{
		String[] columnNames = {"Nome", "Função", "Contato" };
		return columnNames;
	}

//////////////////////////////////////////////////////////////////////////
	
	public String chef() { return "Chef"; }

	public String organization() { return "Organização"; }
	
	public String shortDescription() { return "Descrição"; }

//////////////////////////////////////////////////////////////////////////

	public String msg1() { return "Você gostaria de salvar o projeto antes??"; }

	public String msg2() { return "Não consegui abrir o arquivo "; }

	public String msg3() { return "Deseja realmente deletar a tarefa "; }

	public String msg4() { return "Não consegui abrir o netscape.\n Abra seu browser em http://ganttproject.sourceforge.net"; }

	public String msg5() { return "Não consegui abrir o netscape.\n Abra seu browser em ../doc/index.html"; }
	
	public String msg6() { return "Deseja realmente deletar a pessoa "; }
	
}




