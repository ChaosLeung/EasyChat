package org.zhj.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;

import org.zhj.easychat.app.BaseActionBarActivity;
import org.zhj.easychat.chat.SessionService;
import org.zhj.easychat.leancloud.LeanCloudUser;

/**
 * @author Chaos
 *         2015/04/02.
 */
public class SignUpActivity extends BaseActionBarActivity implements View.OnClickListener {

    private View signUpView;
    private View mdfInfoView;

    private EditText nicknameInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText passwordTwiceInput;
    private EditText areaInput;
    private EditText introductionInput;
    private EditText interestInput;

    private RadioButton maleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("注册");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        signUpView = findViewById(R.id.sign_up_layout);
        mdfInfoView = findViewById(R.id.mdf_info);

        nicknameInput = (EditText) findViewById(R.id.nickname);
        usernameInput = (EditText) findViewById(R.id.username);
        passwordInput = (EditText) findViewById(R.id.new_password);
        passwordTwiceInput = (EditText) findViewById(R.id.new_password_twice);
        areaInput = (EditText) findViewById(R.id.area);
        introductionInput = (EditText) findViewById(R.id.introduction);
        interestInput = (EditText) findViewById(R.id.interest);

        maleButton = (RadioButton) findViewById(R.id.male);

        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button signUpButton = (Button) findViewById(R.id.sign_up);

        confirmButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    private boolean canSignUp() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String passwordTwice = passwordTwiceInput.getText().toString().trim();
        if (username.length() < 6) {
            usernameInput.setError("帐号长度应大于6位");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("密码长度应大于6位");
            return false;
        }
        if (!passwordTwice.equals(password)) {
            passwordTwiceInput.setError("密码不一致");
            return false;
        }
        return true;
    }

    private boolean canModify() {
        String tips = "不能为空";
        if (TextUtils.isEmpty(nicknameInput.getText())) {
            nicknameInput.setError(tips);
            return false;
        }
        if (TextUtils.isEmpty(areaInput.getText())) {
            areaInput.setError(tips);
            return false;
        }
        if (TextUtils.isEmpty(interestInput.getText())) {
            interestInput.setError(tips);
            return false;
        }
        if (TextUtils.isEmpty(introductionInput.getText())) {
            introductionInput.setError(tips);
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirm:
                if (canModify()) {
                    modifyInfo();
                }
                break;
            case R.id.sign_up:
                if (canSignUp()) {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    signUp(username, password);
                }
                break;
        }
    }

    private void modifyInfo() {
        String introduction = introductionInput.getText().toString().trim();
        String nickname = nicknameInput.getText().toString().trim();
        String area = areaInput.getText().toString().trim();
        String interest = interestInput.getText().toString().trim();
        String gender = maleButton.isChecked() ? "男" : "女";
        LeanCloudUser user = LeanCloudUser.getCurrentUser2();
        user.setGender(gender);
        user.setIntroduction(introduction);
        user.setNickname(nickname);
        user.setInterest(interest);
        user.setArea(area);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    openMainActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    signUpView.setVisibility(View.GONE);
                    mdfInfoView.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("用户信息");
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
