package com.jemali.nadhem.chat_and_share;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by nadhem on 22/04/2017.
 */

public class Show_message extends ArrayAdapter {
    Context context;
    ArrayList<String> my_message_list;
    ArrayList<String> my_name_list;
    ArrayList<String> my_date_list;


    Show_message(Context c,ArrayList<String> aux_name_lsit,ArrayList<String> aux_date_lsit
            ,ArrayList<String> aux_message_lsit)
    {
        super(c,R.layout.bulle,R.id.name,aux_name_lsit);
        this.context=c;
        my_message_list = new ArrayList<String>();
        my_name_list = new ArrayList<String>();
        my_date_list = new ArrayList<String>();

        this.my_name_list=aux_name_lsit;
        this.my_date_list=aux_date_lsit;
        this.my_message_list=aux_message_lsit;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.bulle,parent,false);
        TextView name = (TextView) row.findViewById(R.id.name);
        TextView date = (TextView) row.findViewById(R.id.date);
        TextView msg = (TextView) row.findViewById(R.id.message);
        TextView name_first_character = (TextView) row.findViewById(R.id.first_character);
        String [] direction_and_name = my_name_list.get(position).split(" ", 2);
        String format_string="<small>"+direction_and_name[0]+"</small>"+" <big>"+direction_and_name[1]+"</big>";
        name.setText(Html.fromHtml(format_string));
        date.setText(my_date_list.get(position));
        msg.setText(my_message_list.get(position));
        name_first_character.setText(direction_and_name[1].substring(0,1).toUpperCase() );
        String AZ=direction_and_name[1].substring(0,1).toUpperCase();

        if(AZ.equals("A")||AZ.equals("B"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle1));
        else if(AZ.equals("D")||AZ.equals("E"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle2));
        else if(AZ.equals("F")||AZ.equals("G"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle3));
        else if(AZ.equals("H")||AZ.equals("I"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle4));
        else if(AZ.equals("J")||AZ.equals("K"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle5));
        else if(AZ.equals("L")||AZ.equals("M"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle6));
        else if(AZ.equals("N")||AZ.equals("O"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle7));
        else if(AZ.equals("P")||AZ.equals("Q"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle8));
        else if(AZ.equals("R")||AZ.equals("S"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle9));
        else if(AZ.equals("T")||AZ.equals("W"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle10));
        else if(AZ.equals("X")||AZ.equals("Y"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle11));
        else if(AZ.equals("Z")||AZ.equals("C"))
            name_first_character.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.circle));

        return row;
    }
}
