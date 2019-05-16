package ru.snek;

import java.util.*;

public class MapWrapperUtils {

    public static String parseJson(String str) throws Exception{
        class WrongFormatException extends Exception{
            @Override
            public String getMessage() {
                return "Неверный формат данных!";
            }
        }
        str = str.replaceAll("\\s+", "");
        str = str.replaceAll("\n", "");
        if(str.length() < 2) throw new WrongFormatException();
        str = str.substring(1, str.length()-1);
        if(Utils.countInStr(str, '"') % 2 != 0) throw new WrongFormatException();
        ArrayList<Integer> commas = Utils.getExtCommas(str);
        ArrayList<String> strArr = Utils.splitStrByIndex(str, commas);
        if(strArr.size() < 1 && !str.isEmpty())  strArr.add(str);
		String[] fields = new String[9];
		fields[0] = "\"Безымянный\"";
        fields[1] = Integer.toString(18 + (new Random().nextInt(30)));
        fields[2] = Integer.toString(new Random().nextInt(100));
        fields[3] = Integer.toString(new Random().nextInt(100));
        fields[4] = Long.toString(new Date().getTime());
        fields[5] = Malefactor.Condition.AWAKEN.name();
        fields[6] = Malefactor.Box.Weight.HEAVY.name();
        fields[7] = '\"'+Boolean.toString(false)+'\"';
        fields[8] = "\"\"";
        for(String s : strArr) {
            ArrayList<Integer> a = new ArrayList<>();
            if(s.indexOf(":") > -1) a.add(s.indexOf(":"));
            ArrayList<String> pair = Utils.splitStrByIndex(s, a);
            if(pair.size() < 2) throw new WrongFormatException();
            if(pair.get(0).length() < 2) throw new WrongFormatException();
            pair.set(0, pair.get(0).substring(1, pair.get(0).length()-1));
            switch (pair.get(0)) {
                case "name" :
					fields[0] = pair.get(1);
                    break;
                case "age" :
					fields[1] = pair.get(1);
					break;
                case "x" :
					fields[2] = pair.get(1);
					break;
                case "y" :
					fields[3] = pair.get(1);
					break;
                case "birthDate" :
					fields[4] = pair.get(1);
					break;
                case "condition" :
					fields[5] = pair.get(1);
					break;
                case "abilityToLift" :
                    fields[6] = pair.get(1);
                    break;
                case "canSleep" :
                    fields[7] = pair.get(1);
                    break;
                case "pocketContent" :
					StringBuilder strb = new StringBuilder();
                    strb.append("\"");
                    pair.set(1, pair.get(1).substring(1, pair.get(1).length()-1));
                    ArrayList<Integer> coms = Utils.getExtCommas(pair.get(1));
                    ArrayList<String> asd = Utils.splitStrByIndex(pair.get(1), coms);
                    if(asd.size() == 0) asd.add(pair.get(1));
                    for(int i = 0; i < asd.size(); ++i) {
                        String[] ar = asd.get(i).split(":");
                        if(ar.length != 2) throw new WrongFormatException();
                        ar[0] = ar[0].substring(1, ar[0].length()-1);
                        switch (ar[0]) {
                            case "Knife" :
                                strb.append("Knife");
                            break;
                            case "Box" :
                                strb.append("Box");
                                strb.append("{" + ar[1].substring(1, ar[1].length()-1) + "}");
                            break;
                            case "Lamp" :
                                strb.append("Lamp");
                                if(!(ar[1].startsWith("[") && ar[1].endsWith("]"))) throw new WrongFormatException();
                                ar[1] = ar[1].substring(1, ar[1].length()-1);
                                String[] loc = ar[1].split(",");
                                if(loc.length != 2) throw new WrongFormatException();
                                strb.append("{"+loc[0].substring(1,loc[0].length()-1)+", "+loc[1].substring(1,loc[1].length()-1)+"}");
                            break;
                        }
                        if(i < asd.size() - 1) {
                            strb.append(", ");
                        }
                    }
                    strb.append("\"");
                    fields[8] = strb.toString();
                    break;
                default:
                    throw new WrongFormatException();
            }
        }
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < fields.length; ++i) {
		    result.append(fields[i]);
		    if(i < fields.length - 1) result.append("; ");
        }

        return result.toString();
    }

    public static Malefactor elementFromString(String str) throws Exception {
        String[] arr = str.split(";");
        if (arr.length != 9) {
            throw new Exception("Неверный формат данных!");
        }
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = arr[i].trim();
        }
        Malefactor mf = new Malefactor(arr[0].substring(1, arr[0].length() - 1));
        mf.setAge(Integer.valueOf(arr[1]));
        mf.setX(Integer.valueOf(arr[2]));
        mf.setY(Integer.valueOf(arr[3]));
        mf.setBirthDate(new Date(Long.valueOf(arr[4])));
        mf.setCondition(Malefactor.Condition.valueOf(arr[5]));
        mf.setAbilityToLift(Malefactor.Box.Weight.valueOf(arr[6]));
        mf.setCanSleep(Boolean.valueOf(arr[7]));
        mf.setPocketContent(parseArr(arr[8], mf));
        return mf;
    }

    private static ArrayList<Thingable> parseArr(String str, Malefactor mf) throws IllegalArgumentException {
        ArrayList<Thingable> result = new ArrayList<>();
        str = str.substring(1, str.length()-1);
        if(str.equals("")) return  result;
        ArrayList<Integer> commas = Utils.getExtCommas(str);
        ArrayList<String> strArr = Utils.splitStrByIndex(str, commas);
        if(strArr.size() == 0) strArr.add(str);

        for(String s : strArr) {
            String[] sArr = s.split("\\{");
            switch (sArr[0]) {
                case "Knife" :
                    Malefactor.Knife knife =  mf.new Knife();
                    result.add(knife);
                    break;
                case "Lamp" :
                    Malefactor.Lamp lamp = mf.new Lamp();
                    sArr[1] = sArr[1].substring(0, sArr[1].length()-1);
                    String[] fields = sArr[1].split(",");
                    if((fields[0].trim().equals("true") || fields[0].trim().equals("false"))
                    && (fields[1].trim().equals("true") || fields[1].trim().equals("false"))) {
                        lamp.setHidden(Boolean.valueOf(fields[0].trim()));
                        lamp.setCond(Boolean.valueOf(fields[1].trim()));
                        result.add(lamp);
                    }
                    else throw new IllegalArgumentException();
                    break;
                case "Box" :
                    sArr[1] = sArr[1].substring(0, sArr[1].length()-1);
                    Malefactor.Box box = new Malefactor.Box(Malefactor.Box.Weight.valueOf(sArr[1].trim()));
                    result.add(box);
                    break;
            }
        }
        return result;
    }
}
