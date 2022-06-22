package com.example.remake_word_cards;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.renderer.Renderer;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

public class GameFragment extends Fragment {

    ArrayList<String> cards_type_array = new ArrayList<String>();

    CustomCategoryDBHelper customCategoryDBHelper;
    GameResultDBHelper gameResultDBHelper;
    CardsDBHelper cardsDBHelper;

    GameResultAdapter gameResultAdapter;

    Spinner spin_cards_type;

    TextView TV_words_amount_percent;

    CheckBox CB_reverse_translate;

    ImageView IV_amount_minus;
    ImageView IV_amount_plus;

    Button button_start_game;
    Button button_clear_history;

    ////start/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    BarChart barChart;
    Button button_change_graph_list;
    // (!) - прим. Рома: далее объясняю, почему закомментировал
    //RecyclerView RV_game_results_show;
    public boolean status = true;
    ////stop//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onResume() {

        super.onResume();
        gameResultAdapter.notifyDataSetChanged();
        show_bar_chart();

    }



    public void show_bar_chart(){
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(50);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false); //убирает правую ось y
        /*barChart.getDescription().setEnabled(true);*/ // отключение описания
        barChart.setScaleEnabled(false); // отключение приближения графика
        barChart.setTouchEnabled(false); // отключене выбора колонки
        /*barChart.getLegend().setEnabled(false);*/ //убрать надпись снизу ("Результат последних игр")

        SQLiteDatabase db  = gameResultDBHelper.getReadableDatabase();
        Cursor cursor = db.query(GameResultDBHelper.TABLE_RESULT, null, null, null, null, null, GameResultDBHelper.KEY_ID + " DESC");


        int id_index = cursor.getColumnIndex(GameResultDBHelper.KEY_ID);
        int category_name_index = cursor.getColumnIndex(GameResultDBHelper.KEY_CATEGORY_NAME);
        int translates_amount_index = cursor.getColumnIndex(GameResultDBHelper.KEY_TRANSLATES_AMOUNT);
        int right_answer_amount_index = cursor.getColumnIndex(GameResultDBHelper.KEY_RIGHT_ANSWER_AMOUNT);


        ArrayList<BarEntry> barEntries = new ArrayList<>(); // список для элементов гистограммы
        ArrayList<String> game_result_value = new ArrayList<>(); // список для подписей столбиков
        ArrayList<String> description_elements = new ArrayList<String>(); // список для элементов описания


        int k = 0;
        game_result_value.add("0"); // добавляем самый первый элемент который не показывается на графике (нулевое значение)  (!_!) (нулевое значение) вместо аля
        while (cursor.moveToNext() && k<5){
            k++;
            int percent_right = (int) (100 * (Double.parseDouble(cursor.getString(right_answer_amount_index)) / Double.parseDouble(cursor.getString(translates_amount_index))));
            barEntries.add(new BarEntry(k,percent_right));
            //game_result_value.add(cursor.getString(id_index)); // (#) - закомментировать в готовом варианте
            description_elements.add("("+cursor.getString(right_answer_amount_index)+"/"+cursor.getString(translates_amount_index)+")");
            game_result_value.add(changing_line(cursor.getString(category_name_index))); // (#) - раскомментировать в готовом варианте
        }

        String description_text = "";
        String space_text="";
        if(k==0){
            description_text = "";
        }else{
            if(k==1){
                description_text = "                ";
                space_text = "";
            }
            if(k==2){
                description_text = "     ";
                space_text = create_space(82);
            }
            if(k==3){
                description_text = "    ";
                space_text = create_space(51);
            }
            if(k==4){
                description_text = "   ";
                space_text = create_space(36);
            }
            if(k==5){
                description_text = "          ";
                space_text = create_space(22);
            }
            for(int i = k-1; i >= 0; i--){
                description_text = space_text + description_elements.get(i) + description_text ;
            }

        }

        barChart.getDescription().setText(description_text);
        cursor.close();

        String[] array_games = new String[game_result_value.size()+1];
        game_result_value.toArray(array_games);

        BarDataSet barDataSet = new BarDataSet(barEntries,"Results of recent games");
        /*String sssr = "Результаты последних ("+k+") игр";
        BarDataSet barDataSet = new BarDataSet(barEntries,sssr);*/   // вариация подписи с количетсвом значений для построения графика
        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);    //(!_!) разноцветные столбцы



        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //ось x снизу
        xAxis.setDrawAxisLine(false); //скрывает линию оси x
        xAxis.setDrawGridLines(false); //скрывает вертикальные линии сетки

        IAxisValueFormatter formatterx = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return array_games[(int) value];
            }
        };

        xAxis.setGranularity(1f); // минимальный шаг по оси x
        xAxis.setValueFormatter(formatterx);

        IAxisValueFormatter formattery = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int)value + "%";
            }
        };
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMaxValue(100);
        yAxis.setAxisMinValue(0);
        yAxis.setLabelCount(11); // количество значений на оси y . Нормально рабоатет со значением 6 (0-20-40-60-80-100)
        yAxis.setValueFormatter(formattery);
        barChart.setXAxisRenderer(new CustomXAxisRenderer(barChart.getViewPortHandler(), barChart.getXAxis(), barChart.getTransformer(YAxis.AxisDependency.LEFT)));

        barChart.notifyDataSetChanged();
        barChart.invalidate();

        barChart.animateY(1500);    //(!_!) анимауия появления столбцов на графике
    }

    public static String create_space(int i){
        String sp = "";
        for( int u = 0; u < i; u++ ) {
            sp += " ";
        }
        return sp;
    }

    public static String changing_line(String line) {
        if(line.length() > 11){
            StringBuffer sb = new StringBuffer(line);
            sb.insert(10,"-\n");
            return  sb.toString();
        }
        else {
            return line;
        }
    }

    public class CustomXAxisRenderer extends XAxisRenderer {

        public CustomXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
            String line[] = formattedLabel.split("\n");
            Utils.drawXAxisValue(c, line[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
            for (int i = 1; i < line.length; i++) { // we've already processed 1st line
                Utils.drawXAxisValue(c, line[i], x, y + mAxisLabelPaint.getTextSize() * i,
                        mAxisLabelPaint, anchor, angleDegrees);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        customCategoryDBHelper = new CustomCategoryDBHelper(getActivity());
        gameResultDBHelper = new GameResultDBHelper(getActivity());
        cardsDBHelper = new CardsDBHelper(getActivity());

        // (!) - Variable 'words_amount' is accessed from within inner class, needs to be final or effectively final
        // не понял в чём ошибка, но сделал, как сказала IDE
        int[] words_amount = {100};

        // Инициализация элементов
        TV_words_amount_percent = view.findViewById(R.id.id_TV_words_amount_percent);

        IV_amount_minus = view.findViewById(R.id.id_IV_amount_minus);
        IV_amount_plus = view.findViewById(R.id.id_IV_amount_plus);

        button_start_game = view.findViewById(R.id.id_button_start_game);
        button_clear_history = view.findViewById(R.id.id_button_clear_history);

        CB_reverse_translate = view.findViewById(R.id.id_CB_reverse_translate);



        ////start/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        button_change_graph_list = view.findViewById(R.id.id_button_change_graph_list);
        // (!) - прим. Рома: Уже создана RV_gameResults, она ниже.
        // Когда ты создал RV_game_results_show, IDE указала, что уже существует элемент с таким id
        //RV_game_results_show = view.findViewById(R.id.id_RV_game_results);
        barChart = (BarChart) view.findViewById(R.id.id_game_graph);
        ////stop//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // Изменение words_amount - количество словиз выбранной категории в игровой сессии
        TV_words_amount_percent.setText(String.valueOf(words_amount[0]));

        // RV результатов прошлых игр
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView RV_gameResults = (RecyclerView) view.findViewById(R.id.id_RV_game_results);
        gameResultAdapter = new GameResultAdapter();
        RV_gameResults.setAdapter(gameResultAdapter);
        RV_gameResults.setLayoutManager(layoutManager);

        // (#)
        //TextView TV_test = view.findViewById(R.id.id_TV_test);
        //TV_test.setText(String.valueOf(gameResultAdapter.getItemCount()));
        //TV_test.setVisibility(View.GONE);




        ////start/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




        //График









        /*show_bar_chart();*/







        ////stop/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



        IV_amount_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (words_amount[0] > 10) {
                    words_amount[0] -= 10;
                    TV_words_amount_percent.setText(String.valueOf(words_amount[0]));
                }
            }
        });

        IV_amount_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (words_amount[0] < 100) {
                    words_amount[0] += 10;
                    TV_words_amount_percent.setText(String.valueOf(words_amount[0]));
                }
            }
        });

        // Установка значений spinner words_type
        setCards_type_array();

        spin_cards_type = (Spinner) view.findViewById(R.id.id_spin_cards_type);
        ArrayAdapter<String> adapter_cards_type = new ArrayAdapter<String>(getContext(), R.layout.spinner_item_cards_type, cards_type_array);
        adapter_cards_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_cards_type.setAdapter(adapter_cards_type);

        // Запуск игровой сессии
        button_start_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // (!)
                SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
                Cursor cursor;
                String cards_type =  spin_cards_type.getSelectedItem().toString();
                String[] string_current_cards_type = new String[]{cards_type};
                String selection = "custom_category = ?";

                if (cards_type.equals("All words")) {
                    cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);
                } else {
                    cursor = database.query(CardsDBHelper.TABLE_CARDS, null, selection, string_current_cards_type, null, null, null);
                }



                long card_size = DatabaseUtils.queryNumEntries(database, CardsDBHelper.TABLE_CARDS);

                // Если словарь не пустой, запускаем игру
                if (cursor.moveToFirst()) {
                    Intent intent = new Intent(getActivity(), GameSessionActivity.class);
                    intent.putExtra("cards_type", cards_type);
                    intent.putExtra("words_amount_percent", TV_words_amount_percent.getText().toString());

                    if (CB_reverse_translate.isChecked())
                        intent.putExtra("reverse_translate", "true");
                    else
                        intent.putExtra("reverse_translate", "false");

                    startActivity(intent);
                }
                // Если словарь пустой - предупреждение об этом
                else {
                    Toast toast = Toast.makeText(getActivity(), "There are no cards with this category", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });



        ////start/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Смена отображения результатов

        // (!) - прим. Рома: я заменил RV на уже существующий, которому присвоен адаптер (! это важно), делать новую такую же RV не нужно было

        button_change_graph_list.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(status){
                    /*RV_game_results_show.setVisibility(View.GONE);*/
                    RV_gameResults.setVisibility(View.INVISIBLE);
                    show_bar_chart();   //(!_!) отрисовка графика при смене
                    barChart.setVisibility(View.VISIBLE);
                }
                else {
                    barChart.setVisibility(View.GONE);
                    /*barChart.setVisibility(View.INVISIBLE);*/
                    RV_gameResults.setVisibility(View.VISIBLE);
                }
                status = !status;
            }
        });

        ////stop/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



        // Очистка истории результатов игр
        button_clear_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase database = gameResultDBHelper.getWritableDatabase();
                database.delete(GameResultDBHelper.TABLE_RESULT, null, null);
                //gameResultDBHelper.onUpgrade(database,2,3);
                gameResultAdapter.notifyDataSetChanged();
                show_bar_chart();///////////////////////////////////////тут в кнопке очистки вызывается отрисовка график по данным очищенной БД

            }
        });

        return view;
    }

    public class GameResultAdapter extends RecyclerView.Adapter<GameResultHolder> {
        ArrayList<GameResultHolder> game_results_holder_array = new ArrayList<>();

        SQLiteDatabase database = gameResultDBHelper.getWritableDatabase();


        @Override
        public GameResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_game_result, parent, false);

            GameResultHolder holder = new GameResultHolder(v);
            game_results_holder_array.add(holder);
            return holder;

        }

        //Отрисовка карточек
        @Override
        public void onBindViewHolder(GameResultHolder holder, int position) {
            // (!) было ASC, вроде обратный это DESC (GameResultDBHelper.KEY_ID + " DESC")
            Cursor cursor = database.query(GameResultDBHelper.TABLE_RESULT, null, null, null, null, null, GameResultDBHelper.KEY_ID + " DESC");

            cursor.moveToPosition(position);

            int id_index = cursor.getColumnIndex(GameResultDBHelper.KEY_ID);

            int category_name_index = cursor.getColumnIndex(GameResultDBHelper.KEY_CATEGORY_NAME);
            int translates_amount_index = cursor.getColumnIndex(GameResultDBHelper.KEY_TRANSLATES_AMOUNT);
            int right_answer_amount_index = cursor.getColumnIndex(GameResultDBHelper.KEY_RIGHT_ANSWER_AMOUNT);

            String text_id_game = cursor.getString(id_index) + ".";
            holder.TV_game_id.setText(text_id_game);
            holder.TV_game_type_cards.setText(cursor.getString(category_name_index));
            holder.TV_game_right_translates_amount.setText(cursor.getString(right_answer_amount_index));
            holder.TV_game_words_amount.setText(cursor.getString(translates_amount_index));

            int percent_right = (int) (100 * (Double.parseDouble(cursor.getString(right_answer_amount_index)) / Double.parseDouble(cursor.getString(translates_amount_index))));

            holder.TV_game_percent_right.setText(String.valueOf(percent_right));

            cursor.close();
        }

        @Override
        public int getItemCount() {

            long db_size = 0;

            db_size = DatabaseUtils.queryNumEntries(database, GameResultDBHelper.TABLE_RESULT);
            return (int) db_size;
        }
    }

    class GameResultHolder extends RecyclerView.ViewHolder {

        TextView TV_game_id;
        TextView TV_game_type_cards;
        TextView TV_game_right_translates_amount;
        TextView TV_game_words_amount;
        TextView TV_game_percent_right;

        GameResultHolder(View itemView) {
            super(itemView);

            TV_game_id = itemView.findViewById(R.id.id_TV_game_id);
            TV_game_type_cards = itemView.findViewById(R.id.id_TV_game_type_cards);
            TV_game_right_translates_amount = itemView.findViewById(R.id.id_TV_game_right_translates_amount);
            TV_game_words_amount = itemView.findViewById(R.id.id_TV_game_words_amount);
            TV_game_percent_right = itemView.findViewById(R.id.id_TV_game_percent_right);

        }

    }


    // Тип карточки выбирается из пользовательских категорий
    // или можно выбрать игру со всеми словами из пользовательского словаря
    public void setCards_type_array() {
        cards_type_array.add("All words");

        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();
        Cursor cursor = database.query(CustomCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            int category_name_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                cards_type_array.add(cursor.getString(category_name_index));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }
}
