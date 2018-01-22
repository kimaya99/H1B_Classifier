import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

public class Test {

    public static void main(String[] args) {

        String tst = "testeeedsfjghiopp";
        LinkedHashMap<Character,Integer> map = new LinkedHashMap<>();
        char[] arr = tst.toCharArray();
        for(int i = 0; i < arr.length ; i++) {
            if(map.containsKey(arr[i])) {
                map.put(Character.valueOf(arr[i]), map.get(arr[i]) + 1);
            } else {
                map.put(Character.valueOf(arr[i]),1);
            }
        }

       System.out.println(retreiveKey(map));

    }

    public static Character retreiveKey(LinkedHashMap<Character,Integer> map) {
        Set<Character> keys = map.keySet();
        for(Character k:keys){
            if(map.get(k) == 1) {
                return k;
            }
        }
        return null;
    }
}
