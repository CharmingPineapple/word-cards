package com.example.remake_word_cards;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;


// (!) - обратить внимение, исправить, удалить
// (#) - существует только для тестов, в конечном - скрыть
// (*) - очень интересные вещи, которе работают (хотя, возможно, и не должны)


public class DictionaryFragment extends Fragment {

    // (!) - для поиска
    boolean searching;

    //Подключение БД для хранения данных из карточек
    CardsDBHelper cardsDBHelper;
    CustomCategoryDBHelper customCategoryDBHelper;
    StCategoryDBHelper stCategoryDBHelper;

    CardAdapter cardAdapter;

    //Кнопка добавления карточек
    ImageView IV_add_card;
    //ImageView IV_search_cancel;

    //EditText ET_search;

    // Списки категорий нужны в виде ArrayList для их отображения в спинере
    ArrayList<String> st_category_array = new ArrayList<String>();
    ArrayList<String> custom_category_array = new ArrayList<String>();

    @Override
    public void onStop() {
        super.onStop();
        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
        Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            int id_index = cursor.getColumnIndex(CardsDBHelper.KEY_ID);
            int word_index = cursor.getColumnIndex(CardsDBHelper.KEY_WORD);

            do {
                if (cursor.getString(word_index).trim().equals(""))
                    database.delete(CardsDBHelper.TABLE_CARDS, CardsDBHelper.KEY_ID + " = ?", new String[]{cursor.getString(id_index)});
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);

        //Экземпляр БД. Пока оставляю в качестве контекста getActivity. Были проблемы из-за того, что БД в фрагменте
        cardsDBHelper = new CardsDBHelper(getActivity());
        customCategoryDBHelper = new CustomCategoryDBHelper(getActivity());
        stCategoryDBHelper = new StCategoryDBHelper(getActivity());

        // (#) Перманентное удаление. ВРЕМЕННО, ТОЛЬКО ДЛЯ ТЕСТОВ
        /*SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
        database.delete(CardsDBHelper.TABLE_CARDS, null,null);
        showBD();*/

        //Подключаем RecyclerView к адаптеру и layout менеджеру
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerCards = (RecyclerView) view.findViewById(R.id.id_RV_cards);
        cardAdapter = new CardAdapter();
        recyclerCards.setAdapter(cardAdapter);
        recyclerCards.setLayoutManager(layoutManager);

        // (!) Сделать передачу через intent из collection /Всё сделал через БД
        setSt_category_array();
        setCustom_category_array();

        //Создаём первую карточтку
        //НЕОБЯЗАТЕЛЬНО
        //addCard();

        //Подключаесм кнопку добавления карточек

        IV_add_card = (ImageView) view.findViewById(R.id.id_IV_add_card);
        IV_add_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCard();

                // (!) Уже не нужно, так как благодаря сортировке новая карточка появляется в начале списка
                // А нет, нужно, нам же в начало списка надо возвращаться
                // После добавления карточки RecyclerView прокручивается до неё (до конца списка)
                layoutManager.scrollToPosition(0);
            }
        });

        // (!)
        /*ET_search = (EditText) view.findViewById(R.id.id_ET_search);
        ET_search.getBackground()
                .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);

        IV_search_cancel = (ImageView) view.findViewById(R.id.id_IV_search_cancel);
        IV_search_cancel.setImageResource(R.drawable.ic_search);
        searching = false;

        // (!) (#)
        ET_search.setVisibility(View.GONE);
        IV_search_cancel.setVisibility(View.GONE);

        IV_search_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searching) {
                    searching = true;
                    IV_search_cancel.setImageResource(R.drawable.ic_cancel);
                    ET_search.setEnabled(false);
                    ET_search.getBackground()
                            .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                } else {
                    searching = false;
                    IV_search_cancel.setImageResource(R.drawable.ic_search);
                    ET_search.setEnabled(true);
                    ET_search.getBackground()
                            .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
                    ET_search.setText("");
                }

                cardAdapter.notifyDataSetChanged();
            }
        });*/

        return view;
    }


    private void addCard() {

        cardAdapter.adapterAddCard();
        cardAdapter.notifyDataSetChanged();

        /*cardAdapter.notifyItemChanged(cardAdapter.getItemCount());*/

    }

//////////////////////////////////////////////////////////////////////////////

    public class CardAdapter extends RecyclerView.Adapter<CardHolder> {

        //Массив холдеров карточек
        ArrayList<CardHolder> card_holder_array = new ArrayList<>();

        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

        //Сколько карточек создаётся изначально
        //ПЕРЕДЕЛАТЬ, ЧТОБЫ СОХРАНЯЛОСЬ КОЛИЧЕСТВО ВСЕХ КАРТОЧЕК
        //int cardsSize = foreign_words_array.size();
        //ВОЗМОЖНО, ВОЗМОЖНО, ОН СВЯЗАН ЧЕРЕЗ GETITEMCOUNT И ПОЭТОМУ ВЛИЯЕТ НА КОЛИЧЕСТВО ОРИСОВЫВАЕМЫХ КАРТОЧЕК
        int cardsSize = 0;


        //При создании новой карточки (холдера),
        // она добавляется в card_holder_array
        @Override
        public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //holder строится на основе card_rv_item
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_card, parent, false);

            CardHolder holder = new CardHolder(v);
            card_holder_array.add(holder);

            return holder;
        }

        //Отрисовка карточек
        @Override
        public void onBindViewHolder(CardHolder holder, int position) {
            Cursor cursor;


            /*if (ET_search.getText().toString().trim().equals("")){
                cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, CardsDBHelper.KEY_WORD + " ASC");
            } else {
                String[] selection_arg = new String[]{ET_search.getText().toString().trim()};
                String selection = "word = ?";

                cursor = database.query(CardsDBHelper.TABLE_CARDS, null, selection, selection_arg, null, null, CardsDBHelper.KEY_WORD + " ASC");
            }*/

            cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, CardsDBHelper.KEY_WORD + " ASC");

            cursor.moveToPosition(position);

            int id_index = cursor.getColumnIndex(CardsDBHelper.KEY_ID);

            int word_index = cursor.getColumnIndex(CardsDBHelper.KEY_WORD);
            int tran_1_index = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_1);
            int tran_2_index = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_2);
            int tran_3_index = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_3);
            int tran_4_index = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_4);
            int tran_5_index = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_5);
            int description_index = cursor.getColumnIndex(CardsDBHelper.KEY_DESCRIPTION);
            int st_category_index = cursor.getColumnIndex(CardsDBHelper.KEY_ST_CATEGORY);
            int custom_category_index = cursor.getColumnIndex(CardsDBHelper.KEY_CUSTOM_CATEGORY);

            // Необходимо присвоить карточке id, чтобы через него обращаться к БД.
            // position карточки и id строки в БД отличаются, именно поэтому в карточке должна быть запись о id её строки в БД
            holder.TV_card_id.setText(String.valueOf(cursor.getInt(id_index)));
            holder.TV_card_position.setText(String.valueOf(position));

            holder.ET_foreign_word.setText(cursor.getString(word_index));
            holder.ET_translate_1.setText(cursor.getString(tran_1_index));
            holder.ET_translate_2.setText(cursor.getString(tran_2_index));
            holder.ET_translate_3.setText(cursor.getString(tran_3_index));
            holder.ET_translate_4.setText(cursor.getString(tran_4_index));
            holder.ET_translate_5.setText(cursor.getString(tran_5_index));
            holder.ET_description.setText(cursor.getString(description_index));
            holder.spinner_st_category.setSelection(st_category_array.indexOf(cursor.getString(st_category_index)));

            // Если применённую к карточке категорию удалят, то на карточке она сбросится на "Не выбрано"
            if (custom_category_array.contains(cursor.getString(custom_category_index)))
                holder.spinner_custom_category.setSelection(custom_category_array.indexOf(cursor.getString(custom_category_index)));
            else
                holder.spinner_custom_category.setSelection(0);

            //holder.startEdit();
            holder.stopEdit();

            if (position == 0 && holder.ET_foreign_word.getText().toString().equals(""))
                holder.startEdit();

            cursor.close();
        }

        @Override
        public int getItemCount() {

            long db_size = 0;

            db_size = DatabaseUtils.queryNumEntries(database, CardsDBHelper.TABLE_CARDS);
            return (int) db_size;
        }

        void adapterAddCard() {
            //Замена на БД
            addRowBD("", "", "", "", "", "", "", st_category_array.get(0), custom_category_array.get(0));

            //showBD();
        }
    }

    class CardHolder extends RecyclerView.ViewHolder {

        // Нужно, потому что одна кнопка выполняет два разных действия в зависимости от editing
        boolean editing;

        TextView TV_card_id;
        TextView TV_card_position;

        EditText ET_foreign_word;

        EditText ET_translate_1;
        EditText ET_translate_2;
        EditText ET_translate_3;
        EditText ET_translate_4;
        EditText ET_translate_5;

        ArrayList<EditText> ET_translate_array;

        EditText ET_description;

        ImageView IV_edit_card;
        ImageView IV_delete_card;

        Spinner spinner_st_category;
        Spinner spinner_custom_category;

        String card_id;

        // В принципе в CardHolder всё нормально описано
        // Просто описано, какие элементы в нём находятся
        // ЗДЕСЬ ОСУЩЕСТВЛЯЕТСЯ РАБОТА ЭЛЕМЕНТОВ КАРТОЧКИ
        CardHolder(View itemView) {
            super(itemView);

            // (!)
            //Подключение БД
            SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            editing = false;

            // Присваивание id всех ET и TV_id
            TV_card_id = (TextView) itemView.findViewById(R.id.id_card_id);
            TV_card_position = (TextView) itemView.findViewById(R.id.id_card_position);
            // (#) Законментить при тестировании
            TV_card_id.setVisibility(View.GONE);
            TV_card_position.setVisibility(View.GONE);

            ET_foreign_word = (EditText) itemView.findViewById(R.id.id_ET_foreign_word);

            ET_translate_1 = (EditText) itemView.findViewById(R.id.id_ET_translate_1);
            ET_translate_2 = (EditText) itemView.findViewById(R.id.id_ET_translate_2);
            ET_translate_3 = (EditText) itemView.findViewById(R.id.id_ET_translate_3);
            ET_translate_4 = (EditText) itemView.findViewById(R.id.id_ET_translate_4);
            ET_translate_5 = (EditText) itemView.findViewById(R.id.id_ET_translate_5);

            ET_translate_array = new ArrayList<>();
            ET_translate_array.add(ET_translate_1);
            ET_translate_array.add(ET_translate_2);
            ET_translate_array.add(ET_translate_3);
            ET_translate_array.add(ET_translate_4);
            ET_translate_array.add(ET_translate_5);

            ET_description = (EditText) itemView.findViewById(R.id.id_ET_description);

            // Подключение spinner_st_category и spinner_custom category
            spinner_st_category = (Spinner) itemView.findViewById(R.id.id_spin_st_category);
            ArrayAdapter<String> adapter_st_category = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, st_category_array);
            adapter_st_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_st_category.setAdapter(adapter_st_category);

            spinner_custom_category = (Spinner) itemView.findViewById(R.id.id_spin_custom_category);
            ArrayAdapter<String> adapter_custom_category = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, custom_category_array);
            adapter_custom_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_custom_category.setAdapter(adapter_custom_category);

            // Подключение IV для удаления
            // Иконка менятся не будет.
            // При stopEdit - setVisibility(View.GONE) - IV исчезает
            IV_delete_card = (ImageView) itemView.findViewById(R.id.id_IV_delete_card);
            IV_delete_card.setImageResource(R.drawable.delete_icon);

            //ЗАПРЕТ РЕДАКТИРОВАНИЯ И ПРИВЕДЕНИЕ В ВИД КАРТОЧКИ ДЛЯ ЧТЕНИЯ
            IV_edit_card = (ImageView) itemView.findViewById(R.id.id_IV_edit_card);

            //УДАЛЕНИЕ КАРТОЧКИ
            IV_delete_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCard();
                }
            });

            //НАЧАЛО ИЛИ ОКОНЧАНИЕ РЕДАКТИРОВАНИЯ
            IV_edit_card.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    //ОКОНЧАНИЕ РЕДАКТИРОВАНИЯ
                    if (editing) {
                        //editing = false;
                        // Если карточка пустая (ET_foreign_word = ""), карточка удаляется
                        if (ET_foreign_word.getText().toString().trim().equals("")) deleteCard();
                        else {
                            stopEdit();
                            updateCard();
                        }
                    }

                    //НАЧАЛО РЕДАКТИРОВАНИЯ
                    else {
                        startEdit();
                    }
                }
            });
        }

        private void updateCard() {
            SQLiteDatabase database = cardsDBHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            card_id = TV_card_id.getText().toString();

            //Запись в БД конкретно этой строки (карточки)
            //Разве это не saveData?
            contentValues.put(CardsDBHelper.KEY_WORD, ET_foreign_word.getText().toString());
            contentValues.put(CardsDBHelper.KEY_TRAN_1, ET_translate_1.getText().toString());
            contentValues.put(CardsDBHelper.KEY_TRAN_2, ET_translate_2.getText().toString());
            contentValues.put(CardsDBHelper.KEY_TRAN_3, ET_translate_3.getText().toString());
            contentValues.put(CardsDBHelper.KEY_TRAN_4, ET_translate_4.getText().toString());
            contentValues.put(CardsDBHelper.KEY_TRAN_5, ET_translate_5.getText().toString());
            contentValues.put(CardsDBHelper.KEY_DESCRIPTION, ET_description.getText().toString());
            contentValues.put(CardsDBHelper.KEY_ST_CATEGORY, spinner_st_category.getSelectedItem().toString());
            contentValues.put(CardsDBHelper.KEY_CUSTOM_CATEGORY, spinner_custom_category.getSelectedItem().toString());

            int updCount = database.update(CardsDBHelper.TABLE_CARDS, contentValues, CardsDBHelper.KEY_ID + " = ?", new String[]{card_id});
            //Log.d("mLog", "updates rows count - " + updCount);

            cardAdapter.notifyDataSetChanged();
            // showBD();
        }

        private void deleteCard() {
            SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

            card_id = TV_card_id.getText().toString();

            // (!) Нужно ли if
            if (!card_id.equals("")) {
                int delCount = database.delete(CardsDBHelper.TABLE_CARDS, CardsDBHelper.KEY_ID + " = ?", new String[]{card_id});

                // Log.d("mLog", "deleted rows count - " + delCount);
            }
            cardAdapter.notifyDataSetChanged();
            //showBD();
        }

        private void stopEdit() {

            editing = false;

            IV_delete_card.setVisibility(View.GONE);
            IV_edit_card.setImageResource(R.drawable.edit_icon);

            //ЗАПРЕТ РЕДАКТИРОВАНИЯ И ПРИВЕДЕНИЕ В ВИД КАРТОЧКИ ДЛЯ ЧТЕНИЯ

            // Запрет редактирования spinner
            spinner_st_category.setEnabled(false);
            spinner_custom_category.setEnabled(false);

            //Запрет редактирования иностранного слова
            ET_foreign_word.setEnabled(false);
            ET_foreign_word.getBackground()
                    .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

            //Скорытие или запрет редактирования переводов
            for (EditText ET_current : ET_translate_array) {
                if (ET_current.getText().toString().trim().equals("")) {
                    ET_current.setText("");
                    ET_current.setVisibility(View.GONE);
                } else {
                    ET_current.setEnabled(false);
                    ET_current.getBackground()
                            .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
                }
            }

            //Сокрытие или запрет редактирования описания
            if (ET_description.getText().toString().trim().equals(""))
                ET_description.setVisibility(View.GONE);
            else {
                ET_description.setEnabled(false);
                ET_description.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }

        private void startEdit() {

            editing = true;

            IV_delete_card.setVisibility(View.VISIBLE);
            IV_edit_card.setImageResource(R.drawable.done_icon);

            spinner_st_category.setEnabled(true);
            spinner_custom_category.setEnabled(true);

            ET_foreign_word.setEnabled(true);
            ET_foreign_word.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);

            //Появление или разрешение редактирования переводов
            for (EditText ET_current : ET_translate_array) {
                ET_current.setVisibility(View.VISIBLE);
                ET_current.setEnabled(true);
                ET_current.getBackground()
                        .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            }

            //Появление или разрешение редактирования описания
            ET_description.setVisibility(View.VISIBLE);
            ET_description.setEnabled(true);
            ET_description.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }

    }

    public void showBD() {
        //Показ того, что есть в БД
        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

        Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int id_index = cursor.getColumnIndex(CardsDBHelper.KEY_ID);
            int word_index = cursor.getColumnIndex(CardsDBHelper.KEY_WORD);
            int tran_1_index = cursor.getColumnIndex(CardsDBHelper.KEY_TRAN_1);

            do {
                Log.d("mLog", "ID = " + cursor.getString(id_index) + "; word = " + cursor.getString(word_index) + "; tran_1 = " + cursor.getString(tran_1_index));
            } while (cursor.moveToNext());
        } else Log.d("mLog", "0 rows");

        cursor.close();
    }

    public void addRowBD(String word, String tran_1, String tran_2, String tran_3, String tran_4, String tran_5, String description, String st_category, String custom_category) {
        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(CardsDBHelper.KEY_WORD, word);
        contentValues.put(CardsDBHelper.KEY_TRAN_1, tran_1);
        contentValues.put(CardsDBHelper.KEY_TRAN_2, tran_2);
        contentValues.put(CardsDBHelper.KEY_TRAN_3, tran_3);
        contentValues.put(CardsDBHelper.KEY_TRAN_4, tran_4);
        contentValues.put(CardsDBHelper.KEY_TRAN_5, tran_5);
        contentValues.put(CardsDBHelper.KEY_DESCRIPTION, description);
        contentValues.put(CardsDBHelper.KEY_ST_CATEGORY, st_category);
        contentValues.put(CardsDBHelper.KEY_CUSTOM_CATEGORY, custom_category);

        database.insert(CardsDBHelper.TABLE_CARDS, null, contentValues);
        contentValues.clear();
    }

    // Названия стандартных категорий берутся из БД StCategoryDBHelper
    public void setSt_category_array() {
        SQLiteDatabase database = stCategoryDBHelper.getWritableDatabase();

        Cursor cursor = database.query(StCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int category_name_index = cursor.getColumnIndex(StCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                st_category_array.add(cursor.getString(category_name_index));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Названия пользовательских категорий берутся из БД CustomCategoryDBHelper
    // её изменение описано в CollectionFragment
    public void setCustom_category_array() {
        custom_category_array.add("Not selected");

        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();

        Cursor cursor = database.query(CustomCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int category_name_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                custom_category_array.add(cursor.getString(category_name_index));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}