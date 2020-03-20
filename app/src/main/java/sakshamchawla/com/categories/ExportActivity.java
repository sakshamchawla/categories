package sakshamchawla.com.categories;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.google.android.gms.drive.Drive.SCOPE_APPFOLDER;

public class ExportActivity extends AppCompatActivity {
    ListView LVEx;
    ArrayList<LVEXoption> ALexOptions;
    GoogleSignInClient googleSignInClient;
    DriveClient mDriveClient;
    DriveResourceClient mDriveResourceClient;
    GoogleSignInAccount googleSignInAccount;
    String TAG = "Drive";
    private final int REQUEST_CODE_CREATOR = 2013;
    Task<DriveContents> createContentsTask;
    String uri;

    private class LVEXoption {
        String Head, Body;

        LVEXoption(String Head, String Body) {
            this.Head = Head;
            this.Body = Body;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        ALexOptions = new ArrayList<>();
        ALexOptions.add(new LVEXoption("Local Backup", "Creates a local backup on the device"));
        ALexOptions.add(new LVEXoption("Drive Backup", "Creates a backup on your Drive account"));
        final CustomLVAdapter adapter = new CustomLVAdapter();
        LVEx = findViewById(R.id.DLLEX);
        LVEx.setAdapter(adapter);
        LVEx.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        googleSignInClient = buildGoogleSignInClient();
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, 1);
                        break;
                    case 0:
                        isStoragePermissionGranted(1);
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Request Code: " + requestCode);
            Log.v("perm", "Permission: " + permissions[0] + "was " + grantResults[0]);
            performFileSearch(requestCode);
        }
    }

    public boolean isStoragePermissionGranted(int RequestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("perm", "Permission is granted");
                performFileSearch(RequestCode);
                //checkAndWork(RequestCode);
                return true;
            } else {

                Log.v("perm", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestCode);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("perm", "Permission is granted");
            performFileSearch(RequestCode);
            //checkAndWork(RequestCode);
            return true;
        }
    }

    private static final int READ_REQUEST_CODE = 42;


    public void performFileSearch(int RequestCode) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.addCategory(Intent.FILE);
        //intent.setType("*/*");
        if (RequestCode == 2)
            startActivityForResult(intent, READ_REQUEST_CODE);
        else
            checkAndWork(1);
    }

    void checkAndWork(int requestCode) {
        if (requestCode == 1) {
            try {
                final String inFileName = this.getDatabasePath("wordDB").getPath();
                System.out.println("Exporting");
                File dbFile = new File(inFileName);
                FileInputStream fis = new FileInputStream(dbFile);
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YY_HH:mm");
                String currentDateandTime = sdf.format(new Date());
                String outFileName = Environment.getExternalStorageDirectory() + "/cat/cat_db_backup_" + currentDateandTime + ".db";
                String strFolder = Environment.getExternalStorageDirectory() + "/cat/";
                File folder = new File(strFolder);
                if (!folder.exists())
                    folder.mkdir();
                OutputStream output = new FileOutputStream(outFileName);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                System.out.println("File Written to: " + Environment.getExternalStorageDirectory());
                Toast.makeText(this, "Exported to: " + outFileName, Toast.LENGTH_SHORT).show();
                // Close the streams
                output.flush();
                output.close();
                fis.close();
                finish();
            } catch (IOException ex) {
                Toast.makeText(this, "Error in export", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    googleSignInAccount = task.getResult(ApiException.class);
                    googleSignInAccount = GoogleSignIn.getLastSignedInAccount(ExportActivity.this);

                    if (googleSignInAccount != null) {
                        mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
                        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
                    } else {
                        //   Toast.makeText(ExportActivity.this, "Login again and retry", Toast.LENGTH_SHORT).show();

                    }
                    if (mDriveResourceClient != null) {
                        createContentsTask = mDriveResourceClient.createContents();
                        createFile();
                    } else {
                        googleSignInClient = buildGoogleSignInClient();
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
        }
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestScopes(SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    private void createFile() {
        // [START create_file]
        final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = rootFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        uri = getDatabasePath("wordDB").getPath();
                        File file = new File(uri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        FileInputStream fis = new FileInputStream(file);
                        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                            baos.write(buf, 0, readNum);
                        }
                        OutputStream outputStream = contents.getOutputStream();
                        outputStream.write(baos.toByteArray());
                        Date date = Calendar.getInstance().getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YY_HH:mm");
                        String currentDateandTime = sdf.format(new Date());
                        String outFileName = "cat_db_backup_" + currentDateandTime + ".db";
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(outFileName) // Provide you video name here
                                .setMimeType("/")// Provide you video type here
                                .build();

                        return mDriveResourceClient.createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                Toast.makeText(ExportActivity.this, "Upload Started", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ExportActivity.this, "Error uploading", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unable to create file", e);
                        finish();
                    }
                });
        // [END create_file]
    }


    class CustomLVAdapter extends ArrayAdapter<LVEXoption> {
        CustomLVAdapter() {
            super(ExportActivity.this, R.layout.exportlistlayout, ALexOptions);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View itemView = inflater.inflate(R.layout.exportlistlayout, null);

            TextView TVHead = itemView.findViewById(R.id.DELLTVHead);
            TextView TVBody = itemView.findViewById(R.id.DELLTVBody);
            TVHead.setText(ALexOptions.get(position).Head);
            TVBody.setText(ALexOptions.get(position).Body);
            notifyDataSetChanged();
            return itemView;
        }
    }

}
