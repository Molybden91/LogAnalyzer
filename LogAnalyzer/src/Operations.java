import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Pahan on 23.03.2017.
 */

/**
 * Class containing simple tools to calculate parameters for output file
 */
public class Operations {
    static long [] array;
    Operations(ArrayList<Long> integers){
        array=new long[integers.size()];
        for(int i=0;i<integers.size();i++){
            array[i]=integers.get(i);
        }
        Arrays.sort(array);
    }
    public  long getMedian(){
        return array[array.length/2];
    }
    public  long getPercentile90(){
        return array[(int) (array.length*0.9)];
    }
    public  long getPercentle99(){
        return array[(int)(array.length*0.99)];
    }
    public double getAverage(){
        int summ=0;
        for(int i=0;i<array.length;i++){
            summ+=array[i];
        }
        return summ/array.length;
    }
    public long getMax(){return array[array.length];}
}
