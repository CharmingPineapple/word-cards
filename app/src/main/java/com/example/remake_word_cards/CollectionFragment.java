package com.example.remake_word_cards;

import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

// (!) - обратить внимение, исправить, удалить
// (#) - существует только для тестов, в конечном - скрыть
// (*) - очень интересные вещи, которе работают (хотя, возможно, и не должны)

public class CollectionFragment extends Fragment {

    CustomCategoryDBHelper customCategoryDBHelper;
    StCategoryDBHelper stCategoryDBHelper;

    CustomCategoryAdapter customCategoryAdapter;
    StCategoryAdapter stCategoryAdapter;

    //Кнопка добавления карточек
    ImageView IV_add_category;

    // (!) Заменить на БД
    ArrayList<String> custom_category_array = new ArrayList<>();
    // (!) - мне не нравится, что в каждом классе мне приходится заполнять этот массив одинаково,
    // нужно как-то передавать данные, чтобы не изменять данные в каждом классе
    // возможно, БД
    ArrayList<String> st_category_array = new ArrayList<>();

    @Override
    public void onStop() {
        super.onStop();
        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();
        Cursor cursor = database.query(CustomCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            int id_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_ID);
            int category_name_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                if (cursor.getString(category_name_index).trim().equals("")) database.delete(CustomCategoryDBHelper.TABLE_CATEGORY, CustomCategoryDBHelper.KEY_ID + " = ?", new String[]{cursor.getString(id_index)});
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        stCategoryDBHelper = new StCategoryDBHelper(getActivity());
        customCategoryDBHelper = new CustomCategoryDBHelper(getActivity());
        // (#) Перманентное удаление. ВРЕМЕННО, ТОЛЬКО ДЛЯ ТЕСТОВ
        /*SQLiteDatabase database = categoryDBHelper.getWritableDatabase();
        database.delete(CategoryDBHelper.TABLE_CATEGORY, null,null);
        showBD();*/

        // Данные для массива st_category_array
        setSt_category_array();

        LinearLayoutManager linearLayoutManager_st_category = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        LinearLayoutManager linearLayoutManager_custom_category = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };

        //Подключаем RecyclerView к адаптеру и layout менеджеру
        // RecyclerView StCategory
        RecyclerView recyclerStCategory = (RecyclerView) view.findViewById(R.id.id_RV_st_category);
        stCategoryAdapter = new StCategoryAdapter();
        recyclerStCategory.setAdapter(stCategoryAdapter);
        recyclerStCategory.setLayoutManager(linearLayoutManager_st_category);

        // RecyclerView CustomCategory
        RecyclerView recyclerCustomCategory = (RecyclerView) view.findViewById(R.id.id_RV_custom_category);
        customCategoryAdapter = new CustomCategoryAdapter();
        recyclerCustomCategory.setAdapter(customCategoryAdapter);
        recyclerCustomCategory.setLayoutManager(linearLayoutManager_custom_category);

        // Кнопка добавления кастомных категорий
        IV_add_category = (ImageView) view.findViewById(R.id.id_IV_add_category);
        IV_add_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
                linearLayoutManager_custom_category.scrollToPosition(0);
            }
        });

        return view;
    }

    private void addCategory() {
        saveData();

        customCategoryAdapter.adapterAddCategory();
        customCategoryAdapter.notifyDataSetChanged();
        // (!)
        /*cardAdapter.notifyItemChanged(cardAdapter.getItemCount());*/
    }

    //!!!!-РАЗОБРАТЬСЯ-!!// ну вроде всё понятно но //ЗАМЕНИТЬ НА БД
    private void saveData() {
        custom_category_array.clear();


        for (CustomCategoryHolder holder : customCategoryAdapter.custom_category_holder_array) {
            custom_category_array.add(holder.ET_custom_category_name.getText().toString());

            //holder.saveData();
        }
    }

//////////////////////////////////////////////////////////////////////////////

    public class CustomCategoryAdapter extends RecyclerView.Adapter<CustomCategoryHolder> {

        ArrayList<CustomCategoryHolder> custom_category_holder_array = new ArrayList<>();

        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();

        @Override
        public CustomCategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //holder строится на основе rv_item_st_category
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_custom_category, parent, false);

            CustomCategoryHolder holder = new CustomCategoryHolder(v);
            custom_category_holder_array.add(holder);

            return holder;
        }

        @Override
        public void onBindViewHolder(CustomCategoryHolder holder, int position) {
            Cursor cursor = database.query(CustomCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, CustomCategoryDBHelper.KEY_CATEGORY_NAME + " ASC");

            cursor.moveToPosition(position);

            int id_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_ID);
            int category_name_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_CATEGORY_NAME);

            holder.TV_custom_category_id.setText(String.valueOf(cursor.getInt(id_index)));
            holder.ET_custom_category_name.setText(cursor.getString(category_name_index));

            holder.stopEdit();

            if (position == 0 && holder.ET_custom_category_name.getText().toString().equals(""))
                holder.startEdit();

            cursor.close();
        }

        @Override
        public int getItemCount() {
            long db_size = 0;

            db_size = DatabaseUtils.queryNumEntries(database, CustomCategoryDBHelper.TABLE_CATEGORY);
            return (int) db_size;
        }

        void adapterAddCategory() {
            addRowBD("");

            showBD();
        }
    }

    class CustomCategoryHolder extends RecyclerView.ViewHolder {

        boolean editing;

        TextView TV_custom_category_id;

        EditText ET_custom_category_name;

        ImageView IV_edit_category;
        ImageView IV_delete_category;
        ImageView IV_open_custom_category;

        String category_id;

        CustomCategoryHolder(View itemView) {
            super(itemView);

            editing = false;

            TV_custom_category_id = (TextView) itemView.findViewById(R.id.id_TV_category_id);
            // (#) Законментить при тестировании
            TV_custom_category_id.setVisibility(View.GONE);

            ET_custom_category_name = (EditText) itemView.findViewById(R.id.id_ET_custom_category_name);

            IV_edit_category = (ImageView) itemView.findViewById(R.id.id_IV_edit_category);
            IV_delete_category = (ImageView) itemView.findViewById(R.id.id_IV_delete_category);
            IV_open_custom_category = (ImageView) itemView.findViewById(R.id.id_IV_open_custom_category);

            IV_edit_category.setImageResource(R.drawable.done_icon);
            IV_delete_category.setImageResource(R.drawable.delete_icon);
            IV_open_custom_category.setImageResource(R.drawable.ic_next);

            IV_open_custom_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // (!) Понять почему используется getActivity
                    Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                    intent.putExtra("category_name", ET_custom_category_name.getText().toString());
                    intent.putExtra("category_type", "custom_category");
                    startActivity(intent);
                }
            });

            IV_delete_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCategory();
                }
            });

            IV_edit_category.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    //Окончание редактирования
                    if (editing) {
                        if (ET_custom_category_name.getText().toString().trim().equals(""))
                            deleteCategory();
                        else {
                            updateCategory();

                            // (!) РАЗОБРАТЬСЯ С saveData
                            saveData();

                            stopEdit();
                        }

                    }
                    //Начало редактирования
                    else {
                        startEdit();
                    }
                }
            });
        }

        private void updateCategory() {
            SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            category_id = TV_custom_category_id.getText().toString();

            //Запись в БД конкретно этой строки (категории)
            //Разве это не saveData?
            contentValues.put(CustomCategoryDBHelper.KEY_CATEGORY_NAME, ET_custom_category_name.getText().toString());

            int updCount = database.update(CustomCategoryDBHelper.TABLE_CATEGORY, contentValues, CustomCategoryDBHelper.KEY_ID + " = ?", new String[]{category_id});

            Log.d("mLog", "updates rows count - " + updCount);

            customCategoryAdapter.notifyDataSetChanged();
            showBD();
        }

        private void deleteCategory() {
            SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();

            category_id = TV_custom_category_id.getText().toString();

            //Нужно ли if
            if (!category_id.equalsIgnoreCase("")) {
                int delCount = database.delete(CustomCategoryDBHelper.TABLE_CATEGORY, CustomCategoryDBHelper.KEY_ID + " = ?", new String[]{category_id});

                Log.d("mLog", "deleted rows count - " + delCount);
            }
            customCategoryAdapter.notifyDataSetChanged();
            showBD();

        }

        private void stopEdit() {
            editing = false;

            IV_delete_category.setVisibility(View.GONE);
            IV_edit_category.setImageResource(R.drawable.edit_icon);
            IV_open_custom_category.setVisibility(View.VISIBLE);

            ET_custom_category_name.setEnabled(false);
            ET_custom_category_name.getBackground()
                    .setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        }

        private void startEdit() {
            editing = true;

            IV_delete_category.setVisibility(View.VISIBLE);
            IV_edit_category.setImageResource(R.drawable.done_icon);
            IV_open_custom_category.setVisibility(View.GONE);

            ET_custom_category_name.setEnabled(true);
            ET_custom_category_name.getBackground()
                    .setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }
    }

    ////////////////////////////////////////////////////////////
    public class StCategoryAdapter extends RecyclerView.Adapter<StCategoryHolder> {

        ArrayList<StCategoryHolder> st_category_holder_array = new ArrayList<>();


        @Override
        public StCategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //holder строится на основе rv_item_st_category
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_st_category, parent, false);

            StCategoryHolder holder = new StCategoryHolder(v);
            st_category_holder_array.add(holder);

            return holder;
        }

        @Override
        public void onBindViewHolder(StCategoryHolder holder, int position) {
            holder.TV_st_category_name.setText(st_category_array.get(position));
        }

        @Override
        public int getItemCount() {
            return st_category_array.size();
        }

    }

    class StCategoryHolder extends RecyclerView.ViewHolder {

        ImageView IV_open_st_category;

        TextView TV_st_category_name;

        StCategoryHolder(View itemView) {
            super(itemView);

            IV_open_st_category = (ImageView) itemView.findViewById(R.id.id_IV_open_st_category);

            TV_st_category_name = (TextView) itemView.findViewById(R.id.id_TV_st_category_name);

            IV_open_st_category.setImageResource(R.drawable.ic_next);

            IV_open_st_category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                    intent.putExtra("category_name", TV_st_category_name.getText().toString());
                    intent.putExtra("category_type", "st_category");
                    startActivity(intent);
                }
            });
        }
    }

    public void addRowBD(String custom_category_name) {

        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(CustomCategoryDBHelper.KEY_CATEGORY_NAME, custom_category_name);

        database.insert(CustomCategoryDBHelper.TABLE_CATEGORY, null, contentValues);
        contentValues.clear();
    }

    public void showBD() {
        //Показ того, что есть
        SQLiteDatabase database = customCategoryDBHelper.getWritableDatabase();

        Cursor cursor = database.query(CustomCategoryDBHelper.TABLE_CATEGORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {

            int id_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_ID);
            int category_name_index = cursor.getColumnIndex(CustomCategoryDBHelper.KEY_CATEGORY_NAME);

            do {
                Log.d("mLog", "ID = " + cursor.getString(id_index) + "; cat_name = " + cursor.getString(category_name_index));
            } while (cursor.moveToNext());
        } else Log.d("mLog", "0 rows");

        cursor.close();
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


}