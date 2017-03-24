import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Pahan on 23.03.2017.
 */
public class Operations {
    static int [] array;
    Operations(ArrayList<Integer> integers){
        array=new int[integers.size()];
        for(int i=0;i<integers.size();i++){
            array[i]=integers.get(i);
        }
        Arrays.sort(array);
    }
    public static int getMedian(){
        return array[array.length/2];
    }
    public static double getPercentile90(){
        return array[(int) (array.length*0.9)];
    }
    public static double getPercentle99(){
        return array[(int)(array.length*0.99)];
    }

}
