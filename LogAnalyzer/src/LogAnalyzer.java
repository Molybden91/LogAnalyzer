import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Pahan on 22.03.2017.
 */

/**
 *
 */
public class LogAnalyzer {
    private static Logger log=Logger.getLogger(LogAnalyzer.class.getName());
    private static int timeDigits=3;//hours(1 value)+minuted(1 value)+seconds and it's fractions(1 value)=3
    private ArrayList<Long> inputTime=new ArrayList<>();
    private ArrayList<Long> outputTime=new ArrayList<>();
    private ArrayList<Integer> timeDelays=new ArrayList<>();
    private ArrayList<Integer> outputTypes=new ArrayList<>();
    private ArrayList<Long> inputID=new ArrayList<>();
    private ArrayList<Long> outputID=new ArrayList<>();
    public void addToInputTime(long value){
        inputTime.add(value);
    }
    public void addToOutputTime(long value){
        outputTime.add(value);
    }
    public void addToOutputTypes(int type){outputTypes.add(type);}
    public void addToInputID(long id){inputID.add(id);}
    public void addToOutputID(long id){outputID.add(id);}
    public int getTimeDigits() {
        return timeDigits;
    }

    /**
     *
     * @param s
     * @return
     */
    public static long hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        long val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }
    /**
     *
     * @param s
     * @return
     */
    public long findTime(String s){
        double [] time=new double[timeDigits];
        char [] input=s.toCharArray();
        double timeValue=0;
        StringBuilder tmp=new StringBuilder();
        tmp.append(Character.toString(input[0]));
        tmp.append(Character.toString(input[1]));
        timeValue=Double.parseDouble(tmp.toString())*3600;
        tmp=new StringBuilder(Character.toString(input[3]));
        tmp.append(Character.toString(input[4]));
        timeValue=timeValue+Double.parseDouble(tmp.toString())*60;
        tmp=new StringBuilder();
        for(int i=6;i<=14;i++){
            tmp.append(Character.toString(input[i]));
        }
        timeValue=(timeValue+Double.parseDouble(tmp.toString()))*1000000;

        return (long) timeValue;
    }

    /**
     *
     * @param s
     * @return
     */
    public int findType(String s){
        char [] currentString=s.toCharArray();
        StringBuilder result=new StringBuilder();
        int i=s.indexOf("type")+5;
        while (true){
            if(currentString[i]==','){
                break;
            }else {
                result.append(currentString[i]);
            }
            i++;
        }
        return Integer.parseInt(result.toString());
    }
    /**
     *
     * @param s
     * @return
     */
    public long findId(String s){
        char [] currentString=s.toCharArray();
        StringBuilder result=new StringBuilder();
        int i=s.indexOf("id")+3;
        while (true){
            if(currentString[i]==','){
                break;
            }else {
                result.append(currentString[i]);
            }
            i++;
        }

        return hex2decimal(result.toString());
    }
    /**
     *
     */
    public void calculateTimeDelays(){
        for(int i=0;i<inputTime.size();i++){
        }
    }
    /**
     *
     * @param fileName
     */
    public void getData(String fileName){
        try {
            Scanner scanner=new Scanner(new FileInputStream(fileName));
            while (true) {
                String currentInputString = scanner.nextLine();
                if(!scanner.hasNext()){
                    break;
                }
                if(currentInputString.contains("input")){
                    addToInputID(findId(currentInputString));
                    addToInputTime(findTime(currentInputString));
                }
                if(currentInputString.contains("output")){
                    addToOutputID(findId(currentInputString));
                    addToOutputTime(findTime(currentInputString));
                    addToOutputTypes(findType(currentInputString));
                }

            }
        }catch (IOException e){
            log.log(Level.SEVERE, "IOException:"+Thread.currentThread().getStackTrace());
            System.out.println("IOException");
        }
    }

    /**
     *
     * @param inputFileName
     */
    public static void makeTable(String inputFileName){

    }
    public static void main(String [] args)throws IOException{
        LogAnalyzer analyzer=new LogAnalyzer();
        //Scanner scanner=new Scanner(new FileInputStream("input.log"));
        //String s=scanner.nextLine();
        System.out.println(new Date());
        analyzer.getData("input.log");
        System.out.println(new Date());
        System.out.println(analyzer.inputTime.get(0));
        //analyzer.addToInputTime(analyzer.findTime(s));
        //double [] doubles=analyzer.inputTime.get(0);
        //System.out.println(doubles);
        //analyzer.addToOutputTypes(analyzer.findType(s));
        //System.out.println(analyzer.outputTypes);
    }
}
