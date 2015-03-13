package org.zhj.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;

import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.chat.SessionService;
import org.zhj.easychat.leancloud.LeanCloudUser;

/**
 * @author Chaos
 *         2015/02/23.
 */
public class LoginActivity extends BaseActionBarActivity implements View.OnClickListener {

    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(AVUser.getCurrentUser()!=null){
            openMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        usernameInput = (EditText) findViewById(R.id.username);
        passwordInput = (EditText) findViewById(R.id.password);

        Button signIn = (Button) findViewById(R.id.sign_in);
        signIn.setOnClickListener(this);
        Button signUp = (Button) findViewById(R.id.sign_up);
        signUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        if (username.length() < 6) {
            usernameInput.setError("帐号长度应大于6位");
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("密码长度应大于6位");
            return;
        }
        switch (v.getId()) {
            case R.id.sign_in:
                signIn(username, password);
                break;
            case R.id.sign_up:
                signUp(username, password);
                break;
        }
    }

    private void signIn(String username, String password) {
        AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (e == null) {
                    openMainActivity();
                } else {
                    toast(e.getCode());
                }
            }
        });
    }

    private void openMainActivity() {
        SessionService.getInstance().openSession();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void signUp(String username, String password) {
        LeanCloudUser user = new LeanCloudUser();
        user.setUsername(username);
        user.setNickname(username);
        user.setPassword(password);
        user.setInterest("未知");
        user.setArea("未知");
        user.setIntroduction("未知");
        user.setGender("未知");
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                    openMainActivity();
                } else {
                    toast(e.getCode());
                }
            }
        });
    }

    private void toast(int errorCode) {
        String tips = null;
        switch (errorCode) {
            case AVException.USERNAME_TAKEN:
                usernameInput.setError("用户已存在");
                break;
            case AVException.ACCOUNT_ALREADY_LINKED:
                tips = "用户已登录";
                break;
            case AVException.USER_DOESNOT_EXIST:
                /* Falls through */
            case AVException.USERNAME_PASSWORD_MISMATCH:
                tips = "用户名或密码错误";
                break;
            case AVException.INVALID_JSON:
            case AVException.CONNECTION_FAILED:
                /* Falls through */
            case AVException.TIMEOUT:
                tips = "连接失败，请检查网络";
                break;
            case AVException.UNKNOWN:
                /* Falls through */
            default:
                tips = "未知错误";
                break;
        }
        if (!TextUtils.isEmpty(tips)) {
            Toast.makeText(this, tips, Toast.LENGTH_SHORT).show();
        }
    }
}
