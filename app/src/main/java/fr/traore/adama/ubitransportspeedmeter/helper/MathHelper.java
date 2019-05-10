package fr.traore.adama.ubitransportspeedmeter.helper;

import java.util.List;

public class MathHelper {

    public static double calculateAverageFromIntegerList(List<Integer> list){
        Integer sum = 0;
        int size = list != null ? list.size() : 0;

        if(list != null && !list.isEmpty()){
            for(Integer item : list){
                sum += item;
            }
        }

        return sum.doubleValue() / size;
    }




}
