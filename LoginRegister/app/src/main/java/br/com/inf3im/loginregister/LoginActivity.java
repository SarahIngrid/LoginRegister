package br.com.inf3im.loginregister;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextView mTextViewNewUser, mTextViewForgotPassword;
    EditText mEditTextEmail, mEditTextPassword;
    Button mButtonLogin;
    ProgressBar mProgressBarLogin;
    SharedPreferences mSharedPreferences;
    String mStringEmail, mStringPassword, mStringUser, mStringApiKey, mStringIp;


    private boolean isRequiredPassword(){

        return TextUtils.isEmpty(mEditTextPassword.getText());

    }



    private boolean isValidEmail(String mStringEmail){
        if(mStringEmail == null || mStringEmail.isEmpty()  ){
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(mStringEmail).matches();


    }


    // iniciar uma nova atividade
    // fazer aparecer outra tela
    private void showOrder(){
        Intent mIntent = new Intent(getApplicationContext() , OrderActivity.class);
        startActivity(mIntent);
        finish();

    }


    private void verifyLogged(){
        if(mSharedPreferences.getString("logged" , "false").equals("true")){
            showOrder();

        }


    }


    private void getIp(){}

    private void postDataUsingVolley(){
        mStringEmail = String.valueOf(mEditTextEmail.getText()).toLowerCase(Locale.ROOT);
        mStringPassword = String.valueOf(mEditTextPassword.getText());
        //regras de negocio
        //testar as regras
        if(!isValidEmail(mStringEmail)){
            String mTextMessage = "Incorrect email";
            Toast.makeText(getApplicationContext() , mTextMessage , Toast.LENGTH_SHORT).show();
            return;
        }
        if(isRequiredPassword()){
            String mTextMessage = "Required password";
            Toast.makeText(getApplicationContext() , mTextMessage , Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBarLogin.setVisibility(View.VISIBLE);
        String mUrl = "http://127.0.0.1/api/login.php";

        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest mStringRequest = new StringRequest(Request.Method.POST, mUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressBarLogin.setVisibility(View.GONE);
                try {
                    JSONObject mJsonObject = new JSONObject(response);
                    String mStatus = mJsonObject.getString("status");
                    String mMessage = mJsonObject.getString("message");
                    if (mStatus.equals("success")) {
                        mStringUser = mJsonObject.getString("user");
                        mStringEmail = mJsonObject.getString("email");
                        mStringApiKey = mJsonObject.getString("apiKey");
                        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
                        mEditor.putString("logged", "true");
                        mEditor.putString("user", mStringUser);
                        mEditor.putString("email", mStringEmail);
                        mEditor.putString("apiKey", mStringApiKey);
                        mEditor.apply();
                        showOrder(); //apresentar a tela de pedido


                    } else {
                        Toast.makeText(getApplicationContext(), mMessage, Toast.LENGTH_LONG).show();

                    }

                } catch (JSONException mJsonException) {
                    mJsonException.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressBarLogin.setVisibility(View.GONE);
                error.printStackTrace();
                Toast.makeText(getApplicationContext() , "Fail " + error , Toast.LENGTH_LONG).show();
                
            }
        }) {
            @Override
            protected Map<String , String> getParams(){
                Map<String , String> mParams = new HashMap< String , String >();
                mParams.put("email" , mStringEmail);
                mParams.put("password" , mStringPassword);
                return mParams;


            }


        };

        mStringRequest.setRetryPolicy(new DefaultRetryPolicy(2*1000 , 3 , DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(mStringRequest);

    }

    public class ClickMyButtonLogin implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            postDataUsingVolley();
        }
    }


    private void showSignUp(){
        // mostrar a tela de cadastro
        Intent mIntent = new Intent(getApplicationContext() , SignUpActivity.class);
        startActivity(mIntent);
        finish();


    }


    public class ClickMyNewUser implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            showSignUp(); //cadastrar
        }
    }


    private void showForgotPassword(){
        // mostrar tela esqueci a senha
        Intent mIntent = new Intent(getApplicationContext() , ResetPasswordActivity.class);
        startActivity(mIntent);
        finish();
    }

    public class ClickMyForgotPassword implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            showForgotPassword();
        }
    }


    // implementar um escutador para o uso da tecla ENTER no teclado virtual
    public class EditTextAction implements TextView.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if(actionId == EditorInfo.IME_ACTION_DONE){
                postDataUsingVolley();
            }

            return false;
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // impedir o teclado virtual de aparecer na primeira tela LOGIN

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // associar os objetos java com os elementos da tela
        // alguns objetos java terão um comportamento (escutador)

        mEditTextEmail = findViewById(R.id.editText_email_login);

        mEditTextPassword = findViewById(R.id.editText_password_login);
        mEditTextPassword.setOnClickListener((View.OnClickListener) new EditTextAction());
        mButtonLogin.setOnClickListener(new ClickMyButtonLogin());

        mProgressBarLogin = findViewById(R.id.progressBar_login);
        mTextViewForgotPassword.setOnClickListener(new ClickMyForgotPassword());

        mTextViewNewUser = findViewById(R.id.textView_new_user);
        mTextViewNewUser.setOnClickListener(new ClickMyNewUser());

        verifyLogged();
    }
}
