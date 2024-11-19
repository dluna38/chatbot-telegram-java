package com.codegym.telegram;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class UserInfo {
    public String name; //Nombre
    public String sex; //Sexo
    public String age; //Edad
    public String city; //Ciudad
    public String occupation; //Profesión
    public String hobby; //Hobby
    public String handsome; //Belleza, atractivo
    public String wealth; //Ingresos, riqueza
    public String annoys; //Lo que me molesta en las personas
    public String goals; //Objetivos de la relación

    private String fieldToString(String str, String description) {
        if (str != null && !str.isEmpty())
            return description + ": " + str + "\n";
        else
            return "";
    }
    public static Map<String,String> getFrienlyMsgAtributtes(){
        Map<String,String> map = new HashMap<>();
        map.put("name","Nombre");
        map.put("sex","Sexo");
        map.put("age","Edad");
        map.put("city","Ciudad");
        map.put("occupation","Profesión");
        map.put("hobby","Hobby");
        map.put("handsome","Belleza, atractivo en puntos (máximo 10 puntos)");
        map.put("wealth","Ingresos, riqueza");
        map.put("annoys","Lo que molesta en las personas");
        map.put("goals","Objetivos que busca de la relación");

        return map;
    }

    @Override
    public String toString() {
        String result = "";

        result += fieldToString(name, "Nombre");
        result += fieldToString(sex, "Sexo");
        result += fieldToString(age, "Edad");
        result += fieldToString(city, "Ciudad");
        result += fieldToString(occupation, "Profesión");
        result += fieldToString(hobby, "Hobby");
        result += fieldToString(handsome, "Belleza, atractivo en puntos (máximo 10 puntos)");
        result += fieldToString(wealth, "Ingresos, riqueza");
        result += fieldToString(annoys, "Lo que molesta en las personas");
        result += fieldToString(goals, "Objetivos que busca de la relación");

        return result;
    }

    public void setPropiedad(String nombrePropiedad, Object valor) throws NoSuchFieldException, IllegalAccessException {
        Field field = this.getClass().getDeclaredField(nombrePropiedad);
        field.setAccessible(true);

        field.set(this, valor);
    }
}
