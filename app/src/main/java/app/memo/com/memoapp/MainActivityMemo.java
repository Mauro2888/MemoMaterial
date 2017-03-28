package app.memo.com.memoapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import app.memo.com.memoapp.database.ContractMemoApp;
import app.memo.com.memoapp.database.HelperClass;

public class MainActivityMemo extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    RecyclerView mRecyclerMemo ;
    RecyclerView.LayoutManager mLayoutManager;
    CursorAdapterMemo mAdapterMemo;
    List<ModelMemo>mListaMemo;
    HelperClass mHelper;
    SQLiteDatabase mSQLdata;
    public static final int LOADER_ID = 111;
    com.github.clans.fab.FloatingActionButton mFloatAddNote;
    com.github.clans.fab.FloatingActionButton mFloatAddNoteFast;
    EditText mInsNota;
    EditText mInsTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_memo);
        getSupportLoaderManager().initLoader(LOADER_ID,null,this);

        mRecyclerMemo = (RecyclerView)findViewById(R.id.recyclerMemo);
        mFloatAddNote = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.floatingActionButtonAdd);
        mFloatAddNoteFast = (com.github.clans.fab.FloatingActionButton)findViewById(R.id.floatingActionButtonAddFast);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerMemo.setHasFixedSize(true);
        mRecyclerMemo.setLayoutManager(mLayoutManager);
        mListaMemo = new ArrayList<>();
        mAdapterMemo = new CursorAdapterMemo(this);
        mRecyclerMemo.setAdapter(mAdapterMemo);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = (int) viewHolder.itemView.getTag();
                Uri Uri = ContractMemoApp.MemoAppContract.URI_CONTENT.buildUpon().appendPath(String.valueOf(pos)).build();
                getContentResolver().delete(Uri,null,null);

            }
        }).attachToRecyclerView(mRecyclerMemo);



        mFloatAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent insertNote = new Intent(MainActivityMemo.this,InsertNoteActivity.class);
                startActivity(insertNote);
            }
        });

        mFloatAddNoteFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivityMemo.this);
                final View alertView = getLayoutInflater().inflate(R.layout.alertdialog_layout,null);
                mInsNota = (EditText)alertView.findViewById(R.id.ins_nota);
                mInsTitle = (EditText)alertView.findViewById(R.id.ins_title);
                alert.setView(alertView);
                alert.setCancelable(false);
                alert.setTitle("Inserisci la Nota");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Empty
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                //get alert button
                final AlertDialog dialog = alert.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        mHelper = new HelperClass(getApplicationContext());
                        mSQLdata = mHelper.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();

                        if (!mInsNota.getText().toString().trim().isEmpty() && !mInsTitle.getText().toString().isEmpty()){
                            contentValues.put("title",mInsTitle.getText().toString());
                            contentValues.put("note",mInsNota.getText().toString());
                            alertView.getContext().getContentResolver().insert(ContractMemoApp.MemoAppContract.URI_CONTENT,contentValues);
                            dialog.dismiss();
                            mSQLdata.close();
                        }else {
                            Toast.makeText(MainActivityMemo.this, "No Note", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        getSupportLoaderManager().restartLoader(LOADER_ID,null,this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(LOADER_ID,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri Url = ContractMemoApp.MemoAppContract.URI_CONTENT;
        CursorLoader cursorLoader = new CursorLoader(this,Url,null,null,null,null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapterMemo.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapterMemo.swapCursor(null);
    }
}
