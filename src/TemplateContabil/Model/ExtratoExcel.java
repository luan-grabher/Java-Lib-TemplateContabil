package TemplateContabil.Model;

import Dates.Dates;
import TemplateContabil.Model.Entity.LctoTemplate;
import Entity.ErrorIgnore;
import JExcel.JExcel;
import Robo.View.roboView;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExtratoExcel {

    File arquivo;
    List<LctoTemplate> lctos = new ArrayList<>();
    XSSFWorkbook wk;
    XSSFSheet sheet;

    public ExtratoExcel(File arquivo) {
        this.arquivo = arquivo;
    }

    public List<LctoTemplate> getLctos() {
        return lctos;
    }

    /**
     * Adiciona os lançamentos do arquivo Excel com base nas colunas passadas.
     *
     * @param colunaData
     * @param colunaDoc
     * @param colunaPreTexto
     * @param colunaValor
     * @param colunaEntrada
     * @param colunasHistorico
     * @param colunaSaida
     */
    public void setLctos(String colunaData, String colunaDoc, String colunaPreTexto, String colunasHistorico, String colunaEntrada, String colunaSaida, String colunaValor) {
        try {
            System.out.println("Definindo workbook de " + arquivo.getName());
            wk = new XSSFWorkbook(arquivo);
            System.out.println("Definindo Sheet de " + arquivo.getName());
            sheet = wk.getSheetAt(0);

            System.out.println("Iniciando extração em " + arquivo.getName());
            setLctosFromSheet(colunaData, colunaDoc, colunaPreTexto, colunasHistorico, colunaEntrada, colunaSaida, colunaValor);
            wk.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorIgnore("Ocorreu um erro inesperado ao tentar extrair os lançamentos do arquivo " + roboView.link(arquivo));
        }
    }

    /**
     * Adiciona os lançamentos do arquivo Excel com base nas colunas passadas. A
     * sheet e workbook ja devem estar definidos
     *
     * @param colunaData
     * @param colunaDoc
     * @param colunaPreTexto Para definir um pretexto bruto ao invés de uma
     * coluna coloque "#" na frente
     * @param colunasHistorico Coloque as colunas que compoem o historico
     * separados por ";" na ordem em que aparecem. Para configuração avançada do
     * historico separe 3 vetorescom '#', no primeiro vetor coloque a coluna do
     * excel, na segunda o prefixo (pode ficar em branco), na terceira o filtro
     * regex. o prefixo e filtro regex podem ficar em branco
     * @param colunaEntrada coluna com valores de entrada
     * @param colunaSaida coluna com valores de saida, tem que colocar "-" na
     * frente caso no excel os valores apareçam positivos
     * @param colunaValor Coluna que possui valores de entrada e saida(com sinal
     * -)
     */
    private void setLctosFromSheet(String colunaData, String colunaDoc, String colunaPreTexto, String colunasHistorico, String colunaEntrada, String colunaSaida, String colunaValor) {
        if (colunaData != null && !colunaData.isBlank()
                && colunasHistorico != null && !colunasHistorico.isBlank()) {
            //Separa as colunas de historico
            String[] colunasComplemento = colunasHistorico.split(";");

            for (Row row : sheet) {
                try {
                    Cell celData = row.getCell(JExcel.Cell(colunaData));
                    //Se a celula da data existir
                    if (celData != null) {
                        String dateStr = JExcel.getStringCell(celData);                        
                        if (Dates.isDateInThisFormat(dateStr, "dd/MM/yyyy") || (!dateStr.equals("") && JExcel.isDateCell(celData))) {
                            //Converte Data se for data excel
                            if (!Dates.isDateInThisFormat(dateStr, "dd/MM/yyyy")) {
                                dateStr = JExcel.getStringDate(Integer.valueOf(dateStr));
                            }

                            String doc = "";
                            String preTexto = "";
                            String complemento = "";
                            BigDecimal value;

                            //Define o documento se tiver
                            if (colunaDoc != null && !colunaDoc.equals("")) {
                                doc = JExcel.getStringCell(row.getCell(JExcel.Cell(colunaDoc)));
                            }

                            //Define o pretexto se tiver
                            if (colunaPreTexto != null && !colunaPreTexto.equals("")) {
                                if (colunaPreTexto.contains("#")) {
                                    preTexto = colunaPreTexto.replaceAll("#", "");
                                } else {
                                    Cell cell = row.getCell(JExcel.Cell(colunaPreTexto));
                                    if (cell != null) {
                                        preTexto = JExcel.getStringCell(cell);
                                    }
                                }
                            }

                            //Define o completemento se tiver
                            if (colunasComplemento.length > 0) {
                                //Cria String Builder para fazer o Complemento
                                StringBuilder sbComplemento = new StringBuilder();
                                //Percorre todas colunas que tem
                                for (String colunaComplemento : colunasComplemento) {
                                    //Se existir uma coluna para verificar
                                    if (!colunaComplemento.equals("")) {
                                        //Divide para pegar o prefixo
                                        String[] colunaSplit = colunaComplemento.split("#");
                                        if (colunaSplit.length > 0) {
                                            String coluna = colunaSplit[0];
                                            String prefixo = colunaSplit.length > 1 ? colunaSplit[1] : "";
                                            String regex = colunaSplit.length > 2 ? colunaSplit[2] : "";

                                            //Pega celula da coluna
                                            Cell cell = row.getCell(JExcel.Cell(coluna));

                                            //Se a celula nao for nula
                                            if (cell != null) {
                                                //Pega String da celula
                                                String cellString = JExcel.getStringCell(cell);
                                                //Se nao estiver em branco e o regex estiver em branco ou a string bater com o regex
                                                if (!cellString.equals("") && ("".equals(regex) || cellString.matches(regex))) {
                                                    //Se o stringbuilder nao estiver vazio coloca - para separar
                                                    if (!sbComplemento.toString().equals("")) {
                                                        sbComplemento.append(" - ");
                                                    }
                                                    if(!prefixo.equals("")){
                                                        sbComplemento.append(prefixo).append("- ");
                                                    }

                                                    //Adiciona a string da celula
                                                    sbComplemento.append(cellString.trim());
                                                }
                                            }
                                        }
                                    }
                                }

                                complemento = sbComplemento.toString();
                            }

                            if (colunaValor == null || colunaValor.equals("")) {
                                //Pega celulas
                                Cell entryCell = row.getCell(JExcel.Cell(colunaEntrada));
                                Cell exitCell = row.getCell(JExcel.Cell(colunaSaida.replaceAll("-", "")));

                                //Cria variavel de valores
                                BigDecimal entryBD = getBigDecimalFromCell(entryCell, false);
                                BigDecimal exitBD = getBigDecimalFromCell(exitCell, colunaSaida.contains("-"));

                                value = entryBD.compareTo(BigDecimal.ZERO) == 0 ? exitBD : entryBD;
                            } else {
                                //Pega celula
                                Cell cell = row.getCell(JExcel.Cell(colunaValor.replaceAll("-", "")));

                                value = getBigDecimalFromCell(cell, colunaValor.contains("-"));
                            }

                            //Se valor for diferente de zero
                            if (value.compareTo(BigDecimal.ZERO) != 0) {
                                lctos.add(new LctoTemplate(dateStr, doc, preTexto, complemento, value));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else {
            throw new Error("A coluna de extração da data e historico não podem ficar em branco!");
        }
    }

    /**
     * Pega bigdecimal de uma celula do excel numerica
     *
     * @param cell CElula que ira pegar numero
     * @param forceNegative Se deve multiplicar por -1 o numero se for positivo
     * @return celula em número BigDecimal
     */
    private BigDecimal getBigDecimalFromCell(Cell cell, boolean forceNegative) {
        //Pega texto das celulas
        String valueString = cell != null ? JExcel.getStringCell(cell) : "0.00";
        valueString = valueString.replaceAll("[^0-9\\.,-]", "");

        //Se tiver . antes da virgula remove os pontos e coloca ponto no lugar da virgula
        if (valueString.indexOf(".") < valueString.indexOf(",")) {
            valueString = valueString.replaceAll("\\.", "").replaceAll("\\,", ".");
        }

        BigDecimal valueBigDecimal = new BigDecimal(valueString.equals("") ? "0" : valueString);

        //Se a coluna tiver que multiplicar por -1 e o valor encontrado for maior que zero
        if (forceNegative && valueBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
            valueBigDecimal = valueBigDecimal.multiply(new BigDecimal("-1"));
        }

        return valueBigDecimal;
    }

}
