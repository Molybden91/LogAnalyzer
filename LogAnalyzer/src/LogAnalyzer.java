import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Pahan on 22.03.2017.
 */

/**
 * Main class for analyzing log files.
 */
public class LogAnalyzer {
    private static Logger log=Logger.getLogger(LogAnalyzer.class.getName());
    private long outputCount=0;
    private long inputCount=0;
    private static int timeDigits=3;//hours(1 value)+minuted(1 value)+seconds and it's fractions(1 value)=3
    private ArrayList<Long> inputTime=new ArrayList<>();
    private ArrayList<Long> outputTime=new ArrayList<>();
    private ArrayList<Long> timeDelays=new ArrayList<>();
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
     * Method to convert hexadecimal number in form of string in form of decimal long number.
     * @param s- input number in hex form
     * @return decimal number converted from hexadecimal
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
        log.fine("hexadecimal number was converted to decimal");
        return val;
    }

    /**
     * Method that helps to load data from initial log file. Uses fastest methodology- file mapping through NIO library.
     * @param fileName- the name of input file
     * @return list of strings contained in the initial file
     * @throws IOException
     */
    public ArrayList<StringBuilder> loadFile(String fileName)throws IOException{
        long start=new Date().getTime();
        log.info("Starting to load date from input file:"+fileName);
        File file=new File(fileName);
        FileChannel fileChannel=new RandomAccessFile(file,"r").getChannel();
        MappedByteBuffer buffer=fileChannel.map(FileChannel.MapMode.READ_ONLY,0,fileChannel.size());
        log.info("Buffer capacity:"+buffer.capacity());
        ArrayList <StringBuilder> result=new ArrayList<>();
        StringBuilder tmp=new StringBuilder();
        try {
            while (buffer.hasRemaining()){
                byte read= buffer.get();
                if((char)read=='\n'){
                    result.add(tmp);
                    tmp=new StringBuilder();
                }else {
                    tmp.append((char) read);
                }
            }
        }catch (BufferUnderflowException e){
            log.log(Level.SEVERE,"BufferUnderFlowException:"+Thread.currentThread().getStackTrace());
            System.out.println("BufferUnderFlowException:");
        }finally {
            fileChannel.close();
        }
        long end=new Date().getTime();
        log.info("Data was loaded. Consumed time in second:"+(start-end)/1000);
        return result;
    }
    /**
     * Method to find and collect time of input or output from String
     * @param s- initial String to find time
     * @return time of operation in form of long number
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
        log.fine("Iteration for time searching was performed");
        return (long) timeValue;
    }

    /**
     * Method to find and collect type from String
     * @param s- initial String to find type
     * @return type of operation in form of int number
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
        //log.fine("Iteration for type searching was performed");
        return Integer.parseInt(result.toString());
    }
    /**
     * Method to find and collect id of operation from String
     * @param s- initial String to find id
     * @return id of operation in form of long number
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
        log.fine("Iteration for id searching was performed");
        return hex2decimal(result.toString());
    }
    /**
     *
     */
    public void calculateTimeDelays(){
        for(int i=0;i<inputID.size();i++){
            for(int j=i;j<i+1000;j++){
                if(outputID.size()>j) {
                    if (inputID.get(i).equals(outputID.get(j))) {
                        timeDelays.add(outputTime.get(j) - inputTime.get(i));
                    }
                }
            }
        }
    }
    /**
     * Method to fill lists of different data
     * (time of input\output operation, type of output operation, id of input\output operation)
     * @param fileName- is the name of input log file
     */
    public void getData(String fileName){
        try {
            log.info("Getting data from file:"+fileName);
            long start=new Date().getTime();
            ArrayList <StringBuilder> data=loadFile(fileName);
            for(int i=0;i<data.size();i++) {
                String currentInputString=data.get(i).toString();
                if(!currentInputString.contains("SQLProxy")&&!currentInputString.contains("P2_COD")) {
                    if (currentInputString.contains("input")) {
                        inputCount++;
                        addToInputID(findId(currentInputString));
                        addToInputTime(findTime(currentInputString));
                    }
                    if (currentInputString.contains("output")) {
                        outputCount++;
                        addToOutputID(findId(currentInputString));
                        addToOutputTime(findTime(currentInputString));
                        addToOutputTypes(findType(currentInputString));
                    }
                }
            }
            long end=new Date().getTime();
            log.info("Getting data was performed. Consumed time in seconds:"+(start-end)/1000);
        }catch (IOException e){
            log.log(Level.SEVERE, "IOException:"+Thread.currentThread().getStackTrace());
            System.out.println("IOException");
        }
    }

    /**
     *
     * @param outputFileName- desirable name of input file, contains analysis of per second delays between
     *                      input and output operations
     */
    public void makeTable(String outputFileName){
        writeTextToFile(outputFileName,"");
        ArrayList <Long> tmp=new ArrayList<>();
        ArrayList <Integer> tmpTypes=new ArrayList<>();
        long currentOperationNumber=0;
        int currentPosition=0;
        HashSet<Integer> tmpTypesSet=new HashSet<>();
        for(int i=0;i<inputTime.size();i++){
            if(outputTime.get(i)-inputTime.get(currentPosition)<=60000000){
                tmp.add(timeDelays.get(i));
                tmpTypes.add(outputTypes.get(i));
                tmpTypesSet.add(outputTypes.get(i));
            }else {
                ArrayList<Long> delaysForSuchType=new ArrayList<>();
                for(Integer type: tmpTypesSet){
                    for(int j=0;j<tmpTypes.size();j++){
                        if(type==tmpTypes.get(j)){
                            delaysForSuchType.add(tmp.get(j));
                        }
                    }
                    Operations operations=new Operations(delaysForSuchType);
                    writeTextToFileWithoutRewrite(outputFileName,type.toString()+","+operations.getMedian()+","
                            +operations.getPercentile90()+","+operations.getPercentle99()+","+operations.getAverage());
                }
                currentPosition=i;
                tmp.clear();
                tmpTypes.clear();
                tmpTypesSet.clear();
            }
        }
    }
    /**
     *
     * @param fileName is name of file in which you want to put portion of data string by string without rewriting.
     * @param text is the text that will be put in file.
     */
    public static void writeTextToFileWithoutRewrite(String fileName,String text){
        File file=new File(fileName);
        log.fine("writing iteration to file: "+fileName);
        try { if(!file.exists()) throw new FileNotFoundException();
            FileWriter outfile=new FileWriter(file.getAbsoluteFile(),true);
            try{
                outfile.write(text+"\r\n");
            }finally {
                outfile.close();
            }
        }catch (IOException e){
            throw  new RuntimeException(e);
        }
    }

    /**
     * Method, that writes a String in the certain file.
     * @param fileName is name of file in which you want to put data.
     * @param text is the text that will be put in file.
     */
    public static void writeTextToFile(String fileName,String text){
        File file=new File(fileName);
        log.fine("Writing to file: "+fileName);
        try { if (!file.exists()){
            file.createNewFile();
        }
            PrintWriter out=new PrintWriter(file.getAbsoluteFile());
            try {
                out.print(text);
            }finally {
                out.close();
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public static void main(String [] args)throws IOException{
        /*
        try {
            LogManager.getLogManager().readConfiguration(LogAnalyzer.class.getResourceAsStream("logging.properties"));
        }catch (IOException e){
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
        */
        LogAnalyzer analyzer=new LogAnalyzer();
        long start=new Date().getTime();
        //ArrayList<StringBuilder> stringBuilder=analyzer.loadFile("input.log");
        analyzer.getData("input.log");
        analyzer.calculateTimeDelays();
        analyzer.makeTable("output.csv");
        long end=new Date().getTime();
        int inCount=0;
        int outCount=0;
        /*
        for (int i=0;i<stringBuilder.size();i++){
            if(stringBuilder.get(i).toString().contains("input")){
                inCount++;
            } else if(stringBuilder.get(i).toString().contains("output")){
                outCount++;
            }
        }
        System.out.println("in+= "+inCount+" out= "+outCount);
        */
        //for(int i=0;i<20;i++){
        //    System.out.println(stringBuilder.get(i));
        //}
        System.out.println("Consumed time in seconds="+(double)(end-start)/1000);
        System.out.println(analyzer.inputCount+" Input times size="+analyzer.inputTime.size());
        System.out.println(analyzer.outputCount+" Output times size="+analyzer.outputTime.size());
        System.out.println(analyzer.timeDelays.size());

        //double [] doubles=analyzer.inputTime.get(0);
        //System.out.println(doubles);
        //analyzer.addToOutputTypes(analyzer.findType(s));
        //System.out.println(analyzer.outputTypes);
    }
}
