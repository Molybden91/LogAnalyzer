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
    //Constructor takes input list of values, than sort it. Each operation for this instance will be held for this data.
    Operations(ArrayList<Long> integers){
        if(integers.size()==0){
            array=new long[1];
            array[0]=0;
        }else {
            array=new long[integers.size()];
            for (int i = 0; i < integers.size(); i++) {
                array[i] = integers.get(i);
            }
        }
        Arrays.sort(array);
    }

    /**
     * Method for determining median value from initial sorted array of values.
     * @return median value for handled array in form of long value.
     */
    public  long getMedian(){
        return array[array.length/2];
    }

    /**
     * Method for determining 90th percentile value from initial sorted array of values.
     * @return 90th percentile for handled array in form of long value.
     */
    public  long getPercentile90(){
        return array[(int) (array.length*0.9)];
    }

    /**
     * Method for determining 90th percentile value from initial sorted array of values.
     * @return 99th percentile for handled array in form of long value.
     */
    public  long getPercentle99(){
        return array[(int)(array.length*0.99)];
    }

    /**
     * Method for determining average value from initial sorted array of values.
     * @return average value for handled array in form of long value.
     */
    public double getAverage(){
        int summ=0;
        for(int i=0;i<array.length;i++){
            summ+=array[i];
        }
        return summ/array.length;
    }

    /**
     * Method for determining maximum value from initial sorted array of values.
     * @return maximum value for handled array in form of long value.
     */
    public long getMax(){return array[array.length-1];}
}
