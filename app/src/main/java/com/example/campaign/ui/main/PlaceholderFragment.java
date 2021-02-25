package com.example.campaign.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campaign.Activities.MainActivity;
import com.example.campaign.Activities.registrationActivity;
import com.example.campaign.Model.chatListModel;
import com.example.campaign.R;
import com.example.campaign.adapter.chatListAdapter;
import com.example.campaign.adapter.userListAdapter;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private List<chatListModel> list;
    private PageViewModel pageViewModel;
    private Context context;
    private chatListAdapter chatListAdapter;
    private userListAdapter userListAdapter;
    private  RecyclerView recyclerView;

    public  Map<String, String> namePhoneMap = new HashMap<String, String>();
    public FirebaseDatabase database;

    private int CONTACTS_REQUEST=110;
    private int index = 1;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);

        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        context=getContext();
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView= root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (index==1){
            pageViewModel.initChats();
            chatListAdapter=new chatListAdapter(pageViewModel.getChatData().getValue(),context);
            pageViewModel.getChatData().observe(getViewLifecycleOwner(), chatList -> chatListAdapter.notifyDataSetChanged());

            if(chatListAdapter!=null){
                Log.d("Frag",String.valueOf(chatListAdapter.getItemCount()));
                recyclerView.setAdapter(chatListAdapter);
            }
        }else{
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    pageViewModel.initContacts(getPhoneNumbers());
                    userListAdapter=new userListAdapter(pageViewModel.getUsersData().getValue(),context);
                    pageViewModel.getUsersData().observe(getViewLifecycleOwner(), usersList -> userListAdapter.notifyDataSetChanged());

                    if (userListAdapter!=null){
                        recyclerView.setAdapter(userListAdapter);
                    }


            } else {
                requestContactsPermission();
            }



        }


        return root;
    }
    public boolean isAlphanumeric2(String str) {
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
                return false;
        }
        return true;
    }
    private List<String> getPhoneNumbers() {
        List<String> phoneNumbers=new ArrayList<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            namePhoneMap.put(phoneNumber, name);

        }
        for (Map.Entry<String, String> entry : namePhoneMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains("+")){
                phoneNumbers.add(key);
            }else{
                if(isAlphanumeric2(key)){
                    Long i=Long.parseLong(key);
                    String j="+256"+i;
                    phoneNumbers.add(j);
                }
            }
        }
        phones.close();
        return phoneNumbers;
    }

    private void requestContactsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_CONTACTS)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because we require access to your contacts")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CONTACTS_REQUEST)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pageViewModel.initContacts(getPhoneNumbers());
                userListAdapter=new userListAdapter(pageViewModel.getUsersData().getValue(),context);
                pageViewModel.getUsersData().observe(getViewLifecycleOwner(), usersList -> userListAdapter.notifyDataSetChanged());
                if (userListAdapter!=null){
                    recyclerView.setAdapter(userListAdapter);
                }

            } else {
                Toast.makeText(getContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }



}