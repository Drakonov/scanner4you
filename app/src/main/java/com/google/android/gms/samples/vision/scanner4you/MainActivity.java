package com.google.android.gms.samples.vision.scanner4you;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends Activity implements View.OnClickListener {

    DBHelper dbHelper;
    Button btn_table;
    Button btn_import;
    Button btn_export;
    //String FILENAME_INPUT = "/sdcard/pou.csv";
    //String FILENAME_EXPORT = "/storage/32FC-8A50/Android/data/com.google.android.gms/files";
    //String FILENAME_INPUT = "pou.csv";
    //String FILENAME_EXPORT = "pouExit.csv";

    public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private static final int RC_BARCODE_CAPTURE = 9001;
    //private static final String TAG = "BarcodeMain";
    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    boolean CHEEP = false;
    ImageView GO;


    AlertDialog.Builder ad;
    Context context;

    String title = "Проверьте правильность данных";
    String button1String = "Сохранить";
    String button2String = "Отменить";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GO=findViewById(R.id.imageView2);
        statusMessage = findViewById(R.id.status_message);
        barcodeValue = findViewById(R.id.barcode_value);


        autoFocus = findViewById(R.id.auto_focus);
        useFlash = findViewById(R.id.use_flash);



        findViewById(R.id.read_barcode).setOnClickListener(this);
        GO.setOnClickListener(this);



        autoFocus.setChecked(true);

        dbHelper = new DBHelper(this);


        int permissionStatusW = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStatusW == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
            Intent intent;
            intent = new Intent(this, activity_login.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {

        //main
        switch (v.getId()) {
            case R.id.read_barcode:
                // launch barcode activity.
                Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;

            case R.id.imageView2:
                // launch barcode activity.
                intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;

            case R.id.btn_import:

                break;

            case R.id.btn_export:
                //Need more program code
                //ExportFile();
                //exportDB();
                break;


            default:
                break;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case 105:
                Intent intent;
                intent = new Intent(this, activity_press.class);
                startActivity(intent);
                break;
            case 104:
                item.setChecked(!item.isChecked());
                CHEEP = item.isChecked();
                break;
            case R.id.action_table:

                intent = new Intent(this, TableActivity.class);
                startActivity(intent);
                break;
            case R.id.action_export:
                exportDB(CHEEP);
                break;
            case R.id.action_import:
                Log.d("mLog", "Key insert pressed");
                OpenFileDialog fileDialog = new OpenFileDialog(this)
                        .setFilter(".*\\.csv")
                        .setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                            @Override
                            public void OnSelectedFile(String fileName) {
                                new InsertFile(fileName);
                                Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_LONG).show();
                            }
                        });
                fileDialog.show();

                break;

        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.add(2, 104, 4, "Шифровать").setCheckable(true);
        menu.add(2, 105, 4, "Ввод данных");



        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        final ContentValues contentvalues = new ContentValues();

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {

                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Log.d("mLog", "Qr код найден");
                    statusMessage.setText(R.string.barcode_success);

                    String str = barcode.displayValue;
                    Log.d("mLog", "Начало разбиения");
                    String[] subStr;
                    String delimeter = ";"; // Разделитель
                    subStr = str.split(delimeter);//subStr[i]= массив с данными строки (разбитый)

                    for (int i = 0; i < subStr.length; i++) {
                        switch (i) {
                            case 0:
                                contentvalues.put(DBHelper.KEY_Serial, subStr[i]);
                                break;
                            case 1:
                                contentvalues.put(DBHelper.KEY_Type, subStr[i]);
                                break;
                            case 2:
                                contentvalues.put(DBHelper.KEY_Model, subStr[i]);
                                break;
                            case 3:
                                contentvalues.put(DBHelper.KEY_Inventary, subStr[i]);
                                break;
                            case 4:
                                contentvalues.put(DBHelper.KEY_Department, subStr[i]);
                                break;
                            case 5:
                                contentvalues.put(DBHelper.KEY_ID, subStr[i]);
                                break;
                            default:
                                break;
                        }
                    }
                    Log.d("mLog", "Конец разбития");
                    if (contentvalues.get(DBHelper.KEY_Serial) != null &&
                            contentvalues.get(DBHelper.KEY_Type) != null &&
                            contentvalues.get(DBHelper.KEY_Model) != null &&
                            contentvalues.get(DBHelper.KEY_Inventary) != null &&
                            contentvalues.get(DBHelper.KEY_Department) != null &&
                            contentvalues.get(DBHelper.KEY_ID) != null) {
                        Log.d("mLog", "Вывод диалога");

                        context = MainActivity.this;
                        ad = new AlertDialog.Builder(context);
                        ad.setTitle(title);  // заголовок
                        ad.setMessage("Серийный номер: " + contentvalues.getAsString(DBHelper.KEY_Serial) + "\n" +
                                         "Тип оборудования: " + contentvalues.getAsString(DBHelper.KEY_Type) + "\n" +
                                          "Модель: " + contentvalues.getAsString(DBHelper.KEY_Inventary) + "\n" +
                                             "Инвентарный номер: " + contentvalues.getAsString(DBHelper.KEY_Inventary) + "\n" +
                                              "Расположение: " + contentvalues.getAsString(DBHelper.KEY_Department)); // сообщение
                        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                insertOrUpdate(contentvalues);
                                Toast.makeText(context, "Данные добавлены/обновлены", Toast.LENGTH_LONG).show();
                            }
                        });
                        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {

                                Log.d("mLog", "Cancel диалога");
                            }
                        });
                        ad.setCancelable(true);
                        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                Toast.makeText(context, "Вы ничего не выбрали",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        ad.show();
                        Log.d("mLog", "Конец диалога");

                        //insertOrUpdate(contentvalues);
                    }else {
                        barcodeValue.setText(barcode.displayValue);
                        Log.d("mLog", "Barcode read: " + barcode.displayValue);
                    }
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d("mLog", "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        dbHelper.close();
    }

    public void insertOrUpdate(ContentValues cv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int id = getID(cv);
        if (id == -1)
            db.insert(DBHelper.TABLE_CONTACTS, null, cv);
        else
            db.update(DBHelper.TABLE_CONTACTS, cv, "id=?", new String[]{Integer.toString(id)});
    }

    private int getID(ContentValues cv) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(DBHelper.TABLE_CONTACTS, new String[]{"id"}, "id =? AND Serial=? AND Inventary=?",
                new String[]{cv.getAsString(DBHelper.KEY_ID), cv.getAsString(DBHelper.KEY_Serial), cv.getAsString(DBHelper.KEY_Inventary)}, null, null, null, null);

        if (c.moveToFirst()) //if the row exist then return the id
            return c.getInt(c.getColumnIndex("id"));
        return -1;
    }

    public class InsertFile {
        InsertFile(String fileName) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String s;
            new Message("Установка буффера");
            int permissionStatusR = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionStatusR == PackageManager.PERMISSION_GRANTED) {
                try (BufferedReader br = new BufferedReader(new FileReader
                        (fileName))) {
                    //чтение построчно
                    new Message("Начало портирования");
                    db.beginTransaction();
                    while ((s = br.readLine()) != null) {
                        String[] colums = s.split(";");
                        if (colums.length != 6) {
                            Log.d("mLog", "Skipping Bad CSV Row");
                            continue;
                        }
                        ContentValues contentvalues = new ContentValues(5);

                        contentvalues.put(DBHelper.KEY_Serial, colums[0].trim());
                        contentvalues.put(DBHelper.KEY_Type, colums[1].trim());
                        contentvalues.put(DBHelper.KEY_Model, colums[2].trim());
                        contentvalues.put(DBHelper.KEY_Inventary, colums[3].trim());
                        contentvalues.put(DBHelper.KEY_Department, colums[4].trim());
                        contentvalues.put(DBHelper.KEY_ID, colums[5].trim());

                        if (contentvalues.get(DBHelper.KEY_Serial) != null &&
                                contentvalues.get(DBHelper.KEY_Type) != null &&
                                contentvalues.get(DBHelper.KEY_Model) != null &&
                                contentvalues.get(DBHelper.KEY_Inventary) != null &&
                                contentvalues.get(DBHelper.KEY_Department) != null &&
                                contentvalues.get(DBHelper.KEY_ID) != null) {
                            insertOrUpdate(contentvalues);
                        }

                        System.out.println(s);
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    new Message("Конец портирования");
                } catch (IOException ex) {
                    new Message("Ошибка портирования");
                    System.out.println(ex.getMessage());
                }

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            }
        }
    }

    public class Message {
        Message(String s) {
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
        }
    }

    public static class LogMessage {
        LogMessage(String s) {
            Log.d("mLog", s);
        }
    }


    @SuppressLint({"SetWorldWritable", "SetWorldReadable"})
    private void exportDBone() {

        File dbFile = getDatabasePath("contactBD.db");
        DBHelper dbhelper = new DBHelper(getApplicationContext());
        File exportDir = new File("/sdcard/my-logs", "");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "pouExit.csv");
        try {
            file.createNewFile();

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file), ';');
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM contacts", null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to exprort
                String arrStr[] = {curCSV.getString(4), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            new Message("Success!");
        } catch (IOException IoEx) {
            Log.e("mLog", IoEx.getMessage(), IoEx);
            new Message("Error");
        } catch (Exception sqlEx) {
            Log.e("mLog", sqlEx.getMessage(), sqlEx);
            new Message("Error");
        }
    }

    private void exportDB(boolean CHEEP) {

        File dbFile = getDatabasePath("contactBD.db");
        DBHelper dbhelper = new DBHelper(getApplicationContext());
        File exportDir = new File("/sdcard", "");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "pouExit.csv");
        try {
            file.createNewFile();
            CSVWriter csvWrite = null;

            if (CHEEP) {
                csvWrite = new CSVWriter(new FileWriter(file), '*');
            } else {
                csvWrite = new CSVWriter(new FileWriter(file), ';');
            }
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM contacts", null);
            //csvWrite.writeNext(curCSV.getColumnNames());
            String arrStr[];
            while (curCSV.moveToNext()) {
                //Which column you want to exprort

                if (CHEEP) {

                    arrStr = new String[]{SecuritySettings.encrypt(curCSV.getString(4)), SecuritySettings.encrypt(curCSV.getString(1)), SecuritySettings.encrypt(curCSV.getString(2)), SecuritySettings.encrypt(curCSV.getString(3))};

                } else {

                    arrStr = new String[]{curCSV.getString(4), curCSV.getString(1), curCSV.getString(2), curCSV.getString(3)};

                }
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            new Message("Success!");
        } catch (Exception sqlEx) {
            Log.e("mLog", sqlEx.getMessage(), sqlEx);
            new Message("Error");
        }
    }


    }
