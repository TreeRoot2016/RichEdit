package com.example.xkfeng.richedit.Fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.xkfeng.richedit.R;
import com.example.xkfeng.richedit.RoundImage.RoundImage;
import com.example.xkfeng.richedit.SqlHelper.MyImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by initializing on 2018/5/23.
 */

public class SetFragemnt extends Fragment {

    private final static String TAG = "SetFragment" ;
    private MyImageView imageView ;
    private Animation animation ;
    private  static boolean ANIMA_CHANGE = false;
    private RoundImage roundImage ;

    private SimpleAdapter simpleAdapter ;
    private List<Map<String , Object>> mapList ;
    private final String[] array_string = new String[]{"拍照","从相册选取"};
    private final int[] image_id = new int[]{R.drawable.camera , R.drawable.photo_album} ;
    private static final int TAKE_PHOTO = 1 ;
    public Uri imageUri ;
    private static final int CHOOSE_PHOTE = 2 ;
    private static final int REQUEST_CODE_WRITE = 1 ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_layout , container , false) ;


        mapList = new ArrayList<Map<String, Object>>() ;
        for (int i = 0 ; i< 2 ; i++)
        {
            Map<String, Object> map = new HashMap<>() ;
            map.put("Image" , image_id[i]) ;
            map.put("Text" , array_string[i]) ;
            mapList.add(map) ;
        }

        simpleAdapter = new SimpleAdapter(getContext(),mapList , R.layout.header_image_list_item ,
                new String[]{"Image" , "Text"} ,
                new int[]{R.id.headerItemImage ,R.id.headerItemText});
        roundImage = (RoundImage) view.findViewById(R.id.round_image) ;
        roundImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(TAG , "RouunImage is clicked") ;
                //AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(getContext())
                        .setCancelable(true)
                        .setTitle("选择照片来源")
                        .setIcon(R.drawable.app_pic)
                        .setAdapter(simpleAdapter, new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity() , "点击了第"+which+"项",Toast.LENGTH_SHORT).show();
                                if (which == 0)
                                {
                                    imageUri = getImageUri() ;
                                    //启动程序
                                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE") ;
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT , imageUri) ;
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    startActivityForResult(intent , TAKE_PHOTO);
                                }
                                else
                                {
                                    //获取照相机权限
                                    int check = getContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ;
                                    if (check!= PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{"android.Manifest.permission.WRITE_EXTERNAL_STORAGE"} ,REQUEST_CODE_WRITE );
                                    }
                                    else {
                                       openAlbum();
                                    }
                                }
                            }
                        })

                        .setNegativeButton("取消" , null)
                        .create()
                        .show();
            }
        });

        imageView = (MyImageView) view.findViewById(R.id.backImageView) ;
        animation = AnimationUtils.loadAnimation(getContext(),R.anim.image_anima) ;

        animation.setInterpolator(new LinearInterpolator());
        animation.setFillAfter(true);
        imageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation ani ;
                if (!ANIMA_CHANGE)
                {
                    ANIMA_CHANGE = true ;
                    ani = AnimationUtils.loadAnimation(getContext(), R.anim.image_anima1);
                }
                else {
                    ANIMA_CHANGE = false ;
                    ani = AnimationUtils.loadAnimation(getContext(), R.anim.image_anima);
                }
                ani.setAnimationListener(this);
                ani.setFillAfter(true);
                ani.setInterpolator(new LinearInterpolator());
                ani.setRepeatMode(Animation.REVERSE);
                imageView.startAnimation(ani);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        imageView.setAnimation(animation);

        return view;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
             openAlbum();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode , resultCode , data);
        Log.i(TAG , "返回到SetFragment"+ "requestCode is " + requestCode
        +"  resultCode is "+resultCode);

        switch (requestCode)
        {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK)
                {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri)) ;
                        Log.i(TAG,"THE IMAGE URI IS " + imageUri.toString()) ;

                        roundImage.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG , "FILE NOT FOUND") ;
                        e.printStackTrace();
                    }

                }
                break ;
            case CHOOSE_PHOTE:
                if (resultCode == RESULT_OK)
                {
                    if (Build.VERSION.SDK_INT >= 19)
                    {
                        handleImageOnKitKat(data) ;
                    }else
                    {
                        Toast.makeText(getActivity() , "版本过老，已经不兼容" , Toast.LENGTH_SHORT).show();
                    }
                }
            default:
                break ;

        }
    }
    public Uri getImageUri()
    {
        Uri imageUri ;
        File outputImage = new File(Environment.getExternalStorageDirectory() ,  "header_image.jpg") ;
        try{
            outputImage.getParentFile().mkdirs() ;
            Log.i(TAG, "目录创建成功") ;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        try{
            if (outputImage.exists())
            {
                outputImage.delete();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24)
        {
            imageUri = FileProvider.getUriForFile(getContext(),"com.example.xkfeng.richedit.fileprovider",outputImage) ;
        }else {
            imageUri = Uri.fromFile(outputImage)  ;
        }

        return imageUri ;
    }

    private void openAlbum()
    {
        Intent intent = new Intent("android.intent.action.GET_CONTENT") ;
        intent.setType("image/*") ;
        startActivityForResult(intent , CHOOSE_PHOTE);

    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data)
    {
        String imagePath = null ;
        Uri uri = data.getData() ;
        if (DocumentsContract.isDocumentUri(getContext() , uri)){
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri)  ;
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1] ; //解析出数字格式的Id
                String selection = MediaStore.Images.Media._ID + "=" + id ;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , selection) ;
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId)) ;
                imagePath = getImagePath(contentUri , null) ;
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型的uri，则用普通的处理方式
            imagePath = getImagePath(uri , null) ;
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的uri，直接获取图片即可
            imagePath = uri.getPath() ;
        }

        displayImage(imagePath);
    }

    private String getImagePath(Uri uri , String selection)
    {
        String path = null ;
        Cursor cursor = getContext().getContentResolver().query(uri , null , selection , null , null) ;
        if (cursor != null)
        {
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)) ;
            }
            cursor.close();
        }

        return path ;
    }

    private void displayImage(String imagePath)
    {
        if (imagePath != null)
        {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath) ;
            roundImage.setImageBitmap(bitmap);

        }else {
            Toast.makeText(getActivity(),"failed to get image" ,Toast.LENGTH_SHORT).show();
        }
    }
}
