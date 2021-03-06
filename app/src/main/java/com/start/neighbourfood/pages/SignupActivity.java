package com.start.neighbourfood.pages;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.start.neighbourfood.R;
import com.start.neighbourfood.auth.TaskHandler;
import com.start.neighbourfood.models.ApartmentsInfo;
import com.start.neighbourfood.models.ServiceConstants;
import com.start.neighbourfood.models.UserBaseInfo;
import com.start.neighbourfood.services.ServiceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends BaseActivity {

    private final String TAG = "SIGNIN_ACTIVITY";
    private UserBaseInfo userBaseInfo;
    private ServiceManager serviceManager;
    private JSONObject userObject;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        serviceManager = ServiceManager.getInstance(this);
        spinner = (Spinner) findViewById(R.id.apartment_dropdown_list);
        // Create an ArrayAdapter using the string array and a default spinner layout
        List<ApartmentsInfo> list = new ArrayList<>();
        list.add(new ApartmentsInfo("849eff55-e94f-444a-9bd2-8b963d863428","Durga Coral Apartment"));
        ArrayAdapter<ApartmentsInfo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        EditText editText = (EditText) findViewById(R.id.mobile_number);
        String phone = (String) getIntent().getExtras().get("phoneNumber");
        if (!TextUtils.isEmpty(phone)) {
            editText.setText(phone);
        } else {
            editText.setEnabled(true);
        }


        findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long apartmentNo = ((Spinner) findViewById(R.id.apartment_dropdown_list)).getSelectedItemId();
                EditText flatNumber = ((EditText) findViewById(R.id.flat_number_fragment));
                EditText fName = ((EditText) findViewById(R.id.first_name));
                EditText lName = ((EditText) findViewById(R.id.last_name));
                EditText phoneNo = ((EditText) findViewById(R.id.mobile_number));

                if (apartmentNo == -1 || TextUtils.isEmpty(flatNumber.getText())
                        || TextUtils.isEmpty(fName.getText())
                        || TextUtils.isEmpty(lName.getText())
                        || TextUtils.isEmpty(phoneNo.getText())
                        || phoneNo.getText().length() != 10) {
                    return;
                }
                addUserToDB();
            }
        });
    }

    private void addUserToDB() {
        showProgressDialog();
        String url = "http://nfservice.azurewebsites.net/api/user";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userObject = new JSONObject();
        try {
            userObject.put("userUid", user.getUid());
            userObject.put("apartmentId",(((ApartmentsInfo)spinner.getSelectedItem()).getId()));
            userObject.put("flatId", ((EditText) findViewById(R.id.flat_number_fragment)).getText());
            userObject.put("fName", ((EditText) findViewById(R.id.first_name)).getText());
            userObject.put("lName", ((EditText) findViewById(R.id.last_name)).getText());
            userObject.put("phoneNo", ((EditText) findViewById(R.id.mobile_number)).getText());
            userObject.put("userName", user.getDisplayName());
            serviceManager.createUser(userObject, new SignUpTaskHandler(user));
        } catch (JSONException ex) {
            ex.printStackTrace();
            hideProgressDialog();
        } catch (Exception e) {
            hideProgressDialog();
            e.printStackTrace();
        }
    }

    private class SignUpTaskHandler implements TaskHandler {

        private FirebaseUser mUser;

        public SignUpTaskHandler(FirebaseUser user) {
            mUser = user;
        }

        @Override
        public void onTaskCompleted(JSONObject result) {
            hideProgressDialog();
            saveStringInSharedPreference(ServiceConstants.signedInKey, mUser.getUid());
            saveStringInSharedPreference(ServiceConstants.userDetail,userObject.toString());
            navigateToHome();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            hideProgressDialog();
            Toast.makeText(SignupActivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_LONG);
            if (mUser.getUid() != null) {
                LoginManager.getInstance().logOut();
            }
            Log.e(TAG, "onErrorResponse: Response", error);
        }
    }
}
