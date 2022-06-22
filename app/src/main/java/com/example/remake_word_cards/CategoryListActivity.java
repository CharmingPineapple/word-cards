package com.example.remake_word_cards;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.os.Bundle;
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

public class CategoryListActivity extends AppCompatActivity {

    CardsDBHelper cardsDBHelper;
    CustomCategoryDBHelper customCategoryDBHelper;
    StCategoryDBHelper stCategoryDBHelper;

    CategoryListCardsAdapter categoryListCardsAdapter;

    TextView TV_category_list_name;

    ImageView IV_add_card;

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
                if (cursor.getString(word_index).equals(""))
                    database.delete(CardsDBHelper.TABLE_CARDS, CardsDBHelper.KEY_ID + " = ?", new String[]{cursor.getString(id_index)});
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        // (!) Используется в getItemCount, здесь не нужно
        // Получение названия и типа выбранной категории
        Bundle arguments = getIntent().getExtras();
        String category_name = arguments.get("category_name").toString();
        String category_type = arguments.get("category_type").toString();

        TV_category_list_name = findViewById(R.id.id_TV_category_list_name);
        TV_category_list_name.setText(category_name);

        cardsDBHelper = new CardsDBHelper(this);
        customCategoryDBHelper = new CustomCategoryDBHelper(this);
        stCategoryDBHelper = new StCategoryDBHelper(this);

        setSt_category_array();
        setCustom_category_array();

        //Подключаем RecyclerView к адаптеру и layout менеджеру
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerCategoryListCards = (RecyclerView) findViewById(R.id.id_RV_category_list_cards);
        categoryListCardsAdapter = new CategoryListCardsAdapter();
        recyclerCategoryListCards.setAdapter(categoryListCardsAdapter);
        recyclerCategoryListCards.setLayoutManager(layoutManager);

        IV_add_card = (ImageView) findViewById(R.id.id_IV_add_card_in_category);
        IV_add_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryListCardsAdapter.adapterAddCard(category_type, category_name);

                // (!) Уже не нужно, так как благодаря сортировке новая карточка появляется в начале списка
                // А нет, нужно, нам же в начало списка надо возвращаться
                // После добавления карточки RecyclerView прокручивается до неё (до конца списка)
                layoutManager.scrollToPosition(0);
            }
        });

    }

    public class CategoryListCardsAdapter extends RecyclerView.Adapter<CategoryListCardHolder> {

        ArrayList<CategoryListCardHolder> card_holder_array = new ArrayList<>();

        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();


        @Override
        public CategoryListCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //holder строится на основе card_rv_item
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_card, parent, false);

            CategoryListCardHolder holder = new CategoryListCardHolder(v);
            card_holder_array.add(holder);

            return holder;
        }

        @Override
        public void onBindViewHolder(CategoryListCardHolder holder, int position) {

            Bundle arguments = getIntent().getExtras();
            String category_name = arguments.get("category_name").toString();
            String category_type = arguments.get("category_type").toString();

            String selection;

            if (category_type.equals("custom_category"))
                selection = "custom_category = ?";
            else
                selection = "st_category = ?";

            String[] selectionArgs = new String[]{category_name};

            // Берём не всю БД, а только строки с заданной категорией
            Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, selection, selectionArgs, null, null, CardsDBHelper.KEY_WORD + " ASC");

            //Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, CardsDBHelper.KEY_WORD + " ASC");

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

            // (!) проверить отрисовку / st_category отрисовывается нормально, а custom иногда нормально, а иногда отрисовываются пустые холдеры
            // то есть подсёт карточек правильный, а поиск этих карточек - нет
            // пока оставлю как есть, потому что по логике всё правильно да и работает исправно после перезагрузки
            /*int checking_category_index;

            if (category_type.equals("custom_category"))
                checking_category_index = custom_category_index;
            else
                checking_category_index = st_category_index;*/


            //if (cursor.getString(checking_category_index).equals(category_name)) {

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

                holder.spinner_custom_category.setSelection(custom_category_array.indexOf(cursor.getString(custom_category_index)));

                //holder.startEdit();
                holder.stopEdit();

            if (position == 0 && holder.ET_foreign_word.getText().toString().equals(""))
                holder.startEdit();

            cursor.close();


        }

        @Override
        public int getItemCount() {

            int cards_size = 0;

            Bundle arguments = getIntent().getExtras();
            String category_name = arguments.get("category_name").toString();
            String category_type = arguments.get("category_type").toString();

            cards_size = countCards(category_name, category_type);

            return cards_size;
        }

        void adapterAddCard(String category_type, String category_name) {

            if (category_type.equals("st_category"))
                addRowBD("", "", "", "", "", "", "", category_name, custom_category_array.get(0));
            else
                addRowBD("", "", "", "", "", "", "", st_category_array.get(0), category_name);

            categoryListCardsAdapter.notifyDataSetChanged();
            showBD();
        }

    }

    class CategoryListCardHolder extends RecyclerView.ViewHolder {
        // (!) Заблокировать, в зависимости от categoryType, либо spinner_st_category, либо spinner_custom_category

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

        EditText ET_description;

        ImageView IV_edit_card;
        ImageView IV_delete_card;

        Spinner spinner_st_category;
        Spinner spinner_custom_category;

        String card_id;

        CategoryListCardHolder(View itemView) {
            super(itemView);



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

            ET_description = (EditText) itemView.findViewById(R.id.id_ET_description);

            // (!) - я сделал itemView.getContext() вместо this и кажется оно должно работать
            // Подключение spinner_st_category и spinner_custom category
            spinner_st_category = (Spinner) itemView.findViewById(R.id.id_spin_st_category);
            ArrayAdapter<String> adapter_st_category = new ArrayAdapter<String>(itemView.getContext(), R.layout.spinner_item, st_category_array);
            adapter_st_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_st_category.setAdapter(adapter_st_category);

            spinner_custom_category = (Spinner) itemView.findViewById(R.id.id_spin_custom_category);
            ArrayAdapter<String> adapter_custom_category = new ArrayAdapter(itemView.getContext(), R.layout.spinner_item, custom_category_array);
            adapter_custom_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_custom_category.setAdapter(adapter_custom_category);

            // Нельзя изменять spinner категории в листе этой же категории
            /*if (category_type.equals("st_category"))
                spinner_st_category.setEnabled(false);
            else
                spinner_custom_category.setEnabled(false);*/


            // Подключение IV для удаления
            // Иконка менятся не будет.
            // При stopEdit - setVisibility(View.GONE) - IV исчезает
            IV_delete_card = (ImageView) itemView.findViewById(R.id.id_IV_delete_card);
            IV_delete_card.setImageResource(R.drawable.delete_icon);

            //ЗАПРЕТ РЕДАКТИРОВАНИЯ И ПРИВЕДЕНИЕ В ВИД КАРТОЧКИ ДЛЯ ЧТЕНИЯ
            IV_edit_card = (ImageView) itemView.findViewById(R.id.id_IV_edit_card);
            //if (editing) startEdit();
            //else stopEdit();

            // кажется проблема в том что сначала был stopedit, а потом присвоение текста
            //stopEdit();

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
                        if (ET_foreign_word.getText().toString().equals("")) deleteCard();
                        else {
                            updateCard();

                            // (!) РАЗОБРАТЬСЯ С saveData
                            //saveData();

                            stopEdit();
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
            Log.d("mLog", "updates rows count - " + updCount);

            categoryListCardsAdapter.notifyDataSetChanged();
            showBD();
        }

        private void deleteCard() {
            // Решает проблему перенимания состояния удалённого холдера новым на его месте
            //stopEdit();

            SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

            card_id = TV_card_id.getText().toString();

            //Нужно ли if
            if (!card_id.equalsIgnoreCase("")) {
                int delCount = database.delete(CardsDBHelper.TABLE_CARDS, CardsDBHelper.KEY_ID + " = ?", new String[]{card_id});

                Log.d("mLog", "deleted rows count - " + delCount);
            }
            categoryListCardsAdapter.notifyDataSetChanged();
            showBD();
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
            //1
            if (ET_translate_1.getText().toString().equals(""))
                ET_translate_1.setVisibility(View.GONE);
            else {
                ET_translate_1.setEnabled(false);
                ET_translate_1.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
            //2
            if (ET_translate_2.getText().toString().equals(""))
                ET_translate_2.setVisibility(View.GONE);
            else {
                ET_translate_2.setEnabled(false);
                ET_translate_2.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
            //3
            if (ET_translate_3.getText().toString().equals(""))
                ET_translate_3.setVisibility(View.GONE);
            else {
                ET_translate_3.setEnabled(false);
                ET_translate_3.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
            //4
            if (ET_translate_4.getText().toString().equals(""))
                ET_translate_4.setVisibility(View.GONE);
            else {
                ET_translate_4.setEnabled(false);
                ET_translate_4.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
            //5
            if (ET_translate_5.getText().toString().equals(""))
                ET_translate_5.setVisibility(View.GONE);
            else {
                ET_translate_5.setEnabled(false);
                ET_translate_5.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }


            //Сокрытие или запрет редактирования описания
            if (ET_description.getText().toString().equals(""))
                ET_description.setVisibility(View.GONE);
            else {
                ET_description.setEnabled(false);
                ET_description.getBackground()
                        .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }
        }

        private void startEdit() {

            Bundle arguments = getIntent().getExtras();
            String category_type = arguments.get("category_type").toString();

            editing = true;

            IV_delete_card.setVisibility(View.VISIBLE);
            IV_edit_card.setImageResource(R.drawable.done_icon);

            if (category_type.equals("st_category"))
                spinner_custom_category.setEnabled(true);
            else
                spinner_st_category.setEnabled(true);


            ET_foreign_word.setEnabled(true);
            ET_foreign_word.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);

            //Появление или разрешение редактирования переводов
            //1
            ET_translate_1.setVisibility(View.VISIBLE);
            ET_translate_1.setEnabled(true);
            ET_translate_1.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            //2
            ET_translate_2.setVisibility(View.VISIBLE);
            ET_translate_2.setEnabled(true);
            ET_translate_2.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            //3
            ET_translate_3.setVisibility(View.VISIBLE);
            ET_translate_3.setEnabled(true);
            ET_translate_3.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            //4
            ET_translate_4.setVisibility(View.VISIBLE);
            ET_translate_4.setEnabled(true);
            ET_translate_4.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
            //5
            ET_translate_5.setVisibility(View.VISIBLE);
            ET_translate_5.setEnabled(true);
            ET_translate_5.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);


            //Появление или разрешение редактирования описания
            ET_description.setVisibility(View.VISIBLE);
            ET_description.setEnabled(true);
            ET_description.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }

    }

    // (!) надеюсь, что он правда нормально считает
    public int countCards(String category_name, String category_type) {

        int cards_size = 0;

        SQLiteDatabase database = cardsDBHelper.getWritableDatabase();

        Cursor cursor = database.query(CardsDBHelper.TABLE_CARDS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int category_name_index;

            if (category_type.equals("st_category"))
                category_name_index = cursor.getColumnIndex(CardsDBHelper.KEY_ST_CATEGORY);
            else
                category_name_index = cursor.getColumnIndex(CardsDBHelper.KEY_CUSTOM_CATEGORY);

            do {
                if (cursor.getString(category_name_index).equals(category_name)) cards_size++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        Log.d("mLog", "Карточек с той же категорией = " + cards_size);

        return cards_size;
    }

    public void setSt_category_array() {
        SQLiteDatabase database = stCategoryDBHelper.getWritableDatabase();

        Cursor cursor = database.query(StCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int category_name_index = cursor.getColumnIndex(StCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                st_category_array.add(cursor.getString(category_name_index));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    public void setCustom_category_array() {
        custom_category_array.add("Не выбрано");

        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();

        Cursor cursor = database.query(CustomCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int category_name_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                custom_category_array.add(cursor.getString(category_name_index));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    public void showBD() {
        //Показ того, что есть
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

        //addRowBD("new word", "", "", "", "", "", "", st_category_array[0], custom_category_array[0]);

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

}