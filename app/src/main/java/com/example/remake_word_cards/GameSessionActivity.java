package com.example.remake_word_cards;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class GameSessionActivity extends AppCompatActivity {

    CardsDBHelper cardsDBHelper;
    GameResultDBHelper gameResultDBHelper;
    ArrayAdapter list_answers_adapter;

    boolean checked;

    int words_amount;
    int current_word_num;
    int translate_amount;
    int right_answer_amount;

    String cards_type;
    String words_amount_percent;
    String reverse_translate;

    ArrayList<String> current_answer_array = new ArrayList<>();
    ArrayList<String> cards_id_array = new ArrayList<>();
    ArrayList<String> used_cards_id_array = new ArrayList<>();
    ArrayList<String> cards_translations_array = new ArrayList<>();
    ArrayList<String> used_cards_translations_array = new ArrayList<>();


    Button button_check_next_end;

    TextView TV_current_word_num;
    TextView TV_words_amount;
    TextView TV_current_st_category;
    TextView TV_current_custom_category;
    TextView TV_current_word;
    TextView TV_text_right_answers;

    ListView list_answers;

    EditText ET_answer_1;
    EditText ET_answer_2;
    EditText ET_answer_3;
    EditText ET_answer_4;
    EditText ET_answer_5;

    ArrayList<EditText> ET_answers_array = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_session);

        // (#)
        cards_id_array.clear();
        used_cards_id_array.clear();
        cards_translations_array.clear();
        used_cards_translations_array.clear();
        current_answer_array.clear();

        current_word_num = 0;
        translate_amount = 0;
        right_answer_amount = 0;
        checked = false;

        Bundle arguments = getIntent().getExtras();
        cards_type = arguments.getString("cards_type");
        words_amount_percent = arguments.getString("words_amount_percent");
        reverse_translate = arguments.getString("reverse_translate");

        cardsDBHelper = new CardsDBHelper(this);
        gameResultDBHelper = new GameResultDBHelper(this);

        TV_current_word_num = findViewById(R.id.id_TV_current_word_num);
        TV_words_amount = findViewById(R.id.id_TV_words_amount);
        TV_current_st_category = findViewById(R.id.id_TV_current_st_category);
        TV_current_custom_category = findViewById(R.id.id_TV_current_custom_category);
        TV_current_word = findViewById(R.id.id_TV_current_word);
        TV_text_right_answers = findViewById(R.id.id_TV_text_right_answers);

        list_answers = findViewById(R.id.id_list_answers);

        ET_answer_1 = findViewById(R.id.id_ET_answer_1);
        ET_answer_2 = findViewById(R.id.id_ET_answer_2);
        ET_answer_3 = findViewById(R.id.id_ET_answer_3);
        ET_answer_4 = findViewById(R.id.id_ET_answer_4);
        ET_answer_5 = findViewById(R.id.id_ET_answer_5);

        list_answers_adapter = new ArrayAdapter(this,
                R.layout.lv_item_answer, current_answer_array);
        list_answers.setAdapter(list_answers_adapter);

        button_check_next_end = findViewById(R.id.id_button_check_next_end);

        setET_answers_array();
        startEdit_ET_answers();

        getData();

        if (reverse_translate.equals("true"))
            setNewCardReverseTranslate();
        else
            setNewCardStraightTranslate();

        button_check_next_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Если проверки ответов ещё не было,
                // по нажатию проверяются введённые ответы
                if (!checked) {
                    check_answers();
                }
                // Если проверка прошла, устанавливается новое слово и (current_word_num++)
                else {
                    if (words_amount != current_word_num) {
                        if (reverse_translate.equals("true"))
                            setNewCardReverseTranslate();
                        else
                            setNewCardStraightTranslate();
                    }
                    // Если количество пройденных слов равно сумме заданных слов,
                    // сохраняется результат и активность закрывается
                    else {
                        saveResult();
                        finish();
                    }
                }
            }
        });
    }

    // Установка данных из текущей карточки:
    // слово; его переводы; st_category; custom_category
    private void setNewCardStraightTranslate() {
        checked = false;

        current_answer_array.clear();

        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

        // Получение id новой карточки
        String current_id = getNewId();
        String[] string_current_id = new String[]{current_id};

        String selection = "_id = ?";

        // (#)
        //TV_current_st_category.setText(String.valueOf(cards_id_array.size()));
        //TV_current_custom_category.setText(String.valueOf(cards_id_array.contains(String.valueOf(4))));

        // Берём не всю БД, а только строку с id = string_current_id
        Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, selection, string_current_id, null, null, null);

        if (cursor.moveToFirst()) {

            int[] tran_index_array = new int[5];
            tran_index_array[0] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_1);
            tran_index_array[1] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_2);
            tran_index_array[2] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_3);
            tran_index_array[3] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_4);
            tran_index_array[4] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_5);

            int word_index = cursor.getColumnIndex(CardsDBHelper.KEY_WORD);
            int st_category_index = cursor.getColumnIndex(CardsDBHelper.KEY_ST_CATEGORY);
            int custom_category_index = cursor.getColumnIndex(CardsDBHelper.KEY_CUSTOM_CATEGORY);


            TV_current_word.setText(cursor.getString(word_index));
            TV_current_st_category.setText(cursor.getString(st_category_index));
            TV_current_custom_category.setText(cursor.getString(custom_category_index));

            // Если ответ это "", то такой ответ не идёт в массив ответов
            for (int index : tran_index_array) {
                String current_tran = cursor.getString(index);
                // (!)
                if (!current_tran.equals("") && !containsIn(current_answer_array, current_tran)) {
                    current_answer_array.add(cursor.getString(index));
                    translate_amount++;
                }
            }


            list_answers_adapter.notifyDataSetChanged();

            // Увеличение номера текущего слова
            current_word_num++;
            TV_current_word_num.setText(String.valueOf(current_word_num));
        } else
            TV_current_word.setText("Ошибка");

        cursor.close();

        startEdit_ET_answers();
        // Сокрытие лишних полей ввода ответа
        hide_extra_ET_answers();
    }

    private void setNewCardReverseTranslate() {
        checked = false;

        current_answer_array.clear();

        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
        Cursor cursor;

        String current_translation = getNewTranslation();

        String selection = "custom_category = ?";
        String[] selectionArgs = new String[]{cards_type};

        if (cards_type.equals("All words"))
            cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);
        else
            cursor = database.query(CardsDBHelper.TABLE_CARDS, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {

            int word_index = cursor.getColumnIndex(CardsDBHelper.KEY_WORD);

            int[] tran_index_array = new int[5];
            tran_index_array[0] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_1);
            tran_index_array[1] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_2);
            tran_index_array[2] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_3);
            tran_index_array[3] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_4);
            tran_index_array[4] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_5);

            do {
                for (int current_tran_index : tran_index_array) {
                    String current_tran = cursor.getString(current_tran_index);
                    if (current_tran.equals(current_translation)) {
                        current_answer_array.add(cursor.getString(word_index));
                        translate_amount++;
                        break;
                    }
                }
            } while (cursor.moveToNext());
        } else
            TV_current_word.setText("Ошибка");


        list_answers_adapter.notifyDataSetChanged();

        cursor.close();

        current_word_num++;

        TV_current_word_num.setText(String.valueOf(current_word_num));
        TV_current_word.setText(current_translation);
        TV_current_st_category.setText("");
        TV_current_custom_category.setText("");

        startEdit_ET_answers();
        // Сокрытие лишних полей ввода ответа
        hide_extra_ET_answers();
    }


    // Получение карточек заданной категории и количества этих карточек
    private void getData() {


        setCards_id_array(cards_type);

        if (reverse_translate.equals("true"))
            setCards_translations_array();

        words_amount = setWords_amount(words_amount_percent);

        //TV_current_word_num.setText(String.valueOf(current_word_num));
        TV_words_amount.setText(String.valueOf(words_amount));
    }

    // Проверка правильности введённых ответов
    private void check_answers() {
        checked = true;

        stopEdit_ET_answers();
        check_and_color_ET_answers();
    }

    // Получение id новой карточки из массива id карточек заданной категории из setCards_id_array()
    private String getNewId() {
        String id;

        do {
            id = cards_id_array.get((int) (Math.random() * cards_id_array.size() + 0));
            // (!)
        } while (containsIn(used_cards_id_array, id));

        // (!) - переделать на cards_id_array.remove
        used_cards_id_array.add(id);

        return id;
    }

    private String getNewTranslation() {
        String translation;

        do {
            translation = cards_translations_array.get((int) (Math.random() * cards_translations_array.size() + 0));
        } while (containsIn(used_cards_translations_array, translation));

        // (!) - переделать на cards_translations_array.remove
        used_cards_translations_array.add(translation);

        return translation;
    }

    // Нахождение id всех слов заданной категории
    private void setCards_id_array(String cards_type) {
        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
        Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            int id_index = cursor.getColumnIndex(CardsDBHelper.KEY_ID);
            int custom_category_index = cursor.getColumnIndex(CardsDBHelper.KEY_CUSTOM_CATEGORY);

            do {
                if (cards_type.equals("All words")) {
                    cards_id_array.add(cursor.getString(id_index));
                } else {
                    if (cursor.getString(custom_category_index).equals(cards_type))
                        cards_id_array.add(cursor.getString(id_index));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    // Получение всех возможных переводов для игры с обратным переводом
    // переводы не повторяются
    private void setCards_translations_array() {
        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
        Cursor cursor;

        String selection = "custom_category = ?";
        String[] selectionArgs = new String[]{cards_type};

        if (cards_type.equals("All words"))
            cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);
        else
            cursor = database.query(CardsDBHelper.TABLE_CARDS, null, selection, selectionArgs, null, null, null);


        if (cursor.moveToFirst()) {

            int[] tran_index_array = new int[5];
            tran_index_array[0] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_1);
            tran_index_array[1] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_2);
            tran_index_array[2] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_3);
            tran_index_array[3] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_4);
            tran_index_array[4] = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_5);


            // Добавляю все переводы из доступных карточек в лист переводов
            // Если перевод = "" или такой перевод уже есть, то не добавляю его
            do {
                for (int current_tran_index : tran_index_array) {
                    String current_tran = cursor.getString(current_tran_index);
                    if (!current_tran.equals("") && !containsIn(cards_translations_array, current_tran))
                        cards_translations_array.add(current_tran);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    // Нахождение количества слов в данной сессии
    private int setWords_amount(String words_amount_percent) {
        int amount;
        double percent;

        percent = (double) Integer.parseInt(words_amount_percent) / 100.0;

        if (reverse_translate.equals("true"))
            amount = (int) Math.ceil(cards_translations_array.size() * percent);
        else
            amount = (int) Math.ceil(cards_id_array.size() * percent);

        return amount;
    }

    // Начало редактирования полей ответов
    // и сокрытие правильных ответов
    private void startEdit_ET_answers() {

        button_check_next_end.setText("check");

        TV_text_right_answers.setVisibility(View.GONE);
        list_answers.setVisibility(View.GONE);

        for (EditText ET_current : ET_answers_array) {
            ET_current.setText("");
            ET_current.setVisibility(View.VISIBLE);
            ET_current.setEnabled(true);
            ET_current.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
    }

    // Окончание редактирования полей ответов
    // и их закраска в зависимости от правильности ответа
    // и отображение правильных ответов
    private void stopEdit_ET_answers() {

        if (words_amount != current_word_num)
            button_check_next_end.setText("next");
        else
            button_check_next_end.setText("finish");

        TV_text_right_answers.setVisibility(View.VISIBLE);
        list_answers.setVisibility(View.VISIBLE);

        for (EditText ET_current : ET_answers_array) {
            ET_current.setEnabled(false);
        }
    }

    // Проверка правильности ответов для закрашивания
    private void check_and_color_ET_answers() {
        for (EditText ET_current : ET_answers_array) {
            if (containsIn(current_answer_array, ET_current.getText().toString())) {
                ET_current.getBackground()
                        .setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
                right_answer_amount++;
            } else
                ET_current.getBackground()
                        .setColorFilter(getResources().getColor(R.color.bright_red), PorterDuff.Mode.SRC_IN);
        }
    }

    private void hide_extra_ET_answers() {
        int extra_size = 5 - current_answer_array.size();
        int ET_answer_index = 4;

        while (extra_size != 0) {
            extra_size--;
            ET_answers_array.get(ET_answer_index).setVisibility(View.GONE);
            ET_answer_index--;
        }

    }

    private void saveResult() {

        Bundle arguments = getIntent().getExtras();
        String cards_type = arguments.getString("cards_type");

        SQLiteDatabase database = gameResultDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(GameResultDBHelper.KEY_CATEGORY_NAME, cards_type);
        contentValues.put(GameResultDBHelper.KEY_TRANSLATES_AMOUNT, String.valueOf(translate_amount));
        contentValues.put(GameResultDBHelper.KEY_RIGHT_ANSWER_AMOUNT, String.valueOf(right_answer_amount));

        database.insert(GameResultDBHelper.TABLE_RESULT, null, contentValues);

        //(#)
        //showBD();

        contentValues.clear();
    }

    public void showBD() {
        //Показ того, что есть в БД
        SQLiteDatabase database = gameResultDBHelper.getWritableDatabase();

        Cursor cursor = database.query(GameResultDBHelper.TABLE_RESULT, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int id_index = cursor.getColumnIndex(GameResultDBHelper.KEY_ID);

            int category_name_index = cursor.getColumnIndex(GameResultDBHelper.KEY_CATEGORY_NAME);
            int translates_amount_index = cursor.getColumnIndex(GameResultDBHelper.KEY_TRANSLATES_AMOUNT);
            int right_answer_amount_index = cursor.getColumnIndex(GameResultDBHelper.KEY_RIGHT_ANSWER_AMOUNT);

            do {
                Log.d("mLog", "ID = " + cursor.getString(id_index) + "; category = " + cursor.getString(category_name_index) + "; right_amount = " + cursor.getString(right_answer_amount_index));
            } while (cursor.moveToNext());
        } else Log.d("mLog", "0 rows");

        cursor.close();
    }

    // Наполнения массива полей ответов
    private void setET_answers_array() {
        ET_answers_array.add(ET_answer_1);
        ET_answers_array.add(ET_answer_2);
        ET_answers_array.add(ET_answer_3);
        ET_answers_array.add(ET_answer_4);
        ET_answers_array.add(ET_answer_5);
    }

    public boolean containsIn(ArrayList<String> arrayList, String text) {
        boolean isInArray;

        isInArray = false;

        for (String text_arrayList : arrayList) {
            if (text_arrayList.trim().equalsIgnoreCase(text.trim())){
                isInArray = true;
                break;
            }
        }

        return isInArray;
    }
}