package org.zhj.easychat.user;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.UpdatePasswordCallback;

import org.zhj.easychat.R;
import org.zhj.easychat.app.BaseActionBarActivity;

/**
 * @author Chaos
 *         2015/03/12.
 */
public class ModifyPswActivity extends BaseActionBarActivity implements View.OnClickListener {

    private EditText oldPswText;
    private EditText newPswText;
    private EditText newPswTwiceText;

    private Button modifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdf_psw);

        getSupportActionBar().setTitle("修改密码");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        oldPswText = (EditText) findViewById(R.id.old_password);
        newPswText = (EditText) findViewById(R.id.new_password);
        newPswTwiceText = (EditText) findViewById(R.id.new_password_twice);

        modifyButton = (Button) findViewById(R.id.modify);
        modifyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modify:
                String oldPsw = oldPswText.getText().toString().trim();
                String newPsw = newPswText.getText().toString().trim();
                String newPswTwice = newPswTwiceText.getText().toString().trim();
                if (oldPsw.equals(newPsw)) {
                    newPswText.setError("新密码无变化");
                    break;
                }
                if (newPsw.equals(newPswTwice)) {
                    AVUser.getCurrentUser().updatePasswordInBackground(oldPsw, newPsw, new UpdatePasswordCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();

                                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(modifyButton.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "修改失败，请检查原密码是否错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    newPswText.setError("密码不一致");
                    newPswTwiceText.setError("密码不一致");
                }
                break;
        }
    }
}
