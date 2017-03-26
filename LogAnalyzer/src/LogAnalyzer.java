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
    //private field- instance of Logger class
    private static Logger log=Logger.getLogger(LogAnalyzer.class.getName());
    private static int timeDigits=3;//hours(1 value)+minuted(1 value)+seconds and it's fractions(1 value)=3
    public int getTimeDigits() {
        return timeDigits;
    }
    /**
     * Auxiliary method to convert read and translated in microseconds times back in a representation typical of the input file.
     * @param time- time value to convert
     * @return time in String form
     */
    public static String ConvertTimeFromMks(long time){
        long hours=(long) (time/Math.pow(10,6)/3600);
        long minutes=(long)((time-hours*Math.pow(10,6)*3600)/Math.pow(10,6)/60);
        double seconds=(time-hours*Math.pow(10,6)*3600-minutes*Math.pow(10,6)*60)/Math.pow(10,6);
        String forHours=""+hours;
        String forMinutes=""+minutes;
        String forSeconds=""+seconds;
        if(hours<10){
            forHours="0"+forHours;
        }
        if(minutes<10){
            forMinutes="0"+forMinutes;
        }
        if(seconds<10){
            forSeconds="0"+forSeconds;
        }
        return forHours+":"+forMinutes+":"+forSeconds;
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
    public static ArrayList<StringBuilder> loadFile(String fileName)throws IOException{
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
        log.info("Data was loaded. Consumed time in second:"+(double)(end-start)/1000);
        return result;
    }
    /**
     * Primary method for operation with data.  The basic algorithm is based on the assumption, that every second must need
     * to start from input. So if we have one output which does not fit in a second,  we will add it to the set of considered
     * outputs and for this reason, sometimes considered interval can include a little more time than one second. In addition,
     * we considered time delays so that if some input doesn't have output in the analyzed time interval it will not be
     * taken into account.
     * It invokes loadFile(fileName) method as well as auxiliary methods: findTime, findId, findType.
     */
    public static void dataOperation(String fileName){
        try {
            long start=new Date().getTime();
            log.info("Starting to operating with date from input file:"+fileName);
            LogAnalyzer.writeTextToFile("output.csv","");
            ArrayList<StringBuilder> stringBuilders = loadFile(fileName);
            ArrayList<Long> inputTimeSec=new ArrayList<>();
            ArrayList<Long> outputTimeSec=new ArrayList<>();
            ArrayList<Integer> outputTypesSec=new ArrayList<>();
            LinkedHashSet <Integer> outputTypesSet=new LinkedHashSet<>();
            ArrayList<Long> inputIdSec=new ArrayList<>();
            ArrayList<Long> outputIdSec=new ArrayList<>();
            ArrayList<Long> timeDelaysSec=new ArrayList<>();
            for(Iterator<StringBuilder> iter=stringBuilders.iterator();iter.hasNext();){
                String current=iter.next().toString();
                if(!current.contains("SQLProxy")&&!current.contains("P2_COD")) {
                    if(inputTimeSec.size()==0){
                        inputTimeSec.add(findTime(current));
                        inputIdSec.add(findId(current));
                    }else {
                        if (findTime(current) - inputTimeSec.get(0) <= 1000000) {
                            if (current.contains("input")) {
                                inputTimeSec.add(findTime(current));
                                inputIdSec.add(findId(current));
                            }
                            if (current.contains("output")) {
                                outputTimeSec.add(findTime(current));
                                outputIdSec.add(findId(current));
                                outputTypesSec.add(findType(current));
                                outputTypesSet.add(findType(current));
                            }
                        } else {
                            if (current.contains("output")) {
                                outputTimeSec.add(findTime(current));
                                outputIdSec.add(findId(current));
                                outputTypesSec.add(findType(current));
                                outputTypesSet.add(findType(current));
                            }
                            ArrayList<Integer> tmpTypes = new ArrayList<>();
                            ArrayList<Long> tmpDelays = new ArrayList<>();
                            for (int i = 0; i < inputIdSec.size(); i++) {
                                for (int j = 0; j < outputIdSec.size(); j++) {
                                    if (inputIdSec.get(i).equals(outputIdSec.get(j))) {
                                        timeDelaysSec.add(outputTimeSec.get(j) - inputTimeSec.get(i));
                                        tmpTypes.add(outputTypesSec.get(j));
                                    }
                                }
                            }
                            for (Integer type : outputTypesSet) {
                                for (int i = 0; i < tmpTypes.size(); i++) {
                                    if (tmpTypes.get(i).equals(type)) {
                                        tmpDelays.add(timeDelaysSec.get(i));
                                    }
                                }
                                Operations operations = new Operations(tmpDelays);
                                String stringForWrite=LogAnalyzer.ConvertTimeFromMks(inputTimeSec.get(0)) + "," + type + "," + inputTimeSec.size() + "," + operations.getAverage() + "," +
                                        operations.getMedian() + "," + operations.getPercentile90() + "," + operations.getPercentle99() + "," + operations.getMax();
                                LogAnalyzer.writeTextToFileWithoutRewrite("output.csv",stringForWrite);
                            }
                            tmpTypes.clear();
                            tmpDelays.clear();
                            timeDelaysSec.clear();
                            outputTypesSet.clear();
                            outputTypesSec.clear();
                            outputIdSec.clear();
                            inputIdSec.clear();
                            inputTimeSec.clear();
                            outputTimeSec.clear();
                        }
                    }
                }
            }
            long end=new Date().getTime();
            log.info("Data was handled. Consumed time in second:"+(double)(end-start)/1000);
        }catch (IOException e){
            log.log(Level.SEVERE, "IOException:"+Thread.currentThread().getStackTrace());
            System.out.println("IOException");
        }
    }
    /**
     * Method to find and collect time of input or output from String
     * @param s- initial String to find time
     * @return time of operation in form of long number
     */
    public static long findTime(String s){
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
    public static int findType(String s){
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
    public static long findId(String s){
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

    /**
     * The main method contains entry point for logger and for calculations. It invokes only one method- dataOperation.
     * @param args
     */
    public static void main(String [] args){
        try {
            LogManager.getLogManager().readConfiguration(LogAnalyzer.class.getResourceAsStream("logging.properties"));
        }catch (IOException e){
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
        long start=new Date().getTime();
        dataOperation("input.log");
        long end=new Date().getTime();
        System.out.println("Consumed time in seconds="+(double)(end-start)/1000);

    }
}
